/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.messages;

/**
 *
 * @author ph4r05
 */
public class NoiseFloorReadingMsg extends net.tinyos.message.Message{
    /** The default size of this message type in bytes. */
    public static final int DEFAULT_MESSAGE_SIZE = 4;

    /** The Active Message type associated with this message. */
    public static final int AM_TYPE = 18;

    /** Create a new MultiPingResponseMsg of size 4. */
    public NoiseFloorReadingMsg() {
        super(DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /** Create a new MultiPingResponseMsg of the given data_length. */
    public NoiseFloorReadingMsg(int data_length) {
        super(data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg with the given data_length
     * and base offset.
     */
    public NoiseFloorReadingMsg(int data_length, int base_offset) {
        super(data_length, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store.
     */
    public NoiseFloorReadingMsg(byte[] data) {
        super(data);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store, with the given base offset.
     */
    public NoiseFloorReadingMsg(byte[] data, int base_offset) {
        super(data, base_offset);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg using the given byte array
     * as backing store, with the given base offset and data length.
     */
    public NoiseFloorReadingMsg(byte[] data, int base_offset, int data_length) {
        super(data, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg embedded in the given message
     * at the given base offset.
     */
    public NoiseFloorReadingMsg(net.tinyos.message.Message msg, int base_offset) {
        super(msg, base_offset, DEFAULT_MESSAGE_SIZE);
        amTypeSet(AM_TYPE);
    }

    /**
     * Create a new MultiPingResponseMsg embedded in the given message
     * at the given base offset and length.
     */
    public NoiseFloorReadingMsg(net.tinyos.message.Message msg, int base_offset, int data_length) {
        super(msg, base_offset, data_length);
        amTypeSet(AM_TYPE);
    }

    /**
    /* Return a String representation of this message. Includes the
     * message type name and the non-indexed field values.
     */
    public String toString() {
      String s = "Message <NoiseFloorReadingMsg> \n";
      try {
        s += "  [counter=0x"+Long.toHexString(get_counter())+"]\n";
      } catch (ArrayIndexOutOfBoundsException aioobe) { /* Skip field */ }
      try {
        s += "  [noise=0x"+Long.toHexString(get_noise())+"]\n";
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
    public short get_noise() {
        return (short)getSIntBEElement(offsetBits_rssi(), 16);
    }

    /**
     * Set the value of the field 'rssi'
     */
    public void get_noise(short value) {
        setSIntBEElement(offsetBits_rssi(), 16, value);
    }

    /**
     * Return the size, in bytes, of the field 'rssi'
     */
    public static int size_noise() {
        return (16 / 8);
    }

    /**
     * Return the size, in bits, of the field 'rssi'
     */
    public static int sizeBits_noise() {
        return 16;
    }
}
