/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author ph4r05
 */
public class ExperimentCoordinatorImpl implements ExperimentCoordinator, net.tinyos.message.MessageListener{
    private static final Logger log = LoggerFactory.getLogger(ExperimentCoordinatorImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    protected ExperimentInit expInit;
    
    @Autowired
    protected NodeHandlerRegister nodeReg;
    
    @PostConstruct
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public void work() {
        System.out.println("ExpINIT: " + ((this.expInit == null) ? "null" : " not null!"));
        if (this.expInit!=null){
            System.out.println(this.expInit.toString());
        }
        
        System.out.println("Sleeping for a moment - 5 seconds");
        try {
            Thread.sleep(5000000L);
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
        
        // shutdown all registered nodes...
        System.out.println("Shutting down all registered nodes");
        this.nodeReg.shutdownAll();        
        
        System.out.println("Exiting... Returning controll to main application...");
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

    /**
     * Message received event handler
     * !!! WARNING:
     * Please keep in mind that this method is executed by separate thread - 
     *  - messageListener notifier. Take a caution to avoid race conditions and concurrency 
     * probems.
     * 
     * @param i
     * @param msg 
     */
    @Override
    public synchronized void messageReceived(int i, Message msg) {
        System.out.println("Message received: " + i);
    }
}
