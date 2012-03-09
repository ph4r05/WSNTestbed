/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbbenchmark;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author ph4r05
 */
public interface EMdonorI {

    EntityManager getEm();

    void setEm(EntityManager em);
    
    
    public EntityManagerFactory getEmf();

    public void setEmf(EntityManagerFactory emf);
}
