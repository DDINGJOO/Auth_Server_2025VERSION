-- ============================================
-- Slave 유저 생성 스크립트
-- MaxScale이 Slave에 직접 연결할 수 있도록 유저 생성
-- ============================================

-- MaxScale 모니터링 유저
CREATE USER IF NOT EXISTS 'maxscale_monitor'@'%' IDENTIFIED BY 'monitorpass';
GRANT REPLICATION CLIENT, REPLICATION SLAVE ON *.* TO 'maxscale_monitor'@'%';
GRANT SUPER, RELOAD, PROCESS, SHOW DATABASES, EVENT ON *.* TO 'maxscale_monitor'@'%';
GRANT SELECT ON mysql.* TO 'maxscale_monitor'@'%';
GRANT SLAVE MONITOR ON *.* TO 'maxscale_monitor'@'%';

-- MaxScale 라우팅 유저
CREATE USER IF NOT EXISTS 'maxscale_user'@'%' IDENTIFIED BY 'maxscalepass';
GRANT SELECT ON mysql.* TO 'maxscale_user'@'%';
GRANT SHOW DATABASES ON *.* TO 'maxscale_user'@'%';

FLUSH PRIVILEGES;
