/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.forward;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.db.DataCSVWritable;
import fi.wsnusbcollect.db.FileWritableTypes;
import java.io.IOException;

/**
 *
 * @author ph4r05
 */
public class TimeSyncRecord implements DataCSVWritable {

    public int bcastNodeId;
    public int responseNodeId;
    public long miliMessageReceived;
    public long miliBcast;
    public int counter;
    public int flags;
    public long low;
    public long high;
    public long expStart;

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
