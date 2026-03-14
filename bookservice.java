package com.yourname.project.service;

import com.yourname.project.config.DatabaseConfig;
import java.sql.*;

public class bookservice {
    public void getAllBooks() {
        String sql = "SELECT * FROM books";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("--- Books List ---");
            while (rs.next()) {
                // This pulls the title column from your Supabase table
                System.out.println("Book Title: " + rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}