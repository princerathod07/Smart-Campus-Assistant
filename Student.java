package com.campus.users;

/**
 * Student class - extends User (Inheritance).
 * Demonstrates: Inheritance, Method Overriding (Dynamic Method Dispatch / Polymorphism)
 */
public class Student extends User {

    private String studentId;
    private String department;
    private String semester;
    private String program;  // BSc, BEng, MBA, etc.

    // Default constructor
    public Student() {
        super();
    }

    // Full constructor
    public Student(String userId, String username, String passwordHash,
                   String email, String fullName,
                   String studentId, String department,
                   String semester, String program) {
        super(userId, username, passwordHash, email, fullName, "STUDENT");
        this.studentId = studentId;
        this.department = department;
        this.semester = semester;
        this.program = program;
    }

    // Load-from-file constructor
    public Student(String userId, String username, String passwordHash,
                   String email, String fullName,
                   String studentId, String department,
                   String semester, String program, String registeredAt) {
        super(userId, username, passwordHash, email, fullName, "STUDENT", registeredAt);
        this.studentId = studentId;
        this.department = department;
        this.semester = semester;
        this.program = program;
    }

    /**
     * Overrides abstract method - Dynamic Method Dispatch
     */
    @Override
    public String getRole() {
        return "STUDENT";
    }

    /**
     * Overrides abstract method - polymorphic behavior
     */
    @Override
    public String getDashboardMessage() {
        return "Welcome back, " + getFullName() + "! "
             + "You are logged in as a student of " + department + ".";
    }

    /**
     * Serialize to file with extra student fields.
     */
    @Override
    public String toFileString() {
        return super.toFileString() + "|" + studentId + "|"
             + department + "|" + semester + "|" + program;
    }

    // Getters and Setters
    public String getStudentId()              { return studentId; }
    public void setStudentId(String id)       { this.studentId = id; }

    public String getDepartment()             { return department; }
    public void setDepartment(String d)       { this.department = d; }

    public String getSemester()               { return semester; }
    public void setSemester(String s)         { this.semester = s; }

    public String getProgram()                { return program; }
    public void setProgram(String p)          { this.program = p; }

    @Override
    public String toString() {
        return "Student{" + super.toString()
             + ", studentId=" + studentId
             + ", dept=" + department + "}";
    }
}
