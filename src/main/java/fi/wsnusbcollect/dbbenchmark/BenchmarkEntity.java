/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbbenchmark;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 *
 * @author ph4r05
 */
@Entity
@Table(name="BenchmarkEntity")
@org.hibernate.annotations.Entity(
		dynamicInsert = true
)
public class BenchmarkEntity implements Serializable {
//    @GenericGenerator(name="table-hilo-generator", strategy="org.hibernate.id.TableHiLoGenerator",
//                    parameters={@Parameter(value="hibernate_id_generation", name="table")})
    @Id
//    @GeneratedValue(strategy=GenerationType.TABLE, generator="tbl-gen")
//    @TableGenerator(name="tbl-gen", pkColumnName="BenchmarkEntity", allocationSize=100, table="GENERATORS")
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
//    @GeneratedValue(generator="table-hilo-generator")
    @GeneratedValue(strategy=GenerationType.TABLE)
    private Long id;
      
    private int d1;
    private int d2;
    private int d3;
    private int d4;

    public int getD1() {
        return d1;
    }

    public void setD1(int d1) {
        this.d1 = d1;
    }

    public int getD2() {
        return d2;
    }

    public void setD2(int d2) {
        this.d2 = d2;
    }

    public int getD3() {
        return d3;
    }

    public void setD3(int d3) {
        this.d3 = d3;
    }

    public int getD4() {
        return d4;
    }

    public void setD4(int d4) {
        this.d4 = d4;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
