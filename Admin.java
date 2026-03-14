package com.campus.users;

import com.yourname.project.service.bookservice; // Ensure this matches your package

/**
 * Admin class - updated for Database integration.
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

    // New Database-linked method
    public void displayAllBooks() {
        System.out.println("Accessing Library Database as Admin...");
        bookservice service = new bookservice();
        service.getAllBooks();
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getDashboardMessage() {
        return "Admin Panel — " + getFullName()
             + " | Dept: " + department
             + " | Access: " + accessLevel;
    }

    // toFileString is kept for internal use but no longer needed for storage
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
}