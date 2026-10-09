-- 智启未来：MySQL 8.0.16+ / 8.4 建表脚本，选择目标数据库后执行。
-- Spring Boot mysql profile 自动建表；数据库和连接用户需先创建。
-- InnoDB 提供事务、外键和行锁；字符集 utf8mb4 支持中文和表情。
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(11) NOT NULL UNIQUE,
    name VARCHAR(40) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) COLLATE utf8mb4_bin NOT NULL,
    parent_id BIGINT NULL,
    teacher_quota INT NOT NULL DEFAULT 0,
    student_quota INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (role IN ('ordinary','learner','principal','teacher','admin','government')),
    CONSTRAINT users_teacher_quota_check CHECK (teacher_quota >= 0),
    CONSTRAINT users_student_quota_check CHECK (student_quota >= 0),
    CONSTRAINT users_parent_fk FOREIGN KEY (parent_id) REFERENCES users(id),
    INDEX users_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS sessions (
    token_hash VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    CONSTRAINT sessions_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX sessions_user (user_id), INDEX sessions_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS sms_codes (
    phone VARCHAR(11) PRIMARY KEY,
    code_hash VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at BIGINT NOT NULL,
    sent_at BIGINT NOT NULL,
    attempts INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS rate_limits (
    rate_key VARCHAR(200) COLLATE utf8mb4_bin PRIMARY KEY,
    request_count INT NOT NULL,
    reset_at BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS purchases (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    purchase_reference VARCHAR(100) COLLATE utf8mb4_bin NOT NULL UNIQUE,
    confirmed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT purchases_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT purchases_actor_fk FOREIGN KEY (confirmed_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    target_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT audit_actor_fk FOREIGN KEY (actor_id) REFERENCES users(id),
    CONSTRAINT audit_target_fk FOREIGN KEY (target_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS operation_locks (
    lock_key VARCHAR(240) COLLATE utf8mb4_bin PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
