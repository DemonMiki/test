# xmpp-bind-plugin

这是一个 Halo 2 JAR 插件源码成品，用来配合你的游戏账号注册页。

它完成的事：

1. 接收主题页提交的 `username / password / confirmPassword`
2. 从当前登录态读取 Halo 用户名
3. 校验“一个 Halo 账户只能注册一个游戏账号”
4. 调用游戏服务器 `115.191.7.227:8081/register`
5. 成功后保存绑定记录

## 对应前端接口

主题页请把 `gameRegisterApi` 改成：

```txt
/apis/xmpp-bind.halo.run/v1alpha1/game-register
```

如果你用的是我前面给你的主题模板，可以在页面注解里设置：

- `gameRegisterApi: /apis/xmpp-bind.halo.run/v1alpha1/game-register`

## 你必须先改的地方

打开：

- `src/main/java/com/example/xmppbind/service/GameRegisterService.java`

把这两个常量改成你的实际值：

```java
private static final String GAME_SERVER_REGISTER_URL = "http://115.191.7.227:8081/register";
private static final String GAME_SERVER_INTERNAL_KEY = "CHANGE_ME_TO_YOUR_REAL_INTERNAL_KEY";
```

## 构建

环境要求：

- JDK 21
- Gradle 8+
- Halo 2.20+

构建命令：

```bash
./gradlew build
```

构建后安装：

- 把 `build/libs/*.jar` 上传到 Halo 插件管理

## 权限配置

Halo 官方文档说明：插件自定义 API 默认只有超级管理员可访问；如果要给普通用户访问，需要定义并分配角色模板。这里我已经把角色模板文件放好了：

- `src/main/resources/extensions/xmpp-bind-role-template.yaml`

你安装插件后，需要在 Halo 后台把这个模板角色分配给普通注册用户所属角色，否则玩家无法调用这个接口。这个要求来自 Halo 官方权限模型。  

## 存储规则

绑定记录保存在插件自定义模型 `GameAccountBinding` 里：

- `metadata.name = Halo 用户名`
- `spec.gameUsername = 游戏账号`

所以天然就是“一人一号”。

## 游戏服务器前提

你的游戏服务器需要已经支持：

- `POST /register`
- `X-Internal-Key` 校验
- 限制来源 IP 为网站服务器
- 成功后写入 XMPP `config.js -> localAccounts`

## 说明

这是源码成品，不包含你本地 Halo 环境的编译产物。
