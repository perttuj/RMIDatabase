/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import common.FileDTO;

/**
 *
 * @author Perttu Jääskeläinen
 */
public class FileWrapper implements FileDTO {
    private final String owner;
    private final String filename;
    private final String access;
    private final String permission;
    private final String size;
    public FileWrapper(String o, String f, boolean a, boolean p, long s) {
        owner = o;
        filename = f;
        access = String.valueOf(a);
        permission = String.valueOf(p);
        if (s > 1024) {
            size = String.valueOf((int)s/1000) + "kB";
        } else {
            size = String.valueOf(s) + "B";
        };
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public String getAccess() {
        return this.access;
    }

    @Override
    public String getPermission() {
        return this.permission;
    }

    @Override
    public String getSize() {
        return this.size;
    }
}
