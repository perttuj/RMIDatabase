/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.integration;

import common.ClientDTO;
import common.Credentials;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import server.model.FileWrapper;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class FileCatalogDTO {
    private static final String[] TABLE_NAMES = {"FILEDATA", "USERS", "FILES"};
    private Connection conn;
    
    PreparedStatement createFile;
    PreparedStatement selectUsername;
    PreparedStatement selectFilename;
    PreparedStatement selectOwnerOrPublic;
    PreparedStatement insertIntoUsers;
    PreparedStatement deleteUser;
    PreparedStatement deleteFile;
    PreparedStatement updateInfo;
    PreparedStatement updateData;
    
    public FileCatalogDTO() {
       accessDb();
    }
    private void accessDb() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/TestDatabase", "perttu", "perttu");
            createTables(conn);
            prepareStatements(conn);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    private void prepareStatements(Connection con) throws SQLException {
        createFile = con.prepareStatement("INSERT INTO FILES VALUES (?, ?, ?, ?, ?)");
        selectUsername = con.prepareStatement("SELECT * FROM USERS WHERE username = ?");
        selectFilename = con.prepareStatement("SELECT * FROM FILES WHERE filename = ?");
        insertIntoUsers = con.prepareStatement("INSERT INTO USERS VALUES (?, ?)");
        deleteUser = con.prepareStatement("DELETE FROM USERS WHERE username = ?");
        deleteFile = con.prepareStatement("DELETE FROM FILES WHERE filename = ?");
        selectOwnerOrPublic = con.prepareStatement("SELECT * FROM FILES WHERE owner = ? OR access = ?");
        updateInfo = con.prepareStatement("UPDATE FILES SET access = ?, permission = ? WHERE filename = ?");
        updateData = con.prepareStatement("UPDATE FILES SET size = ? WHERE filename = ?");
    }
    public boolean verify(Credentials cred) {
        try {
            selectUsername.setString(1, cred.getUser());
            ResultSet set = selectUsername.executeQuery();
            set.next();
            String pass = set.getString(2);
            return pass.equals(cred.getPass());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean register(Credentials cred) {
        try {
            insertIntoUsers.setString(1, cred.getUser());
            insertIntoUsers.setString(2, cred.getPass());
            insertIntoUsers.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean unregister(Credentials cred) {
        try {
            deleteUser.setString(1, cred.getUser());
            deleteUser.executeQuery();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean exists(String filename) {
        try {
            selectFilename.setString(1, filename);
            ResultSet set = selectFilename.executeQuery();
            if (!set.next()) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isOwner(ClientDTO dto, String filename) {
        try {
            selectFilename.setString(1, filename);
            ResultSet set = selectFilename.executeQuery();
            if (!set.next()) {
                return false;
            }
            return set.getString("owner").equals(dto.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<FileWrapper> allFiles(ClientDTO ID) {
        try {
            selectOwnerOrPublic.setString(1, ID.getUsername());
            selectOwnerOrPublic.setString(2, "1");
            ResultSet set = selectOwnerOrPublic.executeQuery();
            List<FileWrapper> temp = new ArrayList<>();
            while (set.next()) {
                String pub = set.getString(3);
                int publ = Integer.parseInt(pub);
                if (!((publ > 0) || set.getString("owner").equals(ID.getUsername()))) {
                    continue;
                }
                boolean access = publ > 0;
                String permissions = set.getString("permission");
                int perm = Integer.parseInt(permissions);
                boolean p = perm > 0;
                String size = set.getString("size");
                FileWrapper fw = new FileWrapper(set.getString("owner"), set.getString("filename"), access, p, Integer.parseInt(size));
                temp.add(fw);
            }
            return temp;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Called to check if the user DTO is allowed to perform operation op on a file
     * @param dto   the user to check permission for
     * @param filename  the file the user wants to perform actions on
     * @param op        the operation the user wants to do - 0 for read, 1 for write
     * @return          true or false, depending on if the user is allowd to perform the operation
     */
    public boolean verifyPermissions(String clientname, String filename, int op) {
        try {
            selectFilename.setString(1, filename);
            ResultSet set = selectFilename.executeQuery();
            if (!set.next()) {  // if file set is empty, return false
                return false;
            }
            String owner = set.getString("owner");
            if (clientname.equals(owner)) {
                return true;
            }
            int access = set.getInt("access");
            if (access == 0) {
                return false;
            }
            int permission = set.getInt("permission");
            if (permission < op) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateFileInfo(String filename, boolean access, boolean permission) {
        try {
            int acc = access ? 1 : 0;
            int per = permission ? 1 : 0;
            updateInfo.setString(1, String.valueOf(acc));
            updateInfo.setString(2, String.valueOf(per));
            updateInfo.setString(3, filename);
            updateInfo.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public boolean updateFileData(String filename, long size) {
        try {
            updateData.setLong(1, size);
            updateData.setString(2, filename);
            updateData.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteFile(String filename) {
        try {
            deleteFile.setString(1, filename);
            deleteFile.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    /**
     * Called when the user wants to create a file on the server
     * if file exists, it will be overwritten
     * @param dto   the user who creates the file
     * @param filename  the name of the file
     * @param access    the access for the file
     * @return  true if creating the file was possible, false if not
     */
    public boolean createFile(String owner, String filename, long filesize, boolean access, boolean permission) {
        try {
            int allow = access ? 1 : 0;
            int perm = permission ? 1 : 0;
            createFile.setString(1, filename);
            createFile.setString(2, owner);
            createFile.setString(3, String.valueOf(allow));
            createFile.setLong(4, filesize);
            createFile.setString(5, String.valueOf(perm));
            createFile.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void createTables(Connection connection) throws SQLException {
        for (String s : TABLE_NAMES) {
            try {
                createTable(connection, s);
            } catch (SQLException e) {
            }
        }
    }
    private void createTable(Connection connection, String table) throws SQLException {
        Statement st = connection.createStatement();
        switch(table) {
            case "USERS":
                st.executeUpdate("create table USERS (username varchar(10) primary key, password varchar(50))");
                break;
            case "FILES":
                st.executeUpdate("create table FILES (filename varchar(10) primary key, owner varchar(10), access varchar(1), size int, permission varchar(1))");
                break;
        }
    }
}
