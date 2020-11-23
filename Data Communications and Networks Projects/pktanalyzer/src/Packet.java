import java.util.Arrays;

/**
 * The Packet class takes in a byte array of data from a packet and separates it into EtherHeader and IPHeader parts.
 *
 * @author Steven Yu (sky3947)
 */
public class Packet {

    private static final int PAYLOAD = 14;    // The byte location where the payload starts

    private EtherHeader etherHeader;
    private IPHeader ipHeader;

    /**
     * Constructor for Packet. Instantiates an EtherHeader and an IPHeader.
     *
     * @param raw The byte array of data from the incoming packet.
     */
    public Packet(byte[] raw) {
        // Instantiate etherHeader and ipHeader
        etherHeader = new EtherHeader(raw);
        ipHeader = new IPHeader(Arrays.copyOfRange(raw, PAYLOAD, raw.length));
    }

    /**
     * Summarizes the packet details.
     *
     * @return A string representing this packet
     */
    public String toString() {
        return etherHeader.toString() + ((Utility.getEtherType(etherHeader.getEthertype()).equals("IP")) ? ipHeader.toString() : "");
    }
}
