/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import common.Receiver;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class UserInterpreter implements Runnable{
    private static final String PROMPT = ">> ";
    private static Receiver myReceiver;
    private final Scanner console = new Scanner(System.in);
    private final SafePrinter printer = new SafePrinter();
    private boolean running;
    
    public UserInterpreter() throws RemoteException{
        myReceiver = new ConsoleOutput();
    }
    
    public void start() {
        if (running) {
            return;
        }
        running = true;
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                CommandLine line = new CommandLine(console.nextLine());
                switch (line.getCommand()) {
                    case REGISTER:
                        break;
                    case UNREGISTER:
                        break;
                    case LOGIN:
                        break;
                    case LOGOUT:
                        break;
                }
            } catch (Exception e) {
                printer.println("Failed when reading commands");
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
