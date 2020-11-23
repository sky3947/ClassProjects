/**
 * This is an abstract class for all the different types of headers. All headers must have a prefix (for example,
 * EtherHeader has prefix "EH").
 *
 * @author Steven Yu (sky3947)
 */
public abstract class Header {

    private String PFX;

    /**
     * The constructor for a header.
     *
     * @param pfx The header's prefix
     */
    public Header(String pfx) {
        PFX = pfx;
    }

    /**
     * Gets the header's prefix.
     *
     * @return The header's prefix.
     */
    public String getPrefix() {
        return PFX;
    }

    /**
     * Helper function to pretty-print for toString method.
     *
     * @param text The String to print.
     * @return A formatted String.
     */
    public String prettyPrint(String text) {
        return getPrefix() + ": \t" + text + "\n";
    }

    /**
     * Function to streamline the printing of headers.
     *
     * @param strings The individual parts of the header to print.
     * @return The finished header section.
     */
    public String buildHeader(String title, String ... strings) {
        String output = prettyPrint(String.format("----- %s -----", title));
        output += prettyPrint("");

        for(String str : strings) {
            output += prettyPrint(str);
        }
        output += prettyPrint("");

        return output;
    }

    /**
     * Creates a String representation of the header.
     *
     * @return A String representation of the header.
     */
    public abstract String toString();
}
