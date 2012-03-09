/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbManager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author ph4r05
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            /*
             * Build a SessionFactory object from session-factory configuration 
             * defined in the hibernate.cfg.xml file. In this file we register 
             * the JDBC connection information, connection pool, the hibernate 
             * dialect that we used and the mapping to our hbm.xml file for each 
             * POJO (Plain Old Java Object).
             * 
             */
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable e) {
            System.err.println("Error in creating SessionFactory object."
                    + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    /*
     * A static method for other application to get SessionFactory object 
     * initialized in this helper class.
     * 
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }


}
