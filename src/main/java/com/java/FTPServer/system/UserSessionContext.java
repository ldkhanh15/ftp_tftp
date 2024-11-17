package com.java.FTPServer.system;

public class UserSessionContext {
    private static final ThreadLocal<UserSession> userSessionThreadLocal = new ThreadLocal<>();

    public static void setUserSession(UserSession userSession) {
        userSessionThreadLocal.set(userSession);
    }

    public static UserSession getUserSession() {
        UserSession session = userSessionThreadLocal.get();
        if (session == null) {
            throw new IllegalStateException("No user session is set for the current thread.");
        }
        return session;
    }

    public static void clear() {
        userSessionThreadLocal.remove();
    }
}
