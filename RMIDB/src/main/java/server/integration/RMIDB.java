/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.integration;

import common.ClientDTO;
import common.Credentials;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class RMIDB {
    private static final String[] TABLE_NAMES = {"FILEDATA", "USERS", "FILES"};
    private Connection conn;
    
    public RMIDB() {
       accessDb();
    }
    private void accessDb() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/TestDatabase", "perttu", "perttu");
            createTables(conn);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean verify(Credentials cred) {
        try {
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery("SELECT * FROM USERS WHERE username = '" + cred.getUser() + "'");
            set.next();
            String pass = set.getString(2);
            return pass.equals(cred.getPass());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String register(Credentials cred) {
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("INSERT INTO USERS VALUES ('" + cred.getUser() + "','" + cred.getPass() + "')");
            return "registered";
        } catch (SQLException e) {
            e.printStackTrace();
            return "registration failed, username taken";
        }
    }
    public String unregister(Credentials cred) {
        try {
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery("SELECT * FROM USERS WHERE username = '" + cred.getUser() + "'");
            set.next();
            if (!set.getString(2).equals(cred.getPass())) {
                return "wrong credentials";
            }
            st.executeUpdate("DELETE FROM USERS WHERE username = '" + cred.getUser() + "'");
            return "user unregistered";
        } catch (SQLException e) {
            e.printStackTrace();
            return "user does not exist";
        }
    }
    
    public boolean createFile(ClientDTO dto, String filename, boolean access) {
        Statement st;
        try {
            st = conn.createStatement();
            ResultSet set = st.executeQuery("SELECT * FROM FILES WHERE filename = '" + filename + "'");
            set.next();
            if (!set.getString(1).equals(filename)) {
                return false;
            }
        } catch (SQLException e) {
            try {
                st = conn.createStatement();
                int allow = access ? 1 : 0;
                st.executeUpdate("INSERT INTO FILES VALUES ('" + filename + "','f', '1')");
                return true;
            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return false;
    }
    public boolean receiveFile(ClientDTO dto, String filename) {
        try {
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery("SELECT owner, public FROM FILES WHERE (filename = '" + filename + "')");
            set.next();
            if (set.getString(1).equals(dto.getUsername())) {
                return true;
            }
            if (set.getInt(2) > 0) {
                ResultSet set2 = st.executeQuery("SELECT permissions FROM FILEDATA WHERE (filename ='" + filename + "')");
                set2.next();
                if (set2.getInt(1) > 0) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTables(Connection connection) throws SQLException {
        for (String s : TABLE_NAMES) {
            try {
                createTable(connection, s);
            } catch (SQLException e) {
                if (s.equals("FILES")) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void createTable(Connection connection, String table) throws SQLException {
        Statement st = connection.createStatement();
        switch(table) {
            case "FILEDATA":
                st.executeUpdate("create table FILEDATA (filename varchar(10) primary key, size int(4096), permission varchar(1))");
                break;
            case "USERS":
                st.executeUpdate("create table USERS (username varchar(10) primary key, password varchar(50))");
                break;
            case "FILES":
                st.executeUpdate("create table FILES (filename varchar(10) primary key, owner varchar(10), permission varchar(1))");
                break;
        }
    }
}
