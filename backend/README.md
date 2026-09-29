# 后端目录

这里预留给后端服务，目前没有可运行的后端代码。

如果选择 Java Spring Boot，可在本目录创建项目，并按以下结构组织：

```text
backend/
├─ pom.xml
└─ src/
   └─ main/
      ├─ java/          Controller、Service、Repository 等代码
      └─ resources/     application.yml 等配置
```

后端接口建议统一使用 `/api/` 前缀。接入前端时，再将 `frontend/src/data.js` 中相应的浏览器本地数据读写替换为 API 请求。
