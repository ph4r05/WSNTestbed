/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    public Collection<Section> values() {
        return configIni.values();
    }

    public String toString() {
        return configIni.toString();
    }

    public int size() {
        return configIni.size();
    }

    public List<Section> putAll(String key, List<Section> values) {
        return configIni.putAll(key, values);
    }

    public Section put(String key, Section value, int index) {
        return configIni.put(key, value, index);
    }

    public Section put(String key, Section value) {
        return configIni.put(key, value);
    }

    public int length(Object key) {
        return configIni.length(key);
    }

    public Set<String> keySet() {
        return configIni.keySet();
    }

    public boolean isEmpty() {
        return configIni.isEmpty();
    }

    public List<Section> getAll(Object key) {
        return configIni.getAll(key);
    }

    public Section get(Object key, int index) {
        return configIni.get(key, index);
    }

    public Section get(Object key) {
        return configIni.get(key);
    }

    public Set<Entry<String, Section>> entrySet() {
        return configIni.entrySet();
    }

    public boolean containsValue(Object value) {
        return configIni.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return configIni.containsKey(key);
    }

    public void add(String key, Section value, int index) {
        configIni.add(key, value, index);
    }

    public void add(String key, Section value) {
        configIni.add(key, value);
    }

    public String removeComment(Object key) {
        return configIni.removeComment(key);
    }

    public Section remove(Object key, int index) {
        return configIni.remove(key, index);
    }

    public Section remove(Object key) {
        return configIni.remove(key);
    }

    public String putComment(String key, String comment) {
        return configIni.putComment(key, comment);
    }

    public void putAll(Map<? extends String, ? extends Section> map) {
        configIni.putAll(map);
    }

    public String getComment(Object key) {
        return configIni.getComment(key);
    }

    public void clear() {
        configIni.clear();
    }

    public <T> T get(Object sectionName, Object optionName, Class<T> clazz) {
        return configIni.get(sectionName, optionName, clazz);
    }

    public String get(Object sectionName, Object optionName) {
        return configIni.get(sectionName, optionName);
    }

    public <T> T fetch(Object sectionName, Object optionName, Class<T> clazz) {
        return configIni.fetch(sectionName, optionName, clazz);
    }

    public String fetch(Object sectionName, Object optionName) {
        return configIni.fetch(sectionName, optionName);
    }

    public String unescape(String value) {
        return configIni.unescape(value);
    }

    public String escape(String value) {
        return configIni.escape(value);
    }
}

