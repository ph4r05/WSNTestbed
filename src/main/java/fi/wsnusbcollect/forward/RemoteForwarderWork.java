/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * RMI interface for senslab works requested to perform in forwarder.
 * @author ph4r05
 */
public interface RemoteForwarderWork extends Remote{
    /**
     * Performs remote node reset directly in Senslab
     * 
     * @param nodes2reset
     * @return
     * @throws RemoteException 
     */
    List<Integer> resetNodes(List<Integer> nodes2reset) throws RemoteException;
    
    /**
     * Enable timesync on all connected nodes
     * @param enable
     * @param timeInterval
     * @throws RemoteException 
     */
    void enableTimeSync(boolean enable, int timeInterval) throws RemoteException;
}
