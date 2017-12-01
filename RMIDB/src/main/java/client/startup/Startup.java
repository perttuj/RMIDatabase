/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.startup;

import client.view.UserInterpreter;
import common.Server;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class Startup {
    public static void main(String[] args) {
        try {
            Server server = (Server) Naming.lookup(Server.REGISTRY_NAME);
            System.out.println("starting thread");
            new UserInterpreter(server).start();
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            System.out.println("failed to start connection to server");
        }
    }
}
