/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import net.tinyos.message.Message;

/**
 * Extended message listener for tinyOS messages - supports 
 * @author ph4r05
 */
public interface MessageListener extends net.tinyos.message.MessageListener{
    public void messageReceived(int i, Message msg, long mili);
}
