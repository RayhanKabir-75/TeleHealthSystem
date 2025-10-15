package com.mycompany.ths.repo;

import com.mycompany.ths.model.User;
import java.util.*;

public class UserRepo {
    private static final Map<String, User> USERS = new HashMap<>();
    private static final UserRepo INSTANCE = new UserRepo();
    private UserRepo() {
        // seed demo accounts
        add(new User("patient", hash("1234"), "PATIENT"));
        add(new User("doctor",  hash("1234"), "DOCTOR"));
    }
    public static UserRepo get() { return INSTANCE; }

    public boolean exists(String username){ return USERS.containsKey(username.toLowerCase()); }
    public void add(User u){ USERS.put(u.getUsername().toLowerCase(), u); }
    public User find(String username){ return USERS.get(username.toLowerCase()); }

    public static String hash(String raw) { // demo hash; replace later with BCrypt
        return Integer.toHexString(Objects.hash("pepper", raw));
    }
}
