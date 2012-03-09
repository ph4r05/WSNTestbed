/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbbenchmark;

import javax.persistence.EntityManager;

/**
 *
 * @author ph4r05
 */
public interface BenchmarkExecutorI {

    long getRecordsInsert();

    int getThreadCount();

    void setRecordsInsert(long recordsInsert);

    void setThreadCount(int threadCount);
    
    public EntityManager getEm();

    /**
     * Main testing method
     */
    void test();
    
}
