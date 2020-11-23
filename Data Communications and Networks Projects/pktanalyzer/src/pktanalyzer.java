import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * The pktanalyzer program extracts and displays different headers of captured packets in a given file. First, it
 * displays the ethernet header fields of the captured frames. Second, if the ethernet frame contains an IP datagram, it
 * prints the IP header. Third, it prints the packets encapsulated in the IP datagram (TCP, UDP, or ICMP packets).
 *
 * @author Steven Yu (sky3947)
 */
public class pktanalyzer {

    public static void main(String[] args) {
        // Check if correct number of arguments were supplied
        if(args.length == 0) {
            printUsage("No was packet supplied.");
        } else if(args.length > 1) {
            printUsage("More than one argument was be given.");
        } else {
            // Check if file path and file are valid
            String currentDirectory = System.getProperty("user.dir");
            String filePath = currentDirectory.replace("\\", "/")+"/"+args[0];

            try {
                // Using NIO to read bytes to an array
                byte[] data = Files.readAllBytes(Paths.get(filePath));
                Packet packet = new Packet(data);

                // Print out the packet's contents
                System.out.println(packet.toString());
            } catch (NoSuchFileException e) {
                printUsage("No such file \""+args[0]+"\" was found.");
            } catch (IOException e) {
                // Shouldn't be reachable
                printUsage("Oops- some uncaught error occurred!");
            }
        }
    }

    /**
     * This method is used to print the usage message.
     *
     * @param msg An error message.
     */
    public static void printUsage(String msg) {
        System.out.println("Error: "+msg+"\n");

        System.out.println("Usage: java pktanalyzer <datafile>");
        System.out.println("       <datafile> : The path to the packet to analyze");
    }
}
