/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodeManager;

import java.io.Serializable;

/**
 *
 * @author ph4r05
 */
public interface NodeRegisterEventListener extends Serializable{

    /**
     * Accept events
     */
    public void accept(NodeRegisterEvent evt);
}
