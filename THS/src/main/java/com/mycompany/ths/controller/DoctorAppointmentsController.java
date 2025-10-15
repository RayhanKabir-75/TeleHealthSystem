package com.mycompany.ths.controller;

import java.io.IOException;
import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Remote;
import com.mycompany.ths.util.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DoctorAppointmentsController {

    @FXML
    private TableView<String[]> table;
    @FXML
    private TableColumn<String[], String> colId, colPatient, colDate, colLocation, colStatus;
    @FXML
    private TextField newTime, newLocation;
    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("PENDING", "CONFIRMED", "CANCELLED", "DONE"));

        colId.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[0]));
        colPatient.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[1]));
        colDate.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[3]));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[4]));
        colLocation.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[5]));

        refresh();
    }

    private void refresh() {
        try {
            String res = Remote.send("LIST_APPT|" + Session.getUsername() + "|doctor");
            if (!res.startsWith("OK")) {
                alert(Alert.AlertType.ERROR, "Load failed: " + res);
                return;
            }

            String[] rows = res.split("\\|");
            var data = FXCollections.<String[]>observableArrayList();
            for (int i = 1; i < rows.length; i++) {
                data.add(rows[i].split(",", -1)); // id,patient,doctor,datetime,status,location
            }
            table.setItems(data);

        } catch (IOException ex) {
            alert(Alert.AlertType.ERROR, "Network error: " + ex.getMessage());
        }
    }

    @FXML
    private void updateSelected() {
        var sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert(Alert.AlertType.INFORMATION, "Select a row");
            return;
        }

        String id = sel[0];
        String iso = newTime.getText().isBlank() ? sel[3] : newTime.getText().trim();
        String status = statusCombo.getValue() == null ? sel[4] : statusCombo.getValue();
        String loc = newLocation.getText().isBlank() ? sel[5] : newLocation.getText().trim();

        try {
            String res = Remote.send("UPDATE_APPT|" + id + "|" + iso + "|" + status + "|" + loc);
            if (res.startsWith("OK")) {
                refresh();
                clear();
            } else {
                alert(Alert.AlertType.ERROR, "Update failed: " + res);
            }
        } catch (IOException ex) {
            alert(Alert.AlertType.ERROR, "Network error: " + ex.getMessage());
        }
    }

    private void clear() {
        newTime.clear();
        newLocation.clear();
        statusCombo.getSelectionModel().clearSelection();
    }

    @FXML
    private void back() {
        Navigator.to("dashboard.fxml");
    }

    private void alert(Alert.AlertType t, String m) {
        new Alert(t, m).showAndWait();
    }
}
