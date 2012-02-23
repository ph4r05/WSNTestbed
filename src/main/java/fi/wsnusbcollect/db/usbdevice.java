/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;


/**
 *
 * @author Miro
 */
@Entity
public class usbdevice implements Serializable{
    private static final long serialVersionUID = 112312352;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;    
    
    @ManyToOne(cascade={CascadeType.REFRESH})
    @JoinColumn(name = "owner_id")  
       
//    @ManyToMany(cascade={CascadeType.REFRESH},fetch= FetchType.LAZY)
//    private Set<Category> categories = new HashSet<Category>();
    
    private String firstName;
    
    private String secondName;
    
    private String company;
    
    private String email;
    
    private String address;
    
    private String telephone;
    
    private boolean publicVisibility;

    @Lob
    private String description;

//    @MapKeyEnumerated(EnumType.STRING)
//    @OneToMany(cascade=CascadeType.ALL)
//    @JoinColumn(name = "image_id")
//    private Map<ImageVariant,Image> images;// = new EnumMap<ImageVariant, Image>(ImageVariant.class);

    @ElementCollection   
    @MapKeyColumn(name="name")
    @Column(name="value")
    //@Field
    //@IndexedEmbedded(depth = 1, prefix = "properties_")
    private Map<String,String> properties = new HashMap<String, String>();
    
    
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String name, String value){
        this.properties.put(name, value);        
    }
    public String getProperty(String name){
        return this.properties.get(name);
    }
    public void removeProperty(String name){
        this.properties.remove(name);                
    }
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public usbdevice() {      
        
    }

    

    @Override
    public String toString() {
        return "Card: " + this.firstName + "|" + this.secondName + "|" + email + "|" + address;        
    }
}
