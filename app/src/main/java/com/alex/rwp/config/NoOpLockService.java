package com.alex.rwp.config;

import liquibase.database.Database;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.StandardLockService;

public class NoOpLockService extends StandardLockService {

    public NoOpLockService() {}

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public void waitForLock() {}

    @Override
    public boolean acquireLock() {
        return true;
    }

    @Override
    public void releaseLock() {}

    @Override
    public boolean hasChangeLogLock() {
        return true;
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() {
        return new DatabaseChangeLogLock[0];
    }
}
