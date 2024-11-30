package com.java.FTPServer.ulti;

import com.java.FTPServer.system.UserSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserSessionManager {

    public static void setUserSession(UserSession userSession) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userSession.getUsername(),
                null, // mật khẩu không cần thiết
                null // authorities nếu cần, hoặc có thể để null
        );

        // Đặt Authentication vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lưu thông tin người dùng trong context (bạn có thể lưu thêm thông tin khác nếu muốn)
        // Nếu cần thêm thông tin, có thể sử dụng `Authentication` tùy chỉnh
    }

    // Lấy thông tin người dùng từ SecurityContext
    public static UserSession getUserSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu Authentication tồn tại và người dùng đã đăng nhập
        if (authentication != null) {
            String username = authentication.getName();
            // Trả về UserSession chứa thông tin của người dùng
            return new UserSession(username);
        }

        return null;
    }
}

