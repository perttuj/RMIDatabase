/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import common.ClientDTO;
import common.Credentials;
import common.FileDTO;
import common.Receiver;
import common.Server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import server.model.CatalogHandler;
import server.net.UserConnection;

/**
 *  controller of the server, assigning tasks to the model
 * @author Perttu Jääskeläinen
 */
public class Controller extends UnicastRemoteObject implements Server {
    private final CatalogHandler handler;
    private UserConnection connection;
    public Controller() throws RemoteException, IOException {
        connection = new UserConnection(new ConsoleOutput());
        handler = new CatalogHandler();
    }
    /**
     * called by the user to be notified when a file is updated
     * verifies that the client is the file owner before adding to 
     * notification list
     * @param ID    Client that wants to add a notification
     * @param filename  the file the client wants to monitor
     * @param rec       the client receiver, for receiving replies
     * @throws RemoteException 
     */
    @Override
    public synchronized void notifyUpdate(long ID, String filename, Receiver rec) throws RemoteException {
        handler.addNotifier(ID, filename, rec);
    }
    /**
     * Returns a list of all files on the server, including all public files
     * and private files owned by the user
     * @param id    the user who wants to list all files
     * @return      a list of all files
     * @throws RemoteException  if sending the list over RMI fails
     */
    @Override
    public synchronized List<? extends FileDTO> listFiles(long id) throws RemoteException {
        List<? extends FileDTO> files = handler.allFiles(id);
        return files;
    }
    /**
     * Called by the user to update file info, but not data
     * @param ID    The user that wants to update the info
     * @param filename  the file the user wants to update
     * @param access    the new access status of the file
     * @param permission    the new read/write permissions of the file
     * @param rec   the client receiver
     * @return  true or false, depending on if the update succeeded
     * @throws RemoteException  if writing to the user fails
     */
    @Override
    public synchronized boolean updateFileInfo(long ID, String filename, boolean access, boolean permission, Receiver rec) throws RemoteException {
        return handler.updateFileInfo(ID, filename, access, permission, rec);
    }
    /**
     * Called by the user to update file data, but not info
     * @param ID    The user that wants to update the data
     * @param filename  The file the user wants to update
     * @param filesize  The new size of the file
     * @param rec   The user receiver, for receiving responses if something goes wrong
     * @return  A InetSocketAddress the user connects to, or null if failed
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized InetSocketAddress updateFileData(long ID, String filename, long filesize, Receiver rec) throws RemoteException {
        InetSocketAddress addr = handler.updateFileData(ID, filename, filesize, rec);
        if (addr == null) {
            return addr;
        }
        connection.addOperation(ID, filename, true);
        return addr;
    }
    /**
     * Called by the user to delete a file from the catalog
     * @param ID    The user ID who wants to delete a file
     * @param filename  The name of the file to be deleted
     * @param rec   The reciever of the user, where response are written
     *              if something goes wrong while deleting (various responses)
     * @return  true or false, depending on if the delete operation failed
     * @throws RemoteException  if writing to the user over RMI fails
     */
    @Override
    public synchronized boolean deleteFile(long ID, String filename, Receiver rec) throws RemoteException {
        boolean deleted = handler.deleteFile(ID, filename, rec);
        return deleted;
    }
    /**
     * Called by the user to upload a file to the catalog server
     * @param ID    The users ID who wants to upload the file
     * @param filename  The filename
     * @param filesize  The filesize
     * @param access    The files access status (public or not)
     * @param permissions   The files permissions (write or read & write)
     * @param rec   The users receiver, for receiving input if something
     *              goes wrong while trying to upload the file
     * @return  An InetSocketAddress, which the user can connect to
     *          null if something goes wrong
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized InetSocketAddress uploadFile(long ID, String filename, long filesize, boolean access, boolean permissions, Receiver rec) throws RemoteException {
        boolean uploadPossible = handler.uploadFile(ID, filename, filesize, access, permissions, rec);
        if (!uploadPossible) {
            return null;
        }
        boolean added = connection.addOperation(ID, filename, true); // reciving from client, false = receiving, true = sending
        System.out.println(added);
        return UserConnection.getAddress();
    }
    /**
     * Called by the user to download a file
     * @param ID    The user who wants to download a file
     * @param filename  The file to be downloaded
     * @param rec   For receiving responses if something goes wrong
     * @return  An InetSocketAddress, which the user can connect to
     *          null if something goes wrong
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized InetSocketAddress downloadFile(long ID, String filename, Receiver rec) throws RemoteException {
        boolean downloadPossible = handler.downloadFile(ID, filename, rec);
        if (!downloadPossible) {
            return null;
        }
        connection.addOperation(ID, filename, false); // true = sending
        return UserConnection.getAddress();
    }
    /**
     * Called by the user to logout
     * @param ID    The user ID to logout
     * @return  True or false, depending on if the logout succeeds
     *          Can fail if the user is not logged in
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized boolean logout(long ID) throws RemoteException {
        if (!handler.loggedIn(ID)) {
            return false;
        }
        return handler.logout(ID);
    }
    /**
     * Called by the user to login on the catalog
     * @param res   The users receiver for receiving input
     * @param creds The users credentials, used for logging in
     * @return  A ClientDTO object if the login succeeds,
     *          if it fails - returns null
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized long login (Receiver res, Credentials creds) throws RemoteException {
        long id = handler.login(creds, res);
        return id;
    }
    /**
     * Called by the user to register on the server
     * @param cred  The users credentials to register in the database
     * @return  a String response, depending on the result of the registration
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized String register(Credentials cred) throws RemoteException {
        return handler.register(cred);
    }
    /**
     * Called by the user to unregister on the server
     * @param cred  The credentials to unregister
     * @return  a String response, depending on the result of the unregistration
     * @throws RemoteException  If writing to the user over RMI fails
     */
    @Override
    public synchronized String unregister(Credentials cred) throws RemoteException {
        return handler.unregister(cred);
    }
    /**
     * Called by the user when quitting the program, to end
     * all server side notifications and monitoring
     * @param id    The ID to logout
     */
    @Override
    public void leaving(long id) throws RemoteException {
        handler.logout(id);
    }
    private class ConsoleOutput implements ResponseHandler {
        @Override
        public void receive(String msg) {
            System.out.println(msg);
        }
    }
}
