//package chord;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Advani
 */
public class bootstrap extends UnicastRemoteObject implements bootstrapIF, Serializable {

    static String bootNodeName;
    static String bootNodeIP;
    static String LeaderNode = "blank";
    static int port = 7394;

    public bootstrap() throws RemoteException {
        super();
    }

    public void updateLeaderNode(String node) throws RemoteException {
        System.out.println("\nAttempting to update LeaderNode pointer");
        if(node.equals("blank")){
         LeaderNode = node;
            System.out.println("Leader node cannot be updated, network has been dissolved.");
        }else{
        LeaderNode = node;
        System.out.println("LeaderNode successfully updated to "+node);
        }
        System.out.println("");
    }

    public void DeleteNotify(String nodeIP, int IPhash) throws RemoteException {
        //check the node's delete flag, if true..
        //remove the above passed node from the node map

    }

    @Override
    public void addToNetwork(String nodeIP, int IPhash) throws RemoteException {
        System.out.println("\nReceived request to add "+nodeIP+" to the network.");
        try {
            boolean firstNode = false;
            if (LeaderNode.equals("blank")) {
                firstNode = true;
            }
            Registry reg = LocateRegistry.getRegistry(nodeIP, port);
            nodeIF node = (nodeIF) reg.lookup("node");

            if (firstNode) {
                //first node in the network
                System.out.println("Attempting to add "+nodeIP+" to the existing network.");
                node.InsertionFirst();
                System.out.println(nodeIP+" is now a part of the network.");
            } else {
                //insert new node in existing network
                System.out.println("Forwarding add request to LeaderNode @ "+LeaderNode);
                Registry reg_x = LocateRegistry.getRegistry(LeaderNode, port);
                nodeIF node_x = (nodeIF) reg_x.lookup("node");
                node_x.InsertionNewNode(nodeIP, IPhash);
                System.out.println(nodeIP+" is now a part of the network.");
            }
        } catch (NotBoundException | AccessException ex) {
            Logger.getLogger(bootstrap.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) throws IOException {
        InetAddress IP = InetAddress.getLocalHost();
        bootNodeName = IP.getHostName();
        bootNodeIP = IP.getHostAddress();
        Registry r = LocateRegistry.createRegistry(port);
        r.rebind("bootstrap", new bootstrap());
        System.out.println();
        System.out.println(bootNodeName + " (bootstrap server) is now active.");
        System.out.println("request connection @ " + bootNodeIP);
        System.out.println();
    }

}
