/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.utils;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

/**
 *
 * @author ph4r05
 */
public class BlindSessionFactory implements SessionFactory{

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SessionBuilder withOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Session openSession() throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StatelessSession openStatelessSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StatelessSession openStatelessSession(Connection cnctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClassMetadata getClassMetadata(Class type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClassMetadata getClassMetadata(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map getAllCollectionMetadata() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Statistics getStatistics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Cache getCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evict(Class type) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evict(Class type, Serializable srlzbl) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictEntity(String string) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictEntity(String string, Serializable srlzbl) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictCollection(String string) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictCollection(String string, Serializable srlzbl) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictQueries(String string) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void evictQueries() throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set getDefinedFilterNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FilterDefinition getFilterDefinition(String string) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsFetchProfileDefinition(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TypeHelper getTypeHelper() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
