/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.db.USBconfiguration;
import fi.wsnusbcollect.db.USBdevice;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author ph4r05
 */
public interface USBarbitrator {

    /**
     * Checks whether current config corresponds to database.
     * If yes, everything is OK, otherwise new configuration has to be created
     * automaticaly from current. The old configuration is closed (validTo)
     */
    void checkActiveConfiguration();

    /**
     * Check nodes connection to actualy loaded motelist
     */
    void checkNodesConnection();

    /**
     * Checks whether node connection from database corresponds to real one
     * defined in motelist
     */
    boolean checkNodesConnection(Map<String, NodeConfigRecord> localmotelist);

    /**
     * Checks whether node connection from database corresponds to real one
     * defined in motelist. Node database is loaded by current active configuration
     * @param  localmotelist  list of connected nodes
     * @param  output   if true then inconsistencies are printed to stdout
     * @return TRUE <=> localmotelist corresponds to really connected nodes. Serial = primary key
     * criteria to primary keys: nodeid, usbpath, deviceAlias. Newly connected/disconnected
     * nodes are detected as well.
     */
    boolean checkNodesConnection(Map<String, NodeConfigRecord> localmotelist, boolean output);

    /**
     * Creates new USB configuration record with currently connected nodes.
     * @return
     */
    USBconfiguration createNewConfiguration();

    /**
     * Creates new USB configuration record with currently connected nodes.
     * @param refreshMotelist if true motelist is refreshed from currently connected nodes.
     */
    USBconfiguration createNewConfiguration(boolean refreshMotelist);

    /**
     * Detects connected nodes via command: motelist -usb -c
     * New detected nodes not present in database are stored. Database is updated
     * when connection of nodes was changed
     *
     */
    void detectConnectedNodes();

    /**
     * Performs real detection of connected nodes and returns answer as map, indexed
     * by node serial id. Detection is done by external motelist command
     * @return
     */
    Map<String, NodeConfigRecord> getConnectedNodes();

    /**
     * Returns current USB configuration
     * If no configuration exists, new one is created from currently connected nodes.
     * Thus this methoud should never return null
     *
     * @return
     */
    USBconfiguration getCurrentConfiguration();

    EntityManager getEm();

    Map<String, NodeConfigRecord> getMoteList();

    /**
     * Returns list of NodeConfigRecords for nodes to connect to from config strings
     * of inclusion/exclusion.
     *
     * @problem: includeString, excludeString are not strong enough to express all wanted situations easily
     * @extension: for more sophisticated node filters can be used rsync filter syntax
     * + means include, - means exclude. Records can be passed as list of filter lines
     * List<NodeSelectorRecord> configLines
     *
     * Filters: magic constant ALL means all nodes already detected, all next config records are ignored
     * node identifiers are comma separated
     * identifier beginning with # following only by decimal digits means node id.
     * identifier beginning with / means node device path - first aliases then nodePaths are searched
     * otherwise is identifier considered as serial number.
     *
     * @param includeString
     * @param excludeString
     * @return
     */
    List<NodeConfigRecord> getNodes2connect(String includeString, String excludeString);

    JdbcTemplate getTemplate();

    /**
     * Loads all nodes from database and returns map of USBdevice objects
     * indexed by serial number. Loaded are nodes by last configuration.
     * If configuration is null, new one is created.
     *
     * @return
     */
    Map<String, USBdevice> loadNodesFromDatabase();

    /**
     * Loads all nodes from database and returns map of USBdevice objects
     * indexed by serial number
     *
     * @return
     */
    Map<String, USBdevice> loadNodesFromDatabase(USBconfiguration config);

    /**
     * Parses node selector string and returns corresponding records for
     * currently connected nodes.
     *
     * @param selector
     * @return
     */
    List<NodeConfigRecord> parseNodeSelectorString(String selector);

    /**
     * Reprograms specified nodes with makefile.
     * Only path to directory with makefile is required. Then is executed
     * make telosb install,X bsl,/dev/mote_telosX
     *
     * @extension: add multithreading to save time required for reprogramming
     *
     * @param makeDir  absolute path to makefile directory with mote program
     */
    void reprogramNodes(List<NodeConfigRecord> nodes2connect, String makefileDir);

    void setEm(EntityManager em);

    void setMoteList(Map<String, NodeConfigRecord> moteList);

    void setMoteList(NodeSearchMap moteList);

    void setTemplate(JdbcTemplate template);

    /**
     * Dumps information about currently loaded node binding to stdout
     */
    void showBinding();

    /**
     * Updates nodes database according to currently loaded notes.
     * Updates from current working copy of moteList
     */
    void updateNodesDatabase();
    
    /**
     * Restarts node with given command, successful restart returns 0 as returnvalue
     * @param resetCommand
     * @return 
     */
    public boolean resetNode(String resetCommand);
    
}
