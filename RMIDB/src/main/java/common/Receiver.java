/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *  
 * @author Perttu Jääskeläinen
 */
public interface Receiver extends Remote {
    public void receive(String message) throws RemoteException;
}
