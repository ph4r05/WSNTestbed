/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

/**
 * Experiment module test - base class to be extended
 * @author ph4r05
 */
public abstract class ExperimentModuleBase implements ExperimentModule{

    /**
     * Parent experiment coordinator - holder of main objects
     */
    ExperimentCoordinator exc;
    
    /**
     * Experiment initializer
     */
    ExperimentInit exi;
    
    @Override
    public abstract void main();

    @Override
    public void setExc(ExperimentCoordinator exc) {
        this.exc = exc;
    }

    @Override
    public void setExi(ExperimentInit exi) {
        this.exi = exi;
    }
}
