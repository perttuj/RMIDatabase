/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Perttu Jääskeläinen
 */
public interface Server extends Remote {
    public static final String REGISTRY_NAME = "CHAT_SERVER";
    
    public ClientDTO login(Receiver res, Credentials cred) throws RemoteException;
    
    public boolean logout(ClientDTO ID) throws RemoteException;
    public InetSocketAddress sendFile(ClientDTO ID, String filename, boolean access) throws RemoteException;
    public InetSocketAddress receiveFile(ClientDTO ID, String filename) throws RemoteException;
    public String register(Credentials cred)  throws RemoteException;
    public String unregister(Credentials cred)  throws RemoteException;
    // include operations allowed on server
}
