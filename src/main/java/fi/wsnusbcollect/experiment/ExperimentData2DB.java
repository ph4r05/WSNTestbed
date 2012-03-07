/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.nodeCom.MessageListener;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import net.tinyos.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ph4r05
 */
@Repository
//@Transactional
public class ExperimentData2DB extends Thread implements MessageListener{
    private static final Logger log = LoggerFactory.getLogger(ExperimentData2DB.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    boolean running=true;
    
    @PostConstruct
    public void init(){
        log.info("Initialized DB manager");
    }

    @Override
    public void run() {
        log.info("Thread running");
        while(running){
            try {
                Thread.sleep(10L);
            } catch (InterruptedException ex) {
                log.warn("Cannot sleep", ex);
            }
        }
        
        log.info("Finishing");
    }   
    
    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void messageReceived(int i, Message msg) {
        this.messageReceived(i, msg);
    }
    
    @Override
    public void messageReceived(int i, Message msg, long mili) {
        log.info("Message received: " + msg.toString() + "; time=" + mili);
        log.info("EMHashCode: " + this.em.hashCode());
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }
}
