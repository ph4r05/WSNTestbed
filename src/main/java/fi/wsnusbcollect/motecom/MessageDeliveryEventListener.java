/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.motecom;

/**
 * Listen to message delivery events
 * 
 * @author ph4r05
 */
public interface MessageDeliveryEventListener {
    public void messageDeliveryEventAccepted(MessageDeliveryEvent evt);
}
