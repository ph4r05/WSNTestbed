/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodeCom;

import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.messages.MessageTypes;
import fi.wsnusbcollect.nodeManager.legacy.NodeRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery thread discovers new nodes in network and adds it to 
 * node register. Already detected nodes records are updated
 *
 * Warning! This class was used in old version. Now it is not maintained 
 * @author ph4r05
 */
public class NodeDiscovery extends Thread implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);
    
    NodeRegister nodeRegister;
    MessageSender msgSender;

    /**
     * Do node discovery?
     */
    boolean doDiscovery=true;

    /**
     * If true then this execution is first
     * Not to wait for discovery when program started
     * we want to get list of nodes now
     */
    boolean firstCycle=true;

    /**
     * Should I shutdown?
     */
    protected boolean shutdown=false;

    public NodeDiscovery(NodeRegister nodeRegister, MessageSender msgSender) {
        super("NodeDiscovery");
        this.nodeRegister = nodeRegister;
        this.msgSender = msgSender;
    }

    @Override
    public String toString() {
        return "NodeDiscovery; " + super.toString();
    }

    @Override
    public void run() {
        // register listener
        if (this.msgSender!=null && this.msgSender.getGateway() != null){
            this.msgSender.getGateway().registerListener(new CommandMsg(), this);
        }

        while(true){
            try {
                // do discovery every 30 seconds
                if (this.firstCycle==true){
                    sleep(3000);
                } else {
                    sleep(2500);
                }

                // shutdown ?
                if (this.shutdown){
                    break;
                }

                if (doDiscovery==false) continue;
                if (this.msgSender != null && this.msgSender.canAdd()==false) continue;

                // do discovery only on empty sender queue
                // and after 200ms without activity
                if (this.msgSender.getQueueLength() == 0
                        && (System.currentTimeMillis() - this.msgSender.getTimeLastMessageSent()) < 5000
                   ) continue;

                // this cycle is no more first
                this.firstCycle=false;

                // send arbitrary
                CommandMsg payload = new CommandMsg();
                payload.set_command_version((short) 0);
                payload.set_command_code((short) MessageTypes.COMMAND_IDENTIFY);
                payload.set_command_data(1);
                
                // send packet
                this.msgSender.add(MoteIF.TOS_BCAST_ADDR, payload, null);
                this.yield();
            } catch (Exception e) {
                    log.error("Exception thrown", e);
            }
        }
    }

    /**
     * Message listener
     *
     * @param i
     * @param msg
     */
    public void messageReceived(int to, Message msg) {
        if (!(msg instanceof CommandMsg)) return;
        final CommandMsg message = (CommandMsg) msg;

        // accept only ACK messages
        if (message.get_command_code() != MessageTypes.COMMAND_ACK) return;

        // if reply on identification
        if (message.get_reply_on_command() != MessageTypes.COMMAND_IDENTIFY) return;

        // get source
        int nodeSource = message.getSerialPacket().get_header_src();
        boolean isNew = this.nodeRegister.discoveryPong(nodeSource, message.get_command_data(), message.get_command_data_next()[0]);
        
        // if is node new, log info to log window
        if (isNew==true){
            log.info("New node detected, id=" + nodeSource);
        }
    }

    /**
     * Logger override
     * @param s
     */
    public void logToTextarea(String s) {
        log.info(s);
//        if (this.logWindow==null) return;
//        this.logWindow.addLogEntry(s, 18, "NodeDiscovery", JPannelLoggerLogElement.SEVERITY_DEBUG);
    }

    /**
     * Logger override
     *
     * @param s
     * @param severity
     */
    public void logToTextarea(String s, int severity) {
        log.info(s);
//        if (this.logWindow==null) return;
//        this.logWindow.addLogEntry(s, 18, "NodeDiscovery", severity);
    }

    public boolean getDoDiscovery() {
        return doDiscovery;
    }

    public void setDoDiscovery(boolean doDiscovery) {
        this.doDiscovery = doDiscovery;
    }

    /**
     * Gets flag indicating whether NodeDiscovery should shutdown and not to continue
     * in next cycle.
     * @return
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Set flag indicating whether NodeDiscovery should shutdown and not to continue
     * in next cycle.
     * @param shutdown
     */
    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
