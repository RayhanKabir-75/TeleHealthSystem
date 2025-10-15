package com.mycompany.ths.controller;

import com.mycompany.ths.util.Navigator;
import javafx.fxml.FXML;

public class DashboardController {

    // Opens the Appointments page
    @FXML
    private void openAppointments() {
        Navigator.to("appointments.fxml");
    }

    // Opens the Notes page
    @FXML
    private void openNotes() {
        Navigator.to("notes.fxml");
    }

    // Opens the Profile page
    @FXML
    private void openProfile() {
        Navigator.to("profile.fxml");
    }

    // Opens the Vitals page (for recording health metrics)
    @FXML
    private void openVitals() {
        Navigator.to("vitals.fxml");
    }

    // Opens the Prescriptions page (for viewing/refilling medications)
    @FXML
    private void openPrescriptions() {
        Navigator.to("prescriptions.fxml");
    }

    // Opens the Referrals page (for doctor-to-hospital/clinic referrals)
    @FXML
    private void openReferrals() {
        Navigator.to("referrals.fxml");
    }

    // Logs out the user and redirects to the login screen
    @FXML
    private void logout() {
        Navigator.to("login.fxml");
    }
}
