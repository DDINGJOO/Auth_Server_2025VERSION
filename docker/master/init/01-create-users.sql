-- ============================================
-- Master 초기화 스크립트
-- ============================================

-- 1. Replication 유저 (Slave가 연결할 때 사용)
CREATE USER IF NOT EXISTS 'repl_user'@'%' IDENTIFIED BY 'replpass';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';

-- 2. MaxScale 모니터링 유저 (상태 체크, Failover 실행)
CREATE USER IF NOT EXISTS 'maxscale_monitor'@'%' IDENTIFIED BY 'monitorpass';
GRANT REPLICATION CLIENT ON *.* TO 'maxscale_monitor'@'%';
GRANT SUPER, RELOAD, PROCESS, SHOW DATABASES, EVENT ON *.* TO 'maxscale_monitor'@'%';
GRANT SELECT ON mysql.* TO 'maxscale_monitor'@'%';

-- 3. MaxScale 라우팅 유저 (연결 인증 확인용)
CREATE USER IF NOT EXISTS 'maxscale_user'@'%' IDENTIFIED BY 'maxscalepass';
GRANT SELECT ON mysql.user TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.db TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.tables_priv TO 'maxscale_user'@'%';
GRANT SELECT ON mysql.roles_mapping TO 'maxscale_user'@'%';
GRANT SHOW DATABASES ON *.* TO 'maxscale_user'@'%';

-- 4. 애플리케이션 유저 권한 강화
GRANT ALL PRIVILEGES ON auth_db.* TO 'app_user'@'%';

FLUSH PRIVILEGES;

-- 확인용 출력
SELECT user, host FROM mysql.user;
