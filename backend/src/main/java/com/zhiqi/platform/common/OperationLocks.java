package com.zhiqi.platform.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLocks {
    private final JdbcTemplate jdbc;

    private final DatabaseDialect dialect;

    public OperationLocks(JdbcTemplate jdbc, DatabaseDialect dialect) {
        this.jdbc = jdbc;
        this.dialect = dialect;
    }

    // 调用方必须处于事务内。相同键的插入/更新持有行锁直到事务结束。
    public void acquire(String key) {
        jdbc.update(
                dialect.choose(
                        "MERGE INTO operation_locks(lock_key) KEY(lock_key) VALUES(?)",
                        "INSERT INTO operation_locks(lock_key) VALUES(?) ON DUPLICATE KEY UPDATE"
                                + " lock_key=lock_key"),
                key);
        jdbc.queryForObject(
                "SELECT lock_key FROM operation_locks WHERE lock_key=? FOR UPDATE",
                String.class,
                key);
    }
}
