package de.hype.hypenotify.server;

import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RestController
public class CheckTimerController {

    @GetMapping("/hypenotify/checkTimer")
    public String checkTimer(@RequestParam(value = "id") int timerId) {
        boolean valid = (timerId % 2 == 0);
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("valid", valid);
        if (!valid) {
            long replacementTime = System.currentTimeMillis() + 2 * 60 * 1000;
            jsonResponse.addProperty("replacementTimer", replacementTime);
        }
        return jsonResponse.toString();
    }

    @PostMapping("/hypenotify/addDevice")
    public ResponseEntity<String> addDevice(@RequestParam String deviceName, @RequestParam String firebaseKey, HttpServletRequest request) {
        HypeNotifyUser user = (HypeNotifyUser) request.getAttribute("user");
        if (user == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        try (Connection connection = DatabaseConnection.getBingoNetConnection()) {
            String query = "INSERT INTO firebase_devices (user_id, device_name, firebase_key) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE firebase_key = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, user.getUserId());
                statement.setString(2, deviceName);
                statement.setString(3, firebaseKey);
                statement.setString(4, firebaseKey);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error adding device", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        user.addDevice(deviceName, firebaseKey);
        return new ResponseEntity<>("Device added successfully", HttpStatus.OK);
    }
}