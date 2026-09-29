# 智启未来科技教育云平台

项目按前后端分目录：

```text
project/
├─ frontend/            Vue 3 + Vite 前端
│  ├─ src/              Vue 页面、样式和演示数据
│  ├─ package.json      npm 脚本与依赖
│  └─ vite.config.js    Vite 配置
└─ backend/             后端开发目录（目前尚未实现）
```

## 运行前端

在 IDEA 中打开整个 `project` 文件夹，在终端执行：

```powershell
cd frontend
npm install
npm run dev
```

浏览器打开终端给出的地址，通常是 `http://localhost:5173/`。构建命令是 `npm run build`，也应在 `frontend` 目录执行。

目前前端是独立原型，演示数据保存在浏览器 `localStorage` 中。后端接入前，登录、新闻、任务等操作不会写入服务器。前端功能见 [frontend/README.md](frontend/README.md)。

## 添加后端

后端代码放在 `backend/`。如果使用 Java Spring Boot，可以在该目录创建 Maven 项目，使 `backend/pom.xml`、`backend/src/main/java/` 和 `backend/src/main/resources/` 位于同一级。后端独立启动，前端通过 `/api/` 请求其接口；接入时再配置 Vite 开发代理和前端 API 调用。
