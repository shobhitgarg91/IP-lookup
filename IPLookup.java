
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * This program reads in a file to create a router forwarding table and then reads another file that contains the
 * IP address for lookup. It then refers to the forwarding table created to find the next hop.
 * For the purposes of this project, two implementations of routing table have been done:
 * 1. Naive Routing Table: created by using a Hashtable.
 * 2. Optimized Routing Table: created by implementing a Trie data structure
 *
 * Created by Shobhit on 10/25/2016.
 */
public class IPLookup {
    Hashtable<String, Node2> naiveRoutingTable = new Hashtable<>();
    String defaultEntry;
    String subnetArray[] = new String[33];
    FileWriter fileWriter1;
    FileWriter fileWriter2;
    int notFoundCountNaive = 0;
    int notFoundCountTrie = 0;
    HashMap<String, Node> trieStructure;

    /**
     * Constructor function initializes the two file writer objects for writing the results of lookup using
     * naive routing table and trie routing table. It also creates an array subnets for each possible value
     * of CIDR.
     *
     * @throws IOException
     */
    public IPLookup() throws IOException {
        fileWriter1 = new FileWriter("Results_naive.txt");
        fileWriter2 = new FileWriter("Results_trie.txt");
        subnetArray[0] = "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 33; i++) {
            sb = new StringBuilder();
            for (int j = 1; j < 33; j++) {
                if (j <= i)
                    sb.append("1");
                else
                    sb.append("0");
            }
            String binarySubnet = sb.toString();
            // first octet
            sb = new StringBuilder();
            sb.append(Integer.parseInt(binarySubnet.substring(0, 8), 2) + ".");
            // second octet
            sb.append(Integer.parseInt(binarySubnet.substring(8, 16), 2) + ".");
            // third octet
            sb.append(Integer.parseInt(binarySubnet.substring(16, 24), 2) + ".");
            // forth octet
            sb.append(Integer.parseInt(binarySubnet.substring(24, 32), 2));
            subnetArray[i] = sb.toString();
        }
    }

    /**
     * Main function calls the subsequent functions for creating and looking up IPs using naive routing
     * table as well as Trie routing table. It reads the command line arguments to get the names of files
     * that are used to construct the routing table and for IP lookup.
     *
     * @param args command line arguments
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        IPLookup ipLookup = new IPLookup();
        String line = null;
        try {
            FileReader fileReader = new FileReader(args[0]);
            BufferedReader br = new BufferedReader(fileReader);
            // reading line by line and store it in an arraylist
            ArrayList<String> inputForwardingTable = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                inputForwardingTable.add(line);
            }
            // building the naive routing table
            ipLookup.naiveBuilt(inputForwardingTable);
            // building the trie routing table
            ipLookup.buildTrie2(inputForwardingTable);
        } catch (IOException e) {
            System.out.println("Exception in reading file");
        }

        // for lookup
        try {
            FileReader fileReader = new FileReader(args[1]);
            BufferedReader br = new BufferedReader(fileReader);
            ArrayList<String> inputLookups = new ArrayList<>();
            // reading the file containing the IPs line by line and storing it into Arraylist
            while ((line = br.readLine()) != null) {
                inputLookups.add(line);
            }
            StringBuilder solutionNaive = new StringBuilder();
            StringBuilder solutionTrie = new StringBuilder();
            long time1 = System.currentTimeMillis();
            // running the program for naive routing table 100 times
            for (int i = 0; i < 100; i++)
                solutionNaive = ipLookup.lookUp(inputLookups, solutionNaive);
            long time2 = System.currentTimeMillis();
            System.out.println("Time for naive lookup: " + (time2 - time1) + "\n");
            System.out.println("BINARY TRIE BEGINS\n");
            time1 = System.currentTimeMillis();
            // running the program for naive routing table 100 times
            for (int i = 0; i < 100; i++)
                solutionTrie = ipLookup.lookupTrie2(inputLookups, solutionTrie);
            time2 = System.currentTimeMillis();
            System.out.println("Time for Binary trie lookup: " + (time2 - time1));
            System.out.println("Not found count in Naive implementation: " + ipLookup.notFoundCountNaive);
            System.out.println("Not found count in Binary Trie implementation: " + ipLookup.notFoundCountTrie);
            ipLookup.fileWriter1.write(solutionNaive.toString());
            ipLookup.fileWriter2.write(solutionTrie.toString());
            ipLookup.fileWriter1.close();
            ipLookup.fileWriter2.close();
        } catch (IOException e) {
            System.out.println("Error");
        }

    }

    /**
     * buildTrie2 function builds the trie data structure for the given network prefixes. It first reads the IP and
     * CIDR and then calculates the network prefix. This netowrk prefix is then stored using the Trie data structure.
     * As mentioned in the paper specified as a part of this project, it reads the first octet of network prefix and
     * then creates a trie data structure for the remaining bits of the network prefix. This is an optimization step
     * as the code will not look through the first eight bits in a trie hence and will hence result in an optimized
     * performance.
     *
     * @param inputForwardingTable arraylist containing the IPs with CIDRs to be used for constructing the
     *                             routing table
     */
    public void buildTrie2(ArrayList<String> inputForwardingTable) {
        HashMap<String, ArrayList<dataClass>> firstOctetTable = new HashMap<>();
        // for setting the value of default entry to forward the packet to incase the entry corresponding to IP is not found
        defaultEntry = inputForwardingTable.get(0).split("/")[0];

        // reading line by line
        for (String line : inputForwardingTable) {
            String data = line;
            // separating the IP and CIDR
            String[] slashStr = data.split("/");
            // separating the IP octets
            String[] octets = slashStr[0].split("\\.");
            StringBuilder sb = new StringBuilder();
            // converting the IP into binary
            for (String x : octets) {
                sb.append(convertToBinary(x));
            }
            int cidr = Integer.parseInt(slashStr[1]);
            // taking the network prefix by applying CIDR to the IP
            String entry = sb.toString().substring(0, cidr);
            // extracting the first octet
            String octet1 = entry.substring(0, 8);
            String rem = entry.substring(8, entry.length());
            // dataclass object represents a single entry of IP/CIDR
            dataClass dc = new dataClass();
            dc.IP = slashStr[0];
            dc.subnet = subnetArray[cidr];
            dc.rem = rem;
            // checking if the table already contains the first octet. If the entry for first octet is already present
            // it just appends the dataclass object to the arraylist
            if (firstOctetTable.containsKey(octet1)) {
                firstOctetTable.get(octet1).add(dc);
            } else {
                firstOctetTable.put(octet1, new ArrayList<dataClass>());
                firstOctetTable.get(octet1).add(dc);
            }

        }
        // trieStructure is a hashmap of tries that differ from each other based on the first octet
        trieStructure = new HashMap<>();
        for (String octet : firstOctetTable.keySet()) {
            // for each entry for a particular first octet build a trie
            Node node = buildSmallTries(firstOctetTable.get(octet));
            trieStructure.put(octet, node);

        }
    }

    /**
     * buildSmallTries function builds small tries based upon the data it receives. This trie is particular to
     * the first octet of IP.
     *
     * @param remaining the entries for remaining bits particular to the first octet of an IP
     * @return Node object that acts as a root for this trie
     */
    public Node buildSmallTries(ArrayList<dataClass> remaining) {
        Node node = new Node("null");
        // for each entry of remaining bits
        for (dataClass dc : remaining) {
            String x = dc.rem;
            Node node1 = node;
            // looping on the remaining bits
            for (int i = 0; i < x.length(); i++) {
                String nextVal = x.substring(i, i + 1);
                if (nextVal.equalsIgnoreCase("1")) {
                    // if 1 doesn't exist, create new node and traverse
                    if (node1.child[1] == null) {
                        node1.child[1] = new Node("1");
                        node1 = node1.child[1];
                    }
                    // if 1 exist, just traverse
                    else {
                        node1 = node1.child[1];
                    }
                }
                // for 0
                else {
                    // if node 0 doesnt exist
                    if (node1.child[0] == null) {
                        node1.child[0] = new Node("0");
                        node1 = node1.child[0];
                    }
                    // if 0 exist, just traverse
                    else {
                        node1 = node1.child[0];
                    }
                }
            }// added an entry completely
            node1.IP = dc.IP;
            node1.endNode = true;
            node1.subnet = dc.subnet;

        }
        // return the root of the trie
        return node;
    }

    /**
     * this function looks up an IP in the trie data structure. It first extracts the first octet of Trie and looks for
     * it in the routing table. if the first octet is found, then the remaining bits of the IP are searched for in the
     * Trie structure for the first octet. If the first octet is not found, it treats the entry as Not found
     *
     * @param inputLookups arraylist containing IPs to be searched
     * @param solution     StringBuilder used to store the results
     * @return StringBuilder object that stores the result
     * @throws IOException
     */
    public StringBuilder lookupTrie2(ArrayList<String> inputLookups, StringBuilder solution) throws IOException {
        notFoundCountTrie = 0;
        // for each lookup entry
        for (String line : inputLookups) {
            // splitting the octets
            String[] octets = line.split("\\.");
            StringBuilder sb = new StringBuilder();
            // converting the IP into binary
            for (String x : octets)
                sb.append(convertToBinary(x));
            // address to look up
            String lookUpAddr = sb.toString();
            // extract the first octet
            String octet1 = lookUpAddr.substring(0, 8);
            String rem = lookUpAddr.substring(8, lookUpAddr.length());
            // if the first octet is not present in the routing table, then just increment not found count
            if (!trieStructure.containsKey(octet1)) {
                notFoundCountTrie++;
                solution.append(line + " -> " + defaultEntry + "(Not found)\n");
            } else {
                // extract the root of trie for the first octet
                Node node1 = trieStructure.get(octet1);
                int index = 0;
                Node soln = null;
                // traverse the length of remaining bits in the IP
                while (index < rem.length() && node1 != null) {
                    String nextVal = rem.substring(index, ++index);
                    // if the next bit in remaining bits is 0
                    if (nextVal.equalsIgnoreCase("0")) {
                        // if there is an entry for bit 0
                        if (node1.child[0] != null) {
                            node1 = node1.child[0];
                            if (node1.endNode)
                                soln = node1;
                        }
                        // if entry for zero is not present
                        else
                            break;
                    }
                    // if the next bit is 1
                    else {
                        // if there is an entry for bit 1
                        if (node1.child[1] != null) {
                            node1 = node1.child[1];
                            if (node1.endNode)
                                soln = node1;
                        } else
                            break;
                    }
                }
                // appending the solution to StringBuilder
                if (soln != null) {
                    solution.append(line + " -> " + soln.IP + " Subnet:  " + soln.subnet + "\n");
                } else {
                    notFoundCountTrie++;
                    solution.append(line + " -> " + defaultEntry + "(Not found)\n");

                }
            }
        }
        return solution;
    }

    /**
     * naiveBuilt creates a routing table using a hash table and hashing technique
     *
     * @param inputForwardingTable the data to build the hash table from
     */
    public void naiveBuilt(ArrayList<String> inputForwardingTable) {
        // for each IP/CIDR
        for (String line : inputForwardingTable) {
            String data = line;
            // splitting the IP and CIDR
            String[] slashStr = data.split("/");
            // splitting the octets
            String[] octets = slashStr[0].split("\\.");
            StringBuilder sb = new StringBuilder();
            // converting the IP to binary
            for (String x : octets) {
                sb.append(convertToBinary(x));
            }
            int cidr = Integer.parseInt(slashStr[1]);
            // taking the subnet corresponding to the CIDR
            String cidrVal = subnetArray[cidr];
            String entry = sb.toString();
            // extracting the network prefix from the IP using CIDR
            entry = entry.substring(0, cidr);
            // adding the entry to routing table
            naiveRoutingTable.put(entry, new Node2(slashStr[0], cidrVal));

        }
    }

    /**
     * lookUp performs the naive lookup in the naive routing trable
     *
     * @param inputLookups entries to be looked up
     * @param solution     StringBuilder containing the solution
     * @return StringBuilder object containing the solution
     * @throws IOException
     */
    public StringBuilder lookUp(ArrayList<String> inputLookups, StringBuilder solution) throws IOException {
        notFoundCountNaive = 0;
        // for each entry
        for (String line : inputLookups) {
            // splitting the octets
            String[] octets = line.split("\\.");
            StringBuilder sb = new StringBuilder();
            // converting the IP to binary
            for (String x : octets)
                sb.append(convertToBinary(x));
            // address to look up
            String lookUpAddr = sb.toString();
            int i = 32;
            boolean found = false;
            // for each combination of bits of IP look up in the routing table. Start from 32 bits and linearly reduce
            // to find the maximum match
            for (int j = i; j >= 1; j--) {
                String key = lookUpAddr.substring(0, j);
                if (naiveRoutingTable.containsKey(key)) {
                    solution.append(line + " -> " + naiveRoutingTable.get(key).nextHop + " Subnet:  " + naiveRoutingTable.get(key).subnet + "\n");
                    found = true;
                    break;
                }
            }
            if (!found) {
                notFoundCountNaive++;
                solution.append(line + " -> " + defaultEntry + "(Not found)\n");
            }

        }
        return solution;
    }

    /**
     * convertToBinary converts the string data into binary strings
     *
     * @param data the data to be converted
     * @return binary representation in the form of string
     */
    public String convertToBinary(String data) {
        StringBuilder sb = new StringBuilder();
        String octet = Integer.toBinaryString(Integer.parseInt(data));
        if (octet.length() != 8) {
            StringBuilder sb2 = new StringBuilder();
            for (int i = 1; i <= 8 - octet.length(); i++) {
                sb2.append("0");
            }
            sb2.append(octet);
            sb.append(sb2.toString());
        } else
            sb.append(octet);
        return sb.toString();
    }

}
