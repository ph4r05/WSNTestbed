/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;

/**
 * Configuration abstraction layer, simple wrapper
 * @author ph4r05
 */
public class AppConfiguration {
    protected Wini configIni;

    public AppConfiguration(Wini configIni) {
        this.configIni = configIni;
    }
    
    public String getConfig(String section, String key){
        return this.getConfig(section, key, null);
    }
    
    public String getConfig(String section, String key, String defaultValue){
        if (this.configIni==null) return defaultValue;
    
        // read experiment metadata - to be stored in database
        if (this.configIni.containsKey(section)==false){
            return defaultValue;
        }

        // get metadata section from ini file
        Section metadata = this.configIni.get(section);

        // experiment group
        if (metadata.containsKey(key)==false){
            return defaultValue;
        }
        
        return metadata.get(key);
    }
    
    /**
     * Returns true if defined config file has defined config key
     * @param section
     * @param key
     * @return 
     */
    public boolean hasConfig(String section, String key){
        if (this.configIni==null) return false;

        // read experiment metadata - to be stored in database
        if (this.configIni.containsKey(section)==false){
            return false;
        }

        // get metadata section from ini file
        Section metadata = this.configIni.get(section);

        // experiment group
        return metadata.containsKey(key);
    }
    
    public boolean hasSection(String section){
        if (this.configIni==null) return false;

        // read experiment metadata - to be stored in database
        return this.configIni.containsKey(section);
    }

    public Wini getConfigIni() {
        return configIni;
    }
}

