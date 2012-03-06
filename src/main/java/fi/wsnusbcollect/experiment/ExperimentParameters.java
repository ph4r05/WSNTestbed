/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment;

import fi.wsnusbcollect.db.ExperimentDataParameters;
import fi.wsnusbcollect.db.ExperimentMetadata;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.ini4j.Ini;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ph4r05
 */
@Repository
public class ExperimentParameters {
    private static final Logger log = LoggerFactory.getLogger(ExperimentParameters.class);
    
    @PersistenceContext
    private EntityManager em;
    
    private int packetsPerSecond=10;
    private int sendingMoteSecond=10;
    private int txpowerUniform=5;
    private int[] txpower=null;
    private int txpowerUseSeconds=86400;
    private boolean collectNoiseFloor=false;

    public ExperimentParameters() {
    }
    
    /**
     * initialize experiment parameters from config file
     * @param config 
     */
    public void load(Wini config) {
        if (config==null){
            log.error("Config file to read is null");
        }
        
        if (config.containsKey("parameters")==false){
            log.warn("No parameters section found");
            return;
        }
        
        // get parameters section here, will work only with this section
        Ini.Section parameters = config.get("parameters");
        
        if (parameters.containsKey("packetsPerSecond")){
            this.packetsPerSecond = Integer.parseInt(parameters.get("packetsPerSecond"));
            log.info("Found record for packetsPerSecond: " + packetsPerSecond);
        }
        
        if (parameters.containsKey("sendingMoteSecond")){
            this.sendingMoteSecond = Integer.parseInt(parameters.get("sendingMoteSecond"));
            log.info("Found record for sendingMoteSecond: " + sendingMoteSecond);
        }
        
        if (parameters.containsKey("txpowerUniform") && parameters.containsKey("txpower")){
            log.error("Parameters txpowerUniform and txpower are mutually exclusive!");
            throw new IllegalArgumentException("Parameters txpowerUniform and txpower are mutually exclusive!");
        }
        
        if (parameters.containsKey("txpowerUniform")){
            this.txpowerUniform = Integer.parseInt(parameters.get("txpowerUniform"));
            if (this.txpowerUniform<=1){
                log.error("cannot distribute 1 or less tx powers uniformly, for single "
                        + "tx power please use txpower directive instead");
                throw new IllegalArgumentException("Invalid configuration file");
            }
            
            log.info("Found record for txpowerUniform: " + txpowerUniform);
        }
        
        // txpowers?
        if (parameters.containsKey("txpower")){
            this.txpower = parameters.getAll("txpower", int[].class);
        }
        
        if (parameters.containsKey("txpowerUseSeconds")){
            this.txpowerUseSeconds = Integer.parseInt(parameters.get("txpowerUseSeconds"));
            log.info("Found record for txpowerUseSeconds: " + txpowerUseSeconds);
        }
        
        if (parameters.containsKey("collectNoiseFloor")){
            this.collectNoiseFloor =  Integer.parseInt(parameters.get("collectNoiseFloor"))==1;
            log.info("Found record for collectNoiseFloor: " + (collectNoiseFloor ? "true":"false"));
        }
    }

    /**
     * Stores loaded/used experiment parameters to database
     * Should be called only once for one expMeta
     * @param expMeta 
     */
    @Transactional
    public void storeToDatabase(ExperimentMetadata expMeta){
        
        this.storeParameter(expMeta, "packetsPerSecond", Integer.valueOf(this.packetsPerSecond).toString(), int.class);
        this.storeParameter(expMeta, "sendingMoteSecond", Integer.valueOf(this.sendingMoteSecond).toString(), int.class);
        
        if (this.txpower==null){
            this.storeParameter(expMeta, "txpowerUniform", Integer.valueOf(this.txpowerUniform).toString(), int.class);        
        } else {
            StringBuilder sb = new StringBuilder(this.txpower.length * 5);
            for(int i=0; i<this.txpower.length; i++){
                if (i>0) sb.append(",");
                sb.append(this.txpower[i]);
            }
            
            this.storeParameter(expMeta, "txpower", sb.toString(), String.class);        
        }
        
        this.storeParameter(expMeta, "txpowerUseSeconds", Integer.valueOf(this.txpowerUseSeconds).toString(), int.class);        
        this.storeParameter(expMeta, "collectNoiseFloor", collectNoiseFloor ? "1":"0", boolean.class);        
        
        // force to write now
        this.em.flush();
    }
    
    /**
     * Stores single parameter
     * @param paramName
     * @param paramStringValue
     * @param repr 
     */
    public void storeParameter(ExperimentMetadata expMeta, String paramName, 
            String paramStringValue, Class repr){
        ExperimentDataParameters p = new ExperimentDataParameters();
        p.setExperiment(expMeta);
        p.setParameterName(paramName);
        p.setParameterType(repr);
        p.setParameterValue(paramStringValue);
        this.em.persist(p);
        this.em.detach(p);
    }
    
    public boolean isCollectNoiseFloor() {
        return collectNoiseFloor;
    }

    public void setCollectNoiseFloor(boolean collectNoiseFloor) {
        this.collectNoiseFloor = collectNoiseFloor;
    }

    public int getPacketsPerSecond() {
        return packetsPerSecond;
    }

    public void setPacketsPerSecond(int packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
    }

    public int getSendingMoteSecond() {
        return sendingMoteSecond;
    }

    public void setSendingMoteSecond(int sendingMoteSecond) {
        this.sendingMoteSecond = sendingMoteSecond;
    }

    public int[] getTxpower() {
        return txpower;
    }

    public void setTxpower(int[] txpower) {
        this.txpower = txpower;
    }

    public int getTxpowerUniform() {
        return txpowerUniform;
    }

    public void setTxpowerUniform(int txpowerUniform) {
        this.txpowerUniform = txpowerUniform;
    }

    public int getTxpowerUseSeconds() {
        return txpowerUseSeconds;
    }

    public void setTxpowerUseSeconds(int txpowerUseSeconds) {
        this.txpowerUseSeconds = txpowerUseSeconds;
    }
}
