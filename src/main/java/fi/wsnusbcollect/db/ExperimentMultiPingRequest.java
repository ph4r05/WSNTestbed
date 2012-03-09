/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import fi.wsnusbcollect.messages.MultiPingMsg;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Protocoling entity, only sent messages are stored here
 * @author ph4r05
 */
@Entity
public class ExperimentMultiPingRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long miliFromStart;
    
    private int node;
    
    private int nodeBS;
    //************************* END OF COMMON HEADER
    
    // where to send ping message? single node or broadcast
    int destination;
    // SEQ number ot this request
    int counter;
    // tx power of destination
    int txpower;
    // channel at which to send
    int channel;
    // number of packets to send
    int packets;
    // timer delay between message send in ms
    int delay;
    // desired packet size in bytes
    int packetSize;
    // target = packets. CurPacket is incremented when:
    // TRUE => only on succ sent packet => sendDone()==SUCC
    // FALSE => on every Send()==SUCC
    short counterStrategySuccess;
    // if true then timer is started periodically and at each timer tick
    // message is sent 
    // if false new mesage is sent after previous message was successfully sent in 
    // sendDone()
    short timerStrategyPeriodic;

    /**
     * Initializes variables from message
     */
    public void loadFromMessage(MultiPingMsg msg){
        if (msg==null){
            throw new NullPointerException("Cannot load from null message");
        }
        
        this.setChannel(msg.get_channel());
        this.setCounter(msg.get_counter());
        this.setCounterStrategySuccess(msg.get_counterStrategySuccess());
        this.setDelay(msg.get_delay());
        this.setDestination(msg.get_destination());
        this.setPackets(msg.get_packets());
        this.setPacketSize(msg.get_size());
        this.setTimerStrategyPeriodic(msg.get_timerStrategyPeriodic());
        this.setTxpower(msg.get_txpower());
    }
    
    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public short getCounterStrategySuccess() {
        return counterStrategySuccess;
    }

    public void setCounterStrategySuccess(short counterStrategySuccess) {
        this.counterStrategySuccess = counterStrategySuccess;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMiliFromStart() {
        return miliFromStart;
    }

    public void setMiliFromStart(long miliFromStart) {
        this.miliFromStart = miliFromStart;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public int getPackets() {
        return packets;
    }

    public void setPackets(int packets) {
        this.packets = packets;
    }

    public short getTimerStrategyPeriodic() {
        return timerStrategyPeriodic;
    }

    public void setTimerStrategyPeriodic(short timerStrategyPeriodic) {
        this.timerStrategyPeriodic = timerStrategyPeriodic;
    }

    public int getTxpower() {
        return txpower;
    }

    public void setTxpower(int txpower) {
        this.txpower = txpower;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public int getNodeBS() {
        return nodeBS;
    }

    public void setNodeBS(int nodeBS) {
        this.nodeBS = nodeBS;
    }

    @Override
    public String toString() {
        return "ExperimentMultiPingRequest{" + "id=" + id + ", experiment=" + experiment + ", miliFromStart=" + miliFromStart + ", node=" + node + ", nodeBS=" + nodeBS + ", destination=" + destination + ", counter=" + counter + ", txpower=" + txpower + ", channel=" + channel + ", packets=" + packets + ", delay=" + delay + ", packetSize=" + packetSize + ", counterStrategySuccess=" + counterStrategySuccess + ", timerStrategyPeriodic=" + timerStrategyPeriodic + '}';
    }
}
