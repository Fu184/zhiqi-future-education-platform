# Git 提交与推送说明

`.gitignore` 决定新文件是否进入提交候选范围，不会取消已经跟踪的文件。`.gitattributes` 统一源码换行符并保护图片等二进制文件。`.github/workflows/check-project.yml` 检查仓库文件、前端构建和后端测试；`deploy-pages.yml` 仅发布静态前端。

## 应提交的文件

- 前端 `src/`、可选 `public/`、入口 HTML、Vite 配置、格式配置及实际使用的图片。
- 后端 `src/main/`、`src/test/`、`sql/`、`pom.xml`、Maven Wrapper 脚本和 `.mvn/wrapper/`。
- `package.json` 与 `package-lock.json` 必须同时保留，以支持 `npm ci`。
- `backend/.env.example` 使用占位配置；真实 `backend/.env` 留在本机。
- 根目录 Maven 配置、README、Git 配置、校验脚本和 `.run/` 共享运行配置。

不要为了减少提交数量而忽略 `src/`、SQL、依赖锁文件、图片或 Maven Wrapper。数据库建表 SQL 是源码，需要提交；数据库中的实际账号数据不提交。

## 不提交但可保留在本机的文件

`node_modules/`、`dist/`、`target/`、Vite 缓存、IDE 私有设置、`artifacts/` 截图与临时检查输出、日志、数据库文件、真实 `.env` 与密钥。运行依赖和数据库并非垃圾，本次清理保留它们。需要发布 JAR 时通过单独的发布产物流程，不提交到源码仓库。

## 本机提交与推送

本机已设置 `git config --local core.hooksPath .githooks`。`git push` 会先检查实际推送的提交，有遗漏或无关文件就拒绝推送。钩子需要 Git 和 Node.js。在 Linux/macOS 上需保证 `.githooks/pre-push` 可执行（`chmod +x .githooks/pre-push`）；以下提交步骤会保存钩子和 Maven Wrapper 的可执行位。此设置属于本机 Git 配置，不随克隆传递；新机器克隆后在项目根目录执行：

```powershell
git config --local core.hooksPath .githooks
```

在项目根目录运行以下步骤。`git add -A` 同时包含新增源码和已经删除的旧预览，避免只执行 `git commit -am` 漏掉新文件。

```powershell
node scripts/check-git-files.mjs
git add -A
git add --chmod=+x .githooks/pre-push backend/mvnw
git diff --cached --stat
git diff --cached --name-status
git commit -m "整理前后端结构与首页交互"
node scripts/check-git-files.mjs --committed
git push origin main
```

检查器默认核对工作区中的提交候选，提示未加入 Git 的文件数量；`--committed` 核对 HEAD 中真正会随提交推送的文件，发现必需文件遗漏、未提交新增文件或无关文件时返回失败。它不替代上面两条 diff 命令对修改内容的检查，也不会自动暂存、提交或推送。`.githooks/pre-push` 是需提交的共享钩子。

`--committed` 还会阻止已跟踪文件的未提交修改被遗漏。推送只发送提交，不发送工作区修改；`.gitignore` 也不扫描源码中的密码内容。不要把真实数据库密码和短信令牌写进源码、SQL 或说明文档。

查看某个文件被忽略的原因：

```powershell
git check-ignore -v backend/.env
git status --short --untracked-files=all
```

若私有文件以前已经提交，添加忽略规则后还需从 Git 索引移除，并在下一次提交记录该变更；这不会自动移除历史提交中的内容。

## GitHub 自动检查与发布

所有分支推送和 Pull Request 都运行项目检查。GitHub Pages 只在 `main` 的前端、Git 配置或发布工作流变更时发布，也可以手动运行。仓库 Settings → Pages 的 Source 应设置为 GitHub Actions。

Pages 仅托管前端；正常登录和注册仍需已部署的 Spring Boot 服务及 `/api` 路由配置。后端 `.env`、MySQL 数据和本机测试账号不会上传。

要强制合并前通过检查，可在 GitHub 的分支保护规则中要求 `Repository files`、`Frontend` 和 `Backend` 这三个检查通过；本地脚本和工作流本身不会修改远端分支保护设置。
