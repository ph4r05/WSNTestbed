/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbbenchmark;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

/**
 *
 * @author ph4r05
 */
public class EMdonor implements EMdonorI {
    @PersistenceContext
    private EntityManager em;
    
    @PersistenceUnit
    private EntityManagerFactory emf;

    @Override
    public EntityManager getEm() {
        return em;
    }

    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManagerFactory getEmf() {
        return emf;
    }

    @Override
    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }
}
