// src/main/java/com/mycompany/ths/model/User.java
package com.mycompany.ths.model;

public class User {

    private String username;
    private String passwordHash; // store a hash, not plain text (simple demo)
    private String role;         // "PATIENT" or "DOCTOR"

    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }
}
