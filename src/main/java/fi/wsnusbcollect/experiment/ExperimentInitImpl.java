/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class ExperimentInitImpl implements ExperimentInit {
    private static final Logger log = LoggerFactory.getLogger(ExperimentInit.class);

    @PostConstruct
    @Override
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public void initEnvironment() {
        log.info("Environment initialized");
    }

    @Override
    public void initConnectedNodes(Properties props, List<NodeConfigRecord> ncr) {
        log.info("initializing connected nodes here");
        if (ncr==null){
            throw new NullPointerException("NCR is null");
        }
        
        Iterator<NodeConfigRecord> iterator = ncr.iterator();
        while(iterator.hasNext()){
            NodeConfigRecord nextncr = iterator.next();
            System.out.println("Node to connect to: " + nextncr.toString());
        }
    }
    
}
