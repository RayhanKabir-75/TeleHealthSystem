package com.mycompany.ths.controller;

import com.mycompany.ths.model.Prescription;
import com.mycompany.ths.repo.PrescriptionService;
import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PrescriptionController {

    @FXML private TextField medicineField, qtyField, doctorField, newQtyField;
    @FXML private ComboBox<String> statusCombo;

    @FXML private TableView<Prescription> table;
    @FXML private TableColumn<Prescription,String> colId, colPatient, colDoctor, colMed, colQty, colStatus, colWhen;

    private final PrescriptionService service = new PrescriptionService();

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("PENDING", "APPROVED", "REJECTED", "FILLED"));
        mapCols();
        refresh();
    }

    private void mapCols() {
        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getId())));
        colPatient.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getPatient())));
        colDoctor.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getDoctor())));
        colMed.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getMedicine())));
        colQty.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getQty())));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getStatus())));
        colWhen.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().toString()));
    }

    @FXML
    private void addPrescription() {
        if (!"patient".equalsIgnoreCase(Session.getRole())) {
            msg(Alert.AlertType.INFORMATION, "Login as patient to request.");
            return;
        }
        if (medicineField.getText().isBlank() || qtyField.getText().isBlank() || doctorField.getText().isBlank()) {
            msg(Alert.AlertType.INFORMATION, "Enter medicine, qty, and doctor.");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            String res = service.add(Session.getUsername(),
                    doctorField.getText().trim(),
                    medicineField.getText().trim(),
                    qty);
            if (res.startsWith("OK")) {
                clear();
                refresh();
            } else {
                msg(Alert.AlertType.ERROR, "Add failed: " + res);
            }
        } catch (NumberFormatException nfe) {
            msg(Alert.AlertType.INFORMATION, "Qty must be a number.");
        } catch (Exception e) {
            msg(Alert.AlertType.ERROR, "Network error: " + e.getMessage());
        }
    }

    @FXML
    private void updateSelected() {
        if (!"doctor".equalsIgnoreCase(Session.getRole())) {
            msg(Alert.AlertType.INFORMATION, "Doctor only.");
            return;
        }
        var sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { msg(Alert.AlertType.INFORMATION, "Select a row."); return; }

        try {
            // change status if chosen
            if (statusCombo.getValue() != null) {
                String res = service.updateStatus(sel.getId(), statusCombo.getValue());
                if (!res.startsWith("OK")) { msg(Alert.AlertType.ERROR, "Update failed: " + res); return; }
            }
            // optionally adjust quantity
            if (!newQtyField.getText().isBlank()) {
                int newQty = Integer.parseInt(newQtyField.getText().trim());
                String res2 = service.doctorAdjustQty(sel.getId(), newQty);
                if (!res2.startsWith("OK")) { msg(Alert.AlertType.ERROR, "Adjust failed: " + res2); return; }
            }
            refresh();
        } catch (NumberFormatException nfe) {
            msg(Alert.AlertType.INFORMATION, "New Qty must be a number.");
        } catch (Exception e) {
            msg(Alert.AlertType.ERROR, "Network error: " + e.getMessage());
        }
    }

    private void refresh() {
        try {
            String role = Session.getRole();
            String user = Session.getUsername();
            List<Prescription> list = service.listFor(user, role);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            msg(Alert.AlertType.ERROR, "Load failed: " + e.getMessage());
        }
    }

    private void clear() {
        medicineField.clear();
        qtyField.clear();
        doctorField.clear();
        newQtyField.clear();
        statusCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void back() { Navigator.to("dashboard.fxml"); }

    private static String nz(String s) { return s == null ? "" : s; }
    private void msg(Alert.AlertType t, String m) { new Alert(t, m).showAndWait(); }
}
