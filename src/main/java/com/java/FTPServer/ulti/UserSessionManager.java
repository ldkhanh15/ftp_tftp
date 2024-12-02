package com.java.FTPServer.ulti;

import com.java.FTPServer.enums.UserStatus;
import com.java.FTPServer.system.UserSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class UserSessionManager {

    // Lưu tất cả thông tin trong UserSession vào SecurityContext
    public static void setUserSession(UserSession userSession) {
        // Tạo một custom Authentication chứa tất cả thông tin từ UserSession
        Authentication authentication = new AbstractAuthenticationToken(null) {
            @Override
            public Object getPrincipal() {
                return userSession.getUsername(); // Lưu tên người dùng
            }

            @Override
            public Object getCredentials() {
                return null; // Không cần mật khẩu
            }

            @Override
            public Object getDetails() {
                return userSession; // Lưu toàn bộ thông tin của UserSession
            }

            @Override
            public boolean isAuthenticated() {
                return userSession.getStatus() != null && userSession.getStatus() != UserStatus.NOT_LOGGED_IN;
            }
        };

        // Đặt Authentication vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Lấy thông tin người dùng từ SecurityContext
    public static UserSession getUserSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu Authentication tồn tại và người dùng đã đăng nhập
        if (authentication != null) {
            // Lấy thông tin UserSession từ authentication
            UserSession userSession = (UserSession) authentication.getDetails();
            return userSession;
        }

        return null; // Nếu không có thông tin người dùng
    }
}
