/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.nodeCom;

import java.util.concurrent.ConcurrentHashMap;
import net.tinyos.message.Message;

/**
 * Singleton
 * 
 * @author ph4r05
 */
public class AMType2MessageMapper {
    private static AMType2MessageMapper self=null;
    
    /**
     * AMTYPE -> message mapping
     */
    protected ConcurrentHashMap<Integer, net.tinyos.message.Message> amType2Message;
    
    /**
     * hidden constructor for singleton pattern
     */
    private AMType2MessageMapper(){
        // 
    }
    
    /**
     * Singleton generator
     * @return 
     */
    public static AMType2MessageMapper getInstance(){
        if (self!=null){
            return self;
        }
        
        // else instantiate new object
        self = new AMType2MessageMapper();
        self.amType2Message = new ConcurrentHashMap<Integer, Message>(2);
        return self;
    }
    
    /**
     * Stores new message to register
     * @param msg 
     */
    public void registerMessage(Message msg){
        if (msg==null){
            throw new NullPointerException("Cannot register null message");
        }
        
        // already in?
        int amtype = msg.amType();
        if (this.amType2Message.containsKey(amtype)){
            return;
        }
        
        this.amType2Message.put(amtype, msg);
    }
    
    /**
     * Returns whether contains AMtype mapping
     * @param amtype
     * @return 
     */
    public boolean hasAMType(int amtype){
        return this.amType2Message.containsKey(amtype);
    }
    
    /**
     * Returns message mapped by this amtype. Null of message is not registered
     * @param amtype
     * @return 
     */
    public Message getMessage(int amtype){
        if (this.hasAMType(amtype)==false) return null;
        return this.amType2Message.get(amtype);
    }
    
}
