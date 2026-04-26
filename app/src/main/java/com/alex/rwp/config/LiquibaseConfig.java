package com.alex.rwp.config;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.lockservice.LockServiceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfig {

    // RisingWave has two incompatibilities with Liquibase:
    // 1. VARCHAR(n) is not supported — tracking tables must use plain VARCHAR
    // 2. Read-write transactions are not supported — the lock UPDATE returns 0 rows
    //    causing Liquibase to wait forever. We bypass locking with a no-op LockService.
    @Bean
    @ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
    public SpringLiquibase liquibase(DataSource dataSource, LiquibaseProperties properties) {
        LockServiceFactory.getInstance().register(new NoOpLockService());

        SpringLiquibase liquibase = new SpringLiquibase() {
            @Override
            public void afterPropertiesSet() throws LiquibaseException {
                try (Connection conn = dataSource.getConnection()) {
                    var stmt = conn.createStatement();
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS DATABASECHANGELOGLOCK (
                            ID          INT     NOT NULL PRIMARY KEY,
                            LOCKED      BOOLEAN NOT NULL,
                            LOCKGRANTED TIMESTAMP,
                            LOCKEDBY    VARCHAR
                        )""");
                    stmt.execute("""
                        CREATE TABLE IF NOT EXISTS DATABASECHANGELOG (
                            ID            VARCHAR NOT NULL,
                            AUTHOR        VARCHAR NOT NULL,
                            FILENAME      VARCHAR NOT NULL,
                            DATEEXECUTED  TIMESTAMP NOT NULL,
                            ORDEREXECUTED INT NOT NULL,
                            EXECTYPE      VARCHAR NOT NULL,
                            MD5SUM        VARCHAR,
                            DESCRIPTION   VARCHAR,
                            COMMENTS      VARCHAR,
                            TAG           VARCHAR,
                            LIQUIBASE     VARCHAR,
                            CONTEXTS      VARCHAR,
                            LABELS        VARCHAR,
                            DEPLOYMENT_ID VARCHAR
                        )""");
                    stmt.execute("""
                        INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED)
                        SELECT 1, FALSE WHERE NOT EXISTS (
                            SELECT 1 FROM DATABASECHANGELOGLOCK WHERE ID = 1
                        )""");
                } catch (Exception e) {
                    throw new LiquibaseException("Failed to pre-create Liquibase tracking tables for RisingWave", e);
                }
                super.afterPropertiesSet();
            }
        };
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        return liquibase;
    }
}
