package com.mycompany.ths.controller;

import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileController {

    // Input fields for profile information
    @FXML
    private TextField nameField, addressField;
    @FXML
    private DatePicker dobPicker;          // Date of Birth picker
    @FXML
    private ComboBox<String> genderCombo;  // Gender selection dropdown

    // Runs automatically when the FXML loads
    @FXML
    public void initialize() {
        // Populate gender options
        genderCombo.getItems().setAll("Male", "Female", "Other");
        // Pre-fill name field with currently logged-in user
        nameField.setText(Session.user());
    }

    // Handle "Update Profile" button click
    @FXML
    private void updateProfile() {
        // Here you would normally save to DB, but currently just show confirmation
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Profile updated!");
        a.showAndWait();
    }

    // Navigate back to dashboard screen
    @FXML
    private void backToDashboard() {
        Navigator.to("dashboard.fxml");
    }
}
