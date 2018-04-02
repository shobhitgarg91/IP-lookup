/**
 * Used to store each entry in case of Naive routing table
 * Created by Shobhit on 11/1/2016.
 */
public class Node2 {
    String nextHop;
    String subnet;

    public Node2(String nextHop, String subnet)  {
        this.nextHop = nextHop;
        this.subnet = subnet;
    }
}
