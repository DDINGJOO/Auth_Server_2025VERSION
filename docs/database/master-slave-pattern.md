# Master/Slave 데이터베이스 패턴 가이드

## 목차
1. [개요](#1-개요)
2. [아키텍처](#2-아키텍처)
3. [MariaDB Replication](#3-mariadb-replication)
4. [MaxScale](#4-maxscale)
5. [Spring Boot 연동](#5-spring-boot-연동)
6. [Failover](#6-failover)
7. [구현 체크리스트](#7-구현-체크리스트)
8. [참고 자료](#8-참고-자료)

---

## 1. 개요

### 1.1 Master/Slave 패턴이란?

Master/Slave(Primary/Replica) 패턴은 데이터베이스를 **쓰기 전용(Master)**과 **읽기 전용(Slave)**으로 분리하는 아키텍처입니다.

```
[Application]
     │
     ├── Write (INSERT, UPDATE, DELETE) ──► [Master DB]
     │                                           │
     │                                    Replication
     │                                           │
     └── Read (SELECT) ───────────────────► [Slave DB]
```

### 1.2 도입 목적

| 목적 | 설명 |
|------|------|
| **읽기 성능 향상** | Read 요청을 Slave로 분산하여 Master 부하 감소 |
| **고가용성(HA)** | Master 장애 시 Slave를 Master로 승격 |
| **백업 용이** | Slave에서 백업 수행하여 Master 영향 최소화 |
| **지역 분산** | 지역별 Slave 배치로 지연시간 감소 |

### 1.3 트레이드오프

| 장점 | 단점 |
|------|------|
| 읽기 성능 향상 | 복제 지연(Replication Lag) 발생 가능 |
| 장애 대응 가능 | 인프라 복잡도 증가 |
| 수평적 읽기 확장 | 일관성 모델 복잡 (Eventual Consistency) |

---

## 2. 아키텍처

### 2.1 전체 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
│                                                              │
│  @Transactional(readOnly=false) ──► Write                   │
│  @Transactional(readOnly=true)  ──► Read                    │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      MaxScale (Proxy)                        │
│                                                              │
│  ┌─────────────────┐    ┌─────────────────────────────┐    │
│  │  Read/Write     │    │    MariaDB Monitor          │    │
│  │  Splitter       │    │  (Health Check, Failover)   │    │
│  └─────────────────┘    └─────────────────────────────┘    │
└─────────────┬───────────────────────────┬───────────────────┘
              │                           │
              ▼                           ▼
┌─────────────────────┐    ┌─────────────────────┐
│   Master (Primary)  │◄───│   Slave (Replica)   │
│                     │    │                     │
│  - Read/Write       │    │  - Read Only        │
│  - Binlog 생성       │    │  - Binlog 수신       │
└─────────────────────┘    └─────────────────────┘
```

### 2.2 포트 구성

| 컴포넌트 | 포트 | 용도 |
|----------|------|------|
| Master DB | 3306 | 직접 연결 (운영용) |
| Slave DB | 3307 | 직접 연결 (디버깅용) |
| MaxScale Read/Write | 4006 | 애플리케이션 연결 포트 |
| MaxScale Admin | 8989 | REST API, 모니터링 |

---

## 3. MariaDB Replication

### 3.1 Replication 방식

MariaDB는 **Binary Log(binlog)** 기반 복제를 사용합니다.

```
[Master]                    [Slave]
    │                           │
    │  1. 트랜잭션 실행           │
    ▼                           │
┌─────────┐                    │
│ Binlog  │──── 2. 전송 ────►  │
└─────────┘                    ▼
                          ┌─────────┐
                          │ Relay   │
                          │ Log     │
                          └────┬────┘
                               │
                          3. SQL 재실행
                               │
                               ▼
                          [Data 동기화]
```

### 3.2 Binlog 포맷

| 포맷 | 설명 | 장단점 |
|------|------|--------|
| **STATEMENT** | SQL 문장 그대로 복제 | 로그 작음, 비결정적 함수 문제 |
| **ROW** | 변경된 행 데이터 복제 | 정확함, 로그 큼 |
| **MIXED** | 상황에 따라 자동 선택 | 복잡함 |

> **권장**: `binlog-format=ROW` (정확성 보장)

### 3.3 GTID (Global Transaction ID)

전통적 복제는 `binlog 파일명 + position`으로 동기화 지점을 추적합니다.
GTID는 전역 고유 트랜잭션 ID로 이를 단순화합니다.

```sql
-- GTID 형식
domain_id-server_id-sequence_number

-- 예시
0-1-12345
```

**GTID 장점:**
- Failover 시 동기화 지점 자동 파악
- 복잡한 토폴로지에서도 일관성 유지

### 3.4 Master 설정

```ini
# /etc/mysql/conf.d/master.cnf
[mysqld]
server-id=1                    # 고유 서버 ID
log-bin=mysql-bin              # Binlog 활성화
binlog-format=ROW              # ROW 기반 복제
gtid_strict_mode=ON            # GTID 활성화
log-slave-updates=ON           # Slave에서도 binlog 기록 (Failover용)
```

### 3.5 Slave 설정

```ini
# /etc/mysql/conf.d/slave.cnf
[mysqld]
server-id=2                    # Master와 다른 고유 ID
log-bin=mysql-bin              # Failover 대비 binlog 활성화
binlog-format=ROW
gtid_strict_mode=ON
log-slave-updates=ON
read-only=ON                   # 읽기 전용 강제
```

### 3.6 Replication 시작

```sql
-- Slave에서 실행
CHANGE MASTER TO
    MASTER_HOST='master-host',
    MASTER_USER='repl_user',
    MASTER_PASSWORD='password',
    MASTER_USE_GTID=slave_pos;  -- GTID 사용

START SLAVE;

-- 상태 확인
SHOW SLAVE STATUS\G
```

**확인할 항목:**
- `Slave_IO_Running: Yes`
- `Slave_SQL_Running: Yes`
- `Seconds_Behind_Master: 0` (복제 지연 없음)

---

## 4. MaxScale

### 4.1 MaxScale이란?

MariaDB MaxScale은 MariaDB Corporation이 개발한 **데이터베이스 프록시**입니다.

**주요 기능:**
- Read/Write Split (읽기/쓰기 분리)
- Load Balancing (다중 Slave 로드밸런싱)
- Query Routing (쿼리 기반 라우팅)
- Auto Failover (자동 장애 복구)
- Connection Pooling

### 4.2 핵심 컴포넌트

```
MaxScale
├── Server       : 백엔드 DB 서버 정의
├── Monitor      : 서버 상태 감시 (mariadbmon)
├── Service      : 라우팅 로직 (readwritesplit)
└── Listener     : 클라이언트 연결 수신
```

### 4.3 설정 파일 구조

```ini
# /etc/maxscale.cnf

[maxscale]
threads=auto                    # 워커 스레드 수
admin_host=0.0.0.0             # Admin API 바인드 주소
admin_port=8989                # Admin API 포트

# ─────────────────────────────────────
# 서버 정의
# ─────────────────────────────────────
[master]
type=server
address=192.168.1.10           # Master IP
port=3306
protocol=MariaDBBackend

[slave]
type=server
address=192.168.1.11           # Slave IP
port=3306
protocol=MariaDBBackend

# ─────────────────────────────────────
# 모니터 (상태 감시 + Failover)
# ─────────────────────────────────────
[MariaDB-Monitor]
type=monitor
module=mariadbmon              # MariaDB 전용 모니터
servers=master,slave           # 감시 대상
user=maxscale_monitor          # 모니터링 유저
password=password
monitor_interval=2000ms        # 상태 체크 주기

# Failover 설정
auto_failover=true             # 자동 Failover 활성화
auto_rejoin=true               # Master 복구 시 자동 재합류
enforce_read_only_slaves=true  # Slave read-only 강제

# ─────────────────────────────────────
# 서비스 (Read/Write Split)
# ─────────────────────────────────────
[Read-Write-Service]
type=service
router=readwritesplit          # R/W 분리 라우터
servers=master,slave
user=maxscale_user             # 라우팅 유저
password=password

# 라우팅 옵션
master_failure_mode=fail_on_write  # Master 장애 시 Write만 실패
max_slave_connections=100%         # 모든 Slave 사용

# ─────────────────────────────────────
# 리스너 (클라이언트 연결점)
# ─────────────────────────────────────
[Read-Write-Listener]
type=listener
service=Read-Write-Service
protocol=MariaDBClient
port=4006                      # 애플리케이션이 연결할 포트
```

### 4.4 필요한 MariaDB 유저

```sql
-- 1. 모니터링 유저 (상태 체크, Failover 실행)
CREATE USER 'maxscale_monitor'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION CLIENT ON *.* TO 'maxscale_monitor'@'%';
GRANT SUPER, RELOAD, PROCESS, SHOW DATABASES, EVENT ON *.* TO 'maxscale_monitor'@'%';
GRANT SELECT ON mysql.* TO 'maxscale_monitor'@'%';

-- 2. 라우팅 유저 (연결 인증 확인)
CREATE USER 'maxscale_user'@'%' IDENTIFIED BY 'password';
GRANT SELECT ON mysql.user TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.db TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.tables_priv TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.roles_mapping TO 'maxscale_user'@'%';
GRANT SHOW DATABASES ON *.* TO 'maxscale_user'@'%';

-- 3. Replication 유저 (Master-Slave 간)
CREATE USER 'repl_user'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';

FLUSH PRIVILEGES;
```

### 4.5 Read/Write Split 동작 원리

MaxScale은 쿼리를 분석하여 자동 라우팅합니다.

| 쿼리 유형 | 라우팅 대상 |
|-----------|-------------|
| `SELECT` (트랜잭션 외부) | Slave |
| `SELECT` (트랜잭션 내부) | Master (일관성 보장) |
| `SELECT ... FOR UPDATE` | Master |
| `INSERT`, `UPDATE`, `DELETE` | Master |
| `START TRANSACTION` | Master |

**힌트 사용:**
```sql
-- 강제로 Master 사용
SELECT /* maxscale route to master */ * FROM users;

-- 강제로 Slave 사용
SELECT /* maxscale route to slave */ * FROM users;
```

### 4.6 MaxScale 관리 명령

```bash
# 서비스 상태 확인
maxctrl list servers
maxctrl list services
maxctrl list monitors

# REST API로 확인
curl -u admin:mariadb http://localhost:8989/v1/servers | jq

# 수동 Failover
maxctrl call command mariadbmon failover MariaDB-Monitor

# 수동 Rejoin
maxctrl call command mariadbmon rejoin MariaDB-Monitor <server-name>
```

---

## 5. Spring Boot 연동

### 5.1 기본 연결 설정

MaxScale을 사용하면 애플리케이션은 **단일 연결점**만 알면 됩니다.

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mariadb://maxscale-host:4006/auth_db
    username: app_user
    password: ${DB_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver

    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 5.2 @Transactional 사용법

MaxScale은 트랜잭션 상태를 추적하여 라우팅합니다.

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ─────────────────────────────────────────
    // Write 작업: Master로 라우팅
    // ─────────────────────────────────────────
    @Transactional
    public User createUser(UserCreateRequest request) {
        // INSERT → Master
        return userRepository.save(new User(request));
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        // SELECT FOR UPDATE + UPDATE → Master
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        user.update(request);
        return user;
    }

    // ─────────────────────────────────────────
    // Read 작업: Slave로 라우팅
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)  // ★ 핵심
    public User getUser(Long id) {
        // SELECT → Slave
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {
        // SELECT → Slave
        return userRepository.findAll(pageable);
    }

    // ─────────────────────────────────────────
    // 주의: Read after Write
    // ─────────────────────────────────────────
    @Transactional  // readOnly=false
    public User createAndGet(UserCreateRequest request) {
        User saved = userRepository.save(new User(request));

        // 같은 트랜잭션 내 → Master에서 조회 (일관성 보장)
        return userRepository.findById(saved.getId()).orElseThrow();
    }
}
```

### 5.3 주의사항

#### 1) Replication Lag

Slave는 Master보다 데이터가 늦을 수 있습니다 (밀리초~초 단위).

```java
// ❌ 문제 상황
@Transactional
public void createUser(UserCreateRequest request) {
    userRepository.save(new User(request));  // Master에 저장
}

@Transactional(readOnly = true)
public User getUser(Long id) {
    // Slave에서 조회 → 방금 저장한 데이터가 없을 수 있음!
    return userRepository.findById(id).orElseThrow();
}

// ✅ 해결: 동일 트랜잭션에서 처리
@Transactional
public User createAndGetUser(UserCreateRequest request) {
    User user = userRepository.save(new User(request));
    return userRepository.findById(user.getId()).orElseThrow();
}
```

#### 2) 트랜잭션 전파

```java
// ❌ 외부 메서드가 readOnly=true면 내부 Write도 Slave로 갈 수 있음
@Transactional(readOnly = true)
public void outerMethod() {
    innerWriteMethod();  // 문제!
}

@Transactional(propagation = Propagation.REQUIRES_NEW)  // ✅ 새 트랜잭션
public void innerWriteMethod() {
    userRepository.save(new User(...));
}
```

#### 3) LazyLoading

```java
@Transactional(readOnly = true)
public UserDto getUser(Long id) {
    User user = userRepository.findById(id).orElseThrow();

    // Lazy Loading 발생 → 같은 Slave 연결에서 조회
    List<Order> orders = user.getOrders();  // OK

    return new UserDto(user, orders);
}
```

---

## 6. Failover

### 6.1 Failover 시나리오

```
정상 상태:
[App] ──► [MaxScale] ──► [Master] ◄──► [Slave]
                              │            │
                            Write        Read

Master 장애 발생:
[App] ──► [MaxScale] ──► [Master] ✕
                              │
                    감지 (2초 이내)
                              │
                              ▼
          [MaxScale] ──► [Slave → New Master]
                              │
                            Read/Write
```

### 6.2 Failover 프로세스

1. **감지**: mariadbmon이 Master 연결 실패 감지 (monitor_interval)
2. **확인**: 재시도 후 장애 확정
3. **승격**: Slave를 Master로 승격 (`STOP SLAVE`, `RESET SLAVE ALL`)
4. **라우팅**: MaxScale이 새 Master로 Write 라우팅
5. **알림**: 로그 및 이벤트 발생

### 6.3 Failover 중 애플리케이션 동작

| 상황 | 동작 |
|------|------|
| Read 요청 | 정상 (Slave 가용) |
| Write 요청 | 일시적 실패 → 재시도 필요 |
| 진행 중 트랜잭션 | 롤백됨 |

### 6.4 HikariCP 재연결 설정

```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 30000     # 연결 대기 최대 시간
      validation-timeout: 5000      # 연결 유효성 검사 타임아웃
      connection-test-query: SELECT 1

      # Failover 대응
      max-lifetime: 1800000         # 연결 최대 수명 (30분)
      keepalive-time: 30000         # 연결 유지 확인 주기
```

### 6.5 Rejoin (Master 복구)

기존 Master가 복구되면 자동으로 Slave로 재합류합니다.

```
복구 후:
[New Master (기존 Slave)] ◄─── [Old Master (새 Slave)]
```

`auto_rejoin=true` 설정 시 자동 처리됩니다.

---

## 7. 구현 체크리스트

### 7.1 인프라

- [ ] MariaDB Master 서버 구성
- [ ] MariaDB Slave 서버 구성
- [ ] Master-Slave Replication 설정
- [ ] GTID 활성화
- [ ] MaxScale 서버 구성
- [ ] MaxScale 모니터링 유저 생성
- [ ] MaxScale 라우팅 유저 생성
- [ ] Firewall 규칙 설정 (3306, 4006, 8989)

### 7.2 애플리케이션

- [ ] application-prod.yml에 MaxScale 연결 설정
- [ ] HikariCP 설정 최적화
- [ ] 모든 Service 메서드 @Transactional 검토
- [ ] 조회 메서드에 @Transactional(readOnly=true) 적용
- [ ] Replication Lag 영향 분석

### 7.3 테스트

- [ ] 로컬 Docker Compose 환경 구성
- [ ] Read/Write 분리 동작 확인
- [ ] Failover 테스트
- [ ] Rejoin 테스트
- [ ] 부하 테스트

### 7.4 모니터링

- [ ] MaxScale REST API 연동
- [ ] Replication Lag 모니터링
- [ ] 알림 설정 (Failover 발생 시)

---

## 8. 참고 자료

### 공식 문서
- [MariaDB Replication](https://mariadb.com/kb/en/setting-up-replication/)
- [MariaDB GTID](https://mariadb.com/kb/en/gtid/)
- [MariaDB MaxScale](https://mariadb.com/kb/en/maxscale/)
- [MaxScale readwritesplit](https://mariadb.com/kb/en/mariadb-maxscale-6-readwritesplit/)
- [MaxScale mariadbmon](https://mariadb.com/kb/en/mariadb-maxscale-6-mariadb-monitor/)

### 블로그/튜토리얼
- [MaxScale 자동 Failover 설정](https://mariadb.com/resources/blog/automatic-failover-maxscale/)
- [Spring Boot with Read Replicas](https://vladmihalcea.com/read-write-read-only-transaction-routing-spring/)

### 도구
- [MaxScale GUI (MaxGUI)](https://mariadb.com/kb/en/mariadb-maxscale-6-maxgui/)
- [Prometheus + Grafana 모니터링](https://mariadb.com/kb/en/mariadb-maxscale-6-prometheus-exporter/)
