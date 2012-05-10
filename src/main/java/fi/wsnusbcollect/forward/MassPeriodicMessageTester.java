/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import fi.wsnusbcollect.experiment.ExperimentRecords2CSV;
import fi.wsnusbcollect.utils.stats.BoxAndWhiskerCalculator;
import fi.wsnusbcollect.utils.stats.BoxAndWhiskerItem;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ph4r05
 */
public class MassPeriodicMessageTester{
    // main logger instance, configured in log4j.properties in resources
    private static final Logger log = LoggerFactory.getLogger(MassPeriodicMessageTester.class);
    
    // all testers in one
    protected Map<Integer, PeriodicMessageTester> testers;
    
    // data CSV writer
    protected ExperimentRecords2CSV dataWriter=null;
    
    // experiment start in miliseconds
    protected long expStart=0;
    
    /**
     * Queue of queued listeners
     */
    protected Queue<Integer> testersTriggered = new ConcurrentLinkedQueue<Integer>();

    public MassPeriodicMessageTester(Map<Integer, PeriodicMessageTester> testers) {
        this.testers = testers;
        
        dataWriter = new ExperimentRecords2CSV();
        dataWriter.postConstruct();
        dataWriter.setMainExperiment(null);
    }
    
    public void init(){
        for(Integer nodeId : this.testers.keySet()){
            PeriodicMessageTester curTester = this.testers.get(nodeId);
            
            // set itself as parent
            curTester.setParent(this);
            curTester.setRunning(true);
            
            // init and start thread
            curTester.init();
        }
        
        // experiment start init
        this.expStart = System.currentTimeMillis();
    }

    /**
     * Performs blocking testing
     */
    public void test() {
        while(true){
            try {
                Thread.sleep(1000);
                
                while(this.testersTriggered.isEmpty()==false){
                    Integer nodeTriggered = this.testersTriggered.poll();
                    if (this.testers.containsKey(nodeTriggered)==false) continue;
                    
                    PeriodicMessageTester curTester = this.testers.get(nodeTriggered);
                    this.extractDataFromTester(curTester);
                    
                    // clear
                    curTester.test();
                }
            } catch (InterruptedException ex) {
                log.error("Experiment interrupted", ex);
                break;
            }
        }
    }
    
    public void deinit(){
        for(Integer nodeId : this.testers.keySet()){
            PeriodicMessageTester curTester = this.testers.get(nodeId);
            curTester.deinit();
        }
        
        this.dataWriter.flush();
        this.dataWriter.close();
    }
    
    /**
     * Extracts measured data from tester
     * 
     * @param curTester 
     */
    public void extractDataFromTester(PeriodicMessageTester curTester){
        try {
            double stdDev = curTester.getStdDev();
            int rttCounter = curTester.getRttCounter();
            int succRttCoutner = curTester.getSuccRttCoutner();
            
            // classical statistics item
            List<Long> raw = curTester.getRaw();
            BoxAndWhiskerItem statItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(raw);
            double meanRTT = statItem.getMean().doubleValue();

            long mean = 0;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for(Long curLong : raw){
                if (curLong < min) min = curLong.intValue();
                if (curLong > max) max = curLong.intValue();
                mean+=curLong;
            }
            mean /= raw.size();
            
            //int min = Math.min(statItem.getMinOutlier().intValue(), statItem.getMinRegularValue().intValue());
            //int max = Math.max(statItem.getMaxOutlier().intValue(), statItem.getMaxRegularValue().intValue());
            
            // build CSV record
            log.info("Going to write stat information: " + statItem.toString() + "; min="+min+"; max="+max+" listSize: " + raw.size());
            StringBuilder sb = new StringBuilder();
            Iterator<Long> iterator = raw.iterator();
            for(int i=0; iterator.hasNext() && i<100; i++){
                if (i>0) sb.append(", ");
                sb.append(iterator.next());
            }
            
            log.info("Few samples from list: " + sb.toString());
            
            PeriodicMessageRecord rttRecord = new PeriodicMessageRecord(0, curTester.getNodeId(), meanRTT, stdDev, rttCounter, succRttCoutner);
            rttRecord.setExpStart(expStart);
            rttRecord.setMedian(statItem.getMedian().intValue());
            rttRecord.setMin(min);
            rttRecord.setMax(max);
            this.dataWriter.storeEntityCSV(rttRecord);
            this.dataWriter.flush();

        } catch(Exception e){
            log.error("Exception during testing RTT for node id: " + curTester.getNodeId(), e);
        }
    }
    
    /**
     * event from tester when threshold is reached - store to database
     * @param tester 
     */
    public void thresholdReachedEvent(PeriodicMessageTester curTester){
        log.info("Threshold reached on tester nodeId: " + curTester.getNodeId() + "; RTTCounter: " + curTester.getRttCounter());
        
        // add to queue, only if is not already
        if (this.testersTriggered.contains(curTester.getNodeId())==false){
            this.testersTriggered.add(curTester.getNodeId());
        }
    }
}
