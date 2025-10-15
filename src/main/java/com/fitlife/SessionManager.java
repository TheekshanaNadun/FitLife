package com.fitlife;

public class SessionManager {

    private static int userId;
    private static String username;
    private static String role;

    // --- Set user session after login ---
    public static void setSession(int id, String user, String userRole) {
        userId = id;
        username = user;
        role = userRole;
    }

    // --- Getters ---
    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    // --- Clear session (on logout) ---
    public static void clearSession() {
        userId = 0;
        username = null;
        role = null;
    }
}
