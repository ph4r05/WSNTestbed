/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
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
    
    protected boolean running=true;
    
    @PostConstruct
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public void work() {    
        // spawn new thread shell
        // here would be appropriate to suspend experiment until is manually 
        // started from shell
        if (App.getRunningInstance().isShell()){
            System.out.println("Shell will be started in separate thread "
                    + "from execution thread. \nYou can reach this thread by"
                    + "sys._jy_expCo and call appropriate methods");
        }
        
        System.out.println("Sleeping in infinite cycle...");
        try {
            while(running){
                Thread.sleep(10L);
            }
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
     * problems.
     * 
     * @param i
     * @param msg 
     */
    @Override
    public synchronized void messageReceived(int i, Message msg) {
        System.out.println("Message received: " + i);
        log.info("Message received: " + i);
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            System.out.println("Command message: " + cMsg.toString());
            log.info("Command message: " + cMsg.toString());
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            System.out.println("Report message: " + cMsg.toString());
            log.info("Report message: " + cMsg.toString());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }
}
