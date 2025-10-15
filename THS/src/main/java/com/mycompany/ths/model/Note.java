package com.mycompany.ths.model;

import java.time.LocalDateTime;

public class Note {
    private long id;
    private String patient;
    private String doctor;
    private String content;
    private LocalDateTime createdAt;

    public Note(long id, String patient, String doctor, String content, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getPatient() { return patient; }
    public String getDoctor() { return doctor; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
