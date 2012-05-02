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

    int getTimeoutSeconds();

    String getTo();

    /**
     * Send email notification if message is not timeouted
     * @param code
     * @param subcode
     * @param text
     * @param exception
     */
    void notifyEvent(int code, String subcode, String text, Throwable exception);

    void setTimeoutSeconds(int timeoutSeconds);

    void setTo(String to);
    
}
