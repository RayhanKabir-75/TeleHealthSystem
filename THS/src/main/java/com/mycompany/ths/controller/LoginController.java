package com.mycompany.ths.controller;

import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
public void handleLogin() {
    String u = usernameField.getText().trim();
    String p = passwordField.getText();

    if (u.isEmpty() || p.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "Please enter username & password").showAndWait();
        return;
    }

    try {
        String res = com.mycompany.ths.util.Remote.send("LOGIN|" + u + "|" + p);
        // Expecting OK|role  (e.g., OK|patient or OK|doctor)
        if (res.startsWith("OK|")) {
            String role = res.substring(3);
            com.mycompany.ths.util.Session.login(u, role);
            com.mycompany.ths.util.Navigator.to("dashboard.fxml");
        } else {
            new Alert(Alert.AlertType.ERROR,
                res.startsWith("ERR|") ? res.substring(4) : "Invalid username or password").showAndWait();
        }
    } catch (Exception ex) {
        new Alert(Alert.AlertType.ERROR, "Network error: " + ex.getMessage()).showAndWait();
    }
}

    @FXML
    public void openSignup() {
        Navigator.to("signup.fxml");
    }
}
