package com.zhiqi.platform.auth;

import com.zhiqi.platform.common.DatabaseDialect;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RateLimitRepository {
    public record State(int count, long resetAt) {}

    private final JdbcTemplate jdbc;

    private final DatabaseDialect dialect;

    public RateLimitRepository(JdbcTemplate jdbc, DatabaseDialect dialect) {
        this.jdbc = jdbc;
        this.dialect = dialect;
    }

    public State find(String key) {
        return jdbc
                .query(
                        "SELECT request_count,reset_at FROM rate_limits WHERE rate_key=?",
                        (row, index) ->
                                new State(row.getInt("request_count"), row.getLong("reset_at")),
                        key)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void increment(String key) {
        jdbc.update("UPDATE rate_limits SET request_count=request_count+1 WHERE rate_key=?", key);
    }

    public void reset(String key, long until) {
        jdbc.update(
                dialect.choose(
                        "MERGE INTO rate_limits(rate_key,request_count,reset_at) KEY(rate_key)"
                                + " VALUES(?,1,?)",
                        "INSERT INTO rate_limits(rate_key,request_count,reset_at) VALUES(?,1,?) ON"
                            + " DUPLICATE KEY UPDATE request_count=1, reset_at=VALUES(reset_at)"),
                key,
                until);
    }

    public void clear(String key) {
        jdbc.update("DELETE FROM rate_limits WHERE rate_key=?", key);
    }
}
