/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.notify;

import fi.wsnusbcollect.RunningApp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifies user about error event by email, should prevent spaming user mailbox with
 * repeatedly sending same messages in close intervals
 * @author ph4r05
 */
public class EventMailNotiffier implements EventMailNotifierIntf {
    private static final Logger log = LoggerFactory.getLogger(EventMailNotiffier.class);    
    
    /**
     * Timeout to wait to send next same error message, currently 2 hours
     */
    private int timeoutSeconds = 7200;
    
    /**
     * Mail address to send notifications to
     */
    private String to="ph4r05@gmail.com";
    
    /**
     * Event mapping -> Millisecond timestamp of last occurrence
     */
    private Map<EventFootprint, Long> eventHistory = new ConcurrentHashMap<EventFootprint, Long>();
    
    @PostConstruct
    public void postConstruct(){
        Properties props = RunningApp.getRunningInstance().getProps();
        if (props==null){
            return;
        }
        
        String notifyMail = props.getProperty("notifyMail"); 
        if (notifyMail!=null && notifyMail.isEmpty()==false){
            this.to = notifyMail;
        }
    }
    
    /**
     * Send email notification if message is not timeouted
     * @param code
     * @param subcode
     * @param text
     * @param exception 
     */
    @Override
    public void notifyEvent(int code, String subcode, String text, Throwable exception){
        EventFootprint eventFootprint = new EventFootprint(code, subcode, text, exception);
        
        // clear event history
        this.checkTimeouted();
        
        // check if can send notification, timeouted?
        Long eventTime = 0L;
        if (this.eventHistory.containsKey(eventFootprint)){
            eventTime = this.eventHistory.get(eventFootprint);
        }
        
        long currTime = System.currentTimeMillis();
        long thresholdTime = currTime - this.timeoutSeconds*1000;
        if (!(eventTime==null || eventTime<thresholdTime)){
            // NOT timeouted => cannot send message
            log.info("Cannot send mail event notiffication, last time of notif: " + eventTime);
            return;
        }
        
        // build message and send it
        this.eventHistory.put(eventFootprint, currTime);
        
        String subject = "Testbed event mail notification, code: " + code + "; subcode: " + subcode;
        
        StringBuilder sb = new StringBuilder();
        sb.append("EventMailNotifier sends you an event notification: \n");
        sb.append("Code: ").append(code)
                .append("\nSubCode: ").append(subcode)
                .append("\nText: ").append(text)
                .append("\n\n").append(this.buildThrowableDescription(exception));
        
        Emailer.sendMail(to, subject, sb.toString());
    }
    
    /**
     * Builds string description of exception thrown
     * @param exception
     * @return 
     */
    public String buildThrowableDescription(Throwable exception){
        StringBuilder sb = new StringBuilder();
        sb.append("Throwable description: \n");
        if (exception==null){
            sb.append("NULL\n");
            return sb.toString();
        }
        
        sb.append(exception.getMessage());
        
        // stack trace
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace!=null){
            sb.append("\nStackTrace: ");
        
            for(int i=0, cn=stackTrace.length; i<cn; i++){
                if (stackTrace[i]==null) continue;
                sb.append("Class: ").append(stackTrace[i].getClassName())
                        .append("File: ").append(stackTrace[i].getFileName())
                        .append("Line: ").append(stackTrace[i].getLineNumber())
                        .append("Method: ").append(stackTrace[i].getMethodName())
                        .append("ToString: ").append(stackTrace[i].toString())
                        .append("\n");
            }
            
            sb.append("\n\n");
        }
        
        // to string
        sb.append("ToString: \n");
        sb.append(exception.toString());
        sb.append("\n--------------- END OF EXCEPTION INFO ------------\n");
        
        return sb.toString();
    }
    
    /**
     * Cleaning maintenance method
     */
    public void checkTimeouted(){
        long currTime = System.currentTimeMillis();
        long thresholdTime = currTime - this.timeoutSeconds*1000;
        
        if (this.eventHistory.isEmpty()) return;
        
        Iterator<Entry<EventFootprint, Long>> iterator = this.eventHistory.entrySet().iterator();
        for(;iterator.hasNext();){
            Entry<EventFootprint, Long> entry = iterator.next();
            
            Long evntTime = entry.getValue();
            if (evntTime == null || evntTime < thresholdTime){
                iterator.remove();
            }
        }
    }

    @Override
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    @Override
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
    }
    
    /**
     * Event footprint - determinant of event
     */
    protected class EventFootprint{
        private int code;
        private String subcode;
        private String text;
        private Throwable exception;

        public EventFootprint() {
        }
        
        public EventFootprint(int code, String subcode, String text, Throwable exception) {
            this.code = code;
            this.subcode = subcode;
            this.text = text;
            this.exception = exception;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EventFootprint other = (EventFootprint) obj;
            if (this.code != other.code) {
                return false;
            }
            if ((this.subcode == null) ? (other.subcode != null) : !this.subcode.equals(other.subcode)) {
                return false;
            }
            if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
                return false;
            }
            if (this.exception != other.exception && (this.exception == null || !this.exception.equals(other.exception))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.code;
            hash = 37 * hash + (this.subcode != null ? this.subcode.hashCode() : 0);
            hash = 37 * hash + (this.text != null ? this.text.hashCode() : 0);
            hash = 37 * hash + (this.exception != null ? this.exception.hashCode() : 0);
            return hash;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }

        public String getSubcode() {
            return subcode;
        }

        public void setSubcode(String subcode) {
            this.subcode = subcode;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
