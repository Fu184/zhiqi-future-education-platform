# 用户与认证 API

Spring Boot 3.5.6 + Spring Security + Spring JDBC + H2 / MySQL，JDK 17+。启动类为 `src/main/java/com/zhiqi/platform/PlatformApplication.java`。无需额外安装数据库服务，默认 H2 文件数据库自动建表并持久化，也可按下面步骤切换 MySQL。

## 当前本机已完成的配置

本机 MySQL 8.0.43 服务已运行，`zhiqi_platform` 数据库和全部 7 张表已建立；专用账号为 `zhiqi_app`，连接密码只存于 `backend/.env`。启动时自动启用 `dev,mysql`。运行 IDEA 共享配置或在 backend 工作目录直接运行 PlatformApplication 即可，不需要再执行建库操作。

默认管理员：手机号 `13800000000`，密码 `DevAdmin123!`。已验证管理员登录、短信测试验证码注册、注册后会话和重新登录，以及账号实际写入 MySQL。测试账号已移除。

手机号注册当前使用 `console` 测试验证码，验证码在注册页面显示，不会发送真实短信；切换真实短信的参数见下文。

清理了旧测试报告、截图、配置测试目录、空 backend/test、冗余配置模板及生成文件。Maven Wrapper、源码测试、依赖和可运行 JAR 保留。旧 H2 数据文件保留，避免丢失历史账号。

## 直接运行

在 IDEA 中将根目录 `pom.xml` 或 `backend/pom.xml` 加载为 Maven 项目，设置 JDK 17+，等待依赖加载后，点击 PlatformApplication 的 main 方法运行按钮。项目自带 `.run/Spring Boot Backend.run.xml`，工作目录为 backend，普通 Java Application 配置即可运行。

开发默认管理员：`13800000000` / `DevAdmin123!`。默认端口 3001。访问 `http://127.0.0.1:3001/api/health` 返回 `{"status":"ok"}`。

终端运行：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

已安装 Maven 也可使用 `mvn spring-boot:run`。打包：`.\mvnw.cmd package`；运行 JAR：`java -jar target/education-platform-1.0.0.jar`。

## 代码分层

```text
src/main/java/com/zhiqi/platform/
├─ PlatformApplication.java    启动类
├─ auth/                      登录注册 Controller、会话、密码与限流
├─ user/                      用户 Controller、Service、Repository、角色和统计
├─ sms/                       验证码状态与真实短信网关
├─ config/                    Spring Security、请求过滤、管理员初始化与配置
└─ common/                    校验与统一错误响应
src/main/resources/
├─ application.yml            数据库、端口和配置
├─ application-dev.yml        本地管理员默认值
├─ application-prod.yml       生产环境限制
├─ application-mysql.yml      MySQL 驱动、连接与建表配置
├─ schema.sql                 H2 数据库结构
└─ schema-mysql.sql           MySQL 数据库结构
```

Controller 负责 HTTP 参数与响应；Service 负责权限、配额和事务；Repository 集中用户、会话、验证码和限流的 SQL。`UserView` 明确公开字段，不包含密码摘要；`UsersPage` 定义分页协议；`UserUpdate` 区分 PATCH 字段缺失与显式 null。未知字段、错误 JSON 类型以及非法分页参数返回 400。

列表配额使用量按当前页批量聚合，查询数不随本页用户数量逐条增长。账号归属不可直接修改，机构根 ID 保持稳定，因此可按根 ID 同步机构写入；跨机构锁按固定顺序获取。管理员相关操作另用 administrators 锁保护最后一个管理员约束。短信与注册共享手机号锁，限流使用各自键锁；密码编码在获取锁前完成。

## 本机 MySQL 配置

支持 MySQL 8.0.16+（推荐 8.0 或 8.4）。项目已提供 Connector/J 驱动、mysql profile 以及独立 MySQL 建表 SQL；锁、限流与验证码的 upsert 也已按实际数据库自动选择语法。账号事务显式使用 READ_COMMITTED，保证等待机构锁后读取最新配额，避免 MySQL 默认可重复读的旧快照影响并发校验。

1. 在 MySQL 客户端使用管理账号连接 127.0.0.1:3306，执行下面的 SQL（先替换占位密码）：

```sql
CREATE DATABASE IF NOT EXISTS zhiqi_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'zhiqi_app'@'127.0.0.1' IDENTIFIED BY '你设置的数据库密码';
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,INDEX,REFERENCES ON zhiqi_platform.* TO 'zhiqi_app'@'127.0.0.1';
```

已有同名用户时使用该用户现有密码，CREATE USER IF NOT EXISTS 不会重设密码。
2. 将 [`.env.example`](.env.example) 复制成 backend/.env，修改 DATABASE_PASSWORD，与创建用户时的密码一致。已有 .env 时合并配置，保留自己的短信参数。
3. `.env` 按 properties 文件解析，使用 `spring.profiles.active=dev,mysql`。若在 IDEA 的环境变量栏配置，则使用 `SPRING_PROFILES_ACTIVE=dev,mysql`。两种写法对应不同配置来源，不要在 .env 中依赖环境变量名的隐式转换。
4. IDEA 的工作目录设为 backend，直接运行 PlatformApplication；或在 backend 执行 `.\mvnw.cmd spring-boot:run`。首次启动自动执行 schema-mysql.sql，无需再手动导入表。

最少配置：

```properties
spring.profiles.active=dev,mysql
DATABASE_URL=jdbc:mysql://127.0.0.1:3306/zhiqi_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&sslMode=DISABLED&allowPublicKeyRetrieval=true
DATABASE_USERNAME=zhiqi_app
DATABASE_PASSWORD=你设置的数据库密码
SMS_MODE=console
PUBLIC_ORIGIN=http://localhost:5173
```

上述连接参数用于本机开发。远程数据库按服务端证书配置 TLS，例如 sslMode=VERIFY_IDENTITY，并配置受信任证书；生产应用选择 prod,mysql，使用 HTTPS 和真实短信。可在 DATABASE_URL 中修改主机、端口、数据库名称。

数据库和连接用户必须先创建，应用只自动建表。H2 中的账号和数据不会自动迁移到 MySQL；新库的管理员由初始化服务创建。导出的表结构为 [`sql/schema-mysql.sql`](sql/schema-mysql.sql)，与运行时的 src/main/resources/schema-mysql.sql 一致。已有 .env 的 profile 或 DATABASE_URL 不匹配时，优先核对启动日志显示的 profile 与数据库连接错误。

## 手机号注册与真实短信

本地试用保持 SMS_MODE=console：在注册页输入有效的大陆 11 位手机号，点击“获取验证码”，页面显示本地测试验证码，填入后即可注册。console 不会向手机发送短信。已经存在的手机号（包括默认管理员）不能重复注册。

要让手机收到验证码，需要在阿里云、腾讯云等平台完成短信服务开通、签名和验证码模板审核，准备服务端调用凭证及短信额度。当前 SmsGateway 使用通用 HTTPS webhook 协议，短信网关负责通过供应商 SDK 发送短信。

```properties
SMS_MODE=webhook
SMS_SECRET=至少32字符的随机密钥
SMS_WEBHOOK_URL=https://你的短信网关域名/send
SMS_WEBHOOK_TOKEN=网关授权令牌
```

SMS_WEBHOOK_URL 是你自己的短信适配服务 URL；它接收下文的 phone/code/expiresIn/purpose JSON 和 Bearer 令牌，在供应商接受发送后返回 2xx。供应商的原始 API 有独立的签名与模板协议，需要网关适配；阿里云/腾讯云的 AccessKey、模板 ID 不能直接作为通用 webhook 令牌使用。

本地开发也可使用真实 webhook；上线时选择 spring.profiles.active=prod,mysql，配置 HTTPS PUBLIC_ORIGIN、真实网关、稳定随机 SMS_SECRET 和管理员强密码。供应商密钥只存后端或网关配置，前端无需改动。SMS_SECRET 是验证码 HMAC 密钥，SMS_WEBHOOK_TOKEN 是网关调用令牌，两者用途不同。

## 配置

本地直接启动，无需提前复制配置。需要覆盖默认值时，可使用 IDEA 环境变量，或复制 `.env.example` 为 `.env`。Spring 配置会读取工作目录的 `.env`，从根目录启动时也会读取 `backend/.env`。该文件按 Java properties 解析，值不要包引号。配置可在 `src/main/resources/application*.yml` 查看。

| 配置项 | 用途 |
| --- | --- |
| PORT / HOST | 默认 3001 / 127.0.0.1 |
| DATABASE_URL | 默认 jdbc:h2:file:./data/platform;DB_CLOSE_ON_EXIT=FALSE;LOCK_TIMEOUT=10000 |
| DATABASE_USERNAME / DATABASE_PASSWORD | H2 默认 sa / 空密码；MySQL 使用创建的应用账号和密码 |
| ADMIN_PHONE / ADMIN_PASSWORD / ADMIN_NAME | 首次创建管理员；dev 默认 13800000000 / DevAdmin123!，prod 无默认值 |
| ADMIN_ENABLED | 默认 true；已有管理员时可关闭首次初始化 |
| SMS_MODE | console（仅开发测试）或 webhook |
| SMS_SECRET | 验证码 HMAC 密钥，生产至少 32 字符；保持稳定，修改会使现有验证码失效 |
| SMS_WEBHOOK_URL / SMS_WEBHOOK_TOKEN | HTTPS 网关地址与 Bearer 授权令牌 |
| PUBLIC_ORIGIN | 允许写操作的前端 Origin，生产必须为 HTTPS |
| SPRING_PROFILES_ACTIVE | IDEA/系统环境变量；MySQL 选择 dev,mysql 或 prod,mysql；.env 中用 spring.profiles.active |

开发环境没有配置 PUBLIC_ORIGIN 时，仅接受与请求 Host 相同的 Origin、localhost:5173 或 127.0.0.1:5173。代理不能随意重写 Host。生产必须配置唯一的 HTTPS Origin；不要把 API 直接当成跨域接口使用。

## 接口

建表 SQL：[`src/main/resources/schema.sql`](src/main/resources/schema.sql) 和 [`sql/schema-mysql.sql`](sql/schema-mysql.sql)，表说明见 [`sql/README.md`](sql/README.md)。默认执行 schema.sql，mysql profile 执行 schema-mysql.sql；两种脚本各自与运行时副本一致。

所有请求与响应使用 JSON，字符串、布尔及数字字段不接受隐式类型转换。密码至少 8 个字符且 UTF-8 编码不超过 72 字节；中文和表情按字节计算。错误响应为 `{ "message": "说明" }`。登录后使用 HttpOnly、SameSite=Lax 的 `zhiqi_session` Cookie，有效期 7 天；前端不存储身份令牌。状态：400 参数错误，401 未登录，403 无权限，409 重复/名额冲突，429 频率限制。

| 方法 | 路径 | 参数 / 说明 |
| --- | --- | --- |
| GET | /api/health | 健康检查 |
| POST | /api/auth/sms | phone；发送注册验证码 |
| POST | /api/auth/register | phone, name, password, code；只能注册 ordinary |
| POST | /api/auth/login | phone, password |
| POST | /api/auth/logout | 退出并撤销当前会话 |
| GET | /api/auth/me | 当前用户、角色、学习权限和名额使用情况 |
| POST | /api/auth/password | oldPassword, newPassword；撤销旧会话并签发当前新会话 |
| GET | /api/users | page（默认 1）, pageSize（默认 20，最大 100）, search；按管理范围分页，返回 users/me/total/page/pageSize |
| GET | /api/users/parents | role=principal 或 teacher, search；仅管理员查询有效上级，最多 50 条 |
| POST | /api/users | phone, name, password, role；管理员可附加 parentId, teacherQuota, studentQuota |
| PATCH | /api/users/:id | 按管理范围编辑 name, active, password；管理员还可编辑 phone、名额及普通/可学习角色 |
| POST | /api/users/:id/purchase | reference；仅管理员确认实际购买，升级 learner |
| GET | /api/learning/access | 校验学习权益；普通和政府账号被拒绝 |
| GET | /api/government/stats | 仅政府/管理员：用户总量、启用数、六角色计数、购买确认数、统计时间 |

公开注册角色由服务端固定，校长/老师创建的 parentId 由服务端固定；伪造角色、上级或对象 ID 均无法越权。管理员创建老师也必须指定有效校长且满足名额；独立学生可不设置上级，挂到老师时同样占用名额。

校长可创建 teacher 并设置 studentQuota，也可调整自己老师的 studentQuota，总分配量不能超过学校 studentQuota。老师只能创建 learner，不能给自己或学生升权、增加名额。名额为 0 至 100000 的整数；已使用或分配的名额不能被调低。名额检查与创建在同一个 Spring 数据库事务内完成，并按机构根账号加 operation_locks 数据库行锁，避免并发超额。

PATCH 不支持机构、老师、管理员或政府角色互转，也不支持直接改归属，避免破坏现有机构关系；这几种角色需创建专用账号。管理员可以修改全部用户资料、重置密码、停用账号、配置配额以及调整普通/可学习权益。停用替代物理删除，保留归属、购买和审计记录。最后一个管理员和自己的账号不能被停用。

## 短信网关协议

服务端向 SMS_WEBHOOK_URL 发起 HTTPS POST：

```json
{ "phone": "13900000000", "code": "123456", "expiresIn": 300, "purpose": "register" }
```

请求带 `Authorization: Bearer <SMS_WEBHOOK_TOKEN>`。网关应在供应商确认发送成功后返回 2xx，失败返回非 2xx；超时 10 秒视为失败。本项目只提供这个网关适配协议，供应商签名/模板和凭证需要配置在网关中，当前没有部署真实网关。

验证码只存 HMAC，有效 5 分钟，同手机号 60 秒冷却、每天最多 10 次、每 IP 每小时最多 20 次；错误最多 5 次，成功注册后立即销毁。登录按手机号每 15 分钟最多 10 次尝试（成功清除该手机号计数），每 IP 最多 100 次。

## 运行与部署边界

`.\mvnw.cmd verify` 使用 JUnit、Spring Boot 和 MockMvc 检查真实安全过滤链、事务与 H2 内存数据库，覆盖短信注册、验证码状态、层级隔离、配额并发、购买权益、政府只读、密码/会话撤销、登录限流与生产配置，并覆盖 UTF-8 长密码边界、分页搜索和上级查询范围、严格 PATCH 类型以及跨机构锁隔离。密码使用 BCrypt；会话令牌只存 SHA-256 摘要，验证码只存 HMAC。

默认持久化文件为工作目录下 `data/platform.mv.db`，从 backend 或共享 IDEA 配置启动时位于 backend/data。保留同一工作目录或设置绝对 DATABASE_URL，避免产生多套数据库。备份应使用 H2 一致性备份或停止服务后复制文件。使用 MySQL 时同时启用 mysql profile 选择对应的驱动和建表脚本。

生产由 HTTPS 同域代理提供静态前端和 `/api`，后端默认仅监听回环地址。代理后当前 IP 限流使用直连代理地址，不读取用户可伪造的 X-Forwarded-For；多客户端共用代理时，应在可信网关另外配置真实客户端的限流。课程内容/资源下载服务尚未实现，后续接口必须继续调用同样的服务端学习权限检查。

MySQL 适配已使用独立的 MySQL 8.0.43 实例及专用应用用户执行 13 项账号接口回归，包含短信状态、上下级权限、并发配额、购买和分页。测试不会读取或迁移原 H2 数据。默认 H2 仍可直接启动。
