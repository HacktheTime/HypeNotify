package de.hype.hypenotify.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getParameter("apiKey");
        String userIdParam = request.getParameter("userId");

        if (apiKey == null || userIdParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing required parameters: apiKey and userId");
            return false;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid userId format");
            return false;
        }

        HypeNotifyUser user = getUserFromApiKey(userId, apiKey);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid apiKey or userId");
            return false;
        }

        request.setAttribute("user", user);
        return true;
    }

    private HypeNotifyUser getUserFromApiKey(Integer userId, String apiKey) throws SQLException {
        // Implement your logic to extract user ID from the API key

        try (Connection connection = DatabaseConnection.getBingoNetConnection()) {
            String query = "SELECT user_id FROM users WHERE user_id = ? AND user_key = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setString(2, apiKey);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        return new HypeNotifyUser(resultSet.getInt("user_id"));
                    }
                }
            }
        }
        return null;
    }
}