package com.brad.skyserver.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

/** 新密码使用 BCrypt；旧 MD5 仅用于登录兼容，认证成功后自动升级。 */
public class EmployeePasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    public String encode(CharSequence rawPassword) { return bcrypt.encode(rawPassword); }
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        if (encodedPassword.matches("[a-fA-F0-9]{32}")) {
            String digest = DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(digest.getBytes(StandardCharsets.US_ASCII),
                    encodedPassword.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
        }
        return bcrypt.matches(rawPassword, encodedPassword);
    }
    public boolean upgradeEncoding(String encodedPassword) {
        return encodedPassword.matches("[a-fA-F0-9]{32}") || bcrypt.upgradeEncoding(encodedPassword);
    }
}
