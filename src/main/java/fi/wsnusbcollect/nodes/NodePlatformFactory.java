/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.nodes;

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
    public static final int NODE_PLATFORM_GENERIC = 0;
    
    /**
     * Prepares specified platform node for given platformID
     * @param i
     * @return 
     */
    public static NodePlatform getPlatform(int i){
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
                platform = new NodePlatformIris();
                break;
            default:
                log.warn("SPecified platformId was not bound to platform. PlatformID: " + i);
                platform = new NodePlatformIris();
                break;
        }
        
        return platform;
    }
}
