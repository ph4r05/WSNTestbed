/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;


import fi.wsnusbcollect.main.PostConstructable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class ExperimentCtp implements PostConstructable {
    private static final Logger log = LoggerFactory.getLogger(ExperimentCtp.class);
    
    /**
     * CTP roots for collecting data (nodeIds)
     */
    private Set<Integer> ctpRoots;
    
    /**
     * txpower for every node in experiment - CTP node scaling
     */
    private int txpower;
    
    /**
     * TX power for individual nodes
     */
    private Map<Integer, Integer> txpowerIndividuals;
    
    /**
     * Delay between CTP request send
     */
    private int delay=10000;
    
    /**
     * Variability in CTP request send
     */
    private double variability=0.5;
    
    /**
     * Start CTP sending?
     */
    private boolean ctpSend=true;
    
    private ExperimentCoordinator expCoord;
    
    @Override
    @PostConstruct
    public void postConstruct(){
        this.ctpRoots = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
        this.txpowerIndividuals = new ConcurrentHashMap<Integer, Integer>();
    }

    /**
     * Some nodes were restarted, decide what to do according to CTP status (root, non-root)
     * Signaling from upper layer, we should solve it here
     * @param nodeid 
     */
    public void nodeRestarted(Collection<Integer> nodes){
        boolean anyRootRestarted = false;
        for(Integer nodeid : nodes){
            if (this.ctpRoots.contains(nodeid)){
                anyRootRestarted = true;
                break;
            }
        }
        
        if (anyRootRestarted){
            this.ctpSetRoot(nodes);
        } else {
            this.ctpNonRootRestarted(nodes);
        }
        
        // start CTP sending again
        if (ctpSend){
            // sending enabled, broadcast request to every node in network
            this.startCtpSend();
        }
    }
    
    /**
     * Start CTP sending cycle:
     *  1. every node is instructed to send single CTP message every delay+-variability
     */
    public void startCtpSend(){
        this.expCoord.sendCTPRequest(-1, 1, delay, variability, (short)0, true, true, true);
    }
    
    /**
     * Node was restarted and txpower was reseted - need to rescale
     * Strategy:
     *  1. set tx power for all nodes (consider also individual settings)
     *  2. reset route tables
     *  3. beacon all nodes
     *  4. recompute routing tables on all nodes
     */
    public synchronized void nodeRestartedTXpower(){
        log.info("Sending TX power command (global at first)");
        this.expCoord.sendCTPTXPower(-1, 3, txpower);
        this.pause(1000);
        this.expCoord.sendCTPTXPower(-1, 3, txpower);
        this.pause(1000);
        
        // individual tx powers
        if (this.txpowerIndividuals.isEmpty()==false){
            log.info("Setting tx power individuals");
            for(Entry<Integer, Integer> entry : this.txpowerIndividuals.entrySet()){
                this.expCoord.sendCTPTXPower(entry.getKey(), 3, entry.getValue());
            }
        }
        
        log.info("Sending CTP route reset command");
        this.expCoord.sendCTPRouteUpdate(-1, 4);
        this.pause(1000);
        this.expCoord.sendCTPRouteUpdate(-1, 4);
        this.pause(1000);
        
        log.info("Sending CTP route beacon command");
        this.expCoord.sendCTPRouteUpdate(-1, 2);
        this.pause(1000);
        this.expCoord.sendCTPRouteUpdate(-1, 2);
        this.pause(1000);
        this.expCoord.sendCTPRouteUpdate(-1, 2);
        this.pause(1000);
        this.expCoord.sendCTPRouteUpdate(-1, 2);
        this.pause(1000);
        
        log.info("Sending CTP route recompute command");
        this.expCoord.sendCTPRouteUpdate(-1, 3);
        this.pause(1000);
        this.expCoord.sendCTPRouteUpdate(-1, 3);
        this.pause(1000);
    }
    
    /**
     * Root node was restarted, recover strategy:
     *  1. stop CTP sending from all nodes
     *  2. set CTP root again
     *  3. TXpower reset
     * @param nodeId 
     */
    public synchronized void ctpRootRestarted(Collection<Integer> nodes){
        log.info("Stoping all CTP activity");
        this.expCoord.sendCTPRequest(-1, 0, 0, 0.0, (short)0, true, true, false);
        this.pause(1000);
        this.expCoord.sendCTPRequest(-1, 0, 0, 0.0, (short)0, true, true, false);
        this.pause(1000);
        
        log.info("Setting CTP root again");
        for(Integer nodeId : nodes){
            this.expCoord.sendSetCTPRoot(nodeId, true);
        }
        this.pause(1000);
        for(Integer nodeId : nodes){
            this.expCoord.sendSetCTPRoot(nodeId, true);
        }
        this.pause(1000);
        
        log.info("CTP TX reset");
        this.nodeRestartedTXpower();
    }
    
    /**
     * Non-Root node was restarted. It may be problem since
     * tx-power could be reseted and routing tables contains invalid information
     *  1. TXpower reset
     * 
     * @param nodeId 
     */
    public synchronized void ctpNonRootRestarted(Collection<Integer> nodes){
        this.nodeRestartedTXpower();
    }
    
    /**
     * Set node as ctp root, according to ctpRoots set
     *  1. reset all nodes
     *  2. set ctp roots
     *  3. restart network with new roots
     */
    public synchronized void ctpSetRoot(Collection<Integer> nodes){
        // 1. restart nodes before experiment - clean all settings to default
        try {
            this.expCoord.restartNodesBeforeExperiment();
        } catch (TimeoutException ex) {
            log.error("Cannot continue with experiment, nodes timeouted. Cannot start some nodes", ex);
            return;
        }
        
        // has meaning only if root node is nonempty list
        if (nodes!=null && nodes.isEmpty()==false){
            // 2. 
            this.ctpRoots.addAll(nodes);
            
            // 3. 
            this.ctpRootRestarted(nodes);
        } else {
            // no root
            this.nodeRestartedTXpower();
        }
    }
    
    /**
     * Sleeping
     * @param mili 
     */
    protected void pause(int mili){
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
    }
    
    public Set<Integer> getCtpRoots() {
        return ctpRoots;
    }

    public void setCtpRoots(Set<Integer> ctpRoots) {
        this.ctpRoots = ctpRoots;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public ExperimentCoordinator getExpCoord() {
        return expCoord;
    }

    public void setExpCoord(ExperimentCoordinator expCoord) {
        this.expCoord = expCoord;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public double getVariability() {
        return variability;
    }

    public void setVariability(double variability) {
        this.variability = variability;
    }

    public boolean isCtpSend() {
        return ctpSend;
    }

    public void setCtpSend(boolean ctpSend) {
        this.ctpSend = ctpSend;
    }

    public Map<Integer, Integer> getTxpowerIndividuals() {
        return txpowerIndividuals;
    }

    public void setTxpowerIndividuals(Map<Integer, Integer> txpowerIndividuals) {
        this.txpowerIndividuals = txpowerIndividuals;
    }

    @Override
    public String toString() {
        return "ExperimentCtp{" + "ctpRoots=" + ctpRoots + ", txpower=" + txpower + ", txpowerIndividuals=" + txpowerIndividuals + ", delay=" + delay + ", variability=" + variability + ", ctpSend=" + ctpSend + ", expCoord=" + expCoord + '}';
    }
}
