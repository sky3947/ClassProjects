/**
 * The Data class represents the data sent in a packet.
 *
 * @author Steven Yu (sky3947)
 */
public class Data extends Header {

    private static final int BYTES_PER_LINE = 16;
    private static final int ASCII_MIN = 32;
    private static final int ASCII_MAX = 126;

    private byte[] raw;

    /**
     * The Data class represents the sent data in a packet.
     *
     * @param raw The raw data in a byte array.
     */
    public Data(byte[] raw) {
        super("DATA");

        this.raw = raw;
    }

    /**
     * Creates a String representation of this Data. This is done by a hex dump.
     *
     * @return A String representation of this Data.
     */
    @Override
    public String toString() {
        String[] pieces = new String[(raw.length + BYTES_PER_LINE - 1) / BYTES_PER_LINE];   // Align 16 bytes per line

        String current = "";
        String characters = "";
        int align = 0;
        int line = 0;
        for (byte data : raw) {
            if (align == BYTES_PER_LINE) {
                align = 0;
                pieces[line] = String.format("%-48s'%s'", current, characters);
                current = "";
                characters = "";
                line++;
            }

            current += Utility.byteToHexString(data, Utility.HexStringType.NO_PREFIX) + " ";
            if ((Utility.byteToInt(data) >= ASCII_MIN) && (Utility.byteToInt(data) <= ASCII_MAX)) {
                characters += (char) data;
            } else {
                characters += ".";
            }
            align++;
        }
        if(!current.equals("")) {
            pieces[line] = String.format("%-48s'%s'", current, characters);
        }

        return buildHeader("Data", pieces);
    }
}
