package com.mycompany.ths.repo;

import com.mycompany.ths.model.Note;
import com.mycompany.ths.util.Remote;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Talks to ths-server using protocol: ADD_NOTE | LIST_NOTE */
public class NotesService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** ADD_NOTE|patient|doctor|content  -> returns "OK" or "ERR|..." */
    public String add(String patient, String doctor, String content) throws Exception {
        // guard pipes in free text to avoid breaking the wire format
        String safe = content.replace("|", "/");
        return Remote.send("ADD_NOTE|" + patient.trim() + "|" + doctor.trim() + "|" + safe);
    }

    /** LIST_NOTE|username -> "OK|id,patient,doctor,content,created_at|..." */
    public List<Note> listForUser(String user) throws Exception {
        String res = Remote.send("LIST_NOTE|" + user.trim());
        if (!res.startsWith("OK")) throw new IllegalStateException(res);

        List<Note> out = new ArrayList<>();
        String[] rows = res.split("\\|", -1);
        for (int i = 1; i < rows.length; i++) {
            // Each row is: id,patient,doctor,content,created_at
            String[] c = rows[i].split(",", -1);
            if (c.length < 5) continue;
            long id = Long.parseLong(c[0]);
            String patient = c[1];
            String doctor  = c[2];
            String content = c[3];
            LocalDateTime at = LocalDateTime.parse(c[4], ISO);
            out.add(new Note(id, patient, doctor, content, at));
        }
        return out;
    }
}
