package com.zhiqi.platform.config;

import com.zhiqi.platform.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapConfig implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BootstrapConfig.class);
    private final UserService users;
    private final PlatformProperties properties;

    public BootstrapConfig(UserService users, PlatformProperties properties) {
        this.users = users;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        users.bootstrap(properties);
        if (!properties.production()
                && properties.bootstrap().enabled()
                && "13800000000".equals(properties.bootstrap().phone())
                && "DevAdmin123!".equals(properties.bootstrap().password())) {
            log.info("Local development administrator: phone=13800000000, password=DevAdmin123!");
        }
        log.info("Platform API ready");
    }
}
