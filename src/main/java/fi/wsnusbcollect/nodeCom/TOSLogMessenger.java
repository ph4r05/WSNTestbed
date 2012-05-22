/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple log messenger for TinyOS MoteIF object, just own placeholder
 * which redirects error messages from library to log file via common logging 
 * interface.
 * 
 * @author ph4r05
 */
public class TOSLogMessenger implements net.tinyos.util.Messenger {
    private static final Logger log = LoggerFactory.getLogger(TOSLogMessenger.class);
    
    @Override
    public void message(String string) {
        log.warn("Message from tinyOS messenger: " + string);
    }
}
