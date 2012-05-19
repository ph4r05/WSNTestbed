/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

/**
 * Interface for experiment submodule
 * @author ph4r05
 */
public interface ExperimentModule {
    public void main();
    
    public void setExc(ExperimentCoordinator exc);
    public void setExi(ExperimentInit exi);
}
