/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbbenchmark;

import fi.wsnusbcollect.App;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author ph4r05
 */
public class BenchmarkExecutor implements BenchmarkExecutorI {
    private static final Logger log = LoggerFactory.getLogger(BenchmarkExecutor.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @PersistenceUnit
    private EntityManagerFactory emf;
    
    @Autowired
    private JdbcTemplate template;
    
    @Autowired
    private SessionFactory sf;
    
    @Autowired
    private DataSource ds;

    protected ExecutorService tasks;
    
    private int threadCount=1;
    
    private long recordsInsert=200000;
    
    /**
     * Main testing method
     */
    @Override
    public void test(){
        
        // Hibernate
        tasks = Executors.newFixedThreadPool(threadCount);
        System.out.println("Testing Hibernate: ");
        this.clear();
        for(int i=0; i<threadCount; i++){
            tasks.execute(new threadWorkerHB());
        }
        try {
            tasks.shutdown();
            tasks.awaitTermination(70, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.error("interrupted ", ex);
        }
        
        // JPA
        tasks = Executors.newFixedThreadPool(threadCount);
        System.out.println("Testing JPA: ");
        this.clear();
        for(int i=0; i<threadCount; i++){
            tasks.execute(new threadWorkerEM());
        }
        try {
            tasks.shutdown();
            tasks.awaitTermination(70, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            log.error("interrupted ", ex);
        }
        
//        // JDBC
//        tasks = Executors.newFixedThreadPool(threadCount);
//        System.out.println("Testing JDBC: ");
//        this.clear();
//        for(int i=0; i<threadCount; i++){
//            tasks.execute(new threadWorker());
//        }
//        try {
//            tasks.shutdown();
//            tasks.awaitTermination(15, TimeUnit.SECONDS);
//        } catch (InterruptedException ex) {
//            log.error("interrupted ", ex);
//        }
    }
    
    public void clear(){
       // template.execute("TRUNCATE TABLE BenchmarkEntity");
       // template.execute("OPTIMIZE TABLE BenchmarkEntity");
    }
    
    private interface threadWorkerI extends Runnable {
        
    }
    
    /**
     * JDBC worker
     */
    private class threadWorker extends Thread implements threadWorkerI {
        private JdbcTemplate templatex;
        
        public threadWorker() {
            this.setName("JDBC Thread");
        }

        /**
         * Main run method
         */
        @Override
        public void run() {
            templatex = new org.springframework.jdbc.core.JdbcTemplate(ds);
            
            // get current miliseconds
            long miliStart = System.currentTimeMillis();
            
            ArrayList<BenchmarkEntity> list = new ArrayList<BenchmarkEntity>(1000);
            for ( int i=0; i<recordsInsert; i++ ) {
                BenchmarkEntity be = new BenchmarkEntity();
                be.setD1(1);
                be.setD2(i);
                be.setD3((int)this.getId());
                list.add(be);
                
                if ( i % 100 == 0 || (i+1) == recordsInsert) { //20, same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    this.store(list);
                    list.clear();
                }
            }

            
            long miliSum = System.currentTimeMillis() - miliStart;
            
            log.info("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");
            System.out.println("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");            
            
            this.interrupt();
        } // end run()
        
        public void store(List<BenchmarkEntity> list){
            if (list==null || list.isEmpty()) return;
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO BenchmarkEntity(id,d1,d2,d3,d4) VALUES ");
            
            int i=0;
            for (BenchmarkEntity entity : list) {
                if (i>0) sb.append(",");
                sb.append("(NULL,")
                        .append(entity.getD1()).append(",")
                        .append(entity.getD2()).append(",")
                        .append(entity.getD3()).append(",")
                        .append(entity.getD4()).append(")");
                
                ++i;
            }
            
            templatex.execute(sb.toString());
        }
    } // end of class

    
    /**
     * JPA worker
     */
    @Transactional
    private class threadWorkerEM extends Thread implements threadWorkerI {
        private EntityManager emx;
        private EntityManagerFactory emfx;
        ArrayList<BenchmarkEntity> list;
        
        // single TransactionTemplate shared amongst all methods in this instance
        private TransactionTemplate transactionTemplate;
  
        public threadWorkerEM() {
            this.setName("JPA Thread");           
        }

        /**
         * Main run method
         */
        @Override
        public void run() {
            EMdonorI emd = (EMdonorI) App.getRunningInstance().getAppContext().getBean("emdonor");
            emx = emd.getEm();
            emfx = emd.getEmf();
            
            this.transactionTemplate = new TransactionTemplate((PlatformTransactionManager)App.getRunningInstance().getAppContext().getBean("transactionManager"));
            
            if(TransactionSynchronizationManager.hasResource(emfx)){
                System.out.println("Has resource!");
                TransactionSynchronizationManager.unbindResource(emfx);
            } else {
                try {
                    //TransactionSynchronizationManager.bindResource(emfx, new EntityManagerHolder(emx));
                } catch(Exception e){
                    log.error("Exception when registering new em", e);
                }
            }
            
            // get current miliseconds
            long miliStart = System.currentTimeMillis();
            
            list = new ArrayList<BenchmarkEntity>(1000);
            for ( int i=0; i<recordsInsert; i++ ) {
                BenchmarkEntity be = new BenchmarkEntity();
                be.setD1(1);
                be.setD2(i);
                be.setD3((int)this.getId());
                list.add(be);
                
                if ( i % 1000 == 0 || (i+1) == recordsInsert) {
                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    // the code in this method executes in a transactional context
                      @Override
                      protected void doInTransactionWithoutResult(TransactionStatus status) {
                           for (BenchmarkEntity entity : list) {
                                 emx.persist(entity);
                           }
                          emx.flush();
                      }
                    });                   
                    
                    list.clear();
                }
            }
            
            long miliSum = System.currentTimeMillis() - miliStart;
            
            log.info("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");
            System.out.println("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");            
            
        } // end run()
        
        @Transactional
        public void store(List<BenchmarkEntity> list){
            if (list==null || list.isEmpty()) return;
             for (BenchmarkEntity entity : list) {
                 emx.persist(entity);
            }
             
            emx.flush();
        }
    } // end of class
    
    /**
     * Hibernate worker
     */
    private class threadWorkerHB extends Thread implements threadWorkerI {
        public threadWorkerHB() {
            this.setName("Hibernate Thread");
        }

        /**
         * Main run method
         */
        @Override
        public void run() {
            Session session = sf.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            
            // get current miliseconds
            long miliStart = System.currentTimeMillis();
            for ( int i=0; i<recordsInsert; i++ ) {
                BenchmarkEntity be = new BenchmarkEntity();
                be.setD1(1);
                be.setD2(i);
                be.setD3((int)this.getId());
                
                session.save(be);
                if ( i % 100 == 0 || (i+1) == recordsInsert) { //20, same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }

            tx.commit();
            session.close();
            
            long miliSum = System.currentTimeMillis() - miliStart;
            
            log.info("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");
            System.out.println("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");            
            
            /**
             * STATELESS SESSION!
             */
            
            clear();
            
            
            StatelessSession session2 = sf.openStatelessSession();
            tx = session2.beginTransaction();
            miliStart = System.currentTimeMillis();
            for ( int i=0; i<recordsInsert; i++ ) {
                BenchmarkEntity be = new BenchmarkEntity();
                be.setD1(1);
                be.setD2(i);
                be.setD3((int)this.getId());
                session2.insert(be);
            }

            tx.commit();
            session2.close();
            
            miliSum = System.currentTimeMillis() - miliStart;
            
            log.info("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");
            System.out.println("Inserting " + recordsInsert + " from " + this.getName() + "; took: " + miliSum + "ms");            
        }
    } // end of class
    
    
    @Override
    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public SessionFactory getSf() {
        return sf;
    }

    public void setSf(SessionFactory sf) {
        this.sf = sf;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public long getRecordsInsert() {
        return recordsInsert;
    }

    @Override
    public void setRecordsInsert(long recordsInsert) {
        this.recordsInsert = recordsInsert;
    }

    public ExecutorService getTasks() {
        return tasks;
    }

    public void setTasks(ExecutorService tasks) {
        this.tasks = tasks;
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
