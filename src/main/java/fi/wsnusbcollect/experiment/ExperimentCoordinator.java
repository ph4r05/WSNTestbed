/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

/**
 *
 * @author ph4r05
 */
public interface ExperimentCoordinator extends Runnable{
    public void interrupt();
    public void work();
    
    // start suspended?
    public void unsuspend();
}
