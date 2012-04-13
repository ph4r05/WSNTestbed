/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import com.csvreader.CsvWriter;
import java.io.IOException;

/**
 *
 * @author ph4r05
 */
public interface DataCSVWritable extends FileWritable{
    public void writeCSVheader(CsvWriter csvOutput) throws IOException;
    public void writeCSVdata(CsvWriter csvOutput) throws IOException;
    public String getCSVname();
}
