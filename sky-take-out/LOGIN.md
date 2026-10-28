# 管理端登录与认证

启动前设置环境变量 SKY_JWT_ADMIN_SECRET_KEY，值为随机生成、至少 32 字节的密钥；请保持密钥稳定，否则已有令牌失效。数据库连接沿用 application.yml。

POST /admin/employee/login，Content-Type: application/json，请求示例：

```json
{"username":"admin","password":"123456"}
```

用户名必须已存在于 employee 表，status 为 1；项目不会自动创建管理员账号。密码支持 BCrypt 或既有的 32 位 MD5，MD5 在成功登录时自动升级为 BCrypt；新员工默认密码仍为 123456，保存为 BCrypt。

成功响应保持 code/msg/data 格式，data 包含 id、userName、name、token。参数缺失或空白返回 HTTP 400；账号不存在、密码错误或禁用账号返回 HTTP 401，code 为 0。

其余 /admin/** 接口需要携带 token 请求头，值为登录返回的 token；也支持 Authorization: Bearer <token>。JWT 默认有效期两小时，可通过 sky.jwt.admin-ttl 修改，单位为毫秒。令牌校验使用 Spring Security 官方 JWT 资源服务器支持：https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html

POST /admin/employee/logout 保持现有无状态行为，客户端须删除本地 token；服务端不维护撤销列表，已签发 token 在过期前仍有效。账号禁用会阻止后续登录，已签发 token 同样持续到过期。

测试（在 sky-take-out 目录）：

```powershell
mvn -B -pl sky-server -am test
```

EmployeeLoginTests 使用模拟 Mapper，真实执行 Spring Security 过滤器链、密码认证和 JWT 签发校验，不修改实际数据库。
