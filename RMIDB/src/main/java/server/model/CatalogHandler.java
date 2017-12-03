/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.ClientDTO;
import common.Credentials;
import common.FileDTO;
import common.Receiver;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import server.integration.FileCatalogDTO;
import server.net.UserConnection;

/**
 *  Class for handling all clients and logic in the program
 * @author Perttu Jääskeläinen
 */
public class CatalogHandler {
    Map<Long, Client> clients;
    Map<String, Client> notifiers;
    private final FileCatalogDTO db;
    Random random = new Random();
    public CatalogHandler() {
        clients = new HashMap<>();
        notifiers = new HashMap<>();
        db = new FileCatalogDTO();
    }
    public boolean downloadFile(long ID, String filename, Receiver rec) throws RemoteException {
        if (!db.exists(filename)) {
            rec.receive("file does not exist");
            return false;
        }
        System.out.println(ID);
        System.out.println(clients.get(ID));
        if (!db.verifyPermissions(clients.get(ID).getUsername(), filename, 0)) {
            rec.receive("not allowed to read file");
            return false;
        }
        notifyUser(clients.get(ID), filename, "downloading");
        return true;
    }
    public boolean uploadFile(long ID, String filename, long filesize, boolean access, boolean permission, Receiver rec) throws RemoteException {
        if (!loggedIn(ID)) {
            rec.receive("not logged in, log in to send file");
            return false;
        }
        // if file exists, check if user is allowed to overwrite it
        if (db.exists(filename)) { 
            boolean perms = db.verifyPermissions(clients.get(ID).getUsername(), filename, 1);
            if (!perms) {
                rec.receive("file: " + filename + " exists - not allowed to overwrite file");
                return false;
            }
            if (!db.isOwner(clients.get(ID), filename)) {
                notifyUser(clients.get(ID), filename, "overwriting");
            }
        }
        boolean res = db.createFile(clients.get(ID).getUsername(), filename, filesize, access, permission);
        if (!res) {
            rec.receive("error when creating file - duplicate filename found");
            return false;
        }
        return true;
    }
    public boolean deleteFile(long ID, String filename, Receiver rec) throws RemoteException {
        if (!db.exists(filename)) {
            rec.receive("cannot delete file, file does not exist");
            return false;
        }
        boolean ver = db.verifyPermissions(clients.get(ID).getUsername(), filename, 1);
        if (!ver) {
            rec.receive("dont have permission to delete file: " + filename);
            return false;
        }
        notifyUser(clients.get(ID), filename, "deleting file");
        db.deleteFile(filename);
        rec.receive("file deleted");
        return true;
    }
    public InetSocketAddress updateFileData(long ID, String filename, long filesize, Receiver rec) throws RemoteException {
        if (!db.exists(filename)) {
           rec.receive("file does not exist: " + filename);
           return null;
       }
       boolean verify = db.verifyPermissions(clients.get(ID).getUsername(), filename, 1);
       if (!verify) {
           rec.receive("not allowed to write data");
           return null;
       }
       notifyUser(clients.get(ID), filename, "updating data");
       db.updateFileData(filename, filesize);
       return UserConnection.getAddress();
    }
    public boolean updateFileInfo(long ID, String filename, boolean access, boolean permission, Receiver rec) throws RemoteException {
        if (!db.exists(filename)) {
           rec.receive("cannot update file - it does not exist");
           return false;
        }
        boolean owner = db.isOwner(clients.get(ID), filename);
        boolean perm = db.verifyPermissions(clients.get(ID).getUsername(), filename, 1);
        if (!owner && !perm) {
            rec.receive("not allowed to update file");
            return false;
        }
        if (!owner) {
            notifyUser(clients.get(ID), filename, "updating info"); 
        }
        db.updateFileInfo(filename, access, permission);
        return true;
    }
    public List<? extends FileDTO> allFiles(long ID) {
        if (!loggedIn(ID)) {
            return null;
        }
        return db.allFiles(clients.get(ID));
    }
    public void addNotifier(long ID, String filename, Receiver rec) throws RemoteException {
        boolean loggedin = loggedIn(ID);
        if (!loggedin) {
            rec.receive("login first to monitor files");
            return;
        }
        if (!db.isOwner(clients.get(ID), filename)) {
            rec.receive("can only monitor own files");
            return;
        }
        boolean monitoring = addNotifier(ID, filename);
        if (!monitoring) {
            rec.receive("already monitoring file");
        } else {
            rec.receive("now monitoring file: " + filename);
        }
    }
    /**
     * Called when a user ID tries to access file filename
     * If the file is being monitored, notify the owner
     * @param id    the ID accessing the file
     * @param filename  the file to be accessed
     */
    private void notifyUser(ClientDTO id, String filename, String operation) throws RemoteException {
        if (db.isOwner(id, filename)) {
            return;
        }
        if (monitoring(filename)) {
            Receiver res = getMonitoringReceiver(filename);
            res.receive("user: " + id.getUsername() + " is " + operation + ", file: " + filename);
        }
    }
    public boolean addNotifier(long id, String filename) {
        Client c = clients.get(id);
        if (notifiers.containsKey(filename)) {
            return false;
        } else {
            notifiers.put(filename, c);
            return true;
        }
    }
    public Receiver getMonitoringReceiver(String filename) {
        Client owner = notifiers.get(filename);
        return owner.getReceiver();
    }
    public void notifyUser(String filename, ClientDTO id) {
        if (clients.containsKey(id.getUsername())) {
            if (notifiers.containsKey(filename)) {
                ;
            }
        }
    }
    public boolean monitoring(String filename) {
        return notifiers.containsKey(filename);
    }
    private void add(long id, Client c) {
        clients.put(id, c);
    }
    private boolean remove(Client c) {
        Iterator ite = clients.entrySet().iterator();
        while (ite.hasNext()) {
            Entry e = (Entry) ite.next();
            Client c2= (Client) e.getValue();
            if (c.getUsername().equalsIgnoreCase(c2.getUsername())) {
                clients.remove(e.getKey());
                return true;
            }
        }
        return false;
    }
    public boolean loggedIn(long id) {
        return clients.containsKey(id);
    }
    private boolean loggedIn(String username) {
        Iterator ite = clients.entrySet().iterator();
        while (ite.hasNext()) {
            Entry e = (Entry)ite.next();
            Client c = (Client) e.getValue();
            String s = c.getUsername();
            if (username.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
    public String register(Credentials cred) {
        boolean registered = db.register(cred);
        String response;
        if (registered) {
            response = "registered";
        } else {
            response = "username taken";
        }
        return response;
    }
    public String unregister(Credentials cred) {
        if (!db.verify(cred)) {
            return "wrong credentials";
        }
        boolean unregistered = db.unregister(cred);
        if (unregistered) {
            return "unregistered";
        } else {
            return "unregistration failed";
        }
    }
    public long login(Credentials cred, Receiver res) throws RemoteException {
        if (loggedIn(cred.getUser())) {
            res.receive("user already logged in");
            return -1;
        }
        boolean verified = db.verify(cred);
        if (!verified) {
            res.receive("incorrect credentials");
            return -1;
        }
        long generatedLong = random.nextLong();
        Client c = new Client(cred, res);
        add(generatedLong, c);
        res.receive("logged in");
        return generatedLong;
    }
    public boolean logout(long id) throws RemoteException {
        notifiers.values().removeAll(Collections.singleton(id));
        clients.get(id).getReceiver().receive("logging out");
        boolean rem = remove(clients.get(id));
        return rem;
    }
}
