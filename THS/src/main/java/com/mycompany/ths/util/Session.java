package com.mycompany.ths.util;

public final class Session {

    private Session() {
    }
    private static String currentUser;
    private static String role; // "patient" | "doctor" | "admin"

    public static void login(String user, String r) {
        currentUser = user;
        role = r;
    }

    public static void logout() {
        currentUser = null;
        role = null;
    }

    public static String user() {
        return currentUser;
    }

    public static String role() {
        return role;
    }

    public static String getUsername() {
        return currentUser;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isPatient() {
        return "PATIENT".equalsIgnoreCase(role);
    }

    public static boolean isDoctor() {
        return "DOCTOR".equalsIgnoreCase(role);
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
