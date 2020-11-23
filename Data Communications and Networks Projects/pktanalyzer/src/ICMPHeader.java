import java.util.Arrays;

/**
 * The ICMPHeader class represents the ICMP section of a packet, assuming the packet uses ICMP protocol.
 *
 * @author Steven Yu (sky3947)
 */
public class ICMPHeader extends Header {

    private static final int TYPE = 0;          // Byte location where type starts.
    private static final int CODE = 1;          // Byte location where code starts.
    private static final int CHECKSUM = 2;      // Byte location where checksum starts.
    private static final int REST = 4;          // Byte location where rest of header starts.
    private static final int DATA = 8;          // Byte location where data starts.

    private static final int MIN_PKT_LENGTH = 8;    // Minimum length of packet.

    private int type;
    private int code;
    private byte[] checksum;

    private Data data = null;

    /**
     * The constructor for ICMPHeader. It extract information from a packet.
     *
     * @param raw The raw packet in a byte array.
     */
    public ICMPHeader(byte[] raw) {
        super("ICMP");

        // Extract ICMP header information.
        this.type = Utility.byteToInt(Arrays.copyOfRange(raw, TYPE, CODE));
        this.code = Utility.byteToInt(Arrays.copyOfRange(raw, CODE, CHECKSUM));
        this.checksum = Arrays.copyOfRange(raw, CHECKSUM, REST);

        if(raw.length > MIN_PKT_LENGTH) {
            this.data = new Data(Arrays.copyOfRange(raw, DATA, raw.length));
        }
    }

    /**
     * Creates a String representation of this ICMPHeader. Includes type, code, and checksum.
     *
     * @return A String representation of this ICMPHeader.
     */
    @Override
    public String toString() {
        String typeName;

        switch (type) {
            case Utility.ICMPTypes.ECHO_REPLAY:
                typeName = "Echo replay";
                break;
            case Utility.ICMPTypes.DEST_UNREACHABLE:
                typeName = "Destination unreachable";
                break;
            case Utility.ICMPTypes.SOURCE_QUENCH:
                typeName = "Source Quench";
                break;
            case Utility.ICMPTypes.REDIRECT_MESSAGE:
                typeName = "Redirect message";
                break;
            case Utility.ICMPTypes.ECHO_REQUEST:
                typeName = "Echo request";
                break;
            case Utility.ICMPTypes.ROUTER_AD:
                typeName = "Router advertisement";
                break;
            case Utility.ICMPTypes.ROUTER_SO:
                typeName = "Router solicitation";
                break;
            case Utility.ICMPTypes.TIME_EXCEEDED:
                typeName = "Time exceeded";
                break;
            case Utility.ICMPTypes.TRACEROUTE:
                typeName = "Traceroute";
                break;
            default:
                typeName = "Unknown";
        }

        return buildHeader("ICMP Header",
                String.format("Type     = %d (%s)", type, typeName),
                String.format("Code     = %d", code),
                String.format("Checksum = %s", Utility.byteToHexString(checksum, Utility.HexStringType.NORMAL))) +

                // Now, get the data
                ((data == null) ? "" : data.toString());
    }
}
