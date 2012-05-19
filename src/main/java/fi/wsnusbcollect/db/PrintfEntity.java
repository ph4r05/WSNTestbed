/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.PrintfMsg;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author ph4r05
 */
@Entity
public class PrintfEntity implements Serializable, DataCSVWritable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private int connectedNode;
    
    private int sendingNode;
    
    private Long milliTime;
    
    private String buff;

    /**
     * loads contents from message body
     * @param msg 
     */
    public void loadFromMessage(PrintfMsg pmsg){
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < pmsg.totalSize_buffer(); k++) {
            char nextChar = (char) (pmsg.getElement_buffer(k));
            if (nextChar != 0) {
                sb.append(nextChar);
            }
        }
        
        this.milliTime = pmsg.getMilliTime();
        this.buff = sb.toString();
    }
    
    public String getBuff() {
        return buff;
    }

    public void setBuff(String buff) {
        this.buff = buff;
    }

    public int getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(int connectedNode) {
        this.connectedNode = connectedNode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMilliTime() {
        return milliTime;
    }

    public void setMilliTime(Long milliTime) {
        this.milliTime = milliTime;
    }

    public int getSendingNode() {
        return sendingNode;
    }

    public void setSendingNode(int sendingNode) {
        this.sendingNode = sendingNode;
    }
    
    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.connectedNode));
        csvOutput.write(String.valueOf(this.sendingNode));
        csvOutput.write(String.valueOf(this.milliTime));
        csvOutput.write(String.valueOf(this.buff));
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write("connectedNode");
        csvOutput.write("sendingNode");
        csvOutput.write("milliTime");
        csvOutput.write("buff");
    }

    @Override
    public String getCSVname() {
        return "printf";
    }

    @Override
    public FileWritableTypes getPrefferedWriteFormat() {
        return FileWritableTypes.XML;
    }
    
}
