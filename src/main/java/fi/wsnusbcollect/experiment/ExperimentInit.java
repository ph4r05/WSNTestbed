/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.util.List;
import java.util.Properties;

/**
 * Responsible for initializing environment for experiment.
 * Can use read config file, parameters/arguments, init node register, and so on
 * 
 * Will be instantiated by dependency injection container 
 *  - you can provide different implementation of this in applicationContext.xml
 *  - can use annotations  to wire beans (PersistenceContext) or PostConstruct 
 *      annotation to run some init
 * code right after object was constructed.
 * 
 * @author ph4r05
 */
public interface ExperimentInit {    
    /**
     * Use to init this class before experiment start
     */
    public void initClass();
    
    /**
     * Initialize experiment before run
     * Should instruct node register to init connected nodes
     */
    public void initEnvironment();
    
    /**
     * Initializes connected nodes
     * @param props
     * @param ncr 
     */
    public void initConnectedNodes(Properties props, List<NodeConfigRecord> ncr);
    
    public void updateExperimentStart(long mili);
}
