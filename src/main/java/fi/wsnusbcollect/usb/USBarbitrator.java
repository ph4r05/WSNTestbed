package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.db.USBdevice;
import fi.wsnusbcollect.nodes.NodePlatform;
import fi.wsnusbcollect.nodes.NodePlatformFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * USB arbitrator class - manages connected nodes
 * @author ph4r05
 */
@Repository
@Transactional
public class USBarbitrator {
    private static final Logger log = LoggerFactory.getLogger(USBarbitrator.class);
    private static final String UDEV_RULES_LINE_PATTERN = "^ATTRS\\{serial\\}\\s*==\\s*\\\"([0-9a-zA-Z_]+)\\\",\\s*NAME\\s*=\\s*\\\"([0-9a-zA-Z_]+)\\\".*";
    private static final String NODE_ID_PATTERN = ".*?([0-9]+)$";
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    // serial->motelist record association
    private Map<String, NodeConfigRecord> moteList = null;

    /**
     * Loads all nodes from database
     * @return 
     */
    public Map<String, USBdevice> loadNodesFromDatabase(){
        Map<String, USBdevice> result = new HashMap<String, USBdevice>();
        
        TypedQuery<USBdevice> tq = this.em.createQuery("SELECT ubd FROM USBdevice ubd", USBdevice.class);
        List<USBdevice> resultList = tq.getResultList();
        if (resultList.isEmpty()){
            log.info("None nodes records in database");
            return result;
        }
        
        Iterator<USBdevice> iterator = resultList.iterator();
        while(iterator.hasNext()){
            USBdevice ubd = iterator.next();
            result.put(ubd.getSerial(), ubd);
        }
        
        return result;
    }
    
    /**
     * Checks whether node connection from database corresponds to real one
     */
    public void checkNodesConnection(){
        if (this.moteList==null || this.moteList.isEmpty()){
            log.error("Cannot check database - no local data");
            return;
        }
        
        // update nodes if applicable
        // serial -> usbdevice
        Map<String, USBdevice> savedNodes = this.loadNodesFromDatabase();
        // will contain saved, but detected nodes
        Set<String> dbNodesSet = new HashSet<String>(savedNodes.keySet());
        
        Iterator<String> iterator = this.moteList.keySet().iterator();
        while(iterator.hasNext()){
            String nodeSerial = iterator.next();
            NodeConfigRecord mr = this.moteList.get(nodeSerial);
            
            if (savedNodes.containsKey(mr.getSerial())){
                // record already exists, compare critical values here
                log.debug("Record already exists");
                
                // delete from dbNodesSet - node is being checked, thus is connected
                dbNodesSet.remove(mr.getSerial());
                
                // get node from map
                USBdevice ubd = savedNodes.get(mr.getSerial());
                
                // was node id changed
                Integer mrNodeId = mr.getNodeId() != null ? Integer.parseInt(mr.getNodeId()) : null;
                if (mrNodeId!=null && mrNodeId.equals(ubd.getNodeId())==false){
                    System.out.println("NodeID is different for node with serial: " + mr.getSerial()
                            + "; Stored NodeID: " + ubd.getNodeId() + "; Current NodeID: " + mr.getNodeId());
                }
                
                // check if device alias is changed, of yes then is probably changed udev rules file
                if (mr.getDeviceAlias().equals(ubd.getDeviceAlias())==false){
                    System.out.println("Node device alias was changed (probably modified udev rules)"
                            + " for node serial: " + ubd.getSerial()
                            + "; NodeID: " + ubd.getNodeId() );
                }
                
                // check USB connection, changed?
                if (mr.getUsbPath().equals(ubd.getUsbPath())==false){
                    System.out.println("Node USB connection path changed for node serial: " + ubd.getSerial()
                            + "; NodeID: " + ubd.getNodeId() 
                            + "; Should be: " + ubd.getUsbPath()
                            + "; But is: " + mr.getUsbPath());
                }
            } else {
                // new node detected, announce this. Node should be inserted to database
                log.info("New node detected: " + mr.toString());
                // Print new node info on stdout
                System.out.println("New node detected (not in DB): "
                        + "USBpath: " + mr.getUsbPath()
                        + "; NodeDev: " + mr.getDevicePath()
                        + "; NodeAlias: " + mr.getDeviceAlias()
                        + "; Serial: " + mr.getSerial()
                        + "; Node ID: " + mr.getNodeId()
                        + "; Description: " + mr.getDescription());
            }
        }
        // process saved but not detected nodes
        Iterator<String> iterator1 = dbNodesSet.iterator();
        while(iterator1.hasNext()){
            String serial = iterator1.next();
            USBdevice ubd = savedNodes.get(serial);
            
            System.out.println("Saved node was not detected to be connected. "
                    + " Node serial: " + ubd.getSerial()
                    + "; NodeID: " + ubd.getNodeId() 
                    + "; NodeDev: " + ubd.getDevicePath()
                    + "; NodeAlias: " + ubd.getDeviceAlias()
                    + "; Is it intentional? Please check it");
        }
        
        System.out.println("Node connection check completed");
    }
    
    /**
     * Updates nodes database according to currently loaded notes
     */
    public void updateNodesDatabase(){
        if (this.moteList==null || this.moteList.isEmpty()){
            log.error("Cannot update node database - no local data");
            return;
        }
        
        // update nodes if applicable
        // serial -> usbdevice
        Map<String, USBdevice> savedNodes = this.loadNodesFromDatabase();
        
        Iterator<String> iterator = this.moteList.keySet().iterator();
        while(iterator.hasNext()){
            String nodeSerial = iterator.next();
            NodeConfigRecord mr = this.moteList.get(nodeSerial);
            log.info("Saving mote record: " + mr.toString());
            
            USBdevice ubd = new USBdevice();
            if (savedNodes.containsKey(mr.getSerial())){
                log.info("Updating old record");
                ubd = savedNodes.get(mr.getSerial());
            } else {
                log.info("Inserting new record");
                ubd.setUSBConfiguration_id(1L);
                ubd.setDescription(mr.getDescription());
            }

            ubd.setDescription(mr.getDescription());            
            ubd.setBus(mr.getBus());
            ubd.setDeviceAlias(mr.getDeviceAlias());
            ubd.setDevicePath(mr.getDevicePath());
            ubd.setLastModification(new Date());
            ubd.setSerial(mr.getSerial());
            ubd.setUsbPath(mr.getUsbPath());
            ubd.setPlatformId(mr.getPlatformId());
            ubd.setConnectionString(mr.getConnectionString());
            
            try {
                ubd.setNodeId(Integer.parseInt(mr.getNodeId()));
            } catch (Exception e){
                
            }
            
            // store to database
            this.em.persist(ubd);
        }
        this.em.flush();
        System.out.println("Node database updated");
    }
    
    /**
     * Detects connected nodes via command: motelist -usb -c
     * New detected nodes not present in database are stored. Database is updated
     * when connection of nodes was changed
     * 
     */
    public void detectConnectedNodes(){
        try {
            if (App.getRunningInstance().isDebug()){
                log.info("Debugging mode enabled in USBarbitrator");
            }
            
            // if is map nonempty
            if (this.moteList!=null && this.moteList.isEmpty()==false){
                log.debug("moteList map is nonempty, will be flushed with detect data");
            }
            
            this.loadNodesFromDatabase();
            
            this.moteList = new HashMap<String, NodeConfigRecord>();            
            String motelistCommand = App.getRunningInstance().getMotelistCommand() + " -usb -c";
            log.info("Will use motelist command: " + motelistCommand);
            
            // motelist records
            LinkedList<NodeConfigRecord> mlistRecords = new LinkedList<NodeConfigRecord>();
            // parse udev rules list to complete information - get mapping 
            // USB serial -> device path (created by udev)
            Map<String, NodeConfigRecordLocal> udevConfig = loadUdevRules();
            
            // execute motelist command
            Process p = Runtime.getRuntime().exec(motelistCommand);
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
             while ((line = bri.readLine()) != null) {
                 // process detected motes here                 
                 if (line.startsWith("No devices found")){
                     log.info("No devices was found, return null");
                     break;
                 }
                
                // parse motelist output
                NodeConfigRecord motelistOutput = this.parseMotelistOutput(line);
                
                // if udev device alias present, map it
                if (udevConfig.containsKey(motelistOutput.getSerial())){
                    motelistOutput.setDeviceAlias(udevConfig.get(motelistOutput.getSerial()).getDevice());
                    motelistOutput.setNodeId(udevConfig.get(motelistOutput.getSerial()).getNodeid());
                    
                    // now have device alias, if nonempty it has precedense to 
                    // nodePath for connection string
                    if (motelistOutput.getDeviceAlias()!=null 
                            && motelistOutput.getDeviceAlias().isEmpty()==false){
                        // need to get platform again :(
                        NodePlatform platform = NodePlatformFactory.getPlatform(motelistOutput.getPlatformId());
                        motelistOutput.setConnectionString(platform.getConnectionString(motelistOutput.getDeviceAlias()));
                    }
                }
                
                log.info("MoteRecord: " + motelistOutput.toString());
                
                // add parsed node record to list for further processing
                mlistRecords.add(motelistOutput);
                // put motelist
                this.moteList.put(motelistOutput.getSerial(), motelistOutput);
            }
            bri.close();
            
            // sunchronous call, wait for command completion
            p.waitFor();
            
            // check whether are nodes connected well
            if (App.getRunningInstance().isCheckNodesConnection()){
                this.checkNodesConnection();
            }
            
            // update nodes database, if needed
            if (App.getRunningInstance().isUpdateNodeDatabase()){
                this.updateNodesDatabase();
            }
        } catch (IOException ex) {
            log.error("IOException error, try checking motelist command", ex);
        } catch (InterruptedException ex){
            log.error("Motelist command was probably interrupted", ex);
        }
    }
    
    /**
     * Builds motelist record from one line of motelist output
     * @param output
     * @return 
     */
    protected NodeConfigRecord parseMotelistOutput(String output){
        if (output==null) {
            log.error("Empty line in parseMotelistOutput");
            throw new NullPointerException("Null line");
        }
        
        NodeConfigRecord rec = new NodeConfigRecord();
        String[] split = output.split(",");
        if (split.length != 6){
            log.error("Motelist output is different from expected one, please inspect it: " + output);
            throw new IllegalArgumentException("Line is different as expected - command output probably changed");
        }
        
        rec.setBus(split[0]);
        rec.setDev(split[1]);
        rec.setUsbPath(split[2]);
        rec.setSerial(split[3]);
        rec.setDevicePath(split[4]);
        rec.setDescription(split[5]);
        
        // determine platform ID here, violates abstraction, but now meets KISS
        // principle. Problem: reasonable way how to convert description string
        // to platform, now use platformFactory
        NodePlatform platform = NodePlatformFactory.getPlatform(rec.getDescription());
        rec.setPlatformId(platform.getPlatformId());
        
        // cannot determine connection string here correctly - need to wait for 
        // device file alias from udev. now use simple node
        rec.setConnectionString(platform.getConnectionString(rec.getDevicePath()));
        return rec;
    }
    
    /**
     * Parse udev rules from udev config file = udev config file format could change
     * this is quite temporary method to ease initial db population to detect node 
     * dev aliases
     * 
     * @return Mapping USB serial -> node file
     */
    private Map<String, NodeConfigRecordLocal> loadUdevRules() throws FileNotFoundException, IOException{
        String udevRulesFilePath = App.getRunningInstance().getProps().getProperty("moteUdevRules");
        if (udevRulesFilePath==null || udevRulesFilePath.isEmpty()){
            log.warn("udev rules file path is empty, cannot detect alias nodes");
            return new HashMap<String, NodeConfigRecordLocal>();
        }
        
        // file exists & can read it?
        File udevRulesFile = new File(udevRulesFilePath);
        if (udevRulesFile.exists()==false || udevRulesFile.canRead()==false){
            log.warn("Udev file probably does not exist or cannot be read. File: " + udevRulesFilePath);
            return new HashMap<String, NodeConfigRecordLocal>();
        }
        
        // init returning map
        Map resultMap = new HashMap<String, NodeConfigRecordLocal>();
        
        log.debug("Loading udev configuration");
        
        // open reader for file to read file by lines
        BufferedReader br = new BufferedReader(new FileReader(udevRulesFile));
        
        // we will need to parse config file, compile regex pattern
        Pattern linePattern = Pattern.compile(UDEV_RULES_LINE_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Pattern nodeNumberPattern = Pattern.compile(NODE_ID_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
               
        String strLine;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            // process each line of udev rules config file
            // format (by example): ATTRS{serial}=="XBTO3VKQ", NAME="mote_telos48", MODE="0666"
            // if line does not match to this pattern, it is skipped, lines starting with # are skiped
            // as well since its are comments.
            strLine = strLine.trim();
            // is comment or empty line?
            if (strLine.isEmpty() || strLine.startsWith("#")){
                continue;
            }
            
            // matches pattern?
            Matcher m = linePattern.matcher(strLine);
            
            boolean b = m.matches();
            if(b==false) {
                // no match, different or malformed line
                log.debug("Line was not matched:" + strLine);
                continue;
            }
            
            // matched, extract data
            String group = m.group();           
            if (group==null 
                    || group.isEmpty() 
                    || m.group(1)==null 
                    || m.group(2)==null){
                // mallformed, error
                log.warn("Cannot parse this line of idev config file: " + strLine);
                continue;
            }
            
            String serial = m.group(1);
            String device = m.group(2);
            log.info("Read info from config file: Serial=" + serial + "; device=" + device);
            
            // prepend /dev/ to device
            device = "/dev/" + device;
            
            // conflict check - if duplicate serial occurs, warn user
            if (resultMap.containsKey(serial)){
                log.error("Error occurred, duplicate serial detected in config file, "
                        + "please resolve this issue. Returning empty map. Ambiguation present.");
                log.debug("First record: serial=" + serial + " device=" + resultMap.get(serial));
                log.debug("Second record: serial=" + serial + " device=" + device);
                return new HashMap<String, NodeConfigRecordLocal>();
            }
            
            NodeConfigRecordLocal ncr = new NodeConfigRecordLocal();
            ncr.setDevice(device);
            
            // try to extract node id
            Matcher mId = nodeNumberPattern.matcher(device);
            if (mId.matches() && mId.group(1)!=null){
                log.info("Node ID discovered: " + mId.group(1));
                ncr.setNodeid(mId.group(1));
            }
            
            resultMap.put(serial, ncr);
        }
        //Close the input stream
        br.close();
        
        return resultMap;
    }

    public Map<String, NodeConfigRecord> getMoteList() {
        return moteList;
    }

    public void setMoteList(Map<String, NodeConfigRecord> moteList) {
        this.moteList = moteList;
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public JdbcTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }
    
    private class NodeConfigRecordLocal{
        private String device;
        private String nodeid;

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public String getNodeid() {
            return nodeid;
        }

        public void setNodeid(String nodeid) {
            this.nodeid = nodeid;
        }
    }
}
