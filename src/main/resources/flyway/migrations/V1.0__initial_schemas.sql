-- NRM Schema
CREATE SCHEMA IF NOT EXISTS `nrm`;

-- ======================
-- Create NRM service user
-- ======================
CREATE USER IF NOT EXISTS '${db_username}'@'localhost' IDENTIFIED BY '${db_password}';
CREATE USER IF NOT EXISTS '${db_username}'@'%' IDENTIFIED BY '${db_password}';

-- Grant permissions to wildcard application service user
GRANT SELECT, INSERT, UPDATE, DELETE ON voice.* TO '${db_username}'@'%';

-- Grant permissions to localhost application service user
GRANT SELECT, INSERT, UPDATE, DELETE ON voice.* TO '${db_username}'@'localhost';

# -- ======================
# -- Create admin user
# -- ======================
CREATE USER IF NOT EXISTS '${db_admin_username}'@'localhost' IDENTIFIED BY '${db_admin_password}';
CREATE USER IF NOT EXISTS '${db_admin_username}'@'%' IDENTIFIED BY '${db_admin_password}';
#
# -- Grant Permissions to flyway user
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES, INDEX, ALTER, CREATE TEMPORARY TABLES, LOCK TABLES, EXECUTE, CREATE VIEW, SHOW VIEW, CREATE ROUTINE, ALTER ROUTINE, TRIGGER
    ON `${db_schema}`.* TO '${db_admin_username}'@'localhost', '${db_admin_username}'@'%';
FLUSH PRIVILEGES;