/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import common.ClientDTO;
import common.Credentials;
import common.Receiver;
import common.Server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class UserInterpreter implements Runnable {
    private static final String PROMPT = ">> ";
    private static Receiver myReceiver;
    private final Scanner console = new Scanner(System.in);
    private final SafePrinter printer = new SafePrinter();
    private boolean running;
    private Server server;
    private ClientDTO ID;
    
    public UserInterpreter(Server s) throws RemoteException{
        myReceiver = new ConsoleOutput();
        server = s;
        running = false;
    }
    
    public void start() {
        if (running) {
            return;
        }
        System.out.println("starting");
        running = true;
        new Thread(this).start();
    }
    public void run() {
        while (running) {
            try {
                CommandLine line = new CommandLine(console.nextLine());
                switch (line.getCommand()) {
                    case REGISTER:
                        String registered = server.register(new Credentials(line.message[1], line.message[2]));
                        printer.println(registered);
                        break;
                    case UNREGISTER:
                        String unregistered = server.unregister(new Credentials(line.message[1], line.message[2]));
                        printer.println(unregistered);
                        break;
                    case LOGIN:
                        ID = server.login(myReceiver, new Credentials(line.message[1], line.message[2]));
                        if (ID == null) {
                            printer.println("login failed, wrong creds");
                        } else {
                            printer.println("logged in");
                        }
                        break;
                    case LOGOUT:
                        boolean loggedout = server.logout(ID);
                        if (loggedout) {
                            printer.println("logged out");
                        } else {
                            printer.println("logout failed, wrong creds");
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                printer.println("I have no idea what im doing");
            }
        }
    }
    
    private class ConsoleOutput extends UnicastRemoteObject implements Receiver {
        public ConsoleOutput() throws RemoteException { }
        @Override
        public void receive(String message) throws RemoteException{
            printer.print(message);
            printer.println(PROMPT);
        }
    }
}
