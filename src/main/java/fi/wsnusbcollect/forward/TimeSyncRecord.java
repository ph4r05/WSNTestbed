/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.db.DataCSVWritable;
import fi.wsnusbcollect.db.FileWritableTypes;
import fi.wsnusbcollect.messages.TimeSyncReportMsg;
import java.io.IOException;

/**
 *
 * @author ph4r05
 */
public class TimeSyncRecord implements DataCSVWritable {

    /**
     * Id of node which broadcasted time sync report command
     */
    public int bcastNodeId;
    
    /**
     * Report message originating node
     */
    public int responseNodeId;
    
    /**
     * Time received in application
     */
    public long miliMessageReceived;
    
    /**
     * Time received by TinyOS low level layer
     */
    public long miliMessageReceivedTOS;
    
    /**
     * Time when was broadcast command sent
     */
    public long miliBcast;
    
    /**
     * Cycle in which was request broadcasted to network
     */
    public int counter;
    public int flags;
    
    /**
     * Lower 32 bit part of time
     * @deprecated
     */
    public long low;
    /**
     * Higher 32 bit part of time
     * @deprecated
     */
    public long high;
    
    /**
     * Global time reported by node
     */
    public long nodeGlobalTime;
    
    /**
     * Local time reported by node
     */
    public long nodeLocalTime;
    
    /**
     * Last time synchronization from node perspective
     */
    public long nodeLastSync;
    
    /**
     * Number of entries in node's time sync table 
     */
    public int syncEntries;
    
    /**
     * Number of heartbeats for given node
     */
    public int hbeats;
    
    /**
     * Skew of local timer with respect to global timer on node
     */
    public float skew;
    
    /**
     * When particular experiment started
     */
    public long expStart;

    public void loadFromMessage(TimeSyncReportMsg tMsg){
        this.setResponseNodeId(tMsg.getSerialPacket().get_header_src());
        this.setMiliMessageReceivedTOS(tMsg.getMilliTime());
        this.setMiliMessageReceived(System.currentTimeMillis());
        this.setSyncEntries(tMsg.get_entries());
        this.setNodeGlobalTime(tMsg.get_globalTime());
        this.setNodeLocalTime(tMsg.get_localTime());
        this.setNodeLastSync(tMsg.get_lastSync());
        this.setHbeats(tMsg.get_hbeats());
        this.setSkew(tMsg.get_skew());
    }
    
    public int getBcastNodeId() {
        return bcastNodeId;
    }

    public void setBcastNodeId(int bcastNodeId) {
        this.bcastNodeId = bcastNodeId;
    }

    public long getMiliMessageReceived() {
        return miliMessageReceived;
    }

    public void setMiliMessageReceived(long miliMessageReceived) {
        this.miliMessageReceived = miliMessageReceived;
    }

    public int getResponseNodeId() {
        return responseNodeId;
    }

    public void setResponseNodeId(int responseNodeId) {
        this.responseNodeId = responseNodeId;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getMiliBcast() {
        return miliBcast;
    }

    public void setMiliBcast(long miliBcast) {
        this.miliBcast = miliBcast;
    }

    public long getExpStart() {
        return expStart;
    }

    public void setExpStart(long expStart) {
        this.expStart = expStart;
    }

    public long getMiliMessageReceivedTOS() {
        return miliMessageReceivedTOS;
    }

    public void setMiliMessageReceivedTOS(long miliMessageReceivedTOS) {
        this.miliMessageReceivedTOS = miliMessageReceivedTOS;
    }

    public long getNodeGlobalTime() {
        return nodeGlobalTime;
    }

    public void setNodeGlobalTime(long nodeGlobalTime) {
        this.nodeGlobalTime = nodeGlobalTime;
    }

    public long getNodeLocalTime() {
        return nodeLocalTime;
    }

    public void setNodeLocalTime(long nodeLocalTime) {
        this.nodeLocalTime = nodeLocalTime;
    }

    public long getNodeLastSync() {
        return nodeLastSync;
    }

    public void setNodeLastSync(long nodeLastSync) {
        this.nodeLastSync = nodeLastSync;
    }

    public int getSyncEntries() {
        return syncEntries;
    }

    public void setSyncEntries(int syncEntries) {
        this.syncEntries = syncEntries;
    }

    public int getHbeats() {
        return hbeats;
    }

    public void setHbeats(int hbeats) {
        this.hbeats = hbeats;
    }

    public float getSkew() {
        return skew;
    }

    public void setSkew(float skew) {
        this.skew = skew;
    }
    
    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("bcastNodeId");
        csvOutput.write("responseNodeId");
        csvOutput.write("miliMessageReceived");
        csvOutput.write("miliBcast");
        csvOutput.write("counter");
        csvOutput.write("flags");
        csvOutput.write("low");
        csvOutput.write("high");
        
        csvOutput.write("tostime");
        csvOutput.write("globalTime");
        csvOutput.write("localTime");
        csvOutput.write("lastSync");
        csvOutput.write("entries");
        csvOutput.write("hbeats");
        csvOutput.write("skew");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.bcastNodeId));
        csvOutput.write(String.valueOf(this.responseNodeId));
        csvOutput.write(String.valueOf(this.miliMessageReceived));
        csvOutput.write(String.valueOf(this.miliBcast));
        csvOutput.write(String.valueOf(this.counter));
        csvOutput.write(String.valueOf(this.flags));
        csvOutput.write(String.valueOf(this.low));
        csvOutput.write(String.valueOf(this.high));
        
        csvOutput.write(String.valueOf(this.miliMessageReceivedTOS));
        csvOutput.write(String.valueOf(this.nodeGlobalTime));
        csvOutput.write(String.valueOf(this.nodeLocalTime));
        csvOutput.write(String.valueOf(this.nodeLastSync));
        csvOutput.write(String.valueOf(this.syncEntries));
        csvOutput.write(String.valueOf(this.hbeats));
        csvOutput.write(String.valueOf(this.skew));
    }

    @Override
    public String getCSVname() {
        return "timeSyncTest_" + expStart;
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
}
