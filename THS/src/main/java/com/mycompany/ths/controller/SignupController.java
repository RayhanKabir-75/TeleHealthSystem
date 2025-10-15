package com.mycompany.ths.controller;

import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
// If you already have UserRepo/Remote SIGNUP, import and use that instead
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("patient", "doctor"));
        roleCombo.getSelectionModel().selectFirst();
    }

    // SignupController.java (replace register() body)
@FXML
public void register() {
    String u = usernameField.getText().trim();
    String p = passwordField.getText();
    String r = roleCombo.getValue();              // "patient" | "doctor" | "admin"

    if (u.isEmpty() || p.isEmpty() || r == null) {
        alert(Alert.AlertType.ERROR, "Please enter username, password and role.");
        return;
    }

    try {
        String cmd = "SIGNUP|" + u + "|" + p + "|" + r;    // <-- exact format
        String res = com.mycompany.ths.util.Remote.send(cmd);
        if (res.startsWith("OK")) {
            alert(Alert.AlertType.INFORMATION, "Account created.");
            Navigator.to("login.fxml");
        } else {
            alert(Alert.AlertType.ERROR, res.startsWith("ERR|") ? res.substring(4) : res);
        }
    } catch (Exception ex) {
        alert(Alert.AlertType.ERROR, "Network error: " + ex.getMessage());
    }
}


    @FXML
    public void backToLogin() {
        Navigator.to("login.fxml");
    }

    private void alert(Alert.AlertType t, String m) { new Alert(t, m).showAndWait(); }
}
