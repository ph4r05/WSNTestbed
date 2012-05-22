/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

/**
 * Interface for listening to extended TOS messenger events
 * @author ph4r05
 */
public interface TOSMessengerListener {
    /**
     * Event called when ExtendedTosMessenger receives error message from library.
     * @param nodeid
     * @param msg 
     */
    public void tosMsg(Integer nodeid, String msg);
}
