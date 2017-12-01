/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.ClientDTO;
import common.Credentials;
import common.Receiver;
import java.rmi.RemoteException;

/**
 *  Class for each client
 * @author Perttu Jääskeläinen
 */
public class Client implements ClientDTO {
    private final String user;
    private final String pass;
    private final Receiver receiver;
    
    public Client(Credentials cred, Receiver res) {
        this.user = cred.getUser();
        this.pass = cred.getPass();
        this.receiver = res;
    }
    public void printToReceiver(String s) throws RemoteException {
        receiver.receive(s);
    }
    protected String getPass() {
        return pass;
    }
    @Override
    public String getUsername() {
        return user;
    }
}
