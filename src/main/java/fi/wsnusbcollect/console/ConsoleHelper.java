/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.console;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Console helper class
 * @author ph4r05
 */
@Repository
@Transactional
public class ConsoleHelper {
    private static final Logger log = LoggerFactory.getLogger(ConsoleHelper.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;

    public void debug(){
        System.out.println("Debug command started");
        System.out.println("EntityManager NotNull: " + (this.em==null ? "false":"true"));
        System.out.println("EntityManager isOpen: " + (this.em.isOpen() ? "true":"false"));
        
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

    public static Logger getLog() {
        return log;
    }
}
