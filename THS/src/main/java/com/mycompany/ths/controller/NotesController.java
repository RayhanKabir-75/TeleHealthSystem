package com.mycompany.ths.controller;

import com.mycompany.ths.model.Note;
import com.mycompany.ths.repo.NotesService;
import com.mycompany.ths.util.Navigator;
import com.mycompany.ths.util.Session;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.List;

public class NotesController {

    @FXML private TextField patientField;
    @FXML private TextField doctorField;
    @FXML private TextArea  noteArea;

    @FXML private TableView<Note> table;
    @FXML private TableColumn<Note,String> colDate;
    @FXML private TableColumn<Note,String> colPatient;
    @FXML private TableColumn<Note,String> colDoctor;
    @FXML private TableColumn<Note,String> colContent;

    private final NotesService service = new NotesService();

    @FXML
    public void initialize() {
        // Prefill patient with the logged-in user (if any)
        if (Session.getUsername() != null) patientField.setText(Session.getUsername());

        // Defensive table mappings (no PropertyValueFactory)
        colDate.setCellValueFactory(c -> {
            LocalDateTime t = c.getValue().getCreatedAt();
            return new ReadOnlyStringWrapper(t == null ? "" : t.toString());
        });
        colPatient.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getPatient())));
        colDoctor.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getDoctor())));
        colContent.setCellValueFactory(c -> new ReadOnlyStringWrapper(nz(c.getValue().getContent())));

        refresh();
    }

    @FXML
    private void saveNotes() {
        String patient = nz(patientField.getText()).trim();
        String doctor  = nz(doctorField.getText()).trim();
        String content = nz(noteArea.getText()).trim();

        if (patient.isEmpty() || doctor.isEmpty() || content.isEmpty()) {
            info("Please fill patient, doctor and note.");
            return;
        }

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return service.add(patient, doctor, content);
            }
        };
        task.setOnSucceeded(e -> {
            String res = task.getValue();
            if (res.startsWith("OK")) {
                info("Note saved.");
                clearForm();
                refresh();
            } else {
                error("Save failed: " + res);
            }
        });
        task.setOnFailed(e -> error("Network error: " + task.getException().getMessage()));
        new Thread(task, "add-note").start();
    }

    @FXML
    private void back() {
        Navigator.to("dashboard.fxml");
    }

    private void refresh() {
        String user = Session.getUsername();
        if (user == null || user.isBlank()) {
            table.setItems(FXCollections.emptyObservableList());
            return;
        }

        Task<List<Note>> task = new Task<>() {
            @Override protected List<Note> call() throws Exception {
                return service.listForUser(user);
            }
        };
        task.setOnSucceeded(e ->
                table.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> error("Load failed: " + task.getException().getMessage()));
        new Thread(task, "list-notes").start();
    }

    private void clearForm() {
        // keep patient prefilled for convenience
        doctorField.clear();
        noteArea.clear();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static void info(String m)  { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private static void error(String m) { new Alert(Alert.AlertType.ERROR, m).showAndWait(); }
}
