# 智启未来 · 前端

Vue 3 + Vite + Vue Router。运行 `npm install`、`npm run dev`，构建 `npm run build`。后端默认 `127.0.0.1:3001`，开发代理配置在 `vite.config.js`。

源码已统一按 Prettier 格式整理。修改后运行 `npm run format`；检查格式运行 `npm run format:check`。

## 页面源码索引

路由集中在 `src/router/index.js`。独立业务页面按文件名查找；同类专题与网站说明复用页面组件，并从数据配置中读取内容。

| URL | 页面 | 源码（src/pages/） |
| --- | --- | --- |
| / | 首页 | HomePage.vue |
| /news | 新闻资讯 | NewsPage.vue |
| /courses | 课程资源 | CoursesPage.vue |
| /tasks | 学习任务 | TasksPage.vue |
| /create | 创作中心 | CreatePage.vue |
| /works | 作品展示 | WorksPage.vue |
| /assignments | 作业表 | AssignmentsPage.vue |
| /my-works | 我的作品 | MyWorksPage.vue |
| /assistant | AI 助手 | AssistantPage.vue |
| /shop | 商城 | ShopPage.vue |
| /organization | 学校与机构 | OrganizationPage.vue |
| /admin | 原有内容管理演示 | ContentAdminPage.vue |
| /users | 分级用户管理 | UserManagementPage.vue |
| /government | 政府独立数据概览 | GovernmentDashboardPage.vue |
| /account | 账户资料 / 修改密码 | AccountPage.vue |
| /login | 登录 / 短信注册 | LoginPage.vue |
| /teacher-growth | 教师培养中心 | TopicPage.vue |
| /course-services、/products、/competitions、/study-tours 及其首页细分栏目 | 五个中心与专题指引 | TopicPage.vue |
| /privacy、/site-notice | 隐私说明、网站使用说明 | SiteInformationPage.vue |
| /faculty | 师资介绍 | FacultyPage.vue |
| /course-intro | 课程介绍 | CourseIntroPage.vue |
| /join | 加入我们 | JoinPage.vue |
| /about | 关于我们 | AboutPage.vue |
| /contact | 联系我们 | ContactPage.vue |

首页主轮播与资讯轮播由 `HomeHero.vue` 维护，主轮播位于居中的内容区，两侧留白。导航子菜单在选择、按 Escape 或失去焦点时收起。

实际 URL 使用 hash，例如 `http://localhost:5173/#/users`。

同类专题路由复用 `TopicPage.vue`，栏目定义与正文分别位于 `data/homeSections.js` 和 `data/topicContent.js`。`App.vue` 只组装布局、路由出口和全局通知；页面独有状态通过各自 composable 创建，离开页面后销毁。全局 provide/inject 只共享导航、通知和详情弹窗，不共享页面业务表单。

## 其他文件对应什么

下表列出前端除页面之外的所有自有文件。公共文件支持多个页面，不单独对应一个页面。

| 文件 | 对应页面 / 用途 |
| --- | --- |
| `index.html` | 所有页面的 HTML 容器，加载 `src/main.js` |
| `src/main.js` | 所有页面的 Vue 启动入口，注册路由并加载样式 |
| `src/App.vue` | 组装 PortalLayout、RouterView、详情弹窗和全局通知 |
| `src/router/index.js` | 全部页面的 URL、入口文件及访问权限 |
| `src/components/home/HomeHero.vue` | 首页主轮播和资讯轮播、播放暂停及图片切换 |
| `src/components/AboutContent.vue` | 关于我们、联系我们的共用内容 |
| `src/components/InfoPage.vue` | 师资介绍、课程介绍、培训等页面的共用介绍内容 |
| `src/layouts/PortalLayout.vue` | 门户导航、页脚及网络状态提示；政府账号隐藏门户导航和页脚 |
| `src/components/dialogs/ContentDetailsDialog.vue` | 首页、课程、新闻和作品共用的详情弹窗 |
| `src/config/portal.js` | 静态导航、图标、新闻分类和介绍页面文案 |
| `src/composables/useUi.js` | 共享导航、移动菜单、通知及详情弹窗状态 |
| `src/composables/useNews.js` | 新闻页筛选和 URL 分类同步 |
| `src/composables/useCourses.js` | 课程页筛选、课程详情、收藏及学习入口 |
| `src/composables/useTasks.js` | 学习任务筛选及当前用户进度 |
| `src/composables/useCreator.js` | 创作页编辑、预览和保存作品 |
| `src/composables/useWorks.js` | 根据用户 ID 筛选我的作品 |
| `src/composables/useAssistant.js` | AI 助手页面的演示对话；离开页面即销毁 |
| `src/composables/useShop.js` | 当前用户的选课清单 |
| `src/composables/useContentAdmin.js` | 内容管理页面的表单及演示数据操作 |
| `src/composables/useUserManagement.js` | 用户管理的服务端分页、搜索、上级选择和账号编辑 |
| `src/stores/auth.js` | 当前服务端会话身份及网络状态；不持久化令牌 |
| `src/stores/demo.js` | 新闻演示缓存，以及按用户 ID 保存的任务、作品、收藏、学习记录和选课清单 |
| `src/data.js` | 纯静态演示课程、新闻、任务和作品初始数据，不保存身份 |
| `src/services/api.js` | 登录注册、账户设置、用户管理、政府统计等页面的 API 请求 |
| `src/services/auth.js` | 会话恢复、并发请求合并、30 秒公共页缓存、退出及角色入口 |
| `src/services/password.js` | 与后端一致的 BCrypt 密码 UTF-8 字节校验 |
| `src/style.css` | 门户、课程、新闻、任务、创作、作品等页面的公共样式 |
| `src/styles/account.css` | 登录注册、用户管理、账户设置、政府统计页面的补充样式 |
| `img/` | 全部图片与来源说明；保留原始照片，主轮播和登录背景使用同目录的 WebP |
| `scripts/browser-smoke.mjs` | 可选浏览器联调脚本，不是页面；验证登录、分级账号、政府入口等功能 |
| `vite.config.js` | 前端开发与打包配置，包含 `/api` 后端代理 |
| `package.json` | 依赖及启动、构建、格式化命令 |
| `package-lock.json` | 锁定依赖版本，保证安装结果一致 |
| `.prettierrc.json` | 代码格式规则 |
| `README.md` | 本文件，页面和其他源码的对照说明 |

`node_modules/` 是 npm 安装的运行和开发依赖，保留供本地启动使用，不属于页面源码。`dist/` 和 Vite 缓存可重新生成，已在本次清理中删除；执行构建或开发命令时会自动生成。原 `previews/` 是未被页面使用的截图，已删除。检查脚本今后将截图写到项目根目录的 `artifacts/browser/`。

建表 SQL 位于 [`../backend/sql/schema-mysql.sql`](../backend/sql/schema-mysql.sql)，表用途见 [`../backend/sql/README.md`](../backend/sql/README.md)。

路由守卫与服务端双重检查管理/学习权限。政府账号只能进入数据概览和自身密码设置。公共注册不能选择高权限角色。后台名额、购买确认与账号禁用实时使用服务端数据。新闻、课程、作品、任务及原型内容管理仍保存在本地，尚未接入正式内容/支付/文件/AI 服务。

## 状态与接口约定

用户管理每页从后端加载 20 条，搜索由后端按管理范围执行；管理员选择上级时单独搜索校长或老师，最多返回 50 个候选，避免依赖当前列表页。公共页会话缓存 30 秒，进入管理、账户和学习页面时重新验证；网络错误保留当前身份并阻止新的受保护导航，401 才清空身份。

个人演示数据使用 `zhiqi-user-{用户ID}-{类型}` 存储，切换账号同步重载；“我的作品”按 `authorId` 判断归属，改名不影响记录。旧版 `zhiqi-tasks/works/favorites/enrolled` 未记录用户 ID，保留旧缓存供备份，不自动归属任何账号。新闻仍是本地公共演示内容。浏览器缓存不是正式权限边界，课程、文件、作品及学习进度正式上线仍需接入后端资源接口。

密码至少 8 个字符且 UTF-8 编码不超过 72 字节，中文和表情按实际字节计算。

## 浏览器检查

`scripts/browser-smoke.mjs` 会临时启动真实 Spring Boot JAR 与 Vite，使用 H2 内存数据库验证全部页面加载、管理员/校长/老师创建账号、短信注册、学习入口、政府隔离、同名账号数据隔离、改名后作品归属、网络故障恢复和移动端横向溢出，截图存入项目根目录 `artifacts/browser/`。先在 backend 执行 `mvn package`。需可用的 Playwright 模块、Java 和浏览器，可通过 PLAYWRIGHT_MODULE 与 BROWSER_EXECUTABLE 指定已有安装，然后在 frontend 运行：

```powershell
node scripts/browser-smoke.mjs
```

## 网站公开信息与素材维护

复制 `.env.example` 为 `.env.local`，仅填写已经核实、允许公开的运营单位、邮箱、电话、地址和备案信息。所有 `VITE_` 变量会进入前端构建，**不得填写密钥、私人电话或其他非公开资料**。未填写的字段不会在页脚显示；联系页明确说明公开联系方式尚未发布。修改后重新构建。

网站信息由 `src/config/site.js` 集中读取；页脚提供隐私说明和网站使用说明入口。当前说明依据已实现功能撰写，正式对外服务前仍需结合真实运营主体、部署环境和数据处理方式核实。不得仅删除“待配置”文字就视为已经具备上线条件。

首页 16 个细分专题的栏目介绍、使用指引和资源入口统一维护在 `src/data/topicContent.js`。公开课视频、活动排期、合作单位、价格和证书资质需提供已核实资料后发布；现有指引不表示相关服务已经开放。

全部图片放在 `img/`。`DSC_4070.jpg` 位于该目录，公共样式引用其 WebP 版本。重新生成压缩图可在项目根目录执行 `python scripts/optimize-portal-images.py`（需要 Pillow），会保留 JPG 原图，生成宽度不超过 1920 像素的 WebP。素材来源记录在 `img/SOURCES.md`。
