/**
 * Used to store each bit value in a trie data structure
 * Created by Shobhit on 10/26/2016.
 */
public class Node {
    String val;
    // Node array contains the two child nodes for child 0 and child 1
    Node[] child = new Node[2];
    boolean endNode = false;
    String subnet = null;
    String IP = null;
    public Node(String val) {
        this.val = val;
        child[0] = null; child[1] = null;
    }
}
