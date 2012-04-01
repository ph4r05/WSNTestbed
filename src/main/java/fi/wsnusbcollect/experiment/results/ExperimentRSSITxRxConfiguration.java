/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment.results;

/**
 *
 * @author ph4r05
 */
public class ExperimentRSSITxRxConfiguration {
    private int rxnode;
        private int node;
        private int txpower;
        private int packetSize;

        public ExperimentRSSITxRxConfiguration(int rxnode, int node, int txpower, int packetSize) {
            this.rxnode = rxnode;
            this.node = node;
            this.txpower = txpower;
            this.packetSize = packetSize;
        }

        public ExperimentRSSITxRxConfiguration( ) {
            
        }

        public ExperimentRSSITxRxConfiguration(ExperimentRSSITxConfiguration tx, int rxnode) {
            this.node = tx.getNode();
            this.packetSize = tx.getPacketSize();
            this.txpower = tx.getTxpower();
            this.rxnode = rxnode;
        }
        
        public int getNode() {
            return node;
        }

        public void setNode(int node) {
            this.node = node;
        }

        public int getPacketSize() {
            return packetSize;
        }

        public void setPacketSize(int packetSize) {
            this.packetSize = packetSize;
        }

        public int getTxpower() {
            return txpower;
        }

        public void setTxpower(int txpower) {
            this.txpower = txpower;
        }

        public int getRxnode() {
            return rxnode;
        }

        public void setRxnode(int rxnode) {
            this.rxnode = rxnode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExperimentRSSITxRxConfiguration other = (ExperimentRSSITxRxConfiguration) obj;
            if (this.rxnode != other.rxnode) {
                return false;
            }
            if (this.node != other.node) {
                return false;
            }
            if (this.txpower != other.txpower) {
                return false;
            }
            if (this.packetSize != other.packetSize) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 43 * hash + this.rxnode;
            hash = 43 * hash + this.node;
            hash = 43 * hash + this.txpower;
            hash = 43 * hash + this.packetSize;
            return hash;
        }

        @Override
        public String toString() {
            return "TxRxConfiguration{" + "rxnode=" + rxnode + ", node=" + node + ", txpower=" + txpower + ", packetSize=" + packetSize + '}';
        }
}
