/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.console.Console;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MultiPingResponseReportMsg;
import fi.wsnusbcollect.messages.RssiMsg;
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
public class ExperimentCoordinatorImpl extends Thread implements ExperimentCoordinator, net.tinyos.message.MessageListener{
    private static final Logger log = LoggerFactory.getLogger(ExperimentCoordinatorImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    protected ExperimentInit expInit;
    
    @Autowired
    protected NodeHandlerRegister nodeReg;
    
    @Autowired
    protected Console console;
    
    protected boolean running=true;
    
    protected boolean suspended=true;
    
    private Message lastMsg;

    public ExperimentCoordinatorImpl(String name) {
        super(name);
    }

    public ExperimentCoordinatorImpl() {
        super("ExperimentCoordinatorImpl");
    }

    @PostConstruct
    public void initClass() {
        log.info("Class initialized");
    }

    @Override
    public synchronized void interrupt() {
        this.running=false;
        this.nodeReg.shutdownAll();
        super.interrupt();
    }

    // if has shell, needs to spawn new thread
    // without shell it is not necessary
    @Override
    public void work() {
        if (App.getRunningInstance().isShell()){
            this.start();
        } else {
            this.run();
        }
    }

    @Override
    public void run() {
        // start suspended?
        if (App.getRunningInstance().isStartSuspended()){
            this.startSuspended();
        }
        
        this.main();
    }

    /**
     * Holds suspended until script allows execution
     */
    public void startSuspended(){
        System.out.println("Waiting in suspend sleep. To start call unsuspend().");
        try {
            while(suspended){
                Thread.sleep(100L);
                Thread.yield();
            }
            
            System.out.println("Suspend sleep stopped.");
        } catch (InterruptedException ex) {
            log.error("Cannot sleep", ex);
        }
    }
    
    public void main() {  
        // register node coordinator as message listener
        System.out.println("Register message listener for commands and pings");
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.CommandMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.PingMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.RssiMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseMsg(), this);
        this.nodeReg.registerMessageListener(new fi.wsnusbcollect.messages.MultiPingResponseReportMsg(), this);
        
        // work
        System.out.println("Sleeping in infinite cycle...");
        try {
            while(running){
                Thread.sleep(10L);
                Thread.yield();
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
        //System.out.println("Message received: " + i);
        log.info("Message received: " + i + "; type: " + msg.amType()
                + "; dataLen: " + msg.dataLength() 
                + "; hdest: " + msg.getSerialPacket().get_header_dest()
                + "; hsrc: " + msg.getSerialPacket().get_header_src());
        
        // command message?
        if (CommandMsg.class.isInstance(msg)){
            // Command message
            final CommandMsg cMsg = (CommandMsg) msg;
            //System.out.println("Command message: " + cMsg.toString());
            log.info("Command message: " + cMsg.toString());
        }
        
        // report message?
        if (MultiPingResponseReportMsg.class.isInstance(msg)){
            final MultiPingResponseReportMsg cMsg = (MultiPingResponseReportMsg) msg;
            //System.out.println("Report message: " + cMsg.toString());
            log.info("Report message: " + cMsg.toString());
        }
        
        // rssi message
        if (RssiMsg.class.isInstance(msg)){
            final RssiMsg rMsg = (RssiMsg) msg;
            //System.out.println("RSSI message: " + rMsg.toString());
            log.info("RSSI message: " + rMsg.toString());
        }
        
        this.lastMsg = msg;
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public synchronized void unsuspend(){
        this.suspended=false;
    }

    public Message getLastMsg() {
        return lastMsg;
    }
}
