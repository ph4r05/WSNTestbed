/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.notify;

/**
 *
 * @author ph4r05
 */
public interface EventMailNotifierIntf {
    /**
     * Send email notification if message is not timeouted
     * @param code
     * @param subcode
     * @param text
     * @param exception
     */
    public void notifyEvent(int code, String subcode, String text, Throwable exception);

    /**
     * Set timeout after which can be same event reported again in seconds.
     * @param timeoutSeconds 
     */
    public void setTimeoutSeconds(int timeoutSeconds);

    /**
     * Get timeout after which can be same event reported again in seconds.
     * @return 
     */
    public int getTimeoutSeconds();
    
    /**
     * Set recipient of notify mails
     * @param to 
     */
    public void setTo(String to);
    
    /**
     * Get recipient of notify mails
     * @return 
     */
    public String getTo();
    
    /**
     * Setting to TRUE will disable any notifications until is set to FALSE
     * @param disable 
     */
    public void disableNotifications(boolean disable);
}
