/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbManager;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import fi.wsnusbcollect.db.ExperimentMetadata;

/**
 * XStream converter to convert Experiment objects to its ID only
 * @author ph4r05
 */
public class ExperimentConverter implements Converter{

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        ExperimentMetadata meta = (ExperimentMetadata) o;
        if (meta==null){
            writer.setValue("-1");
        } else {
            writer.setValue(String.valueOf(meta.getId()));
        }
        
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        ExperimentMetadata meta = new ExperimentMetadata();
        meta.setId(Long.parseLong(reader.getValue()));
        return meta;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(ExperimentMetadata.class);
    }
}
