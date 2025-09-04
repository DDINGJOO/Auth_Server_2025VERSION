-- Seed data for roles
INSERT INTO user_role (role) VALUES ('ADMIN') ON DUPLICATE KEY UPDATE role=role;
INSERT INTO user_role (role) VALUES ('USER') ON DUPLICATE KEY UPDATE role=role;
INSERT INTO user_role (role) VALUES ('GUEST') ON DUPLICATE KEY UPDATE role=role;
