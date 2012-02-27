/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

/**
 *
 * @author ph4r05
 */
public interface NodePlatform {
    public int[] getTxLevels();
    public double[] getTxOutputPower();
    
    public int getPlatformId();
    public String getPlatform();
}
