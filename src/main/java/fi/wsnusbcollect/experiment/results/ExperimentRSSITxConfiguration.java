/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.experiment.results;

/**
 *
 * @author ph4r05
 */
public class ExperimentRSSITxConfiguration{
    private int node;
        private int txpower;
        private int packetSize;

        public ExperimentRSSITxConfiguration() {
        }

        public ExperimentRSSITxConfiguration(int node, int txpower, int packetSize) {
            this.node = node;
            this.txpower = txpower;
            this.packetSize = packetSize;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExperimentRSSITxConfiguration other = (ExperimentRSSITxConfiguration) obj;
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
            int hash = 3;
            hash = 67 * hash + this.node;
            hash = 67 * hash + this.txpower;
            hash = 67 * hash + this.packetSize;
            return hash;
        }

        @Override
        public String toString() {
            return "ExperimentRSSITxConfiguration{" + "node=" + node + ", txpower=" + txpower + ", packetSize=" + packetSize + '}';
        }
        
        /**
         * Gets code for this configuration - to be used sa filename for example
         * @return 
         */
        public String getCode(){
            return node + "-" + txpower + "-" + packetSize;
        }
}
