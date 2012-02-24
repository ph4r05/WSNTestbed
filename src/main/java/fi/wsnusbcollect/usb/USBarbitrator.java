/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.usb;

import fi.wsnusbcollect.App;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ph4r05
 */
public class USBarbitrator {
    private static final Logger log = LoggerFactory.getLogger(USBarbitrator.class);
    private static final String UDEV_RULES_LINE_PATTERN = "^ATTRS\\{serial\\}\\s*==\\s*\\\"([0-9a-zA-Z_]+)\\\",\\s*NAME\\s*=\\s*\\\"([0-9a-zA-Z_]+)\\\".*";
    
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
            
            String motelistCommand = App.getRunningInstance().getMotelistCommand() + " -usb -c";
            log.info("Will use motelist command: " + motelistCommand);
            
            // motelist records
            LinkedList<MotelistRecord> mlistRecords = new LinkedList<MotelistRecord>();
            
            // parse udev rules list to complete information - get mapping 
            // USB serial -> device path (created by udev)
            loadUdevRules();
            
            // execute motelist command
            Process p = Runtime.getRuntime().exec(motelistCommand);
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
             while ((line = bri.readLine()) != null) {
                 // process detected motes here                 
                 log.info("Output from motelist: " + line);
                 System.out.println(line);
                 
                 if (line.startsWith("No devices found")){
                     log.info("No devices was found, return null");
                     break;
                 }
                 
                MotelistRecord motelistOutput = this.parseMotelistOutput(line);
                log.info(motelistOutput.toString());
                
                mlistRecords.add(motelistOutput);
            }
            bri.close();
            
            // sunchronous call
            p.waitFor();
            
            
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
    private MotelistRecord parseMotelistOutput(String output){
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
     */
    public void loadUdevRules() throws FileNotFoundException, IOException{
        String udevRulesFilePath = App.getRunningInstance().getProps().getProperty("moteUdevRules");
        if (udevRulesFilePath==null || udevRulesFilePath.isEmpty()){
            log.warn("udev rules file path is empty, cannot detect alias nodes");
            return;
        }
        
        // file exists & can read it?
        File udevRulesFile = new File(udevRulesFilePath);
        if (udevRulesFile.exists()==false || udevRulesFile.canRead()==false){
            log.warn("Udev file probably does not exist or cannot be read. File: " + udevRulesFilePath);
            return;
        }
        
        log.debug("Loading udev configuration");
        
        // open reader for file to read file by lines
        BufferedReader br = new BufferedReader(new FileReader(udevRulesFile));
        
        // we will need to parse config file, compile regex pattern
        Pattern linePattern = Pattern.compile(UDEV_RULES_LINE_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        log.debug("Mathching against pattern: " + UDEV_RULES_LINE_PATTERN);
        
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
            log.info("Serial=" + serial + "; device=" + device);
        }
        //Close the input stream
        br.close();
    }
}
