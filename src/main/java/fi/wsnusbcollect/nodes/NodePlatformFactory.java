/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

import fi.wsnusbcollect.usb.NodeConfigRecord;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepares node object for specified platform. Now use simple local register.
 * @extension:
 * If needed more abstraction, can use this solution:
 *  - each node platform can be registered as class to this object.
 *  - then can platformId be resolved by HashMap<Int (platformId), NodePlatform>
 * But there is need to perform platform registration at start - too complicated 
 * for my purpose. 
 * @author ph4r05
 */
public class NodePlatformFactory {
    private static final Logger log = LoggerFactory.getLogger(NodePlatformFactory.class);
    
    public static final int NODE_PLATFORM_TELOSB = 1;
    public static final int NODE_PLATFORM_IRIS = 2;
    public static final int NODE_PLATFORM_MICAZ = 3;
    public static final int NODE_PLATFORM_TMOTE = 4;
    public static final int NODE_PLATFORM_WSN430 = 5;
    public static final int NODE_PLATFORM_GENERIC = 0;
    
    // all registered platforms here
    private static Map<Integer, NodePlatform> platforms;
    
    /**
     * Initializes node platform factory.
     * Registers available platforms.
     * 
     * @problem: singleton pattern when not needed? Does it matter when testing or
     * something?
     * @extension: call somebody to init all platforms / add/remove platform runtime
     */
    public static void init(){
        if (NodePlatformFactory.platforms != null){
            // initialized, nothing to do
            return;
        }
        
        // init manually here (KISS)
        NodePlatformFactory.platforms = new HashMap<Integer, NodePlatform>(8);
        platforms.put(Integer.valueOf(NODE_PLATFORM_GENERIC), new NodePlatformGeneric());
        platforms.put(Integer.valueOf(NODE_PLATFORM_IRIS), new NodePlatformIris());
        platforms.put(Integer.valueOf(NODE_PLATFORM_MICAZ), new NodePlatformMicaZ());
        platforms.put(Integer.valueOf(NODE_PLATFORM_TELOSB), new NodePlatformTelosb());
        platforms.put(Integer.valueOf(NODE_PLATFORM_TMOTE), new NodePlatformTmoteSky());
        platforms.put(Integer.valueOf(NODE_PLATFORM_WSN430), new NodePlatformWSN430());
    }
    
    /**
     * Returns node platform determined from device description string.
     * Can be slow - regular expressions, pattern matching, should be used only 
     * on application start (config reading, initialization).
     * 
     * @param description
     * @return 
     */
    public static NodePlatform getPlatform(String description){
        NodePlatformFactory.init();
        // iterate over all registered platforms. If no matches, return generic
        NodePlatform platform = null;
        Iterator<Integer> iterator = platforms.keySet().iterator();
        while(iterator.hasNext()){
            Integer platformId = iterator.next();
            platform = platforms.get(platformId);
            
            if (platform.isPlatformFromNodeDescription(description)){
                break;
            }
        }
        
        if (platform==null){
            // platform was not found, return generic
            return new NodePlatformGeneric();
        } else {
            return platform;
        }
    }
    
    /**
     * Returns node object initialized from NCR
     * @param ncr
     * @return u
     */
    public static NodePlatform getPlatform(NodeConfigRecord ncr){
        return NodePlatformFactory.getPlatform(ncr.getPlatformId());
    }
    
    /**
     * Prepares specified platform node for given platformID
     * @param i
     * @return 
     */
    public static NodePlatform getPlatform(int i){
        NodePlatformFactory.init();
        
        NodePlatform platform = null;
        switch(i){
            case NODE_PLATFORM_TELOSB:
                platform = new NodePlatformTelosb();
                break;
            case NODE_PLATFORM_TMOTE:
                platform = new NodePlatformTmoteSky();
                break;
            case NODE_PLATFORM_MICAZ:
                platform = new NodePlatformMicaZ();
                break;
            case NODE_PLATFORM_IRIS:
                platform = new NodePlatformIris();
                break;              
            case NODE_PLATFORM_GENERIC:
                platform = new NodePlatformGeneric();
                break;
            case NODE_PLATFORM_WSN430:
                platform = new NodePlatformWSN430();
                break;
            default:
                log.warn("SPecified platformId was not bound to platform. PlatformID: " + i);
                platform = new NodePlatformGeneric();
                break;
        }
        
        return platform;
    }
    
    public final static int CONNECTION_SERIAL=1;
    public final static int CONNECTION_SF=2;
    public final static int CONNECTION_NETWORK=3;
    public final static int CONNECTION_NONE=-1;
    
    /**
     * Type of node connection
     * serial@PORT:SPEED 	Serial ports                            1
     * sf@HOST:PORT             SerialForwarder, TMote Connect          2
     * network@HOST:PORT 	MIB 600                                 3
     * 
     * -1 = error
     */
    public static int getNodeConnection(String connectionStr){
        if (connectionStr==null || connectionStr.isEmpty()){
            return CONNECTION_NONE;
        }
        
        if (connectionStr.startsWith("serial@")){
            return CONNECTION_SERIAL;
        }
        
        if (connectionStr.startsWith("sf@")){
            return CONNECTION_SF;
        }
        
        if (connectionStr.startsWith("network@")){
            return CONNECTION_NETWORK;
        }
        
        // default
        return CONNECTION_NONE;
    }
}
