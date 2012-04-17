/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.utils;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 *
 * @author ph4r05
 */
public class BlindTransactionManager implements TransactionManager, org.springframework.transaction.PlatformTransactionManager {

    @Override
    public void begin() throws NotSupportedException, SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getStatus() throws SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transaction suspend() throws SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resume(Transaction t) throws InvalidTransactionException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition td) throws TransactionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commit(TransactionStatus ts) throws TransactionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rollback(TransactionStatus ts) throws TransactionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
