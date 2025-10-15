package com.mycompany.ths.repo;

import com.mycompany.ths.model.Appointment;
import com.mycompany.ths.util.Remote;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the socket server using the protocol:
 * BOOK_APPT|patient|doctor|yyyy-MM-ddTHH:mm|location LIST_APPT|username
 * UPDATE_APPT|id|yyyy-MM-ddTHH:mm|status
 *
 * LIST_APPT response:
 * OK|id,patient,doctor,datetime,status,location|id,patient,doctor,datetime,status,location|...
 */
public class AppointmentService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String book(String patient, String doctor, LocalDateTime dt, String location) throws Exception {
        String payload = String.join("|",
                "BOOK_APPT",
                patient,
                doctor,
                dt.format(ISO),
                (location == null || location.isBlank()) ? "Online" : location.trim()
        );
        return Remote.send(payload);
    }

    public List<Appointment> listFor(String user, String role) throws IOException {
        String res = Remote.send("LIST_APPT|" + user + "|" + role);
        if (!res.startsWith("OK")) {
            throw new IOException(res);
        }

        String[] rows = res.split("\\|");
        List<Appointment> out = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            String[] c = rows[i].split(",", -1); // id,patient,doctor,when_at,location,status
            out.add(new Appointment(
                    (int) Long.parseLong(c[0]),
                    c[1],
                    c[2],
                    java.time.LocalDateTime.parse(c[3]),
                    c[4],
                    Appointment.Status.valueOf(c[5])
            ));
        }
        return out;
    }

    public String update(int id, LocalDateTime newDateTime, String newStatus) throws Exception {
        String payload = String.join("|",
                "UPDATE_APPT",
                String.valueOf(id),
                newDateTime.format(ISO),
                newStatus == null ? "" : newStatus
        );
        return Remote.send(payload);
    }

    public List<String> listDoctors() throws IOException {
        String res = Remote.send("LIST_DOCTORS");
        if (!res.startsWith("OK")) {
            throw new IOException(res);
        }
        String[] parts = res.split("\\|", -1);
        List<String> out = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isBlank()) {
                out.add(parts[i]);
            }
        }
        return out;
    }
}
