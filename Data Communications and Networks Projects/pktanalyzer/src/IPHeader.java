import java.util.Arrays;

/**
 * The IPHeader class represents the IP header section of a packet.
 *
 * @author Steven Yu (sky3947)
 */
public class IPHeader extends Header {

    private static final int VERSION_HEADER_LENGTH = 0;     // Byte location where the version and header length start.
    private static final int DSCP_ECN = 1;                  // Byte location where DSCP and ECN start.
    private static final int TOTAL_LENGTH = 2;              // Byte location where total length starts.
    private static final int IDENTIFICATION = 4;            // Byte location where identification starts.
    private static final int FLAGS_FRAGMENT_OFFSET = 6;     // Byte location where flags and fragment offset start.
    private static final int TIME_TO_LIVE = 8;              // Byte location where TTL starts.
    private static final int PROTOCOL = 9;                  // Byte location where protocol starts.
    private static final int HEADER_CHECKSUM = 10;          // Byte location where header checksum starts.
    private static final int SOURCE_ADDRESS = 12;           // Byte location where source address starts.
    private static final int DESTINATION_ADDRESS = 16;      // Byte location where destination address starts.
    private static final int OPTIONS = 20;                  // Byte location where options start.

    private int version;
    private int headerLength;
    private byte dscp_ecn;                  // For toString aesthetic purposes only
    private byte dscp;
    private byte ecn;
    private int totalLength;
    private int identification;
    private byte flags;
    private byte rawFlags;                  // For toString aesthetic purposes only
    private int fragmentOffset;
    private int ttl;
    private int protocol;
    private byte[] headerChecksum;
    private byte[] sourceAddress;
    private byte[] destinationAddress;
    private boolean hasOptions;

    private Header subHeader = null;        // Set depending on a packet's protocol

    /**
     * Constructor for IPHeader. It extracts information from a packet.
     *
     * @param raw The raw packet in a byte array.
     */
    public IPHeader(byte[] raw) {
        super("IP");

        // Extract IP header information.
        byte[] ver_hl = Utility.splitByte(raw[VERSION_HEADER_LENGTH]);
        this.version = Utility.byteToInt(ver_hl[0]);
        this.headerLength = 4 * Utility.byteToInt(ver_hl[1]);
        this.dscp_ecn = raw[DSCP_ECN];
        byte[] dscp_ecn_split = Utility.splitByte(dscp_ecn, 6);
        this.dscp = dscp_ecn_split[0];
        this.ecn = dscp_ecn_split[1];
        this.totalLength = Utility.byteToInt(Arrays.copyOfRange(raw, TOTAL_LENGTH, IDENTIFICATION));
        this.identification = Utility.byteToInt(Arrays.copyOfRange(raw, IDENTIFICATION, FLAGS_FRAGMENT_OFFSET));
        byte[] flags_fragoffset = Utility.splitByte(raw[FLAGS_FRAGMENT_OFFSET], 3);
        this.rawFlags = raw[FLAGS_FRAGMENT_OFFSET];
        this.flags = flags_fragoffset[0];
        this.fragmentOffset = Utility.byteToInt(Utility.combine(flags_fragoffset[1], Arrays.copyOfRange(raw, FLAGS_FRAGMENT_OFFSET + 1, TIME_TO_LIVE)));
        this.ttl = Utility.byteToInt(Arrays.copyOfRange(raw, TIME_TO_LIVE, PROTOCOL));
        this.protocol = Utility.byteToInt(Arrays.copyOfRange(raw, PROTOCOL, HEADER_CHECKSUM));
        this.headerChecksum = Arrays.copyOfRange(raw, HEADER_CHECKSUM, SOURCE_ADDRESS);
        this.sourceAddress = Arrays.copyOfRange(raw, SOURCE_ADDRESS, DESTINATION_ADDRESS);
        this.destinationAddress = Arrays.copyOfRange(raw, DESTINATION_ADDRESS, OPTIONS);
        this.hasOptions = (headerLength > 20);

        // Figure out protocol type.
        switch (protocol) {
            case Utility.Protocols.ICMP:
                this.subHeader = new ICMPHeader(Arrays.copyOfRange(raw, headerLength, raw.length));
                break;
            case Utility.Protocols.TCP:
                this.subHeader = new TCPHeader(Arrays.copyOfRange(raw, headerLength, raw.length));
                break;
            case Utility.Protocols.UDP:
                this.subHeader = new UDPHeader(Arrays.copyOfRange(raw, headerLength, raw.length));
                break;
            default:
                // Unsupported protocol: subHeader == null
        }
    }

    /**
     * Creates a String representation of this IPHeader. Includes version, header length, DSCP, ECN, total length,
     * identification, flags, fragment offset, time to live, protocol, header checksum, source address, destination
     * address, existence of options, and its data.
     *
     * @return A string representation of this EtherHeader.
     */
    @Override
    public String toString() {
        byte[] temp = Utility.splitByte(Utility.splitByte(flags, 6)[1], 7);
        byte fragment = temp[0];
        byte more = temp[1];
        int i_fragment = Utility.byteToInt(fragment);
        int i_more = Utility.byteToInt(more);

        return buildHeader("IP Header",
                String.format("Version             = %d", version),
                String.format("Header length       = %d bytes", headerLength),
                String.format("DSCP and ECN field  = %s", Utility.byteToHexString(dscp_ecn, Utility.HexStringType.NORMAL)),
                String.format("\txxxx xx.. = %d (DSCP)", Utility.byteToInt(dscp)),
                String.format("\t.... ..xx = %d (ECN)", Utility.byteToInt(ecn)),
                String.format("Total length        = %d bytes", totalLength),
                String.format("Identification      = %d", identification),
                String.format("Flags               = %s", Utility.byteToHexString(rawFlags, Utility.HexStringType.NORMAL)),
                String.format("\t.%d.. .... = %s fragment", i_fragment, (i_fragment == 0) ? "OK to" : "do not"),
                String.format("\t..%d. .... = %s fragment(s)", i_more, (i_more == 0) ? "last" : "more"),
                String.format("Fragment offset     = %d bytes", fragmentOffset),
                String.format("Time to live        = %d seconds/hops", ttl),
                String.format("Protocol            = %d (%s)", protocol, (subHeader == null) ? "unknown" : subHeader.getPrefix()),
                String.format("Header checksum     = %s", Utility.byteToHexString(headerChecksum, Utility.HexStringType.NORMAL)),
                String.format("Source address      = %s", Utility.byteToIPAddress(sourceAddress)),
                String.format("Destination address = %s", Utility.byteToIPAddress(destinationAddress)),
                (hasOptions) ? "Has options" : "No options") +

                // Now, get the protocol's header
                ((subHeader == null) ? "" : subHeader.toString());
    }
}
