-- ============================================
-- Slave Replication 시작 스크립트
-- ============================================

-- Slave의 자체 binlog 초기화 (GTID 충돌 방지)
RESET MASTER;

-- Master 연결 설정 (GTID 기반)
CHANGE MASTER TO
    MASTER_HOST = 'mariadb-master',
    MASTER_PORT = 3306,
    MASTER_USER = 'repl_user',
    MASTER_PASSWORD = 'replpass',
    MASTER_USE_GTID = current_pos;

-- Replication 시작
START SLAVE;

-- 상태 확인 (로그에서 확인 가능)
-- SHOW SLAVE STATUS\G
