import java.util.Arrays;

/**
 * The UDPHeader class represents the UDP section of a packet, assuming the packet uses UDP protocol.
 *
 * @author Steven Yu (sky3947)
 */
public class UDPHeader extends Header {

    private static final int SOURCE_PORT = 0;                           // Byte location where the source port starts.
    private static final int DESTINATION_PORT = 2;                      // Byte location where the destination port starts.
    private static final int LENGTH = 4;                                // Byte location where the length of header and data starts.
    private static final int CHECKSUM = 6;                              // Byte location where the checksum starts.
    private static final int DATA = 8;                                  // Byte location where the data starts.

    private static final int MIN_PKT_LENGTH = 8;                        // Minimum length of packet.

    private int sourcePort;
    private int destinationPort;
    private int length;
    private byte[] checksum;

    private Data data = null;

    /**
     * The constructor for UDPHeader. It extracts information from a packet.
     *
     * @param raw The raw packet in a byte array.
     */
    public UDPHeader(byte[] raw) {
        super("UDP");

        // Extract UDP header information.
        this.sourcePort = Utility.byteToInt(Arrays.copyOfRange(raw, SOURCE_PORT, DESTINATION_PORT));
        this.destinationPort = Utility.byteToInt(Arrays.copyOfRange(raw, DESTINATION_PORT, LENGTH));
        this.length = Utility.byteToInt(Arrays.copyOfRange(raw, LENGTH, CHECKSUM));
        this.checksum = Arrays.copyOfRange(raw, CHECKSUM, DATA);

        if(raw.length > MIN_PKT_LENGTH) {
            this.data = new Data(Arrays.copyOfRange(raw, DATA, raw.length));
        }
    }

    /**
     * Creates a String representation of this UDPHeader. Includes source port, destination port, length, checksum, and
     * its data.
     *
     * @return A String representation of this UDPHeader.
     */
    @Override
    public String toString() {
        return buildHeader("UDP Header",
                String.format("Source port      = %d", sourcePort),
                String.format("Destination port = %d", destinationPort),
                String.format("Length           = %d bytes", length),
                String.format("Checksum         = %s", Utility.byteToHexString(checksum, Utility.HexStringType.NORMAL))) +

                // Now, get the data
                ((data == null) ? "" : data.toString());
    }
}
