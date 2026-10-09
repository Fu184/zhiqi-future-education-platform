package com.zhiqi.platform.sms;

import com.zhiqi.platform.common.DatabaseDialect;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SmsRepository {
    public record Code(String hash, long expiresAt, long sentAt, int attempts) {}

    private final JdbcTemplate jdbc;

    private final DatabaseDialect dialect;

    public SmsRepository(JdbcTemplate jdbc, DatabaseDialect dialect) {
        this.jdbc = jdbc;
        this.dialect = dialect;
    }

    public Code find(String phone) {
        return jdbc
                .query(
                        "SELECT * FROM sms_codes WHERE phone=?",
                        (row, index) ->
                                new Code(
                                        row.getString("code_hash"),
                                        row.getLong("expires_at"),
                                        row.getLong("sent_at"),
                                        row.getInt("attempts")),
                        phone)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void reserve(String phone, String hash, long expiresAt, long sentAt) {
        jdbc.update(
                dialect.choose(
                        "MERGE INTO sms_codes(phone,code_hash,expires_at,sent_at,attempts)"
                                + " KEY(phone) VALUES(?,?,?,?,0)",
                        "INSERT INTO sms_codes(phone,code_hash,expires_at,sent_at,attempts)"
                                + " VALUES(?,?,?,?,0) ON DUPLICATE KEY UPDATE"
                                + " code_hash=VALUES(code_hash), expires_at=VALUES(expires_at),"
                                + " sent_at=VALUES(sent_at), attempts=0"),
                phone,
                hash,
                expiresAt,
                sentAt);
    }

    public void release(String phone, long sentAt) {
        jdbc.update("DELETE FROM sms_codes WHERE phone=? AND sent_at=?", phone, sentAt);
    }

    public void attempt(String phone) {
        jdbc.update("UPDATE sms_codes SET attempts=attempts+1 WHERE phone=?", phone);
    }

    public void consume(String phone) {
        jdbc.update("DELETE FROM sms_codes WHERE phone=?", phone);
    }
}
