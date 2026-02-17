# 登录"Bad credentials"问题诊断指南

## 问题原因

"Bad credentials" 错误通常表示：
- 用户名不存在
- 密码验证失败

## 已修复的问题

在 `SecurityConfig.java` 中进行了以下修复：

### 1. **AuthenticationManager 配置** 
- **原问题**: 使用 `AuthenticationConfiguration.getAuthenticationManager()` 可能不会正确使用 `DaoAuthenticationProvider`
- **解决方案**: 改为直接使用 `ProviderManager` 创建 `AuthenticationManager`，明确指定使用 `DaoAuthenticationProvider`

```java
@Bean
public AuthenticationManager authenticationManager() {
    // 直接创建ProviderManager，确保使用DaoAuthenticationProvider
    return new ProviderManager(authenticationProvider());
}
```

### 2. **PasswordEncoder 配置**
- 使用 `BCryptPasswordEncoder` 进行密码加密和验证

## 测试步骤

### 步骤 1: 重新编译项目
```bash
cd cashflow
mvn clean compile -DskipTests
```

### 步骤 2: 启动后端服务
```bash
mvn spring-boot:run
```

### 步骤 3: 测试注册和登录

#### 注册请求
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "password": "Test@123456",
    "gender": "MALE",
    "age": 25
  }'
```

预期响应：
```json
{
  "message": "User registered successfully!"
}
```

#### 登录请求
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456"
  }'
```

预期响应：
```json
{
  "token": "eyJhbGc...",
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    ...
  }
}
```

## 如果问题仍然存在

### 检查 1: 验证数据库中的密码

```sql
SELECT id, username, password FROM users WHERE username = 'testuser';
```

数据库中的 `password` 字段应该包含以 `$2a$` 或 `$2b$` 开头的 BCrypt 哈希值。

**错误情况示例**:
```
password: Test@123456  -- ❌ 明文密码，说明加密失败
```

**正确情况示例**:
```
password: $2a$10$slYQm... -- ✅ BCrypt 加密密码
```

### 检查 2: 验证 UserDetailsServiceImpl

确保 [UserDetailsServiceImpl.java](cashflow/src/main/java/com/cashflow/app/security/services/UserDetailsServiceImpl.java) 正确实现：

```java
@Override
@Transactional
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + username));
    return UserDetailsImpl.build(user);
}
```

### 检查 3: 验证 DaoAuthenticationProvider

观察应用日志中的身份验证过程：

```log
[INFO] Authenticating user: testuser
[DEBUG] UserDetailsImpl loaded with password hash from database
[DEBUG] Password verification: matches = true/false
```

如果显示 `matches = false`，说明密码验证失败，可能原因：
- 加密时使用的 `PasswordEncoder` 和验证时不一致
- 密码在数据库中存储时被意外修改

### 检查 4: 验证数据库连接

```sql
-- 验证数据库是否正确存储了密码
SELECT username, CHAR_LENGTH(password) as password_length FROM users;
```

BCrypt 密码通常长度为 60 个字符。

## 解决方案总结

✅ **已应用的修复**:
1. 重新配置 `AuthenticationManager` 使用 `ProviderManager`
2. 明确指定 `DaoAuthenticationProvider`
3. 确保 `PasswordEncoder` 在所有步骤中一致

✅ **后续建议**:
1. 清除旧的用户数据，重新注册测试账户
2. 检查数据库中的密码是否正确加密
3. 查看应用日志获取详细的认证错误信息

## 常见问题

**Q: 为什么注册成功但登录失败？**
A: 可能是注册时没有使用 `PasswordEncoder` 进行加密，导致密码以明文方式存储在数据库中。

**Q: 密码有特殊字符会影响认证吗？**
A: 不会。`BCryptPasswordEncoder` 可以处理任何字符。

**Q: 需要重启应用吗？**
A: 是的。修改 `SecurityConfig` 后需要重启应用，确保新的配置生效。

## 获取更多调试信息

在 `application.properties` 中添加：

```properties
# 启用Spring Security调试
logging.level.org.springframework.security=DEBUG

# 启用SQL日志
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

这样可以在日志中看到详细的身份验证过程。
