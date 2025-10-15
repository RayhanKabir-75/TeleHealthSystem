package com.mycompany.ths.repo;

import com.mycompany.ths.util.Remote;

public class AuthService {
    public String login(String user, String pass) throws Exception {
        return Remote.send("LOGIN|" + user + "|" + pass);
    }
}
