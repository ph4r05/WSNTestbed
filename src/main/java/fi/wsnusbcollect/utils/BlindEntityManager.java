/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.utils;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author ph4r05
 */
public class BlindEntityManager implements EntityManager {

    @Override
    public void persist(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T merge(T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> type, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> type, Object o, Map<String, Object> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> type, Object o, LockModeType lmt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T find(Class<T> type, Object o, LockModeType lmt, Map<String, Object> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getReference(Class<T> type, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlushMode(FlushModeType fmt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FlushModeType getFlushMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(Object o, LockModeType lmt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void lock(Object o, LockModeType lmt, Map<String, Object> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object o, Map<String, Object> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object o, LockModeType lmt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Object o, LockModeType lmt, Map<String, Object> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void detach(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LockModeType getLockMode(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProperty(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createQuery(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> cq) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createQuery(String string, Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNamedQuery(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String string, Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String string, Class type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Query createNativeQuery(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void joinTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getDelegate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
