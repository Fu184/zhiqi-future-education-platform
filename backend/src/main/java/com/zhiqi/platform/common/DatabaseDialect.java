package com.zhiqi.platform.common;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseDialect {
    private final boolean mysql;

    public DatabaseDialect(JdbcTemplate jdbc) {
        String product =
                jdbc.execute(
                        (ConnectionCallback<String>)
                                connection -> connection.getMetaData().getDatabaseProductName());
        if (!"H2".equals(product) && !"MySQL".equals(product))
            throw new IllegalStateException("Only H2 and MySQL are supported");
        mysql = "MySQL".equals(product);
    }

    public String choose(String h2, String mysql) {
        return this.mysql ? mysql : h2;
    }
}
