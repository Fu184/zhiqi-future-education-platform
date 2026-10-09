package com.zhiqi.platform.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {
    private final JdbcTemplate jdbc;

    public SessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long userId(String hash, long now) {
        var rows =
                jdbc.queryForList(
                        "SELECT user_id FROM sessions WHERE token_hash=? AND expires_at>?",
                        Long.class,
                        hash,
                        now);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void insert(String hash, long userId, long expires) {
        jdbc.update(
                "INSERT INTO sessions(token_hash,user_id,expires_at) VALUES(?,?,?)",
                hash,
                userId,
                expires);
    }

    public void delete(String hash) {
        jdbc.update("DELETE FROM sessions WHERE token_hash=?", hash);
    }

    public void removeExpired(long now) {
        jdbc.update("DELETE FROM sessions WHERE expires_at<=?", now);
    }
}
