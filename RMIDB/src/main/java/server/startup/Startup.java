/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.controller.Controller;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class Startup {
    public static void main(String[] args) {
        try {
            new Startup().startRegistry();
            Naming.rebind(Controller.REGISTRY_NAME, new Controller());
            System.out.println("server running");
        } catch (MalformedURLException | RemoteException e) {
            System.out.println("couldnt start server");
        }
    }
    private void startRegistry() throws RemoteException {
        try {
            LocateRegistry.getRegistry().list();
        } catch (RemoteException e) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
    }
}
