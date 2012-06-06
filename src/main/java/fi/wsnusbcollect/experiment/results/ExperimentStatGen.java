/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment.results;

import fi.wsnusbcollect.db.ExperimentMetadata;
import java.util.Collection;

/**
 *
 * @author ph4r05
 */
public interface ExperimentStatGen {

    /**
     * Generates RSSI stats (CSV files + gnuplot scripts + gnuplot graphs)
     * @param experiment_id
     */
    void generateRSSIStats(long experiment_id);
    public ExperimentMetadata loadExperiment(long experiment_id);
    public void generateNoiseStats(long experiment_id);
    public void generateRSSITotalStats(Collection<Long> experiment_ids, boolean joinTime);
    public void generateRSSITotalStats(long experiment_id, boolean joinTime);
    
    public boolean isRssiDataSupportsRequestId();
    public void setRssiDataSupportsRequestId(boolean rssiDataSupportsRequestId);
}
