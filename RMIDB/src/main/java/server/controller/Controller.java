/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import common.ClientDTO;
import common.Credentials;
import common.Receiver;
import common.Server;
import java.io.Console;
import java.io.File;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import server.integration.RMIDB;
import server.model.ClientHandler;
import server.net.UserConnection;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class Controller extends UnicastRemoteObject implements Server {
    private final ClientHandler handler;
    private final RMIDB db;
    public Controller() throws RemoteException {
        db = new RMIDB();
        handler = new ClientHandler();
    }
    private class Receive implements Receiver {
        @Override
        public void receive(String msg) {
            System.out.println(msg);
        }
    }
    @Override
    public InetSocketAddress sendFile(ClientDTO ID, String filename, boolean access) throws RemoteException {
        boolean res = db.createFile(ID, filename, access);
        if (!res) {
            return null;
        }
        InetSocketAddress add = UserConnection.getAddress();
        new Thread(new UserConnection(filename, true, new Receive())).start();
        return add;
    }
    @Override
    public InetSocketAddress receiveFile(ClientDTO ID, String filename) throws RemoteException {
        boolean res = db.receiveFile(ID, filename);
        if (!res) {
            return null;
        }
        InetSocketAddress add = UserConnection.getAddress();
        new Thread(new UserConnection(filename, true, new Receive())).start();
        return add;
    }
    @Override
    public synchronized boolean logout(ClientDTO ID) throws RemoteException {
        return handler.logout(ID);
    }
    @Override
    public synchronized ClientDTO login (Receiver res, Credentials creds) throws RemoteException {
        boolean verified = db.verify(creds);
        if (verified) {
            ClientDTO dto = handler.login(creds, res);
            return dto;
        }
        return null;
    }
    @Override
    public synchronized String register(Credentials cred) throws RemoteException {
        return db.register(cred);
    }
    @Override
    public synchronized String unregister(Credentials cred) throws RemoteException {
        return db.unregister(cred);
    }
}
