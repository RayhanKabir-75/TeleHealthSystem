package com.mycompany.ths.server.db;

import java.sql.*;

public final class DB {
  static final String URL = "jdbc:mysql://localhost:3306/ths_enhanced";
  static final String USER = "ths"; // <- set yours
  static final String PASS = "ths123";
  public static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
}
