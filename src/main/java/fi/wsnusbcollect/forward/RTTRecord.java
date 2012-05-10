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
public class RTTRecord implements DataCSVWritable {

    public int cycle;
    public int nodeId;
    public double meanRTT;
    public double stddev;
    public int counter;
    public int succCounter;
    public long expStart;
    
    public int min;
    public int max;
    public int median;

    public RTTRecord() {
    }

    public RTTRecord(int cycle, int nodeId, double meanRTT, double stddev, int counter, int succCounter) {
        this.cycle = cycle;
        this.nodeId = nodeId;
        this.meanRTT = meanRTT;
        this.stddev = stddev;
        this.counter = counter;
        this.succCounter = succCounter;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public double getMeanRTT() {
        return meanRTT;
    }

    public void setMeanRTT(double meanRTT) {
        this.meanRTT = meanRTT;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public double getStddev() {
        return stddev;
    }

    public void setStddev(double stddev) {
        this.stddev = stddev;
    }

    public int getSuccCounter() {
        return succCounter;
    }

    public void setSuccCounter(int succCounter) {
        this.succCounter = succCounter;
    }

    public long getExpStart() {
        return expStart;
    }

    public void setExpStart(long expStart) {
        this.expStart = expStart;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMedian() {
        return median;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }
    
    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("cycle");
        csvOutput.write("nodeId");
        csvOutput.write("meanRTT");
        csvOutput.write("stddev");
        csvOutput.write("median");
        csvOutput.write("min");
        csvOutput.write("max");
        csvOutput.write("counter");
        csvOutput.write("succCounter");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.cycle));
        csvOutput.write(String.valueOf(this.nodeId));
        csvOutput.write(String.valueOf(this.meanRTT));
        csvOutput.write(String.valueOf(this.stddev));
        csvOutput.write(String.valueOf(this.median));
        csvOutput.write(String.valueOf(this.min));
        csvOutput.write(String.valueOf(this.max));
        csvOutput.write(String.valueOf(this.counter));
        csvOutput.write(String.valueOf(this.succCounter));
    }

    @Override
    public String getCSVname() {
        return "RTTTest_" + expStart;
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.CSV;
    }
}