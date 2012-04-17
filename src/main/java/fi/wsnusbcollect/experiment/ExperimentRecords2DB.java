/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentMetadata;

/**
 *
 * @author ph4r05
 */
public interface ExperimentRecords2DB {
    /**
     * Stores passed annotated db entity to database
     * @param entity 
     */
    public void storeEntity(Object entity);
    
    /**
     * Store main experiment to which is all next data related
     * Can be used to correctly determine file names for file writer - redundant for DB
     * @param meta 
     */
    public void setMainExperiment(ExperimentMetadata meta);
    
    public void storeExperimentMeta(ExperimentMetadata meta);
    public void updateExperimentStart(ExperimentMetadata meta, long mili);
    public void closeExperiment(ExperimentMetadata meta);
    
    public void flush();
    public void close();
}
