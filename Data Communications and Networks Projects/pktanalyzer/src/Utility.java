import java.util.Arrays;

/**
 * The Utility class provides static functions for various formatting and bitwise tasks.
 *
 * @author Steven Yu (sky3947)
 */
public class Utility {

    public static final int BYTE_LENGTH = 8;

    /**
     * Turns a byte array into an int.
     * Assumes big endian and 4 byte int size.
     *
     * @param bytes The bytes to turn into an int.
     * @return An int created by putting together the bytes.
     */
    public static int byteToInt(byte[] bytes) {
        int result = 0;

        for(int i = 0; i < bytes.length; i++) {
            result |= ((bytes[i] & 0xff) << (8 * (bytes.length - i - 1)));
        }
        return result;
    }

    /**
     * Overloads byteToInt function to accept a single byte.
     *
     * @param data The byte to turn into an int.
     * @return An int created from the byte.
     */
    public static int byteToInt(byte data) {
        return byteToInt(new byte[]{data});
    }

    /**
     * Supported formats of printing hex
     */
    public enum HexStringType {
        /**
         * Normal hex printing: 0xabcd
         */
        NORMAL,

        /**
         * Print hex with colon delimiters: ab:cd
         */
        COLON,

        /**
         * Normal hex printing without the prefix: abcd
         */
        NO_PREFIX
    }

    /**
     * Turns hex into string in the form requested.
     *
     * @param bytes The hex to turn into string.
     * @param type The format for printing the hex.
     * @return String representation of the given hex.
     */
    public static String byteToHexString(byte[] bytes, HexStringType type) {
        String output = "";
        switch (type) {
            case COLON:
                if(bytes.length > 0) {
                    output += String.format("%02x", bytes[0]);

                    for(int i = 1; i < bytes.length; i++) {
                        output += String.format(":%02x", bytes[i]);
                    }
                }
                break;

            case NO_PREFIX:
                for(byte data : bytes) {
                    output += String.format("%02x", data);
                }
                break;

            default:    // Assume HexStringType NORMAL
                if(bytes.length > 0) {
                    output = "0x";

                    for(byte data : bytes) {
                        output += String.format("%02x", data);
                    }
                }
        }

        return output;
    }

    /**
     * Turns hex into a decimal IP address.
     *
     * @param bytes The hex to turn into a decimal IP address.
     * @return Decimal IP address representation of the given hex.
     */
    public static String byteToIPAddress(byte[] bytes) {
        String output = "";
        if(bytes.length > 0) {
            output += byteToInt(bytes[0]);

            for(int i = 1; i < bytes.length; i++) {
                output += "." + byteToInt(bytes[i]);
            }
        }

        return output;
    }

    /**
     * Overloaded byteToHexString function to accept a single byte.
     *
     * @param data The hex byte to turn into String.
     * @param type The format for printing the hex.
     * @return String representation of the given hex.
     */
    public static String byteToHexString(byte data, HexStringType type) {
        return byteToHexString(new byte[]{data}, type);
    }

    private static final byte[] IP = new byte[]{(byte) 0x08, (byte) 0x00};      // The hex value for IP ethertype
    private static final byte[] ARP = new byte[]{(byte) 0x08, (byte) 0x06};     // The hex value for ARP ethertype

    /**
     * Figures out the name of the given ethertype
     *
     * @param ethertype The ethertype (in bytes) to analyze.
     * @return The name of the ethertype.
     */
    public static String getEtherType(byte[] ethertype) {
        if(Arrays.equals(ethertype, IP)) {
            return "IP";
        } else if(Arrays.equals(ethertype, ARP)) {
            return "ARP";
        } else if(byteToInt(ethertype) <= 1500) {
            return "Length Field";
        } else {
            return "Unknown Type";
        }
    }

    /**
     * Splits a byte into a byte array of two bytes.
     *
     * @param data The byte to split.
     * @param split The number of bits in the first piece.
     * @return A byte array containing the split byte.
     */
    public static byte[] splitByte(byte data, int split) {
        byte piece1 = (byte) ((data & 0xff) >>> (BYTE_LENGTH - split));
        byte piece2 = (byte) (( ( (byte)((data & 0xff) << split) ) & 0xff) >>> split);

        return new byte[]{piece1, piece2};
    }

    /**
     * Overload splitByte method with a default split of four bits.
     *
     * @param data The byte to split.
     * @return A byte array containing the split byte.
     */
    public static byte[] splitByte(byte data) {
        return splitByte(data, BYTE_LENGTH/2);
    }

    /**
     * Stores individual bits in their own byte.
     *
     * @param bytes The byte array to break up.
     * @return A byte array of singular bits (i.e. 0000000x).
     */
    public static byte[] toBits(byte[] bytes) {
        byte[] bits = new byte[bytes.length * BYTE_LENGTH];

        for(int i = 0; i < bytes.length; i++) {
            for(int j = 0; j < BYTE_LENGTH; j++) {
                bits[i*BYTE_LENGTH + j] = splitByte(splitByte(bytes[i], j)[1], j+1)[0];
            }
        }
        return bits;
    }

    /**
     * Overloads toBits method to accept a single byte.
     *
     * @param data The byte to break up.
     * @return A byte array of singular bits (i.e. 0000000x).
     */
    public static byte[] toBits(byte data) {
        return toBits(new byte[]{data});
    }

    /**
     * Concatenates two byte arrays into one.
     *
     * @param a The first byte array.
     * @param b The second byte array.
     * @return The combined byte array.
     */
    public static byte[] combine(byte[] a, byte[] b) {
        byte[] ab = new byte[a.length + b.length];

        int pos = 0;
        for (byte data : a) {
            ab[pos] = data;
            pos++;
        }
        for (byte data : b) {
            ab[pos] = data;
            pos++;
        }

        return ab;
    }

    /**
     * Overloaded combine function to accept a single byte for param a.
     *
     * @param a The first byte.
     * @param b The byte array.
     * @return The combined byte array.
     */
    public static byte[] combine(byte a, byte[] b) {
        return combine(new byte[]{a}, b);
    }

    /**
     * Overloaded combine function to accept single bytes for both parameters.
     *
     * @param a The first byte.
     * @param b The second byte.
     * @return The combined byte array.
     */
    public static byte[] combine(byte a, byte b) {
        return combine(new byte[]{a}, new byte[]{b});
    }

    /**
     * A list of tracked packet types.
     */
    public static class Protocols {
        public static final int ICMP = 1;
        public static final int TCP = 6;
        public static final int UDP = 17;
    }

    /**
     * A list of tracked ICMP types.
     */
    public static class ICMPTypes {
        public static final int ECHO_REPLAY = 0;
        public static final int DEST_UNREACHABLE = 3;
        public static final int SOURCE_QUENCH = 4;
        public static final int REDIRECT_MESSAGE = 5;
        public static final int ECHO_REQUEST = 8;
        public static final int ROUTER_AD = 9;
        public static final int ROUTER_SO = 10;
        public static final int TIME_EXCEEDED = 11;
        public static final int TRACEROUTE = 30;
    }
}
