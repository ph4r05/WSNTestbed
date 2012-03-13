/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentMetadata;
import fi.wsnusbcollect.messages.CommandMsg;
import fi.wsnusbcollect.nodeCom.MessageListener;

/**
 * Interface to data2db - data2db extends thread which has final methods thus it
 * cannot be completly proxied correctly. Needs this interface.
 * @author ph4r05
 */
public interface ExperimentData2DB extends MessageListener{
    public void init();
    public void run();
    public void identificationReceived(int i, CommandMsg cMsg, long mili);
    public boolean isRunning();
    public void setRunning(boolean running);
    public ExperimentMetadata getExpMeta();
    public void setExpMeta(ExperimentMetadata expMeta);
    public int getCurrentMessageThresholdFlush();
    public void setCurrentMessageThresholdFlush(int currentMessageThresholdFlush);
    public int getMaxMessageThresholdFlush();
    public void setMaxMessageThresholdFlush(int maxMessageThresholdFlush);
    public int getMessageFromLastFlush();
    public void setMessageFromLastFlush(int messageFromLastFlush);
    public int getMinMessageThresholdFlush();
    public void setMinMessageThresholdFlush(int minMessageThresholdFlush);
    public long getMiliFLushThreshold();
    public void setMiliFLushThreshold(long miliFLushThreshold);
    public long getMiliLastFlush();
    public void setMiliLastFlush(long miliLastFlush);
    public void checkQueues();
}
