/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

/**
 *
 * @author Perttu Jääskeläinen
 */
public enum Command {
    // write commands for program
    HELP("HELP"),
    
    QUIT("QUIT"),
    
    UPLOAD("UPLOAD <filename> <public> <permissions>"), 
    
    DOWNLOAD("DOWNLOAD <filename>"),
    
    DELETE("DELETE <filename>"),
    
    NOTIFY("NOTIFY <filename>"),
    
    UPDATEINFO("UPDATEINFO <filename> <public> <permissions>"),
    
    UPDATEDATA("UPDATEDATA <filename>"),
    
    LISTALL("LISTALL"),
    
    REGISTER("REGISTER <username> <password>"),
    
    UNREGISTER("UNREGISTER <username> <password>"),
    
    LOGIN("LOGIN <username> <password>"),
    
    LOGOUT("LOGOUT");
    private final String description;
    private final int length;
    private Command(String desc) {
        description = desc;
        length = desc.split(CommandLine.DELIMETER).length;
    }
    public String getDescription() {
        return description;
    }
    public int getLength() {
        return length;
    }
}
