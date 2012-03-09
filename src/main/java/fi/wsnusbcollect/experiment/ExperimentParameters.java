/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentMetadata;
import org.ini4j.Wini;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ph4r05
 */
public interface ExperimentParameters {

    int getPacketsPerSecond();

    int getSendingMoteSecond();

    int[] getTxpower();

    int getTxpowerUniform();

    int getTxpowerUseSeconds();

    boolean isCollectNoiseFloor();

    /**
     * initialize experiment parameters from config file
     * @param config
     */
    void load(Wini config);

    void setCollectNoiseFloor(boolean collectNoiseFloor);

    void setPacketsPerSecond(int packetsPerSecond);

    void setSendingMoteSecond(int sendingMoteSecond);

    void setTxpower(int[] txpower);

    void setTxpowerUniform(int txpowerUniform);

    void setTxpowerUseSeconds(int txpowerUseSeconds);

    /**
     * Stores single parameter
     * @param paramName
     * @param paramStringValue
     * @param repr
     */
    void storeParameter(ExperimentMetadata expMeta, String paramName, String paramStringValue, Class repr);

    /**
     * Stores loaded/used experiment parameters to database
     * Should be called only once for one expMeta
     * @param expMeta
     */
    @Transactional
    void storeToDatabase(ExperimentMetadata expMeta);
    
}
