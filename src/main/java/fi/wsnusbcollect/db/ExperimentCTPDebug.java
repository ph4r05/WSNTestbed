/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.db;

import fi.wsnusbcollect.messages.CollectionDebugMsg;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author ph4r05
 */
@Entity
public class ExperimentCTPDebug implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne
    private ExperimentMetadata experiment;
    private long militime;
    private int node;
    private int nodeBS;
    
    private int type;
    private int arg;
    private int msg_msg_uid;   
    private int msg_origin;
    private int msg_other_node;
    
    private int  route_info_parent;
    private int  route_info_hopcount;
    private int  route_info_metric;
       
    private int  dbg_a;
    private int  dbg_b;
    private int  dbg_c;

    private int seqno;

    // string annotation
    private String anot;
    /**
     * Loads entity from command message
     */
    public void loadFromMessage(CollectionDebugMsg msg){
        this.arg = msg.get_data_arg();
        this.dbg_a = msg.get_data_dbg_a();
        this.dbg_b = msg.get_data_dbg_b();
        this.dbg_c = msg.get_data_dbg_c();
        
        this.msg_msg_uid = msg.get_data_msg_msg_uid();
        this.msg_origin = msg.get_data_msg_origin();
        this.msg_other_node = msg.get_data_msg_other_node();
        
        this.route_info_hopcount = msg.get_data_route_info_hopcount();
        this.route_info_metric = msg.get_data_route_info_metric();
        this.route_info_parent = msg.get_data_route_info_parent();
        
        this.seqno = msg.get_seqno();
        this.type = msg.get_type();
        
        // GENERATED FROM c header file with regex:
        // SEARCH: ([a-zA-Z0-9_]+)\s*=\s*(0x[0-9a-fA-F]+),[ ]*(//([^\n]+))?
        // REPLACE: case $2: this.anot = "$1 $3"; break;
        switch(this.type){
            case 0xDE:
                this.anot = "NET_C_DEBUG_STARTED ";
                break;
            case 0x10:
                this.anot = "NET_C_FE_MSG_POOL_EMPTY //::no args";
                break;
            case 0x11:
                this.anot = "NET_C_FE_SEND_QUEUE_FULL //::no args";
                break;
            case 0x12:
                this.anot = "NET_C_FE_NO_ROUTE //::no args";
                break;
            case 0x13:
                this.anot = "NET_C_FE_SUBSEND_OFF ";
                break;
            case 0x14:
                this.anot = "NET_C_FE_SUBSEND_BUSY ";
                break;
            case 0x15:
                this.anot = "NET_C_FE_BAD_SENDDONE ";
                break;
            case 0x16:
                this.anot = "NET_C_FE_QENTRY_POOL_EMPTY ";
                break;
            case 0x17:
                this.anot = "NET_C_FE_SUBSEND_SIZE ";
                break;
            case 0x18:
                this.anot = "NET_C_FE_LOOP_DETECTED ";
                break;
            case 0x19:
                this.anot = "NET_C_FE_SEND_BUSY ";
                break;
            case 0x50:
                this.anot = "NET_C_FE_SENDQUEUE_EMPTY ";
                break;
            case 0x51:
                this.anot = "NET_C_FE_PUT_MSGPOOL_ERR ";
                break;
            case 0x52:
                this.anot = "NET_C_FE_PUT_QEPOOL_ERR ";
                break;
            case 0x53:
                this.anot = "NET_C_FE_GET_MSGPOOL_ERR ";
                break;
            case 0x54:
                this.anot = "NET_C_FE_GET_QEPOOL_ERR ";
                break;
            case 0x20:
                this.anot = "NET_C_FE_SENT_MSG //:app. send       :msg uid, origin, next_hop";
                break;
            case 0x21:
                this.anot = "NET_C_FE_RCV_MSG //:next hop receive:msg uid, origin, last_hop";
                break;
            case 0x22:
                this.anot = "NET_C_FE_FWD_MSG //:fwd msg         :msg uid, origin, next_hop";
                break;
            case 0x23:
                this.anot = "NET_C_FE_DST_MSG //:base app. recv  :msg_uid, origin, last_hop";
                break;
            case 0x24:
                this.anot = "NET_C_FE_SENDDONE_FAIL ";
                break;
            case 0x25:
                this.anot = "NET_C_FE_SENDDONE_WAITACK ";
                break;
            case 0x26:
                this.anot = "NET_C_FE_SENDDONE_FAIL_ACK_SEND ";
                break;
            case 0x27:
                this.anot = "NET_C_FE_SENDDONE_FAIL_ACK_FWD ";
                break;
            case 0x28:
                this.anot = "NET_C_FE_DUPLICATE_CACHE //dropped duplicate packet seen in cache";
                break;
            case 0x29:
                this.anot = "NET_C_FE_DUPLICATE_QUEUE //dropped duplicate packet seen in queue";
                break;
            case 0x2A:
                this.anot = "NET_C_FE_DUPLICATE_CACHE_AT_SEND //dropped duplicate packet seen in cache";
                break;
            case 0x30:
                this.anot = "NET_C_TREE_NO_ROUTE //:        :no args";
                break;
            case 0x31:
                this.anot = "NET_C_TREE_NEW_PARENT //:        :parent_id, hopcount, metric";
                break;
            case 0x32:
                this.anot = "NET_C_TREE_ROUTE_INFO //:periodic:parent_id, hopcount, metric";
                break;
            case 0x33:
                this.anot = "NET_C_TREE_SENT_BEACON ";
                break;
            case 0x34:
                this.anot = "NET_C_TREE_RCV_BEACON ";
                break;
            case 0x40:
                this.anot = "NET_C_DBG_1 //:any     :uint16_t a";
                break;
            case 0x41:
                this.anot = "NET_C_DBG_2 //:any     :uint16_t a, b, c";
                break;
            case 0x42:
                this.anot = "NET_C_DBG_3 //:any     :uint16_t a, b, c";
                break;
        }
    }
    
    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }

    public int getDbg_a() {
        return dbg_a;
    }

    public void setDbg_a(int dbg_a) {
        this.dbg_a = dbg_a;
    }

    public int getDbg_b() {
        return dbg_b;
    }

    public void setDbg_b(int dbg_b) {
        this.dbg_b = dbg_b;
    }

    public int getDbg_c() {
        return dbg_c;
    }

    public void setDbg_c(int dbg_c) {
        this.dbg_c = dbg_c;
    }

    public ExperimentMetadata getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentMetadata experiment) {
        this.experiment = experiment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getMilitime() {
        return militime;
    }

    public void setMilitime(long militime) {
        this.militime = militime;
    }

    public int getMsg_msg_uid() {
        return msg_msg_uid;
    }

    public void setMsg_msg_uid(int msg_msg_uid) {
        this.msg_msg_uid = msg_msg_uid;
    }

    public int getMsg_origin() {
        return msg_origin;
    }

    public void setMsg_origin(int msg_origin) {
        this.msg_origin = msg_origin;
    }

    public int getMsg_other_node() {
        return msg_other_node;
    }

    public void setMsg_other_node(int msg_other_node) {
        this.msg_other_node = msg_other_node;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public int getNodeBS() {
        return nodeBS;
    }

    public void setNodeBS(int nodeBS) {
        this.nodeBS = nodeBS;
    }

    public int getRoute_info_hopcount() {
        return route_info_hopcount;
    }

    public void setRoute_info_hopcount(int route_info_hopcount) {
        this.route_info_hopcount = route_info_hopcount;
    }

    public int getRoute_info_metric() {
        return route_info_metric;
    }

    public void setRoute_info_metric(int route_info_metric) {
        this.route_info_metric = route_info_metric;
    }

    public int getRoute_info_parent() {
        return route_info_parent;
    }

    public void setRoute_info_parent(int route_info_parent) {
        this.route_info_parent = route_info_parent;
    }

    public int getSeqno() {
        return seqno;
    }

    public void setSeqno(int seqno) {
        this.seqno = seqno;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAnot() {
        return anot;
    }

    public void setAnot(String anot) {
        this.anot = anot;
    }

    @Override
    public String toString() {
        return "ExperimentCTPDebug{" + "id=" + id + ", experiment=" + experiment + ", militime=" + militime + ", node=" + node + ", nodeBS=" + nodeBS + ", type=" + type + ", arg=" + arg + ", msg_msg_uid=" + msg_msg_uid + ", msg_origin=" + msg_origin + ", msg_other_node=" + msg_other_node + ", route_info_parent=" + route_info_parent + ", route_info_hopcount=" + route_info_hopcount + ", route_info_metric=" + route_info_metric + ", dbg_a=" + dbg_a + ", dbg_b=" + dbg_b + ", dbg_c=" + dbg_c + ", seqno=" + seqno + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExperimentCTPDebug other = (ExperimentCTPDebug) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
     
}
