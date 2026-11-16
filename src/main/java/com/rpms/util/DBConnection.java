package com.rpms.util;
import java.sql.*;
import java.io.*;
import java.util.Properties;
public final class DBConnection {
 private static final Properties CONFIG = load();
 private static Properties load(){ Properties p=new Properties(); try(InputStream in=new FileInputStream("config.properties")){p.load(in);}catch(IOException ignored){} return p; }
 private static final String URL=System.getenv().getOrDefault("RPMS_DB_URL",CONFIG.getProperty("db.url","jdbc:mysql://localhost:3306/recruitment_placement?useSSL=false&serverTimezone=UTC"));
 private static final String USER=System.getenv().getOrDefault("RPMS_DB_USER",CONFIG.getProperty("db.username","root"));
 private static final String PASS=System.getenv().getOrDefault("RPMS_DB_PASSWORD",CONFIG.getProperty("db.password","root"));
 private DBConnection(){}
 // Explicitly load MySQL Connector/J before requesting a connection.
 static {
  try {
   Class.forName("com.mysql.cj.jdbc.Driver");
  } catch (ClassNotFoundException e) {
   throw new ExceptionInInitializerError("MySQL Connector/J is missing. Run the project with Maven so dependencies are included: " + e.getMessage());
  }
 }
 public static Connection getConnection() throws SQLException { return DriverManager.getConnection(URL,USER,PASS); }
}
