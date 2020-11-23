import java.util.Arrays;

/**
 * The EtherHeader class represents the ether header section of a packet.
 *
 * @author Steven Yu (sky3947)
 */
public class EtherHeader extends Header {

    private static final int DESTINATION_MAC = 0;   // Byte location where the destination MAC address starts.
    private static final int SOURCE_MAC = 6;        // Byte location where the source MAC address starts.
    private static final int ETHERTYPE = 12;        // Byte location where the ethertype starts.
    private static final int PAYLOAD = 14;          // Byte location where the payload starts.

    private int packetSize;
    private byte[] destinationMac;
    private byte[] sourceMac;
    private byte[] ethertype;

    /**
     * Constructor for EtherHeader. It extracts header information from a packet.
     *
     * @param raw The raw packet in a byte array.
     */
    public EtherHeader(byte[] raw) {
        super("ETHER");    // The prefix for printing ether information

        // Extract ether header information
        this.packetSize = raw.length;
        this.destinationMac = Arrays.copyOfRange(raw, DESTINATION_MAC, SOURCE_MAC);
        this.sourceMac = Arrays.copyOfRange(raw, SOURCE_MAC, ETHERTYPE);
        this.ethertype = Arrays.copyOfRange(raw, ETHERTYPE, PAYLOAD);
    }

    /**
     * Getter method for ethertype.
     *
     * @return The ethertype in a byte array.
     */
    public byte[] getEthertype() {
        return ethertype;
    }

    /**
     * Creates a string representation of this EtherHeader. Includes packet size, destination, source, and ethertype.
     *
     * @return A string representation of this EtherHeader.
     */
    @Override
    public String toString() {
        return buildHeader("Ether Header",
                String.format("Packet size = %d bytes", packetSize),
                String.format("Destination = %s", Utility.byteToHexString(destinationMac, Utility.HexStringType.COLON)),
                String.format("Source      = %s", Utility.byteToHexString(sourceMac, Utility.HexStringType.COLON)),
                String.format("Ethertype   = %s (%s)", Utility.byteToHexString(ethertype, Utility.HexStringType.NO_PREFIX), Utility.getEtherType(ethertype)));
    }
}
