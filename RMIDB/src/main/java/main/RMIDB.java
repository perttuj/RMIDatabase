/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class RMIDB {
    private static String TABLE_NAME = "person";
    private PreparedStatement create;
    private PreparedStatement findAll;
    private PreparedStatement delete;
    private void accessDb() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            Connection connection = DriverManager.getConnection("jdbc:derby://localhost:1527/TestDatabase", "perttu", "Peja1501!");
            createTable(connection);
            Statement st = connection.createStatement();
            st.executeUpdate("insert into " + TABLE_NAME + " values ('emil', '0123456789', 12)");
            listAllPersons(connection);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(Connection connection) throws SQLException {
        if (!tableExists(connection)) {
            Statement st = connection.createStatement();
            st.executeUpdate("create table " + TABLE_NAME + " (name varchar(32) primary key, phone varchar(12), age int)");
        }  
    }

    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet results = metaData.getTables(null, null, null, null);
        while (results.next()) {
            if (results.getString(3).equalsIgnoreCase(TABLE_NAME)) {
                return true;
            }
        }
        return false;
    }
    
    private void listAllPersons(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select * from " + TABLE_NAME);
        while (rs.next()) {
            System.out.println("Name: " + rs.getString(1) + ", phone: " + rs.getString(2) + ", age: " + rs.getInt(3));
        }
    }
    
    private void prepareStatements(Connection connection) throws SQLException {
        create = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?)");
        findAll = connection.prepareStatement("SELECT * FROM " + TABLE_NAME);
        delete = connection.prepareStatement("DELET FROM " + TABLE_NAME + " WHERE name = ?");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RMIDB s = new RMIDB();
        s.accessDb();
    }
}
