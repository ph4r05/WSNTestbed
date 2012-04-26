/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.db.DataCSVWritable;
import fi.wsnusbcollect.db.FileWritableTypes;
import fi.wsnusbcollect.experiment.ExperimentRecords2CSV;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs mass RTT testing
 * @author ph4r05
 */
public class MassRTTtester {
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(MassRTTtester.class);
    
    // all testers in one
    protected Map<Integer, RTTtester> testers;
    
    // data CSV writer
    protected ExperimentRecords2CSV dataWriter=null;
    
    // experiment start in miliseconds
    protected long expStart=0;

    public MassRTTtester(Map<Integer, RTTtester> testers) {
        this.testers = testers;
        
        dataWriter = new ExperimentRecords2CSV();
        dataWriter.postConstruct();
        dataWriter.setMainExperiment(null);
    }
    
    
    
    public void init(){
        for(Integer nodeId : this.testers.keySet()){
            RTTtester curTester = this.testers.get(nodeId);
            curTester.init();
        }
    }
    
    public void deinit(){
        for(Integer nodeId : this.testers.keySet()){
            RTTtester curTester = this.testers.get(nodeId);
            curTester.deinit();
        }
    }
    
    /**
     * Starts main RTT test on all nodes 
     * @param repeat  - how many times to run whole test
     * @param subcycle - how many to run test on particular node
     */
    public void test(int repeat, int subcycle){
        this.expStart = System.currentTimeMillis();
        for(int cycle=0; cycle<repeat; cycle++){
            log.info("Started new cycle: " + cycle + "/" + repeat);
            
            for(Integer nodeId : this.testers.keySet()){
                RTTtester curTester = this.testers.get(nodeId);
                log.info("Going to RTT test on nodeId: " + nodeId + "; cycle: " + cycle + " subcycleCount: " + subcycle);

                try {
                    curTester.testRTT(subcycle);
                    double meanRTT = curTester.getMeanRTT();
                    double stdDev = curTester.getStdDev();
                    int rttCounter = curTester.getRttCounter();
                    int succRttCoutner = curTester.getSuccRttCoutner();

                    // build CSV record
                    log.info("RTT test finished, going to write stat information");
                    RTTRecord rttRecord = new RTTRecord(cycle, nodeId, meanRTT, stdDev, rttCounter, succRttCoutner);
                    rttRecord.setExpStart(expStart);
                    this.dataWriter.storeEntityCSV(rttRecord);

                } catch(Exception e){
                    log.error("Exception during testing RTT for node id: " + nodeId);
                }
            }
        }
    }
}
