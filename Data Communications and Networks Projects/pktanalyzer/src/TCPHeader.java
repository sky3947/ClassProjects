import java.math.BigInteger;
import java.util.Arrays;

/**
 * The TCPHeader class represents the TCP section of a packet, assuming the packet uses TCP protocol.
 *
 * @author Steven Yu (sky3947)
 */
public class TCPHeader extends Header {

    private static final int SOURCE_PORT = 0;                           // Byte location where the source port starts.
    private static final int DESTINATION_PORT = 2;                      // Byte location where the destination port starts.
    private static final int SEQUENCE_NUMBER = 4;                       // Byte location where the sequence number starts.
    private static final int ACK_NUMBER = 8;                            // Byte location where the acknowledgement number starts.
    private static final int OFFSET_RESERVED_NS = 12;                   // Byte location where the data offset, reserved, and ns start.
    private static final int CWR_ECE_URG_ACK_PSH_RST_SYN_FIN = 13;      // Byte location where the other flags start.
    private static final int WINDOW_SIZE = 14;                          // Byte location where the window size starts.
    private static final int CHECKSUM = 16;                             // Byte location where the checksum starts.
    private static final int URGENT_POINTER = 18;                       // Byte location where the urgent pointer starts.
    private static final int OPTIONS = 20;                              // Byte location where the options start.

    private static final int MIN_PKT_LENGTH = 20;                       // Minimum length of packet.

    private int sourcePort;
    private int destinationPort;
    private int sequenceNumber;
    private BigInteger ackNumber;
    private int dataOffset;
    private byte[] flags;
    private byte ns;
    private byte cwr;
    private byte ece;
    private byte urg;
    private byte ack;
    private byte psh;
    private byte rst;
    private byte syn;
    private byte fin;
    private int window;
    private byte[] checksum;
    private byte[] urgPointer;
    private boolean hasOptions;

    private Data data = null;

    /**
     * The constructor for TCPHeader. It extract information from a packet.
     *
     * @param raw The raw packet in a byte array.
     */
    public TCPHeader(byte[] raw) {
        super("TCP");

        byte[] temp;

        // Extract TCP header information.
        this.sourcePort = Utility.byteToInt(Arrays.copyOfRange(raw, SOURCE_PORT, DESTINATION_PORT));
        this.destinationPort = Utility.byteToInt(Arrays.copyOfRange(raw, DESTINATION_PORT, SEQUENCE_NUMBER));
        this.sequenceNumber = Utility.byteToInt(Arrays.copyOfRange(raw, SEQUENCE_NUMBER, ACK_NUMBER));
        this.ackNumber = new BigInteger(Utility.byteToHexString(Arrays.copyOfRange(raw, ACK_NUMBER, OFFSET_RESERVED_NS), Utility.HexStringType.NO_PREFIX), 16);
        temp = Utility.splitByte(raw[OFFSET_RESERVED_NS]);
        this.dataOffset = 4 * Utility.byteToInt(temp[0]);
        temp = Utility.splitByte(temp[1], 7);
        this.ns = temp[1];
        this.flags = Utility.combine(ns, raw[CWR_ECE_URG_ACK_PSH_RST_SYN_FIN]);
        temp = Utility.toBits(raw[CWR_ECE_URG_ACK_PSH_RST_SYN_FIN]);
        this.cwr = temp[0];
        this.ece = temp[1];
        this.urg = temp[2];
        this.ack = temp[3];
        this.psh = temp[4];
        this.rst = temp[5];
        this.syn = temp[6];
        this.fin = temp[7];
        this.window = Utility.byteToInt(Arrays.copyOfRange(raw, WINDOW_SIZE, CHECKSUM));
        this.checksum = Arrays.copyOfRange(raw, CHECKSUM, URGENT_POINTER);
        this.urgPointer = Arrays.copyOfRange(raw, URGENT_POINTER, OPTIONS);
        this.hasOptions = (dataOffset > 20);

        if(raw.length > MIN_PKT_LENGTH) {
            this.data = new Data(Arrays.copyOfRange(raw, dataOffset, raw.length));
        }
    }

    /**
     * Creates a String representation of this TCPHeader. Includes source port, destination port, sequence number,
     * acknowledgement number, data offset, flags, window, checksum, urgent pointer, existence of options, and its data.
     *
     * @return A String representation of this TCPHeader.
     */
    @Override
    public String toString() {
        return buildHeader("TCP Header",
                String.format("Source port            = %d", sourcePort),
                String.format("Destination port       = %d", destinationPort),
                String.format("Sequence number        = %d", sequenceNumber),
                String.format("Acknowledgement number = %d", ackNumber),
                String.format("Data offset            = %d bytes", dataOffset),
                String.format("Flags                  = %s", Utility.byteToHexString(flags, Utility.HexStringType.NORMAL)),
                String.format("\t%d .... .... = %sNS pointer", Utility.byteToInt(ns), (Utility.byteToInt(ns) == 0) ? "No " : ""),
                String.format("\t. %d... .... = %sCWR pointer", Utility.byteToInt(cwr), (Utility.byteToInt(cwr) == 0) ? "No " : ""),
                String.format("\t. .%d.. .... = %sECE pointer", Utility.byteToInt(ece), (Utility.byteToInt(ece) == 0) ? "No " : ""),
                String.format("\t. ..%d. .... = %sURG pointer", Utility.byteToInt(urg), (Utility.byteToInt(urg) == 0) ? "No " : ""),
                String.format("\t. ...%d .... = %sACK pointer", Utility.byteToInt(ack), (Utility.byteToInt(ack) == 0) ? "No " : ""),
                String.format("\t. .... %d... = %sPSH pointer", Utility.byteToInt(psh), (Utility.byteToInt(psh) == 0) ? "No " : ""),
                String.format("\t. .... .%d.. = %sRST pointer", Utility.byteToInt(rst), (Utility.byteToInt(rst) == 0) ? "No " : ""),
                String.format("\t. .... ..%d. = %sSYN pointer", Utility.byteToInt(syn), (Utility.byteToInt(syn) == 0) ? "No " : ""),
                String.format("\t. .... ...%d = %sFIN pointer", Utility.byteToInt(fin), (Utility.byteToInt(fin) == 0) ? "No " : ""),
                String.format("Window                 = %d", window),
                String.format("Checksum               = %s", Utility.byteToHexString(checksum, Utility.HexStringType.NORMAL)),
                String.format("Urgent pointer         = %s", Utility.byteToHexString(urgPointer, Utility.HexStringType.NORMAL)),
                (hasOptions) ? "Has options" : "No options") +

                // Now, get the data
                ((data == null) ? "" : data.toString());
    }
}
