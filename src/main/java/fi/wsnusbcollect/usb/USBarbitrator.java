package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.App;
import fi.wsnusbcollect.db.USBdevice;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

/**
 * USB arbitrator class - manages connected nodes
 * @author ph4r05
 */
@Repository
public class USBarbitrator {
    private static final Logger log = LoggerFactory.getLogger(USBarbitrator.class);
    private static final String UDEV_RULES_LINE_PATTERN = "^ATTRS\\{serial\\}\\s*==\\s*\\\"([0-9a-zA-Z_]+)\\\",\\s*NAME\\s*=\\s*\\\"([0-9a-zA-Z_]+)\\\".*";
    private static final String NODE_ID_PATTERN = ".*?([0-9]+)$";
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    // serial->motelist record association
    private Map<String, MotelistRecord> moteList = null;

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
    
    public void updateNodesDatabase(){
        if (this.moteList==null || this.moteList.isEmpty()){
            log.info("Cannot update node database - no local data");
            return;
        }
        
        // update nodes if applicable
        // serial -> usbdevice
        Map<String, USBdevice> savedNodes = this.loadNodesFromDatabase();
        
        Iterator<String> iterator = this.moteList.keySet().iterator();
        while(iterator.hasNext()){
            String nodeSerial = iterator.next();
            MotelistRecord mr = this.moteList.get(nodeSerial);
            log.info("Saving mote record: " + mr.toString());
            
            USBdevice ubd = new USBdevice();
            if (savedNodes.containsKey(mr.getSerial())){
                log.info("Updating old record");
                ubd = savedNodes.get(mr.getSerial());
            } else {
                log.info("Inserting new record");
            }

            ubd.setBus(mr.getBus());
            ubd.setDeviceAlias(mr.getDeviceAlias());
            ubd.setDevicePath(mr.getDevicePath());
            ubd.setLastModification(new Date());
            ubd.setSerial(mr.getSerial());
            ubd.setUsbPath(mr.getUsbPath());
            
            try {
                ubd.setNodeId(Integer.parseInt(mr.getNodeId()));
            } catch (Exception e){
                
            }
            
            // store to database
            this.em.persist(ubd);
        }
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
            
            this.moteList = new HashMap<String, MotelistRecord>();            
            String motelistCommand = App.getRunningInstance().getMotelistCommand() + " -usb -c";
            log.info("Will use motelist command: " + motelistCommand);
            
            // motelist records
            LinkedList<MotelistRecord> mlistRecords = new LinkedList<MotelistRecord>();
            // parse udev rules list to complete information - get mapping 
            // USB serial -> device path (created by udev)
            Map<String, NodeConfigRecord> udevConfig = loadUdevRules();
            
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
                MotelistRecord motelistOutput = this.parseMotelistOutput(line);
                
                // if udev device alias present, map it
                if (udevConfig.containsKey(motelistOutput.getSerial())){
                    motelistOutput.setDeviceAlias(udevConfig.get(motelistOutput.getSerial()).getDevice());
                    motelistOutput.setNodeId(udevConfig.get(motelistOutput.getSerial()).getNodeid());
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
            
            // update nodes database
            this.updateNodesDatabase();
            
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
    protected MotelistRecord parseMotelistOutput(String output){
        if (output==null) {
            log.error("Empty line in parseMotelistOutput");
            throw new NullPointerException("Null line");
        }
        
        MotelistRecord rec = new MotelistRecord();
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
        return rec;
    }
    
    /**
     * Parse udev rules from udev config file = udev config file format could change
     * this is quite temporary method to ease initial db population to detect node 
     * dev aliases
     * 
     * @return Mapping USB serial -> node file
     */
    protected Map<String, NodeConfigRecord> loadUdevRules() throws FileNotFoundException, IOException{
        String udevRulesFilePath = App.getRunningInstance().getProps().getProperty("moteUdevRules");
        if (udevRulesFilePath==null || udevRulesFilePath.isEmpty()){
            log.warn("udev rules file path is empty, cannot detect alias nodes");
            return new HashMap<String, NodeConfigRecord>();
        }
        
        // file exists & can read it?
        File udevRulesFile = new File(udevRulesFilePath);
        if (udevRulesFile.exists()==false || udevRulesFile.canRead()==false){
            log.warn("Udev file probably does not exist or cannot be read. File: " + udevRulesFilePath);
            return new HashMap<String, NodeConfigRecord>();
        }
        
        // init returning map
        Map resultMap = new HashMap<String, NodeConfigRecord>();
        
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
                return new HashMap<String, NodeConfigRecord>();
            }
            
            NodeConfigRecord ncr = new NodeConfigRecord();
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

    public Map<String, MotelistRecord> getMoteList() {
        return moteList;
    }

    public void setMoteList(Map<String, MotelistRecord> moteList) {
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
    
    private class NodeConfigRecord{
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
