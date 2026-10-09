# 智启未来科技教育云平台

Vue 3 + Vite 前端，Spring Boot 3.5.6 + Spring Security + Spring JDBC + H2 / MySQL 后端。登录、短信验证注册、六类用户、机构层级与名额管理、政府数据概览已接入后端。

本机 MySQL 数据库、表和专用用户已经建立，实际连接配置位于 `backend/.env`。在 IDEA 使用“Spring Boot 后端”配置或在 backend 工作目录运行启动类，会自动连接 MySQL。默认管理员 `13800000000` / `DevAdmin123!`。注册页面显示的测试验证码可以完成普通用户注册。

## 启动

后端需要 JDK 17 或更高版本。Maven Wrapper 已随项目提供，首次导入需下载依赖。前端使用 Node.js 和 npm。

1. 在 IDEA 打开项目根目录并加载根目录 `pom.xml` 为 Maven 项目，等待依赖导入。
2. 打开 `backend/src/main/java/com/zhiqi/platform/PlatformApplication.java`，点击 main 方法旁边的绿色运行按钮；也可选择共享运行配置“Spring Boot 后端”。建议工作目录设为 `backend`，共享配置已设置。社区版 IDEA 的普通 Java Application 运行方式即可，无需专属 Spring 插件。
3. 或者在终端启动后端：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

4. 在另一个终端启动前端：

```powershell
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173/`。默认 dev 环境首次启动创建本地管理员：手机号 `13800000000`，密码 `DevAdmin123!`，登录后进入用户管理。无需先安装 MySQL，也无需复制 `.env` 才能本地启动。后端健康检查：`http://127.0.0.1:3001/api/health`。

API 默认监听 `127.0.0.1:3001`，Vite 将 `/api` 转发到后端。H2 自动建表，持久化文件为工作目录下 `data/platform.mv.db`；使用共享配置或在 backend 启动时，位于 `backend/data/`。保持工作目录一致，避免读到不同数据库。可选复制 `backend/.env.example` 为 `.env` 或在 IDEA 设置环境变量，覆盖管理员、数据库和短信配置。创建后的账号不会随环境变量变更自动重置密码。

后端已移除 Node 服务与 npm 启动脚本，后端命令统一使用 Java/Maven。若以前自行生成过 SQLite 文件，需要单独迁移；Spring Boot 不会覆盖或读取这些旧文件。

MySQL 配置：先按后端说明创建数据库和专用账号，将 `backend/.env.example` 复制为 `.env` 并填写相同密码，选择 `spring.profiles.active=dev,mysql`。数据库配置和真实手机号短信注册步骤见 [backend/README.md](backend/README.md)。

## 用户体系

| 角色 | 创建方式 | 学习权限 | 管理范围 |
| --- | --- | --- | --- |
| 普通用户 ordinary | 手机短信验证自行注册 | 暂未开通 | 自己的账号 |
| 可学习用户 learner | 确认购买后开通，或老师在名额内创建 | 有 | 自己的账号 |
| 校长 principal | 管理员为购买机构创建 | 有 | 自己创建的老师，以及这些老师名下的学生 |
| 老师 teacher | 校长在名额内创建，或管理员指定所属校长 | 有 | 自己创建的学生 |
| 最大管理员 admin | 首次启动配置，或已有管理员创建 | 有 | 全部用户 |
| 政府账号 government | 管理员创建 | 独立统计入口 | 只读汇总数据，无用户明细与管理权限 |

管理员设置校长的老师名额和学校学生总名额；校长分配学生名额给老师，分配总量不能超过学校总名额。老师创建学生时消耗自己的名额。停用账号不释放名额，避免通过反复停用绕过限制。停用校长或老师会同时阻止其下级账号继续登录与访问。

政府账号登录后进入独立的 `/government` 页面，仅展示用户总量、启用账号数、角色分布和已确认购买记录数。其他门户页面会跳回该页面。

## 短信与购买

开发默认 `SMS_MODE=console`：界面显示测试验证码，**不会发送真实短信**。正式环境需设置 `SMS_MODE=webhook`、HTTPS 短信网关地址及令牌；网关负责调用阿里云、腾讯云等供应商，协议见 [backend/README.md](backend/README.md)。生产环境拒绝测试短信配置，也不会返回验证码。

公共注册必须验证短信。内部老师和学生账号由已认证的上级配置，使用初始密码登录，可在“账户设置”修改密码。机构、管理、政府角色无法通过公开注册获取。

尚未接入支付供应商：管理员在用户管理中填写已核实的真实购买凭证，确认后开通学习角色；凭证编号全局唯一，记录确认人和时间。也可由管理员直接调整普通/可学习角色，以处理人工权益修正。课程、新闻、作品、任务、AI 问答等仍为前端演示数据；政府页不把演示课程/作品数量当成后台实数。

## 代码组织

前端的布局、路由页面、业务 composable、认证状态和个人演示缓存分别存放；页面名称与 URL 的完整对照见前端说明。后端按 Controller / Service / Repository 分层，用户 DTO 与数据库记录分开，列表支持服务端分页，配额事务按机构加锁。项目根目录 `.editorconfig` 统一编码和缩进规则。

个人演示数据按用户 ID 隔离；旧版没有账号 ID 的浏览器缓存保留供备份，不自动分配给当前用户。

## 源码索引与验证

- [frontend/README.md](frontend/README.md)：每个页面的 URL、名称和源码文件。
- [backend/README.md](backend/README.md)：接口、配置、权限和部署说明。
- [H2 建表 SQL](backend/src/main/resources/schema.sql)、[MySQL 建表 SQL](backend/sql/schema-mysql.sql)及[数据库说明](backend/sql/README.md)。
- 后端检查：在 `backend` 运行 `.\mvnw.cmd verify`（已安装 Maven 时也可 `mvn verify`）。
- 前端构建：在 `frontend` 运行 `npm run build`。
- 可选浏览器检查：`frontend/scripts/browser-smoke.mjs`，使用内存数据库，不改动正式账号。

生产发布需同域反向代理 `/api` 至后端，使用 HTTPS，设置 `SPRING_PROFILES_ACTIVE=prod`、`PUBLIC_ORIGIN`、真实短信参数及管理员强密码。prod 不提供开发管理员默认值，并拒绝测试短信配置与已知开发密码。GitHub Pages 只能托管静态前端，无法独立提供后端功能。

## Git 文件范围与推送

提交前运行 `node scripts/check-git-files.mjs`，提交后运行 `node scripts/check-git-files.mjs --committed`。新增源码需要 `git add -A`；仅使用 `git commit -am` 会遗漏未跟踪文件。完整提交范围、忽略规则和自动检查说明见 [GIT_GUIDE.md](GIT_GUIDE.md)。真实 `backend/.env`、数据库、运行依赖和构建输出保留在本机，不进入 Git；源码、SQL、依赖锁文件与 Maven Wrapper 必须提交。
