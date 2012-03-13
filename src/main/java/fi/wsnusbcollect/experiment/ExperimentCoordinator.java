/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.List;
import javax.persistence.EntityManager;
import net.tinyos.message.Message;
import org.springframework.jdbc.core.JdbcTemplate;

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
    public void sendMessageToNode(Message payload, int nodeId, boolean protocol);
    public void storeGenericMessageToProtocol(Message payload, int nodeId, boolean sent, boolean external);
    public void getNodesLastSeen();
    public List<Integer> getNodesLastResponse(long mili);
    public NodeHandlerRegister getNodeReg();
    public void hwresetAllNodes();
    public void hwresetNode(int nodeId);
    public void resetNode(int nodeId);
    
    public  EntityManager getEm();
    public JdbcTemplate getTemplate();
    public void emRefresh(Object o);
    public void emPersist(Object o);
    public void emFlush();
    public void storeData(Object o);
    
    public ExperimentState geteState();
    public void seteState(ExperimentState eState);
}
