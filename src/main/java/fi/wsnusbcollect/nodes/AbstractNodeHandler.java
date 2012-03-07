/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodes;

/**
 *
 * @author ph4r05
 */
abstract public class AbstractNodeHandler implements NodeHandler{
    public static final int NODE_HANDLER_CONNECTED=1;
    public static final int NODE_HANDLER_REMOTE=2;
    
    @Override
    public void shutdown() {
        // nothing by default
    }

    @Override
    public void close() {
        // nothing by default
    }
}
