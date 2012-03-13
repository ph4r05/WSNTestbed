/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import fi.wsnusbcollect.nodes.NodeHandler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Describes current experiment state
 * @author ph4r05
 */
public class ExperimentState implements Serializable {
    // packets requested
    private int packetsRequested = 100;
    // in miliseconds
    private int packetDelay = 100;
    // time needed for nodes to transmit (safety zone 1 second)
    private long timeNeededForNode;
    // message sizes to try
    private ArrayList<Integer> messageSizes;
    // node handler - needed
    private volatile NodeHandlerRegister nodeReg;
    
    
    // curnode index in nodeHandlers list
    private ArrayList<Integer> nodeList;
    private Integer curNodeIndex;
    // cur message size index
    private Integer curMsgSizeIndex;
    // cur tx power index;
    private Integer curTxPowerIndex;
    
    private int curTransition=0;
    
    // real current node handler
    private NodeHandler curNodeHandler;
    // real tx power
    private int curTxPower;
    // real msg size
    private int curMsgSize;
    
    // rounds completed at high level
    private Integer roundsCompleted;
    
    /**
     * Initialize state structures, state variables
     */
    public ExperimentState(){
        // non null state
        if (messageSizes==null){
            messageSizes = new ArrayList<Integer>(1);
            messageSizes.add(0);
        }
        
        this.nodeList = new ArrayList<Integer>();
        this.curNodeHandler=null;
        this.curMsgSizeIndex=null;
        this.curTxPowerIndex=null;
        this.curNodeIndex=null;
        
        // real tx power
        curTxPower=0;
        // real msg size
        curMsgSize=0;
        // rounds completed at high level
        roundsCompleted=0;
        curTransition=0;
    }
    
    public NodeHandler getCurrentNodeHandler(){
        return this.curNodeHandler;
    }

    public int getCurTxPower() {
        return curTxPower;
    }    

    public int getCurMsgSize() {
        return curMsgSize;
    }
    
    public int[] getTxLevels(NodeHandler nh){
        int[] txLevels = null;
        if (nh.getNodeObj()!=null && nh.getNodeObj().getPlatform()!=null){
            txLevels = nh.getNodeObj().getPlatform().getTxLevels();
        }

        // init empty txlevels with something
        if (txLevels==null){
            txLevels = new int[1];
            txLevels[0]=1;
        }
        
        return txLevels;
    }
    
    /**
     * Again, from the beginning, starting state
     */
    public void reinitState(){
        // init node list
        this.nodeList = new ArrayList<Integer>();
        for(NodeHandler nh : this.nodeReg.values()){
            nodeList.add(nh.getNodeId());
        }
        
        // initialization
        this.curNodeIndex = 0;
        this.curNodeHandler = this.nodeReg.get(this.nodeList.get(this.curNodeIndex));
        // txlevel init
        int[] txLevels = this.getTxLevels(curNodeHandler);
        this.curTxPowerIndex=0;
        this.curTxPower = txLevels[curTxPowerIndex];
        // msg sizes init
        this.curMsgSizeIndex=0;
        this.messageSizes.get(curMsgSizeIndex);
    }
    
    /**
     * Initialize to last state
     */
    public void reinitLastState(){
        // init node list
        this.nodeList = new ArrayList<Integer>();
        for(NodeHandler nh : this.nodeReg.values()){
            nodeList.add(nh.getNodeId());
        }
        
        // get last node from list
        this.curNodeIndex = nodeList.size()-1;
        Integer lastNode = nodeList.get(this.curNodeIndex);
        this.curNodeHandler = this.nodeReg.get(lastNode);
        // txlevel init
        int[] txLevels = this.getTxLevels(curNodeHandler);
        this.curTxPowerIndex=txLevels.length-1;
        this.curTxPower = txLevels[curTxPowerIndex];
        // msg sizes init
        this.curMsgSizeIndex=this.messageSizes.size()-1;
        this.messageSizes.get(curMsgSizeIndex);
    }
    
    /**
     * Moves next node
     * TXpower is node dependent, thus next iteration needs to reinit txpower correctly
     */
    public boolean nextNode(){
        // can move forward?
        if (this.curNodeIndex+1 < this.nodeList.size()){
            this.curNodeIndex+=1;
            this.curNodeHandler = this.nodeReg.get(this.nodeList.get(this.curNodeIndex));
            
            // restart tx power according to selected node
            int[] txLevels = this.getTxLevels(curNodeHandler);
            this.curTxPowerIndex=0;
            this.curTxPower = txLevels[this.curTxPowerIndex];
            
            return true;
        } else {
            // cannot move forward, last element, start again, refresh
            this.curNodeIndex=0;
            this.roundsCompleted+=1;
            this.reinitState();
            return true;
        }
    }
    
    public boolean prevNode(){
        // invalid index?
        if (this.curNodeIndex==null) {
            reinitLastState();
            return true;
        }
        
        //  can step backward?
        if (this.curNodeIndex>0){
            // step backward, proceed
            this.curNodeIndex-=1;
            this.curNodeHandler = this.nodeReg.get(this.nodeList.get(this.curNodeIndex));
        } else {
            // =0, thus need to reinit 
            this.curNodeIndex = nodeList.size()-1;
            Integer lastNode = nodeList.get(this.curNodeIndex);
            this.curNodeHandler = this.nodeReg.get(lastNode);
        }
        
        // restart tx power according to selected node
        int[] txLevels = this.getTxLevels(curNodeHandler);
        this.curTxPowerIndex=txLevels.length-1;
        this.curTxPower = txLevels[this.curTxPowerIndex];
        
        // message sizes restarted in method calling this method
        return true;
    }
    
    /**
     * Move next message size
     */
    public boolean incMsgSize(){
        if (this.curMsgSizeIndex==null){
            this.curMsgSizeIndex=-1;
        }
        
        if (this.messageSizes.size() > (this.curMsgSizeIndex+1)){
            this.curMsgSizeIndex+=1;
            this.curMsgSize = this.messageSizes.get(this.curMsgSizeIndex);
            return true;
        } else {
            this.curMsgSizeIndex=0;
            this.curMsgSize = this.messageSizes.get(this.curMsgSizeIndex);
            return this.nextNode();
        }
    }
    
    /**
     * Move back message size
     * @return 
     */
    public boolean decMsgSize(){
        if (this.curMsgSizeIndex==null){
            this.curMsgSizeIndex=-1;
        }
        
        // can move backward?
        if (this.curMsgSizeIndex>0){
            this.curMsgSizeIndex-=1;
            this.curMsgSize = this.messageSizes.get(this.curMsgSizeIndex);
            return true;
        } else {
            // cannot move backward
            this.curMsgSizeIndex=this.messageSizes.size()-1;
            this.curMsgSize = this.messageSizes.get(this.curMsgSizeIndex);
            return this.prevNode();
        }
    }
    
    /**
     * increments tx power, if the end -> increments next counter
     */
    public boolean incTxPower(){
        int[] txLevels = this.getTxLevels(curNodeHandler);
        // is null?
        if (this.curTxPowerIndex==null){
            this.curTxPowerIndex=-1;
        }
        
        // has next element?
        if (txLevels.length > (this.curTxPowerIndex+1)){
            // has next element, increment, set currTxPowerCorrectly
            this.curTxPowerIndex+=1;
            this.curTxPower = txLevels[this.curTxPowerIndex];
            return true;
        } else {
            // has no next element
            this.curTxPowerIndex=0;
            this.curTxPower = txLevels[this.curTxPowerIndex];
            return this.incMsgSize();
        }
    }
    
    public boolean decTxPower(){
        int[] txLevels = this.getTxLevels(curNodeHandler);
        // is null?
        if (this.curTxPowerIndex==null){
            this.curTxPowerIndex=-1;
        }
        
        // has next element?
        if (this.curTxPowerIndex>0){
            // has next element, increment, set currTxPowerCorrectly
            this.curTxPowerIndex-=1;
            this.curTxPower = txLevels[this.curTxPowerIndex];
            return true;
        } else {
            // has no next element
            this.curTxPowerIndex=txLevels.length-1;
            this.curTxPower = txLevels[this.curTxPowerIndex];
            return this.decMsgSize();
        }
    }
    
    public boolean next(int i){
        boolean succ=true;
        for(int j=0; j<i; j++){
            succ = this.next();
            if (succ==false){
                return false;
            }
        }
        
        return true;
    }
    
    public boolean prev(int i){
        boolean succ=true;
        for(int j=0; j<i; j++){
            succ = this.prev();
            if (succ==false){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Next state transition in automaton
     * Current implementation: incTx->incMsg->incNode->ResetCounters,reinit
     */
    public boolean next(){
        this.curTransition+=1;
        
        // check current node handler is null
        if (this.curNodeHandler==null){
            if (this.nodeList!=null && this.nodeList.isEmpty()==false){
                // reinit starting state
                this.reinitState();
                // state initialized / restarted
                return true;
            } else {
                // no node in handler
                return false;
            }
        }
        
        // node handler is not null. try to increment txpower
        return this.incTxPower();
    }
    
    /**
     * Backward state transition to previous one.
     * @return 
     */
    public boolean prev(){
        //throw new UnsupportedOperationException("Backward iterator move - not implemented yet");  
        this.curTransition-=1;
        
        // check current node handler is null
        if (this.curNodeHandler==null){
            if (this.nodeList!=null && this.nodeList.isEmpty()==false){
                // reinit starting state
                this.reinitLastState();
                // state initialized / restarted
                return true;
            } else {
                // no node in handler
                return false;
            }
        }
        
        // node handler is not null. try to increment txpower
        return this.decTxPower();
    }
    
    public int getCurMsgSizeIndex() {
        return curMsgSizeIndex;
    }

    public void setCurMsgSizeIndex(int curMsgSizeIndex) {
        this.curMsgSizeIndex = curMsgSizeIndex;
    }

    public int getCurTxPowerIndex() {
        return curTxPowerIndex;
    }

    public void setCurTxPowerIndex(int curTxPowerIndex) {
        this.curTxPowerIndex = curTxPowerIndex;
    }

    public ArrayList<Integer> getMessageSizes() {
        return messageSizes;
    }

    public void setMessageSizes(ArrayList<Integer> messageSizes) {
        this.messageSizes = messageSizes;
    }

    public int getPacketDelay() {
        return packetDelay;
    }

    public void setPacketDelay(int packetDelay) {
        this.packetDelay = packetDelay;
    }

    public int getPacketsRequested() {
        return packetsRequested;
    }

    public void setPacketsRequested(int packetsRequested) {
        this.packetsRequested = packetsRequested;
    }

    public int getRoundsCompleted() {
        return roundsCompleted;
    }

    public long getTimeNeededForNode() {
        return timeNeededForNode;
    }

    public void setTimeNeededForNode(long timeNeededForNode) {
        this.timeNeededForNode = timeNeededForNode;
    }

    public NodeHandlerRegister getNodeReg() {
        return nodeReg;
    }

    public void setNodeReg(NodeHandlerRegister nodeReg) {
        this.nodeReg = nodeReg;
        
        Collection<NodeHandler> values = this.nodeReg.values();
        
        // init node list
        this.nodeList = new ArrayList<Integer>();
        for(NodeHandler nh : values){
            nodeList.add(nh.getNodeId());
        }
        
        // reset node handler - force to reinit
        this.curNodeHandler=null;
    }

    public int getCurTransition() {
        return curTransition;
    }
}
