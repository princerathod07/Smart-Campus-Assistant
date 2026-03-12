package com.campus.users;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all campus users.
 * Demonstrates: OOP (classes, constructors, encapsulation), Inheritance base
 */
public abstract class User implements Serializable {

    // Encapsulated fields
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String role;
    private String registeredAt;

    // Default constructor
    public User() {}

    // Parameterized constructor - demonstrates constructor overloading
    public User(String userId, String username, String passwordHash,
                String email, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.registeredAt = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Constructor with timestamp (used when loading from file)
    public User(String userId, String username, String passwordHash,
                String email, String fullName, String role, String registeredAt) {
        this(userId, username, passwordHash, email, fullName, role);
        this.registeredAt = registeredAt;
    }

    // Abstract method - forces subclasses to implement (polymorphism setup)
    public abstract String getRole();
    public abstract String getDashboardMessage();

    // Getters and Setters (Encapsulation)
    public String getUserId()       { return userId; }
    public void setUserId(String id){ this.userId = id; }

    public String getUsername()           { return username; }
    public void setUsername(String u)     { this.username = u; }

    public String getPasswordHash()       { return passwordHash; }
    public void setPasswordHash(String p) { this.passwordHash = p; }

    public String getEmail()              { return email; }
    public void setEmail(String e)        { this.email = e; }

    public String getFullName()           { return fullName; }
    public void setFullName(String n)     { this.fullName = n; }

    public String getRegisteredAt()       { return registeredAt; }
    public void setRegisteredAt(String t) { this.registeredAt = t; }

    /**
     * Serialize user to CSV line for file storage.
     * Demonstrates: File Handling concept
     */
    public String toFileString() {
        return userId + "|" + username + "|" + passwordHash + "|"
             + email + "|" + fullName + "|" + getRole() + "|" + registeredAt;
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username=" + username
             + ", role=" + getRole() + ", email=" + email + "}";
    }
}
