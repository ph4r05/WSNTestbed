/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentMetadata;
import fi.wsnusbcollect.nodeManager.NodeHandlerRegister;
import java.util.Date;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
@Transactional
public class ExperimentRecords2DBImpl implements ExperimentRecords2DB{
    private static final Logger log = LoggerFactory.getLogger(ExperimentRecords2DBImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    //@Autowired
    @Resource(name="experimentInit")
    protected ExperimentInit expInit;
    
    @Resource(name="nodeHandlerRegister")
    protected NodeHandlerRegister nodeReg;

    @Override
    public void storeEntity(Object entity) {
        try {
            this.em.persist(entity);
            this.em.flush();
        } catch(Exception e){
            log.error("Cannot persist object, exception thrown", e);
        }
    }
    
    /**
     * Nothing to do here
     * @param meta 
     */
    @Override
    public void setMainExperiment(ExperimentMetadata meta){
        return;
    }
    
    @Override
    public void storeExperimentMeta(ExperimentMetadata meta) {
        this.storeEntity(meta);
    }

    @Override
    public void updateExperimentStart(ExperimentMetadata expMeta, long mili) {
        // attached?
        if (this.em.contains(expMeta)){
            expMeta.setMiliStart(mili);
        } else {
            log.info("Entity is not managed");
            expMeta = this.em.merge(expMeta);
            expMeta.setMiliStart(mili);
            this.em.persist(expMeta);
        }
        
        this.em.flush();
    }

    @Override
    public void closeExperiment(ExperimentMetadata expMeta) {
        // attached?
        if (this.em.contains(expMeta)){
            expMeta.setDatestop(new Date());
        } else {
            log.info("Entity is not managed");
            expMeta = this.em.merge(expMeta);
            expMeta.setDatestop(new Date());
            this.em.persist(expMeta);
        }
        
        this.em.flush();
    }
    
    
    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public ExperimentInit getExpInit() {
        return expInit;
    }

    public void setExpInit(ExperimentInit expInit) {
        this.expInit = expInit;
    }

    public NodeHandlerRegister getNodeReg() {
        return nodeReg;
    }

    public void setNodeReg(NodeHandlerRegister nodeReg) {
        this.nodeReg = nodeReg;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void flush() {
        this.em.flush();
    }

    @Override
    public void close() {
        this.em.flush();
    }
}
