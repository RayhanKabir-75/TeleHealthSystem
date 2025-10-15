package com.mycompany.ths.server.notes;

import java.time.LocalDateTime;

public class NoteRow {
    private final long id;
    private final String patient;
    private final String doctor;
    private final String content;
    private final LocalDateTime createdAt;

    public NoteRow(long id, String patient, String doctor, String content, LocalDateTime createdAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.content = content;
        this.createdAt = createdAt;
    }

    public long getId()                { return id; }
    public String getPatient()         { return patient; }
    public String getDoctor()          { return doctor; }
    public String getContent()         { return content; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
