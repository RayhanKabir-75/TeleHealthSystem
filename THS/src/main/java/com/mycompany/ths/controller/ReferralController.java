package com.mycompany.ths.controller;

import com.mycompany.ths.util.Remote;
import com.mycompany.ths.util.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class ReferralController {

    @FXML
    private TextField patientField;
    @FXML
    private TextField hospitalField;
    @FXML
    private TextField procedureField;
    @FXML
    private TextField whenField;              // e.g. 2025-10-25T09:30

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private TableView<String[]> table;
    @FXML
    private TableColumn<String[], String> colId;
    @FXML
    private TableColumn<String[], String> colPatient;
    @FXML
    private TableColumn<String[], String> colDoctor;
    @FXML
    private TableColumn<String[], String> colHospital;
    @FXML
    private TableColumn<String[], String> colProcedure;
    @FXML
    private TableColumn<String[], String> colWhen;
    @FXML
    private TableColumn<String[], String> colStatus;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "PENDING", "APPROVED", "REJECTED", "BOOKED"
        ));
        mapCols();
        refresh();
    }

    private void mapCols() {
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 0)));
        colPatient.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 1)));
        colDoctor.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 2)));
        colHospital.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 3)));
        colProcedure.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 4)));
        colWhen.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 5)));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(s(c.getValue(), 6)));
    }

    // Doctor creates referral for a patient
    @FXML
    private void createReferral() {
        if (!"doctor".equalsIgnoreCase(Session.getRole())) {
            msg(Alert.AlertType.INFORMATION, "Doctor only.");
            return;
        }
        if (patientField.getText().isBlank() || hospitalField.getText().isBlank()
                || procedureField.getText().isBlank() || whenField.getText().isBlank()) {
            msg(Alert.AlertType.INFORMATION, "Fill all referral fields.");
            return;
        }
        String wire = "ADD_REF|" + enc(patientField.getText().trim()) + "|"
                + enc(Session.getUsername()) + "|"
                + enc(hospitalField.getText().trim()) + "|"
                + enc(procedureField.getText().trim()) + "|"
                + enc(whenField.getText().trim());
        try {
            String res = Remote.send(wire);
            if (res.startsWith("OK")) {
                clear();
                refresh();
            } else {
                msg(Alert.AlertType.ERROR, "Create failed: " + res);
            }
        } catch (IOException e) {
            msg(Alert.AlertType.ERROR, "Network error: " + e.getMessage());
        }
    }

    // Doctor updates status (e.g., APPROVED/BOOKED/REJECTED)

    @FXML
    private void updateSelected() {
        if (!"doctor".equalsIgnoreCase(Session.getRole())) {
            msg(Alert.AlertType.INFORMATION, "Doctor only.");
            return;
        }
        var sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            msg(Alert.AlertType.INFORMATION, "Select a row.");
            return;
        }

        String id = sel[0];
        String when = (whenField.getText() == null || whenField.getText().isBlank())
                ? sel[5] // use existing When from table
                : whenField.getText().trim();          // or the edited value
        String status = (statusCombo.getValue() == null) ? sel[6] : statusCombo.getValue();

        try {
            String res = Remote.send("UPDATE_REFERRAL|" + id + "|" + when + "|" + status);
            if (res.startsWith("OK")) {
                refresh();
            } else {
                msg(Alert.AlertType.ERROR, "Update failed: " + res);
            }
        } catch (IOException ex) {
            msg(Alert.AlertType.ERROR, "Network error: " + ex.getMessage());
        }
    }

    @FXML
    private void back() {
        com.mycompany.ths.util.Navigator.to("dashboard.fxml");
    }

    @FXML
    private void clear() {
        patientField.clear();
        hospitalField.clear();
        procedureField.clear();
        whenField.clear();
        statusCombo.getSelectionModel().clearSelection();
    }

    private void refresh() {
        try {
            // Doctors see referrals they created; patients see their own.
            String scope = "doctor".equalsIgnoreCase(Session.getRole()) ? "doctor" : "patient";
            String res = Remote.send("LIST_REF|" + Session.getUsername() + "|" + scope);
            if (!res.startsWith("OK")) {
                msg(Alert.AlertType.ERROR, "Load failed: " + res);
                return;
            }
            String[] rows = res.split("\\|");
            var data = FXCollections.<String[]>observableArrayList();
            for (int i = 1; i < rows.length; i++) {
                data.add(rows[i].split(",", -1));
            }
            table.setItems(data);
        } catch (IOException e) {
            msg(Alert.AlertType.ERROR, "Network error: " + e.getMessage());
        }
    }

    // --- helpers ---
    private static String enc(String s) {
        return s == null ? "" : s.replace("|", " ").replace(",", " ");
    }

    private static String s(String[] a, int idx) {
        return (a != null && idx < a.length) ? a[idx] : "";
    }

    private void msg(Alert.AlertType t, String m) {
        new Alert(t, m).showAndWait();
    }
}
