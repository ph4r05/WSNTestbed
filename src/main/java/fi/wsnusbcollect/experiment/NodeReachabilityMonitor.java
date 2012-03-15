/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.nodeCom.MessageListener;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.ConnectedNode;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import javax.annotation.Resource;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors node reachability for experiment coordinator, handles node restarts, etc...
 * 
 * Class consists of two relatively independent components. 
 * 1. node responsiveness monitor based on lastSeen counter from nodeHandlerRegister
 * takes care about node alive status. If there is any "long lasting" problem with
 * node responsivity this component provides ways how to make nodes reachable again
 * by means of reconnecting, hw/sw reset. This component is relatively protocol/message 
 * independent. Uses only lastSeen indicator (updated by another external classes on 
 * message reception) for detection, then is used information from nodeHandlerRegister 
 * for best node restart.
 * 
 * 2. component watches alive messages sent by nodes and detects any gaps between
 * sequence numbers of consecutive alive messages. If gap is bigger than idSequenceMaxGap
 * node is added to lastNodeRestarted set. It can be considered as automatically 
 * restarted without application intervention (sequences begin from 0) or there 
 * could occur some error in data transmission (long gap in sequence). This component
 * is protocol/message dependent, recognizes CommandMsg IDENTIFY messages as I AM ALIVE
 * message and watches sequence numbers here. This component can be in future 
 * extracted to separate class, maybe more universal to support sequence watching
 * in another message types. Now it is not necessary and it would increase code 
 * complexity. Meeting KISS principle for now...
 * 
 * @author ph4r05
 */
public class NodeReachabilityMonitor implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(NodeReachabilityMonitor.class);
    
    // experiment init class - parameters/settings
    protected ExperimentInit expInit;
    
    /**
     * nodes received identification packet after reset.
     * Will be instantiated as concurrent data structure - no need to synchronize on whole
     */
    private Set<Integer> nodePrepared;
    
    /**
     * Nodes waiting on prepared state after reset/reconnect
     */
    private Set<Integer> nodesWaiting;
    
    /**
     * When was the last reset sent to this node?
     */
    private Map<Integer, Long> lastResetSent;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;
    
    /**
     * Experiment coordinator - object to notify changes to
     * @extension: this can be re-designed to extract interface for possible events
     * or messages passed to this object and build observer pattern
     * register/deregister event listener. Multiple listeners could be connected
     */
    protected ExperimentCoordinatorImpl expCoord;
    
    /**
     * counter for IDENTIFY messages - detect gaps/possible restarts 
     */ 
    private ConcurrentHashMap<Integer, Integer> lastNodeAliveCounter;
    
    /**
     * Maximum gap in sequence numbers of IDENTIFY messages to consider it normal.
     * Otherwise it is considered as restarted/failed
     */
    private int idSequenceMaxGap=15;
    
    /**
     * Reference time in miliseconds for decisions on node timeouts
     */
    private long referenceNowTime;
    
    /**
     * Time of monitor launched last time
     */
    private long lastMonitorTime;
    
    /**
     * Time of unreachable node to perform reconnecting
     */
    private int nodeDelayReconnect=5500;
    
    /**
     * How long is timeout to consider node unreachable?
     */
    private int nodeAliveThreshold=4000;
    
    /**
     * if TRUE then nodes are reseted in wait cycle if unreachable
     */
    private boolean resetNodesDuringWaitIfUnreachable=true;
    
    /** 
     * How long to wait between 2 reset attempts?
     * In ms.
     */
    private int resetNodeDelay=2000;
    
    
    
    
    
    /**
     * State variable, monitor
     * Contains error description from last monitor cycle
     */
    private String lastErrorDescription;
    
    /**
     * State variable, monitor. error in last cycle
     */
    private String lastError;
    
    /**
     * State variable, monitor. true if there was error in last monitor cycle
     */
    private boolean lastCycleError;
    
    /**
     * State variable, monitor. nodeIDs of unreachable nodes in last monitor detection cycle
     */
    private Set<Integer> lastNodeUnreachable;
    
    /**
     * State variable, monitor. NodeIDs of restarted nodes, but NOT unreachable...
     */
    private Set<Integer> lastNodeRestarted;
    
    /**
     * State variable, monitor. greater time of timeouted node from last monitor cycle.
     */
    private long lastMinLastSeen;
    
    /**
     * String output from reset - to protocol
     */
    private StringBuilder resetOutput;

    /**
     * Default constructor - need nodeRegister and expCoord to work properly
     * @param nodeReg
     * @param expCoord 
     */
    public NodeReachabilityMonitor(NodeHandlerRegister nodeReg, ExperimentCoordinatorImpl expCoord) {
        this.nodeReg = nodeReg;
        this.expCoord = expCoord;
        this.referenceNowTime = System.currentTimeMillis();
        this.initStructures();
    }
    
    /**
     * Initializes internal structures
     */
    public final void initStructures(){
        this.lastNodeAliveCounter = new ConcurrentHashMap<Integer, Integer>(this.nodeReg.size());
        this.nodePrepared = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>(this.nodeReg.size()));
        this.nodesWaiting = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>(this.nodeReg.size()));
        this.lastNodeUnreachable = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>(this.nodeReg.size()));
        this.lastNodeRestarted = Collections.newSetFromMap(new ConcurrentHashMap<Integer,Boolean>(this.nodeReg.size()));
        this.lastResetSent = new ConcurrentHashMap<Integer, Long>(this.nodeReg.size());
        this.resetOutput = new StringBuilder();
    }
    
    /**
     * Blocking method tries to make nodes reachable. If waiting exceeds timeout, 
     * collection of dead nodes is returned. This method should be called only on
     * nodes suspected to be unreachable/unresponsive. One should assume that after 
     * this method ends successfully every node passed as parameter is now restarted
     * and needs to be reinitialized if needed. 
     * 
     * Calling this method will remove all nodes passed as parameter from set of 
     * reseted nodes detected via counter-sequence-gaps.
     * 
     * @param nodes
     * @param mili 
     * @param agresivity  5 = reset is massive, then wait for reconnect, 
     *                    0 = try reconnect first if possible
     *                    1 = no reconnecting
     *                   -1 = no reset, just wait
     */
    public Collection<Integer> makeNodesReachable(Collection nodes, long mili, int agresivity){
        // first phase - unifiing collection - get node handlers
        List<Integer> deadNodes = new ArrayList<Integer>(nodes.size());
        ArrayList<NodeHandler> nhList = new ArrayList<NodeHandler>(nodes.size());
        for(Object obj : nodes){
            if (obj==null) continue;
            if (obj instanceof NodeHandler){
                nhList.add((NodeHandler) obj);
            }
            
            // read Integer->NodeHandler mapping
            if (obj instanceof Integer && this.nodeReg.containsKey((Integer) obj)){
                nhList.add(this.nodeReg.get((Integer) obj));
            }
        }
        
        // now we have initialized nodes to make reachable
        // init state and structures
        this.resetOutput = new StringBuilder();
        this.nodePrepared.clear();
        this.nodesWaiting.clear();
        this.lastResetSent.clear();
        for(NodeHandler nh : nhList){
            this.nodesWaiting.add(nh.getNodeId());
        }
        
        // set reference time now
        this.referenceNowTime = System.currentTimeMillis();
        
        // if agresivity is big - reset all nodes batch, then wait to reconnect
        if (agresivity>=5){
            this.expCoord.resetAllNodes();
            // reset prepared nodes -  detect from now
            this.nodePrepared.clear();
            long resetTime = System.currentTimeMillis();
            for(NodeHandler nh : nhList){
                this.lastResetSent.put(nh.getNodeId(), resetTime);
            }
        }
        
        // gracefull restarts
        if (agresivity==0 || agresivity==1){
            // reset node by node
            for(NodeHandler nh : nhList){
                this.resetNodeGracefully(nh.getNodeId(), agresivity);
                // reset prepared values
                this.nodePrepared.remove(nh.getNodeId());
                this.lastResetSent.put(nh.getNodeId(), System.currentTimeMillis());
            }
        }
        try {
            // wait here, restart if applicable
            this.blockingWaitingForPrepared(mili);
        } catch (TimeoutException ex) {
            log.warn("Node waiting timeouted, cannot get nodes prepared", ex);
            
            // update dead nodes
            deadNodes.addAll(this.nodesWaiting);
            deadNodes.removeAll(this.nodePrepared);
        }
        
        // prepared nodes now should be definitely considered as freshly restarted 
        // - need reinitialization.
        this.lastNodeRestarted.removeAll(this.nodesWaiting);
        
        return deadNodes;
    }
    
    /**
     * Blocking wait for nodes to become prepared.
     * If waiting takes more than timeout, TimeoutException is thrown.
     * 
     * If resetNodesDuringWaitIfUnreachable==true then node is reseted
     * again if no response arrived.
     * @param timeoutMilis 
     */
    public void blockingWaitingForPrepared(long timeoutMilis) throws TimeoutException{
        long restartStartedMili = System.currentTimeMillis();
        
        // check all nodes are prepared
        while(true){
            // now need to compare two sets on identity
            if (this.nodePrepared.containsAll(this.nodesWaiting)){
                // everything is prepared now... can exit
                return;
            }
                        
            // timeouted?
            long nowMili = System.currentTimeMillis();
            if (nowMili-restartStartedMili > timeoutMilis){
                log.warn("Node prepare cycle wait expired, timeout more than [ms]: " + timeoutMilis);
                throw new TimeoutException("Node prepare cycle wait timeouted");
            }
            
            // try to recover reachability atively?
            // if YES then reset nodes during wait until become responsive or timeout reached
            if (this.resetNodesDuringWaitIfUnreachable){
                // get timeouted nodes - try to restart if applicable from atributes
                for(Integer nodeId : this.nodesWaiting){
                    if (this.nodePrepared.contains(nodeId)) continue;
                    nowMili = System.currentTimeMillis();

                    // get last reset sent...
                    long curLastResetSent = this.referenceNowTime;
                    if (this.lastResetSent.containsKey(nodeId)){
                        curLastResetSent = this.lastResetSent.get(nodeId);
                    }
                    
                    // is greater than threshold?
                    if ((nowMili-curLastResetSent) > this.resetNodeDelay){
                        // again reached threshold, restart again, be more agressive
                        String resetResult = this.resetNodeGracefully(nodeId, 1);
                        
                        log.info("NodeId: " + nodeId + "; is being restarted again");
                        this.resetOutput.append("NodeId: ").append(nodeId).append(" restarting again. ");
                        this.resetOutput.append(resetResult).append("\n");
                        
                        // store time of last reset -> do not reset in each cycle
                        // this would be contraproductive, cycle pause is too short
                        // for restarted node to send alive message
                        this.lastResetSent.put(nodeId, nowMili);
                    }
                }
            }
            
            // sleep now, wait
            this.pause(200);
        }
    }
    
    /**
     * Init class for new waiting for prepared task.
     * Clears internal set.
     */
    public void newWaitingForPreparedTask(){
        this.nodePrepared.clear();
    }
    
    /**
     * Clears set of nodes waiting to be prepared. Needed to call if adding node to be
     * prepared sequentially. 
     */
    public void clearWaitingForPreparedList(){
        this.nodesWaiting.clear();
    }
    
    /**
     * Set nodes waiting to be prepared.
     * Does not manipulate with state in nodePrepared. Assuming that this 
     * is done BEFORE and state is prepared for new detection. 
     * 
     * This design pattern helps to optimize performance in concurrency (reset signal
     * for node can be sent in cycle when processing particular node, but this 
     * method can be called after cycle finished (need collection of nodes)
     * => messages received during cycle would be useless if this method would 
     * flush set)
     */
    public void setNodesWaitingForPrepared(Collection<Integer> nodes){
        this.clearWaitingForPreparedList();
        this.nodesWaiting.addAll(nodes);
    }
    
    /**
     * Adds node waiting to be prepared sequentially. For correct results you need
     * to call newWaitingForPreparedTask() before reset signal sent.
     */
    public void addNodeWaitingForPrepared(int nodeId){
        this.nodesWaiting.add(nodeId);
    }
    
    /**
     * Returns how long is node unreachable, using reference time.
     * If nh is null then returns Long.MAX_VALUE
     * @param nh 
     */
    public long getNodeDelay(NodeHandler nh){
        if (nh==null) return Long.MAX_VALUE;
        
        return this.referenceNowTime - nh.getNodeObj().getLastSeen();
    }
    
    /**
     * Tries to reset node gracefully if it is possible for given node.
     * Gracefully = if it is possible retain message queues. For connected nodes
     * is tried reconnecting at first (maybe is out of synchronization). If is 
     * waiting time too big, hwreset is performed if possible (more reliable) or
     * software reset by sending a reset message. 
     * 
     * @param nodeId
     * @param agresivity if >1 then reconnecting is not enough
     * @return String description of operations performed on node
     */
    public String resetNodeGracefully(int nodeId, int agresivity){
        NodeHandler nh = this.nodeReg.get(nodeId);
        long nodeDelay = this.getNodeDelay(nh);
        boolean resetDone = false;
        StringBuilder sb = new StringBuilder();
        
        // was node recovered already?
        if (nodeDelay <= 0 && agresivity==0){
            log.info("Node is already up, no reset, delay: " + nodeDelay 
                    + "; refTime: " + this.referenceNowTime
                    + "; lastSeen: " + nh.getNodeObj().getLastSeen());
            return "OK";
        }
        
        // determine adequate/gracefull-first way of restart
        if (ConnectedNode.class.isInstance(nh)) {
            // connected node needs special manipulation
            final ConnectedNode cn = (ConnectedNode) nh;

            // decide what to do depending on timeout
            if (nodeDelay <= this.nodeDelayReconnect && agresivity<=0) {
                // maybe is enought to resync, queue on node will
                // remain untouched => no message 
                resetDone = true;
                cn.setResetQueues(false);
                cn.reconnect();

                sb.append("Node reconnected, delay: ").append(nodeDelay).append("\n");
                log.info("Node was reconnected, timeout is not so big: " + nodeDelay + " ms");
            } else {
                // latency increased a lot, do hard reset
                if (cn.hwresetPossible()) {
                    // try HW reset
                    cn.hwreset();
                    resetDone = true;

                    sb.append("Node HWreset, delay: ").append(nodeDelay).append("\n");
                    log.info("HW reset performed, delay: " + nodeDelay + " ms");
                } else {
                    // HW reset not available, send reset message
                    this.resetNode(nh.getNodeId());
                    cn.reconnect();
                    resetDone = true;

                    sb.append("Node SWreset, delay: ").append(nodeDelay).append("\n");
                    log.info("SW reset performed. delay: " + nodeDelay + " ms");
                }
            }
        }

        // reset failed or not a connected node - reset by old way - send reset message
        // do not reconnect, this node is not connected
        if (resetDone == false) {
            this.resetNode(nh.getNodeId());

            sb.append("Node SWreset, delay: ").append(nodeDelay).append("\n");
            log.info("SW reset performed");
        }
        
        return sb.toString();
    }
    
    /**
     * delegates work to experiment coordinator - send messages, protocol
     * @param nodeId 
     */
    private void resetNode(int nodeId) {
        this.expCoord.resetNode(nodeId);
    }
    
    /**
     * Main monitor cycle - detect unreachable nodes and set error state if applicable.
     * No reset is performed here - need to check state variables after this method.
     */
    public void nodeMonitorCycle(){
        long timeNow = System.currentTimeMillis();
        this.referenceNowTime = timeNow;
        this.lastMonitorTime = timeNow;
        this.lastCycleError=false;
        
        // string builder - build experiment revoke description
        StringBuilder sb = null;
        
        for(NodeHandler nh : this.nodeReg.values()){
            long lastSeen = nh.getNodeObj().getLastSeen();
            long nodeDelay = timeNow - lastSeen;
            if ((timeNow-lastSeen) <= nodeAliveThreshold){
                // node time is OK
                continue;
            }
            
            // first such node ?
            // => init, error occurred
            if (this.lastCycleError==false){
                sb = new StringBuilder();
                // need to flush set of unreachable nodes - new records will be inserted
                this.lastNodeUnreachable.clear();
                this.lastMinLastSeen = Long.MAX_VALUE;
                
                log.warn("There are some unreachable nodes here: (mili=" + timeNow + "), restarting");
            }
            this.lastCycleError=true;

            
            String revokedNodeMsg = "NodeID: " + nh.getNodeId()
                    + "; Obj: " + nh.getNodeObj().toString()
                    + "; lastSeen: " + lastSeen
                    + "; delay: " + nodeDelay;
            log.warn(revokedNodeMsg);
            sb.append("Revoked node: ").
                    append(revokedNodeMsg).
                    append("; \n");

            // add to set
            this.lastNodeUnreachable.add(nh.getNodeId());
            // update greatest monitor
            this.lastMinLastSeen = Math.min(lastSeen, this.lastMinLastSeen);
        }

        // set error state here
        if (this.lastCycleError){
            // remove unreachable nodes from restarted ones
            this.lastNodeRestarted.removeAll(this.lastNodeUnreachable);
            
            this.lastError="Unreachable nodes";
            this.lastErrorDescription=sb.toString();
        }
    }
    
    @Override
    public synchronized void messageReceived(int i, Message msg, long mili) {
        // accept only command messages
        if (CommandMsg.class.isInstance(msg)==false){
            return;
        }
        
        CommandMsg cMsg = (CommandMsg) msg;
        // accept only identify messages
        if (cMsg.get_command_code() != (short)MessageTypes.COMMAND_ACK
             || cMsg.get_reply_on_command() != (short)MessageTypes.COMMAND_IDENTIFY){
            return;
        }
        
        // process requested identify messages
        int nodeIdSrc = cMsg.getSerialPacket().get_header_src();

        // update node last seen, synchronized method called
        this.nodeReg.updateLastSeen(nodeIdSrc, mili);

        // check alive sequence for suspicious gaps
        // if gap > idSequenceMaxGap and current sequence is under reasonable constant
        // (node could fail sending - reconnecting) consider node as newly restarted
        if (this.lastNodeAliveCounter.containsKey(nodeIdSrc)) {
            Integer lastCounter = this.lastNodeAliveCounter.get(nodeIdSrc);
            int currGap = (cMsg.get_command_id() - lastCounter) % 65535;
            if (currGap < 0) {
                currGap += 65535;
            }
            
            if (cMsg.get_command_id() < 10000 && currGap > idSequenceMaxGap) {
                // node counter is low && gep between last recorded node and 
                // current recorded node is too big (gap is modular - in a way
                // of incrementing counters)
                log.warn("Node " + nodeIdSrc + "; was probably reseted. "
                        + "Last sequence: " + lastCounter 
                        + "; now: " + cMsg.get_command_id()
                        + "; currentGap: " + currGap);
                
                // notify experiment coordinator
                //this.expCoord.nodeStartedFresh(nodeIdSrc);
                this.lastNodeRestarted.add(nodeIdSrc);
            }
        }
        // insert current alive counter in order to detect gaps in future
        this.lastNodeAliveCounter.put(nodeIdSrc, cMsg.get_command_id());
        
        // nodes prepared after reset
        // first node identification after reset? 
        // add to prepared set - indicates nodes was restarted successfully
        if (this.nodePrepared != null && this.nodePrepared.contains(nodeIdSrc) == false) {
            log.info("NodeId: " + nodeIdSrc + " is now prepared to communicate, timeReceived: " + mili);
            this.nodePrepared.add(nodeIdSrc);
        }
    }

    @Override
    public synchronized void messageReceived(int i, Message msg) {
        // redirect to global method
        this.messageReceived(i, msg, 0);
    }
    
    /**
     * Pause execution of this thread for specified time
     * @param mili 
     */
    public void pause(long mili){
        try {
            Thread.sleep(mili);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep " + ex);
        }
    }
    
    /**
     * Returns reset output as string
     * @return 
     */
    public String getResetProtocol(){
        return this.resetOutput.toString();
    }
    
    /**
     * Returns set of nodes restarted in time window from previous call of this method
     * @return 
     */
    public Set<Integer> getLastNodeRestarted() {
        Set<Integer> toReturn = new HashSet<Integer>(this.lastNodeRestarted);
        this.lastNodeRestarted.clear();
        return toReturn;
    }
    
    public void clearLastNodeRestarted(){
        this.lastNodeRestarted.clear();
    }
    

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }

    public NodeHandlerRegister getNodeReg() {
        return nodeReg;
    }

    public void setNodeReg(NodeHandlerRegister nodeReg) {
        this.nodeReg = nodeReg;
    }

    public long getReferenceNowTime() {
        return referenceNowTime;
    }

    public void setReferenceNowTime(long referenceNowTime) {
        this.referenceNowTime = referenceNowTime;
    }

    public int getNodeDelayReconnect() {
        return nodeDelayReconnect;
    }

    public void setNodeDelayReconnect(int nodeDelayReconnect) {
        this.nodeDelayReconnect = nodeDelayReconnect;
    }

    public int getIdSequenceMaxGap() {
        return idSequenceMaxGap;
    }

    public void setIdSequenceMaxGap(int idSequenceMaxGap) {
        this.idSequenceMaxGap = idSequenceMaxGap;
    }

    public int getNodeAliveThreshold() {
        return nodeAliveThreshold;
    }

    public void setNodeAliveThreshold(int nodeAliveThreshold) {
        this.nodeAliveThreshold = nodeAliveThreshold;
    }

    public String getLastError() {
        return lastError;
    }

    public String getLastErrorDescription() {
        return lastErrorDescription;
    }

    public ConcurrentHashMap<Integer, Integer> getLastNodeAliveCounter() {
        return lastNodeAliveCounter;
    }

    public int getResetNodeDelay() {
        return resetNodeDelay;
    }

    public void setResetNodeDelay(int resetNodeDelay) {
        this.resetNodeDelay = resetNodeDelay;
    }

    public boolean isResetNodesDuringWaitIfUnreachable() {
        return resetNodesDuringWaitIfUnreachable;
    }

    public void setResetNodesDuringWaitIfUnreachable(boolean resetNodesDuringWaitIfUnreachable) {
        this.resetNodesDuringWaitIfUnreachable = resetNodesDuringWaitIfUnreachable;
    }

    public boolean isLastCycleError() {
        return lastCycleError;
    }

    public Set<Integer> getLastNodeUnreachable() {
        return lastNodeUnreachable;
    }

    public long getLastMinLastSeen() {
        return lastMinLastSeen;
    }

    public long getLastMonitorTime() {
        return lastMonitorTime;
    }
        
}
