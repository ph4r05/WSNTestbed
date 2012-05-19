/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.experiment.results.ExperimentStatGen;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.nodeCom.MessageToSend;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.List;
import java.util.concurrent.TimeoutException;
import net.tinyos.message.Message;

/**
 *
 * @author ph4r05
 */
public interface ExperimentCoordinator extends Runnable{
    public void interrupt();
    public void work();
    public void main();
    
    // start suspended?
    public void unsuspend();
    
    public void sendCommand(CommandMsg payload, int nodeId);
    public void resetAllNodes();
    public void sendMultiPingRequest(int nodeId, int txpower,
            int channel, int packets, int delay, int size, 
            boolean counterStrategySuccess, boolean timerStrategyPeriodic);
    
    public void sendReset(int nodeId);
    public void sendNoiseFloorReading(int nodeId, int delay);
    public void sendMessageToNode(MessageToSend payload, boolean protocol);
    public void sendMessageToNode(Message payload, int nodeId, boolean protocol);
    public void sendMessageToNode(Message payload, int nodeId, boolean protocol, boolean blocking);
    public void sendSetAddressRecognition(int nodeId, boolean enabled);
    public void sendSetCTPRoot(int nodeId, boolean isRoot);
    
        /**
     * Send request to CTP reading to node
     * @param nodeId            
     * @param packets           number of packets to be send
     * @param delay             delay between two consecutive packets send
     * @param variability       absolute +- variability in packet delay, works only if timerStrategyPeriodic==false
     * @param dataSource        
     * @param counterStrategySuccess    
     * @param timerStrategyPeriodic 
     */
    public void sendCTPRequest(int nodeId, int packets, int delay, int variability, 
            short dataSource, boolean counterStrategySuccess, boolean timerStrategyPeriodic, boolean unlimitedPackets);
    
    public void sendCTPRouteUpdate(int nodeId, int routeUpdate);
    public void sendCTPGetInfo(int nodeId);
    public void sendCTPGetNeighInfo(int nodeId, int n);
    public void sendCTPTXPower(int nodeId, int type, int txpower);
    
    public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external);
    public void getNodesLastSeen();
    public List<Integer> getNodesLastResponse(long mili);
    public NodeHandlerRegister getNodeReg();
    public void hwresetAllNodes();
    public void hwresetNode(int nodeId);
    public void resetNode(int nodeId);
    
    public void storeData(Object o);
    
    public ExperimentState geteState();
    public void seteState(ExperimentState eState);
    
    public ExperimentStatGen getStatGen();
    public void usage();
    
    
    public void suspendExperiment();
    
    /**
     * Restarts all nodes before experiment and waits for them to show alive again
     */
    public void restartNodesBeforeExperiment() throws TimeoutException;
    
    public int getExperiment2Start();

    /**
     * Sets experiment to start after unsuspend
     * 1=none
     * 2=rssi
     * 3=ctp
     * @param experiment2Start 
     */
    public void setExperiment2Start(int experiment2Start);
    
    public ExperimentCtp getExpCTP();
    public NodeReachabilityMonitor getNodeMonitor();
    
    public ExperimentRSSIConfiguration getRSSIExperimentConfig();
    
    public boolean isLogGenericMessages();
    public void setLogGenericMessages(boolean logGenericMessages);
    public boolean isLogCTPMessages();
    public void setLogCTPMessages(boolean logCTPMessages);
    public boolean isLogCommandMessages();
    public void setLogCommandMessages(boolean logCommandMessages);
    public boolean isLogPrintfMessages();
    public void setLogPrintfMessages(boolean logPrintfMessages);
    public boolean isRestartNodesBeforeExperiment();
    public void setRestartNodesBeforeExperiment(boolean restartNodesBeforeExperiment);
    
    
    public void sendCTPLogger(int nodeId, int enable);
    public void sendCTPComm(int nodeId, int cmdId, int val);
    
    public void add2experimentLog(String severity, int code, String reason, String reasonDesc);
    public void add2experimentLog(String severity, int code, String reason, String reasonDesc, String desc);
    
    /**
     * Holds suspended until script allows execution
     */
    public void waitSuspended();
    public void pause(long mili);
}
