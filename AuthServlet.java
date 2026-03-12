package com.campus.servlets;

import com.campus.exceptions.FileStorageException;
import com.campus.exceptions.LoginException;
import com.campus.users.Student;
import com.campus.users.User;
import com.campus.utils.FileManager;
import com.campus.utils.NotificationThread;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * AuthServlet — handles /api/auth/* requests (login, register, logout, session).
 * Demonstrates: Servlet lifecycle, Exception Handling, Session management
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private FileManager fileManager;

    @Override
    public void init() throws ServletException {
        String dataPath = getServletContext().getRealPath("/data");
        fileManager = FileManager.getInstance(dataPath);
        // Create default admin if no users exist
        try {
            List<String> users = fileManager.readLines("users.txt");
            if (users.isEmpty()) {
                seedDefaultUsers();
            }
        } catch (Exception e) {
            throw new ServletException("AuthServlet init failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/login".equals(path)) {
                handleLogin(req, res);
            } else if ("/register".equals(path)) {
                handleRegister(req, res);
            } else if ("/logout".equals(path)) {
                handleLogout(req, res);
            } else {
                sendError(res, 404, "Unknown auth endpoint: " + path);
            }
        } catch (LoginException e) {
            sendError(res, 401, e.getMessage());
        } catch (FileStorageException e) {
            sendError(res, 500, "Storage error: " + e.getMessage());
        } catch (Exception e) {
            sendError(res, 500, "Internal error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        if ("/session".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                Map<String, Object> userData = (Map<String, Object>) session.getAttribute("user");
                sendJson(res, 200, userData);
            } else {
                sendError(res, 401, "Not logged in");
            }
        } else {
            sendError(res, 404, "Unknown path");
        }
    }

    // --- Login Handler ---
    private void handleLogin(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        String username = req.getParameter("username");
        String password  = req.getParameter("password");

        // Exception handling for invalid input
        if (username == null || username.trim().isEmpty())
            throw new LoginException("Username cannot be empty.");
        if (password == null || password.trim().isEmpty())
            throw new LoginException("Password cannot be empty.");

        Map<String, String> userRecord = findUser(username.trim());
        if (userRecord == null)
            throw new LoginException(username, "User not found");

        String hashedInput = hashPassword(password);
        if (!hashedInput.equals(userRecord.get("password")))
            throw new LoginException(username, "Incorrect password");

        // Build session data
        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("userId",    userRecord.get("userId"));
        userData.put("username",  userRecord.get("username"));
        userData.put("fullName",  userRecord.get("fullName"));
        userData.put("email",     userRecord.get("email"));
        userData.put("role",      userRecord.get("role"));
        userData.put("studentId", userRecord.getOrDefault("studentId", ""));
        userData.put("department",userRecord.getOrDefault("department", ""));
        userData.put("semester",  userRecord.getOrDefault("semester", ""));

        // Create HTTP session
        HttpSession session = req.getSession(true);
        session.setAttribute("user", userData);
        session.setMaxInactiveInterval(3600); // 1 hour

        NotificationThread.pushNotification("LOGIN",
            "Welcome back!", userRecord.get("fullName") + " logged in.");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("user", userData);
        sendJson(res, 200, response);
    }

    // --- Register Handler ---
    private void handleRegister(HttpServletRequest req, HttpServletResponse res)
            throws Exception {
        String username   = req.getParameter("username");
        String password   = req.getParameter("password");
        String fullName   = req.getParameter("fullName");
        String email      = req.getParameter("email");
        String studentId  = req.getParameter("studentId");
        String department = req.getParameter("department");
        String semester   = req.getParameter("semester");
        String program    = req.getParameter("program");

        // Validation — exception handling
        if (username == null || username.length() < 3)
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email address.");

        if (findUser(username.trim()) != null)
            throw new IllegalArgumentException("Username already exists.");

        String userId = "USR-" + System.currentTimeMillis();
        Student student = new Student(
            userId, username.trim(), hashPassword(password),
            email.trim(), fullName != null ? fullName.trim() : username,
            studentId != null ? studentId : userId,
            department != null ? department : "General",
            semester   != null ? semester   : "1",
            program    != null ? program    : "BSc"
        );

        fileManager.appendLine("users.txt", student.toFileString());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Registration successful! You can now log in.");
        sendJson(res, 200, response);
    }

    // --- Logout Handler ---
    private void handleLogout(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        Map<String, Object> response = Map.of("success", true, "message", "Logged out.");
        sendJson(res, 200, response);
    }

    // --- Utilities ---
    private Map<String, String> findUser(String username) throws FileStorageException {
        List<String> lines = fileManager.readLines("users.txt");
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6 && parts[1].equalsIgnoreCase(username)) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("userId",     parts[0]);
                m.put("username",   parts[1]);
                m.put("password",   parts[2]);
                m.put("email",      parts[3]);
                m.put("fullName",   parts[4]);
                m.put("role",       parts[5]);
                if (parts.length > 7) {
                    m.put("studentId",  parts[7]);
                    m.put("department", parts.length > 8 ? parts[8] : "");
                    m.put("semester",   parts.length > 9 ? parts[9] : "");
                }
                return m;
            }
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // fallback (not recommended in production)
        }
    }

    private void seedDefaultUsers() throws FileStorageException {
        // Default admin
        String adminLine = "USR-ADMIN-001|admin|"
            + hashPassword("admin123") + "|admin@campus.edu"
            + "|Campus Administrator|ADMIN||ADMIN-001|IT Department|FULL";
        // Default student
        String studentLine = "USR-STU-001|student|"
            + hashPassword("student123") + "|student@campus.edu"
            + "|Demo Student|STUDENT||STU-2024-001|Computer Science|3|BSc";

        fileManager.appendLine("users.txt", adminLine);
        fileManager.appendLine("users.txt", studentLine);
    }

    private void sendJson(HttpServletResponse res, int status, Object data)
            throws IOException {
        res.setStatus(status);
        PrintWriter out = res.getWriter();
        out.print(toJson(data));
        out.flush();
    }

    private void sendError(HttpServletResponse res, int status, String msg)
            throws IOException {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("success", false);
        err.put("error",   msg);
        sendJson(res, status, err);
    }

    // Minimal JSON serializer (avoids external dependencies)
    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Boolean || obj instanceof Number) return obj.toString();
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            map.forEach((k, v) -> {
                if (sb.length() > 1) sb.append(",");
                sb.append("\"").append(escapeJson(k)).append("\":").append(toJson(v));
            });
            return sb.append("}").toString();
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            list.forEach(item -> {
                if (sb.length() > 1) sb.append(",");
                sb.append(toJson(item));
            });
            return sb.append("]").toString();
        }
        return "\"" + escapeJson(obj.toString()) + "\"";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
