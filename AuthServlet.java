package com.campus.servlets;

import com.campus.exceptions.LoginException;
import com.campus.users.User;
import com.campus.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        // REMOVED: All FileManager and dataPath logic
        this.userService = new UserService();
        System.out.println("AuthServlet initialized: Database auth ready.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        if ("/login".equals(path)) {
            handleLogin(req, res);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String userParam = req.getParameter("username");
        String passParam = req.getParameter("password");

        try {
            // This calls Supabase instead of reading users.txt
            User user = userService.validateLogin(userParam, passParam);

            if (user != null) {
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                res.setStatus(200);
                res.getWriter().write("{\"success\": true, \"message\": \"Logged in as " + user.getRole() + "\"}");
            } else {
                res.setStatus(401);
                res.getWriter().write("{\"success\": false, \"message\": \"Invalid credentials\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"success\": false, \"message\": \"Login error\"}");
        }
    }
}