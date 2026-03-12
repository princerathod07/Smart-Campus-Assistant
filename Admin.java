package com.campus.users;

/**
 * Admin class - extends User (Inheritance).
 * Demonstrates: Inheritance, Polymorphism (overrides abstract methods differently)
 */
public class Admin extends User {

    private String adminCode;
    private String department;
    private String accessLevel; // FULL, RESTRICTED

    public Admin() {
        super();
    }

    public Admin(String userId, String username, String passwordHash,
                 String email, String fullName,
                 String adminCode, String department, String accessLevel) {
        super(userId, username, passwordHash, email, fullName, "ADMIN");
        this.adminCode = adminCode;
        this.department = department;
        this.accessLevel = accessLevel;
    }

    public Admin(String userId, String username, String passwordHash,
                 String email, String fullName,
                 String adminCode, String department,
                 String accessLevel, String registeredAt) {
        super(userId, username, passwordHash, email, fullName, "ADMIN", registeredAt);
        this.adminCode = adminCode;
        this.department = department;
        this.accessLevel = accessLevel;
    }

    /**
     * Dynamic Method Dispatch - different from Student's implementation
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardMessage() {
        return "Admin Panel — " + getFullName()
             + " | Department: " + department
             + " | Access: " + accessLevel;
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + adminCode
             + "|" + department + "|" + accessLevel;
    }

    // Getters and Setters
    public String getAdminCode()              { return adminCode; }
    public void setAdminCode(String c)        { this.adminCode = c; }

    public String getDepartment()             { return department; }
    public void setDepartment(String d)       { this.department = d; }

    public String getAccessLevel()            { return accessLevel; }
    public void setAccessLevel(String l)      { this.accessLevel = l; }

    @Override
    public String toString() {
        return "Admin{" + super.toString()
             + ", code=" + adminCode
             + ", access=" + accessLevel + "}";
    }
}
