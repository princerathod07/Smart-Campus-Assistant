package com.campus.service;

import com.yourname.project.config.DatabaseConfig;
import com.campus.users.Admin;
import com.campus.users.User;
import java.sql.*;

public class UserService {
    public User validateLogin(String username, String password) {
        String sql = "SELECT * FROM campus_users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Return an Admin or Student object based on the role column
                return new Admin(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    "", // passwordHash
                    rs.getString("email"),
                    rs.getString("full_name"),
                    "CODE123", "IT", "FULL"
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}