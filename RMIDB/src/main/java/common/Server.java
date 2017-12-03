/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author Perttu Jääskeläinen
 */
public interface Server extends Remote {
    public static final String REGISTRY_NAME = "CHAT_SERVER";
    
    public long login(Receiver res, Credentials cred) throws RemoteException;
    public boolean logout(long ID) throws RemoteException;
    
    public void leaving(long ID) throws RemoteException;
    public void notifyUpdate(long ID, String filename, Receiver rec) throws RemoteException;
    public List<? extends FileDTO> listFiles (long ID) throws RemoteException;
    public boolean deleteFile(long ID, String filename, Receiver rec) throws RemoteException;
    public boolean updateFileInfo(long ID, String filename, boolean access, boolean permissions, Receiver rec) throws RemoteException;
    public InetSocketAddress updateFileData(long ID, String filename, long filesize, Receiver rec) throws RemoteException;
    public InetSocketAddress uploadFile(long ID, String filename, long filesize, boolean access, boolean permissions, Receiver rec) throws RemoteException;
    public InetSocketAddress downloadFile(long ID, String filename, Receiver rec) throws RemoteException;
    
    public String register(Credentials cred)  throws RemoteException;
    public String unregister(Credentials cred)  throws RemoteException;
    // include operations allowed on server
}
