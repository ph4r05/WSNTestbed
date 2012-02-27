/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class experimentInitImpl implements experimentInit {
    private static final Logger log = LoggerFactory.getLogger(experimentInit.class);

    @Override
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public void initEnvironment() {
        log.info("Environment initialized");
    }
    
}
