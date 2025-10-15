package com.mycompany.ths.util;

import java.io.*;
import java.net.*;

public final class Remote {

    private Remote() {} // Prevent instantiation

    public static String send(String line) throws IOException {
        try (Socket s = new Socket("localhost", 5555);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            out.println(line);
            return in.readLine(); // single-line reply
        }
    }
}
