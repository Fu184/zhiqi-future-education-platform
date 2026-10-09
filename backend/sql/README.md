# 数据库 SQL

当前本机已配置 MySQL：数据库 `zhiqi_platform`，专用账号 `zhiqi_app`，连接 `127.0.0.1:3306`。连接密码保存在未提交版本控制的 `backend/.env`，运行时选择 `dev,mysql`。

[`schema-mysql.sql`](schema-mysql.sql) 与运行时 [`../src/main/resources/schema-mysql.sql`](../src/main/resources/schema-mysql.sql) 相同。七张表已经创建；后续启动自动检查建表，无需重复手工导入。脚本适用 MySQL 8.0.16+，使用 InnoDB 和 utf8mb4。

| 表 | 用途 |
| --- | --- |
| users | 六类用户、上下级关系及名额 |
| sessions | 登录会话摘要和有效期 |
| sms_codes | 验证码摘要、冷却和错误次数 |
| rate_limits | 注册、登录、改密码限流 |
| purchases | 购买确认凭证 |
| audit_logs | 账号操作记录 |
| operation_locks | 按机构、手机号和限流键的事务锁 |

管理员由 Spring Boot 初始化，SQL 不保存明文登录密码。更改结构时同步更新运行时和本目录的 MySQL 脚本。运行中的锁行不可手动删除。

H2 的备用建表脚本保留在 `src/main/resources/schema.sql`，供自动化测试及未配置 MySQL 的环境使用。旧 `backend/data/platform.mv.db` 保留为历史数据，不会被当前 MySQL 配置读取，也没有自动迁移。
