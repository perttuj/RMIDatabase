/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author Perttu Jääskeläinen
 */
public interface FileDTO extends Serializable {
    public String getOwner();
    public String getFilename();
    public String getAccess();
    public String getPermission();
    public String getSize();
}
