package fi.wsnusbcollect.messages;

/**
 * This class is automatically generated by mig. DO NOT EDIT THIS FILE.
 * This class implements a Java interface to the 'MultiPingResponseMsg'
 * message type.
 */

public class MultiPingResponseMsg extends net.tinyos.message.Message {

    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 4;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 13;

    /** Create a new MultiPingResponseMsg of size 4. */
    public MultiPingResponseMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new MultiPingResponseMsg of the given data_length. */
    public MultiPingResponseMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg with the given data_length
     * and base offset.
     */
    public MultiPingResponseMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store.
     */
    public MultiPingResponseMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public MultiPingResponseMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public MultiPingResponseMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg embedded in the given message
     * at the given base offset.
     */
    public MultiPingResponseMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg embedded in the given message
     * at the given base offset and length.
     */
    public MultiPingResponseMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <MultiPingResponseMsg> \n";
      try {
        s += "  [counter=0x"+Long.toHexString(get_counter())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [rssi=0x"+Long.toHexString(get_rssi())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      return s;
    }

    // Message-type-specific access methods appear below.

    /////////////////////////////////////////////////////////
    // Accessor methods for field: counter
    //   Field type: int, signed
    //   Offset (bits): 0
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'counter' is signed (true).
     */
    public static boolean isSigned_counter() {
        return true;
    }

    /**
     * Return whether the field 'counter' is an array (false).
     */
    public static boolean isArray_counter() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'counter'
     */
    public static int offset_counter() {
        return (0 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'counter'
     */
    public static int offsetBits_counter() {
        return 0;
    }

    /**
     * Return the value (as a int) of the field 'counter'
     */
    public int get_counter() {
        return (int)getUIntBEElement(offsetBits_counter(), 16);
    }

    /**
     * Set the value of the field 'counter'
     */
    public void set_counter(int value) {
        setUIntBEElement(offsetBits_counter(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'counter'
     */
    public static int size_counter() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'counter'
     */
    public static int sizeBits_counter() {
        return 16;
    }

    /////////////////////////////////////////////////////////
    // Accessor methods for field: rssi
    //   Field type: short, signed
    //   Offset (bits): 16
    //   Size (bits): 16
    /////////////////////////////////////////////////////////

    /**
     * Return whether the field 'rssi' is signed (true).
     */
    public static boolean isSigned_rssi() {
        return true;
    }

    /**
     * Return whether the field 'rssi' is an array (false).
     */
    public static boolean isArray_rssi() {
        return false;
    }

    /**
     * Return the offset (in bytes) of the field 'rssi'
     */
    public static int offset_rssi() {
        return (16 / 8);
    }

    /**
     * Return the offset (in bits) of the field 'rssi'
     */
    public static int offsetBits_rssi() {
        return 16;
    }

    /**
     * Return the value (as a short) of the field 'rssi'
     */
    public short get_rssi() {
        return (short)getSIntBEElement(offsetBits_rssi(), 16);
    }

    /**
     * Set the value of the field 'rssi'
     */
    public void set_rssi(short value) {
        setSIntBEElement(offsetBits_rssi(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'rssi'
     */
    public static int size_rssi() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'rssi'
     */
    public static int sizeBits_rssi() {
        return 16;
    }

}
