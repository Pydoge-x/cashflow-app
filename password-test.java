import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 简单的密码加密验证测试
 * 使用方式: 
 * 1. 将此文件放入项目中
 * 2. 在项目中运行: java password-test.java
 * 
 * 或直接在数据库中验证密码
 */
public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String rawPassword = "testPassword123";
        
        // 加密密码
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后的密码: " + encodedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 测试错误密码
        boolean wrongMatches = encoder.matches("wrongPassword", encodedPassword);
        System.out.println("错误密码验证结果: " + wrongMatches);
    }
}
