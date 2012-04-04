/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import fi.wsnusbcollect.messages.CommandMsg;
import java.io.IOException;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Commands sent/received
 * @author ph4r05
 */
@Entity
@Table(name="experimentDataCommands")
public class ExperimentDataCommands implements Serializable, DataCSVWritable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    @ManyToOne
    private ExperimentMetadata experiment;
    
    private long militime;
    
    private int node;
    
    private int nodeBS;
    
    private boolean sent;
    
    //************************* END OF COMMON HEADER
    // command
    private int  command_code;

    // command version. Support for protocol versioning. Some nodes may use older
    // firmware. May be used as packet subtype field
    private int  command_version;

    // unique command identifier
    // nodes would be able to ACK or NACK commands.
    // It poses more reliable communication.
    // @not-implemented-yet
    private int  command_id;

    // in case of ACK command, it is used as answer on specific command
    // only sugar, this info could be stored in command_data_next
    private int reply_on_command;
    private int reply_on_command_id;

    // some data associated with command (parameters for example)s
    private int  command_data;

    // for future use
    // may contain another parameters while command_data would tell subtype of protocol
    private int command_data_next1;
    private int command_data_next2;
    private int command_data_next3;
    private int command_data_next4;

    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CommandMsg msg){
        this.setCommand_code(msg.get_command_code());
        this.setCommand_data(msg.get_command_data());
        this.setCommand_id(msg.get_command_id());
        this.setCommand_version(msg.get_command_version());
        this.setCommand_data_next1(msg.getElement_command_data_next(0));
        this.setCommand_data_next2(msg.getElement_command_data_next(1));
        this.setCommand_data_next3(msg.getElement_command_data_next(2));
        this.setCommand_data_next4(msg.getElement_command_data_next(3));
        
        this.setReply_on_command(msg.get_reply_on_command());
        this.setReply_on_command_id(msg.get_reply_on_command_id());
    }
    
    public int getCommand_code() {
        return command_code;
    }

    public void setCommand_code(int command_code) {
        this.command_code = command_code;
    }

    public int getCommand_data() {
        return command_data;
    }

    public void setCommand_data(int command_data) {
        this.command_data = command_data;
    }

    public int getCommand_data_next1() {
        return command_data_next1;
    }

    public void setCommand_data_next1(int command_data_next1) {
        this.command_data_next1 = command_data_next1;
    }

    public int getCommand_data_next2() {
        return command_data_next2;
    }

    public void setCommand_data_next2(int command_data_next2) {
        this.command_data_next2 = command_data_next2;
    }

    public int getCommand_data_next3() {
        return command_data_next3;
    }

    public void setCommand_data_next3(int command_data_next3) {
        this.command_data_next3 = command_data_next3;
    }

    public int getCommand_data_next4() {
        return command_data_next4;
    }

    public void setCommand_data_next4(int command_data_next4) {
        this.command_data_next4 = command_data_next4;
    }

    public int getCommand_id() {
        return command_id;
    }

    public void setCommand_id(int command_id) {
        this.command_id = command_id;
    }

    public int getCommand_version() {
        return command_version;
    }

    public void setCommand_version(int command_version) {
        this.command_version = command_version;
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

    public long getMilitime() {
        return militime;
    }

    public void setMilitime(long militime) {
        this.militime = militime;
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

    public int getReply_on_command() {
        return reply_on_command;
    }

    public void setReply_on_command(int reply_on_command) {
        this.reply_on_command = reply_on_command;
    }

    public int getReply_on_command_id() {
        return reply_on_command_id;
    }

    public void setReply_on_command_id(int reply_on_command_id) {
        this.reply_on_command_id = reply_on_command_id;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentDataCommands other = (ExperimentDataCommands) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return "ExperimentDataCommands{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", sent=" + sent + ", command_code=" + command_code + ", command_version=" + command_version + ", command_id=" + command_id + ", reply_on_command=" + reply_on_command + ", reply_on_command_id=" + reply_on_command_id + ", command_data=" + command_data + ", command_data_next1=" + command_data_next1 + ", command_data_next2=" + command_data_next2 + ", command_data_next3=" + command_data_next3 + ", command_data_next4=" + command_data_next4 + '}';
    }

    @Override
    public void writeCSVheader(CsvWriter csvOutput) throws IOException {
        csvOutput.write("experiment");
        csvOutput.write("militime");
        csvOutput.write("node");
        csvOutput.write("nodeBS");
        
        csvOutput.write("sent");
        csvOutput.write("command_code");
        csvOutput.write("command_version");
        csvOutput.write("command_id");
        csvOutput.write("reply_on_command");
        csvOutput.write("reply_on_command_id");
        csvOutput.write("command_data");
        csvOutput.write("command_data_next1");
        csvOutput.write("command_data_next2");
        csvOutput.write("command_data_next3");
        csvOutput.write("command_data_next4");
    }

    @Override
    public void writeCSVdata(CsvWriter csvOutput) throws IOException {
        csvOutput.write(String.valueOf(this.experiment));
        csvOutput.write(String.valueOf(this.militime));
        csvOutput.write(String.valueOf(this.node));
        csvOutput.write(String.valueOf(this.nodeBS));
        
        csvOutput.write(String.valueOf(this.sent));
        csvOutput.write(String.valueOf(this.command_code));
        csvOutput.write(String.valueOf(this.command_version));
        csvOutput.write(String.valueOf(this.command_id));
        csvOutput.write(String.valueOf(this.reply_on_command));
        csvOutput.write(String.valueOf(this.reply_on_command_id));
        csvOutput.write(String.valueOf(this.command_data));
        csvOutput.write(String.valueOf(this.command_data_next1));
        csvOutput.write(String.valueOf(this.command_data_next2));
        csvOutput.write(String.valueOf(this.command_data_next3));
        csvOutput.write(String.valueOf(this.command_data_next4));
    }

    @Override
    public String getCSVname() {
        return "commands";
    }
    
    
}
