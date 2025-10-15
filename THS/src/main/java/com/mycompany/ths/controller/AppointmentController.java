package com.mycompany.ths.controller;

import com.mycompany.ths.model.Appointment;
import com.mycompany.ths.repo.AppointmentService;
import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AppointmentController {

    @FXML private ComboBox<String> doctorCombo;      // from DB (e.g., "Dr. Lee")
    @FXML private DatePicker datePicker;             // appointment date
    @FXML private ComboBox<LocalTime> timeCombo;     // 09:00, 09:30, ...
    @FXML private TextField locationField;           // default "Online"

    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colLocation;
    @FXML private TableColumn<Appointment, String> colStatus;

    private final AppointmentService service = new AppointmentService();
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @FXML
    public void initialize() {
        // UX prompts
        doctorCombo.setPromptText("Select a doctor…");
        locationField.setPromptText("Online / Clinic A");

        // Load doctors from server (async)
        var loadDocs = new Task<java.util.List<String>>() {
            @Override protected java.util.List<String> call() throws Exception {
                return service.listDoctors();
            }
        };
        loadDocs.setOnSucceeded(e ->
            doctorCombo.setItems(FXCollections.observableArrayList(loadDocs.getValue()))
        );
        loadDocs.setOnFailed(e -> {
            doctorCombo.setItems(FXCollections.observableArrayList());
            info("Couldn’t load doctors: " + loadDocs.getException().getMessage());
        });
        new Thread(loadDocs, "load-doctors").start();

        // Generate 30-min time slots 09:00..17:30
        var times = FXCollections.<LocalTime>observableArrayList();
        for (int h = 9; h <= 17; h++) {
            times.add(LocalTime.of(h, 0));
            times.add(LocalTime.of(h, 30));
        }
        timeCombo.setItems(times);
        timeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LocalTime t) { return t == null ? "" : t.toString(); }
            @Override public LocalTime fromString(String s) { return LocalTime.parse(s); }
        });

        // Prevent picking past dates
        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // If user selects "today", hide past time slots
        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                timeCombo.setItems(times);
            } else if (newV.isEqual(LocalDate.now())) {
                var now = LocalTime.now();
                timeCombo.setItems(times.filtered(t -> t.isAfter(now)));
            } else {
                timeCombo.setItems(times);
            }
            timeCombo.getSelectionModel().clearSelection();
        });

        // Table mappings
        colDoctor.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getDoctor())));
        colLocation.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getLocation())));
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(String.valueOf(c.getValue().getStatus()))));
        colDate.setCellValueFactory(c -> {
            LocalDateTime dt = c.getValue().getDateTime();
            return new ReadOnlyStringWrapper(dt == null ? "" : dt.toString());
        });

        // Initial load
        refresh();
    }

    @FXML
    private void book() {
        String doctorDisplay = doctorCombo.getValue();
        LocalDate d = datePicker.getValue();
        LocalTime t = timeCombo.getValue();

        String loc = (locationField.getText() == null || locationField.getText().isBlank())
                ? "Online" : locationField.getText().trim();

        if (doctorDisplay == null || d == null || t == null) {
            info("Please choose doctor, date and time.");
            return;
        }

        LocalDateTime dt = LocalDateTime.of(d, t);
        if (dt.isBefore(LocalDateTime.now())) {
            info("Choose a future date/time.");
            return;
        }

        String patient = Session.getUsername();
        if (patient == null || patient.isBlank()) {
            error("No active session. Please login again.");
            return;
        }

        // If server expects a username, strip "Dr. " prefix: "Dr. Lee" -> "Lee"
        String doctorKey = normalizeDoctorKey(doctorDisplay);

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return service.book(patient, doctorKey, dt, loc);
            }
        };
        task.setOnSucceeded(e -> {
            String res = task.getValue();
            if (res.startsWith("OK")) {
                info("Appointment booked.");
                clearForm();
                refresh();
            } else {
                error("Failed to book: " + res);
            }
        });
        task.setOnFailed(e -> error("Network error: " + task.getException().getMessage()));
        new Thread(task, "book-appt").start();
    }

    @FXML
    private void backToDashboard() {
        Navigator.to("dashboard.fxml");
    }

    private void refresh() {
        try {
            var list = service.listFor(Session.getUsername(), Session.getRole());
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            error("Load failed: " + e.getMessage());
        }
    }

    private void clearForm() {
        doctorCombo.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeCombo.getSelectionModel().clearSelection();
        locationField.clear();
    }

    /** Converts "Dr. Lee" -> "Lee". Adjust if your server expects full "Dr. Lee". */
    private String normalizeDoctorKey(String s) {
        return s == null ? null : s.replaceFirst("(?i)^dr\\.\\s*", "").trim();
    }

    private String nz(String s) { return s == null ? "" : s; }
    private void info(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void error(String m){ new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
}
