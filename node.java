//package chord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Advani
 */
public class node extends UnicastRemoteObject implements nodeIF, Serializable {

    //class variables
    static String path = "/home/stu4/s1/aa5394/Documents/Courses/Distributed Systems/proj1/";
    static int maxIDSpace = 25;
    static int port = 7394;
    static String complete_path = "";
    static String localmachineName;
    static String nodeAddress;
    static int hashValue;
    static String BootStrapIP;
    static String predecessor;
    static String predecessorName;
    static String successor;
    static String successorName;
    static int lowerLimit;
    static int upperLimit;
    static String menu;
    static boolean part_of_network = false;
    static boolean LeaderNode = false;

    //HashMap structure for file storage is --> <file hash value, <file name, file contents>>
    static HashMap< Integer, ArrayList<String>> files = new HashMap<>();

    //constructor
    public node() throws RemoteException {
        super();
    }

    @Override
    //runs at current node, called by bootstrap
    public void InsertionFirst() throws RemoteException {
        try {
            predecessor = nodeAddress;
            successor = nodeAddress;

            upperLimit = hashValue;
            if (upperLimit == (maxIDSpace - 1)) {
                //crossover point in focus
                lowerLimit = 0;
            } else {
                //crossover point not in focus
                lowerLimit = upperLimit + 1;
            }

            System.out.println(localmachineName + " added to the network.");
            Registry reg = LocateRegistry.getRegistry(BootStrapIP, port);
            bootstrapIF bs = (bootstrapIF) reg.lookup("bootstrap");
            bs.updateLeaderNode(nodeAddress);

            LeaderNode = true;
            System.out.println(localmachineName + " is now the LeaderNode of the network.");

            //initialize node storage within range
            if (lowerLimit > upperLimit) {
                //crossover
                for (int i = upperLimit; i >= 0; i--) {
                    files.put(i, new ArrayList<String>());
                }
                for (int i = lowerLimit; i < maxIDSpace; i++) {
                    files.put(i, new ArrayList<String>());
                }
            } else if (lowerLimit <= upperLimit) {
                for (int i = lowerLimit; i <= upperLimit; i++) {
                    files.put(i, new ArrayList<String>());
                }
            }

        } catch (NotBoundException | AccessException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    //called by external node
    public void setPredecessor(String pre) throws RemoteException {
        predecessor = pre;
    }

    @Override
    //called by external node
    public void setSuccessor(String suc) throws RemoteException {
        successor = suc;
    }

    @Override
    //called by external node
    public String getPredecessor() throws RemoteException {
        return predecessor;
    }

    @Override
    //called by external node
    public String getSuccessor() throws RemoteException {
        return successor;
    }

    @Override
    //called by external node
    public String getName() throws RemoteException {
        return localmachineName;
    }

    @Override
    //called by external node
    public int getIPhash() throws RemoteException {
        int IPhash = Math.abs(nodeAddress.hashCode());
        IPhash = IPhash % maxIDSpace;
        return IPhash;
    }

    @Override
    //called by external node
    public void setUpperLimit(int uLimit) throws RemoteException {
        upperLimit = uLimit;
    }

    @Override
    //called by external node
    public int getUpperLimit() throws RemoteException {
        return upperLimit;
    }

    @Override
    //called by external node
    public void setLowerLimit(int lLimit) throws RemoteException {
        lowerLimit = lLimit;
    }

    @Override
    //called by external node
    public int getLowerLimit() throws RemoteException {
        return lowerLimit;
    }

    @Override
    //called by external node
    public boolean isLeaderNode() throws RemoteException {
        return LeaderNode;
    }

    @Override
    //called by external node
    public void setLeaderNode(boolean status) throws RemoteException {
        LeaderNode = status;
    }

    @Override
    //resets or initializes the storage for node as called
    public void setFileMap() throws RemoteException {
        if (lowerLimit > upperLimit) {
            //crossover
            for (int i = upperLimit; i >= 0; i--) {
                files.put(i, new ArrayList<String>());
            }
            for (int i = lowerLimit; i < maxIDSpace; i++) {
                files.put(i, new ArrayList<String>());
            }
        } else if (lowerLimit <= upperLimit) {
            //no crossover
            for (int i = lowerLimit; i <= upperLimit; i++) {
                files.put(i, new ArrayList<String>());
            }
        }
    }

    @Override
    //runs at current node, called by successor
    public void addFile(int i, String file, String content) throws RemoteException {
        try {
            complete_path = path + localmachineName + "/" + file;
            PrintWriter out = new PrintWriter(complete_path);
            out.println(content);
            out.flush();
            if (!files.containsKey(i)) {
                files.put(i, new ArrayList<String>());
            }
            files.get(i).add(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    //removes files from storage of calling node
    public void removeFile(String file) throws RemoteException {
        complete_path = path + localmachineName + "/" + file;
        File f = new File(complete_path);
        try {
            if (f.delete()) {
                System.out.println(f.getName() + " is deleted!");
            } else {
                System.out.println("Deletion failed for " + f.getName());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    //gets the file content for a file from the calling node
    public String getFileContent(String file) throws RemoteException {
        String line, content = "";
        try {
            FileReader fileReader;
            BufferedReader file_in_br;

            complete_path = path + localmachineName + "/" + file;
            fileReader = new FileReader(complete_path);
            file_in_br = new BufferedReader(fileReader);

            //read whole file
            while ((line = file_in_br.readLine()) != null) {
                content += line + "\n";
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }

    @Override
    //runs at successor, called by current node
    public void InsertionNewNode(String nodeIP, int IPhash) throws RemoteException, AccessException {
        System.out.println("\n\nReceived request to connect " + nodeIP + " to the system at " + localmachineName);
        try {
            node n = new node();
            boolean crossover = false;
            boolean withinrange = false;
            boolean conflict = false;
            Registry reg;

            if (lowerLimit > upperLimit) {
                //discover crossover
                crossover = true;
            }

            if (crossover) {
                //if crossover detected
                if (IPhash >= lowerLimit || IPhash < upperLimit) {
                    //if node addition is within current node's range
                    withinrange = true;
                }
            } else {
                //if no crossover
                if (IPhash >= lowerLimit && IPhash < upperLimit) {
                    //if node addition is within current node's range
                    withinrange = true;
                }
            }
            if (IPhash == upperLimit) {
                //if conflict with current node
                conflict = true;
            }

            if (!conflict) {
                if (!withinrange) {

                    //call successor.InsertionNewNode
                    System.out.println("new node out of range, forwarding request to successor node " + successorName + " @ " + successor);
                    reg = LocateRegistry.getRegistry(successor, port);
                    nodeIF node_new = (nodeIF) reg.lookup("node");
                    node_new.InsertionNewNode(nodeIP, IPhash);

                    System.out.println(menu);

                } else {
                    //within range
                    System.out.println("Attempting to add new node to the network.");

                    //open registry and get object for current node
                    reg = LocateRegistry.getRegistry(nodeIP, port);
                    nodeIF node_new = (nodeIF) reg.lookup("node");

                    //open registry and get object for predecessor node
                    Registry reg_p = LocateRegistry.getRegistry(predecessor, port);
                    nodeIF node_pre = (nodeIF) reg_p.lookup("node");

                    //setting node's links
                    //set node's successor to node_x
                    //set node's predecessor to node_x's predecessor
                    node_new.setSuccessor(nodeAddress);
                    node_new.setPredecessor(predecessor);

                    //get node's range
                    //get node_x's predecessor's IPhash to get node's range
                    //set node's range limits
                    //initilize node's fileMap
                    //change node_x's lowerLimit to node's IPhash+1
                    int pre_node_hash = node_pre.getIPhash();
                    node_new.setLowerLimit((pre_node_hash + 1) % maxIDSpace);
                    node_new.setUpperLimit(IPhash % maxIDSpace);
                    node_new.setFileMap();
                    lowerLimit = (IPhash + 1) % maxIDSpace;

                    //setting remaining links
                    //set node_x's predecessor's successor to node
                    //set node_x's predecessor to node
                    node_pre.setSuccessor(nodeIP);
                    predecessor = nodeIP;

                    //rehashing files
                    //check hashmap for any fileMaps with hashValues within node's range
                    //add those fileMaps to node's map
                    //remove those fileMaps from node_x's map
                    int new_node_lowerLimit = node_new.getLowerLimit();
                    int new_node_upperLimit = node_new.getUpperLimit();

                    //transfer, create and delete files
                    FileReader fileReader;
                    BufferedReader file_in_br;
                    String line, content = "";

                    if (new_node_lowerLimit <= new_node_upperLimit) {
                        //if current node has no crossover
                        for (int i = new_node_lowerLimit; i <= new_node_upperLimit; i++) {

                            //every fileList at ID in range of new node
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            if (files.get(i).size() > 0) {
                                //if any files are present
                                for (String file : files.get(i)) {
                                    //for every file in fileList

                                    complete_path = path + localmachineName + "/" + file;
                                    fileReader = new FileReader(complete_path);
                                    file_in_br = new BufferedReader(fileReader);

                                    //read whole file
                                    while ((line = file_in_br.readLine()) != null) {
                                        content += line + "\n";
                                    }

                                    //add to new node's disk and memory
                                    node_new.addFile(i, file, content);
                                    //remove from successor's disk
                                    n.removeFile(file);
                                }
                            }
                            //remove FileList from successor's memory
                            files.remove(i);
                        }
                    } else {
                        //if there is a crossover
                        for (int i = new_node_lowerLimit; i < maxIDSpace; i++) {
                            //for left section
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            if (files.containsKey(i) && files.get(i).size() > 0) {
                                //if any files present in FileList
                                for (String file : files.get(i)) {
                                    //for every file

                                    complete_path = path + localmachineName + "/" + file;
                                    fileReader = new FileReader(complete_path);
                                    file_in_br = new BufferedReader(fileReader);

                                    //read whole file
                                    while ((line = file_in_br.readLine()) != null) {
                                        content += line + "\n";
                                    }
                                    //add to new node's disk and memory
                                    node_new.addFile(i, file, content);
                                    //remove from successor's disk
                                    n.removeFile(file);
                                }
                            }
                            //remove FileList from successor's memory
                            files.remove(i);
                        }
                        for (int i = new_node_upperLimit; i >= 0; i--) {
                            //for right section
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            if (files.containsKey(i) && files.get(i).size() > 0) {
                                //if any files present in FileList
                                for (String file : files.get(i)) {
                                    //for every file

                                    complete_path = path + localmachineName + "/" + file;
                                    fileReader = new FileReader(complete_path);
                                    file_in_br = new BufferedReader(fileReader);

                                    //read whole file
                                    while ((line = file_in_br.readLine()) != null) {
                                        content += line + "\n";
                                    }
                                    //add to new node's disk and memory
                                    node_new.addFile(i, file, content);
                                    //remove from successor's disk
                                    n.removeFile(file);
                                }
                            }
                            //remove FileList from successor's memory
                            files.remove(i);
                        }
                        System.out.println(menu);
                    }
                    if (part_of_network) {
                        n.viewMachineDetils();
                    }
                    System.out.println(menu);
                }
            } else {
                System.out.println("Collision detected.");
                System.out.println("**Insertion Failure**");
                //what to do if conflict arises during insertion
            }

        } catch (NotBoundException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    //runs at succrssor node, called by current node
    public void RemoveNode(String nodeIP, HashMap<Integer, ArrayList<String>> f, int lLimit, int uLimit) throws RemoteException {
        try {

            int node_lowerLimit;
            String node_predecessor;

            node n = new node();

            Registry reg;
            reg = LocateRegistry.getRegistry(nodeIP, port);
            nodeIF node = (nodeIF) reg.lookup("node");

            if (node.isLeaderNode()) {
                Registry reg_bs = LocateRegistry.getRegistry(BootStrapIP, port);
                bootstrapIF bs = (bootstrapIF) reg_bs.lookup("bootstrap");

                if (nodeAddress.equals(nodeIP)) {
                    LeaderNode = false;
                    bs.updateLeaderNode("blank");

                } else {
                    bs.updateLeaderNode(nodeAddress);
                    System.out.println(localmachineName + " is the new LeaderNode");
                    LeaderNode = true;
                    node.setLeaderNode(false);
                }
            }

            //get current node's predecessor and lowerLimit
            node_predecessor = node.getPredecessor();
            node_lowerLimit = node.getLowerLimit();

            //change successor's predecessor to current node's predecessor
            //change successor's lowerLimit to current node's lowerLimit
            predecessor = node_predecessor;
            lowerLimit = node_lowerLimit;

            //update predecessor's successor to current node's successor
            reg = LocateRegistry.getRegistry(predecessor, port);
            nodeIF node_p = (nodeIF) reg.lookup("node");
            node_p.setSuccessor(nodeAddress);

            String content;
            //extend fileList capacity
            if (lLimit <= uLimit) {
                for (int i = lLimit; i <= uLimit; i++) {
                    files.put(i, new ArrayList<String>());
                    for (String file : f.get(i)) {
                        content = node.getFileContent(file);
                        if (!nodeIP.equals(nodeAddress)) {
                            n.addFile(i, file, content);
                        }
                        node.removeFile(file);
                    }
                }
            } else {
                for (int i = lLimit; i < maxIDSpace; i++) {
                    files.put(i, new ArrayList<String>());
                    for (String file : f.get(i)) {
                        content = node.getFileContent(file);
                        if (!nodeIP.equals(nodeAddress)) {
                            n.addFile(i, file, content);
                        }
                        node.removeFile(file);
                    }
                }
                for (int i = uLimit; i > 0; i--) {
                    files.put(i, new ArrayList<String>());
                    for (String file : f.get(i)) {
                        content = node.getFileContent(file);
                        if (!nodeIP.equals(nodeAddress)) {
                            n.addFile(i, file, content);
                        }
                        node.removeFile(file);
                    }
                }
            }

            //display successor's stats
            if (part_of_network) {
                n.viewMachineDetils();
            }
            System.out.println(menu);

        } catch (NotBoundException | AccessException ex) {
            Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//runs at current node, called by current node
    public void viewMachineDetils() throws RemoteException {
        try {
            Registry reg;
            reg = LocateRegistry.getRegistry(predecessor, port);
            nodeIF node_p = (nodeIF) reg.lookup("node");
            predecessorName = node_p.getName();

            reg = LocateRegistry.getRegistry(successor, port);
            nodeIF node_s = (nodeIF) reg.lookup("node");
            successorName = node_s.getName();

            System.out.println("\nMachine Details are as follows:");
            System.out.println("Local Machine: " + localmachineName + " (" + nodeAddress + ")");
            System.out.println("LeaderNode: " + LeaderNode);
            System.out.println("Local Range: LowerLimit- " + lowerLimit + ", UpperLimit- " + upperLimit);
            System.out.println("Successor Machine: " + successorName + " (" + successor + ")");
            System.out.println("Predecessor Machine: " + predecessorName + " (" + predecessor + ")");
            System.out.println("Files at local machine-");
            String FileList = "";
            if (lowerLimit <= upperLimit) {
                for (int i = lowerLimit; i <= upperLimit; i++) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        FileList += f + "\n";
                    }
                }
                System.out.println(FileList);
            } else {
                for (int i = lowerLimit; i < maxIDSpace; i++) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        FileList += f + "\n";
                    }
                }
                for (int i = upperLimit; i > 0; i--) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        FileList += f + "\n";
                    }
                }
                System.out.println(FileList);
            }
            if (FileList.equals("")) {
                System.out.println("**No Files At This Machine**");
            }
        } catch (NotBoundException | AccessException ex) {
            Logger.getLogger(node.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String InsertFile(int HashOfFile, String FileName, String Content, String pathway, String direction) throws RemoteException {
        node n = new node();
        pathway += localmachineName + " (" + upperLimit + ") ";
        System.out.println("Trying to insert file @ " + localmachineName);
        if (lowerLimit > upperLimit) {
            if (HashOfFile >= lowerLimit || HashOfFile <= upperLimit) {
                try {
                    files.get(HashOfFile).add(FileName);

                    complete_path = path + localmachineName + "/" + FileName;
                    PrintWriter out = new PrintWriter(complete_path);
                    out.println(Content);
                    out.flush();

                    pathway += "[file inserted]";
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                try {
                    //pass to successor
                    String nextnode = "";
                    System.out.print("Forwarding file insertion request to ");
                    if (direction.equals("pre")) {
                        nextnode = predecessor;
                        System.out.println("predecessor.");
                    } else if (direction.equals("suc")) {
                        nextnode = successor;
                        System.out.println("sucessor.");
                    }

                    pathway += "-->";

                    Registry reg;
                    reg = LocateRegistry.getRegistry(nextnode, port);
                    nodeIF node = (nodeIF) reg.lookup("node");

                    pathway = node.InsertFile(HashOfFile, FileName, Content, pathway, direction);

                } catch (NotBoundException | AccessException ex) {
                    Logger.getLogger(node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            if (HashOfFile >= lowerLimit && HashOfFile <= upperLimit) {
                try {
                    files.get(HashOfFile).add(FileName);

                    complete_path = path + localmachineName + "/" + FileName;
                    PrintWriter out = new PrintWriter(complete_path);
                    out.println(Content);
                    out.flush();

                    pathway += "[file inserted]";
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    //pass to successor
                    String nextnode = "";
                    System.out.print("Forwarding file insertion request to ");
                    if (direction.equals("pre")) {
                        nextnode = predecessor;
                        System.out.println("predecessor.");
                    } else if (direction.equals("suc")) {
                        nextnode = successor;
                        System.out.println("sucessor.");
                    }

                    pathway += "-->";

                    Registry reg;
                    reg = LocateRegistry.getRegistry(nextnode, port);
                    nodeIF node = (nodeIF) reg.lookup("node");

                    pathway = node.InsertFile(HashOfFile, FileName, Content, pathway, direction);

                } catch (NotBoundException | AccessException ex) {
                    Logger.getLogger(node.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (part_of_network) {
            n.viewMachineDetils();
        }
        System.out.println(menu);
        return pathway;
    }

    @Override
    public String searchFile(int HashOfFile, String name, String nodeIP, int uLimit, String pathway, String direction) throws RemoteException {
        pathway += " " + localmachineName + " (" + upperLimit + ")";
        boolean file_at_current_node = false;
        boolean file_found = false;
        String nextnode = "";
        if (lowerLimit <= upperLimit) {
            if (HashOfFile >= lowerLimit && HashOfFile <= upperLimit) {
                file_at_current_node = true;
            }
        } else {
            if (HashOfFile >= lowerLimit || HashOfFile <= upperLimit) {
                file_at_current_node = true;
            }
        }

        if (file_at_current_node == true) {
            if (lowerLimit <= upperLimit) {
                for (int i = lowerLimit; i <= upperLimit; i++) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        if (f.equals(name)) {
                                file_found = true;
                            pathway += " [file found]\n\n**File Found at "+localmachineName+"**";
                            }
                        }
                    }
            } else {
                for (int i = lowerLimit; i < maxIDSpace; i++) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        if (f.equals(name)) {
                                file_found = true;
                            pathway += " [file found]\n\n**File Found at "+localmachineName+"**";
                            }
                        }
                    }
                for (int i = upperLimit; i > 0; i--) {
                    if (!files.containsKey(i)) {
                        files.put(i, new ArrayList<String>());
                    }
                    for (String f : files.get(i)) {
                        if (f.equals(name)) {
                                file_found = true;
                            pathway += " [file found]\n\n**File Found at "+localmachineName+"**";
                            }
                        }
                    }
                }
            if (file_found == false) {
                pathway += " [search ended]\n\n**File Not Found**\nFile is not present in the network.";
                return pathway;
            }
        } else {
            pathway += " -->";
            try {
                if (direction.equals("pre")) {
                    nextnode = predecessor;
                } else if (direction.equals("suc")) {
                    nextnode = successor;
                }

                Registry reg;
                reg = LocateRegistry.getRegistry(nextnode, port);
                nodeIF node = (nodeIF) reg.lookup("node");
                pathway = node.searchFile(HashOfFile, name, nodeAddress, upperLimit, pathway, direction);
            } catch (NotBoundException | AccessException ex) {
                Logger.getLogger(node.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pathway;
    }

    //main method
    public static void main(String[] args) throws IOException, RemoteException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            //node and registry setup
            InetAddress IP = InetAddress.getLocalHost();
            localmachineName = IP.getHostName();
            nodeAddress = IP.getHostAddress();

            //start registry
            Registry r = LocateRegistry.createRegistry(port);
            r.rebind("node", new node());

            //create local directory
            File file = new File(path + "/" + localmachineName + "/");
            file.mkdir();
            System.out.println(localmachineName + " is now an active node");

            //establish connection to bootstrap server registry
            System.out.print("Enter BootStrap server IP:   ");
            BootStrapIP = br.readLine();
//            System.out.println("BootStrap server IP autoset to glados");
//            BootStrapIP = "129.21.22.196";

            String input;
            node n = new node();

            //before join or after leave
            String menu1 = "\n\nEnter 'join' to be added to the network"
                    + "\nEnter 'exit' to deactivate " + localmachineName + " "
                    + "\n-------------------------------------------------------";

            //after join
            String menu2 = "\n\nEnter 'view' to see " + localmachineName + "'s stats: "
                    + "\nEnter 'insert [file name]' to store a file in the system "
                    + "\nEnter 'search [file name]' to find a file in the network "
                    + "\nEnter 'leave' to exit the network "
                    + "\n-------------------------------------------------------";

            //initial menu
            menu = menu1;

            //till server is active
            while (true) {
                //display current menu and request input
                System.out.println(menu);
                input = br.readLine();
                String[] s = input.split(" ");

                //if 'view' command request is issued by node
                if (s.length == 1 && s[0].equals("view") && part_of_network == true) {
                    n.viewMachineDetils();

                    //if 'exit' command request is issued by node when not a part of the network
                } else if (s.length == 1 && s[0].equals("exit") && part_of_network == false) {
                    System.out.println("\n\nThank you for using the application!");
                    System.exit(0);

                    //if 'join' command request is issued by node when not a part of the network
                } else if (s.length == 1 && s[0].equals("join") && part_of_network == false) {
                    System.out.println("\n" + localmachineName + " is contacting the BootStrap server @" + BootStrapIP);
                    System.out.println("gaining entry into the system.");
                    Registry reg = LocateRegistry.getRegistry(BootStrapIP, port);
                    bootstrapIF bs = (bootstrapIF) reg.lookup("bootstrap");

                    //connect to network via bootstrap server
                    hashValue = Math.abs(nodeAddress.hashCode());
                    hashValue = hashValue % maxIDSpace;

                    //addToNetwork va bootstrap server
                    bs.addToNetwork(nodeAddress, hashValue);

                    //change connection status
                    System.out.println(localmachineName + " added to the network.");
                    part_of_network = true;

                    //update menu
                    menu = menu2;

                    //display machine details
                    if (part_of_network) {
                        n.viewMachineDetils();
                    }

                    //if 'join' command request is issued by node when a part of the network
                } else if (s.length == 1 && s[0].equals("join") && part_of_network == true) {
                    System.out.println("Invalid input, already a part of the network.");

                    //if 'leave' command request is issued by node when a part of the network
                } else if (s.length == 1 && s[0].equals("leave") && part_of_network == true) {

                    Registry reg = LocateRegistry.getRegistry(successor, port);
                    nodeIF node = (nodeIF) reg.lookup("node");
                    node.RemoveNode(nodeAddress, files, lowerLimit, upperLimit);

                    part_of_network = false;
                    menu = menu1;

                    //if 'leave' command request is issued by node when not a part of the network
                } else if (s.length == 1 && s[0].equals("leave") && part_of_network == false) {
                    System.out.println("Invalid input, not a part of the network.");

                    //if 'insert' command request is issued by node when a part of the network
                } else if (s.length == 2 && s[0].equals("insert") && part_of_network == true) {
                    String name = s[1];
                    String content = "", line;
                    try {
                        try {
                            FileReader fileReader = new FileReader(path + name);
                            BufferedReader file_in_br;
                            file_in_br = new BufferedReader(fileReader);

                            while ((line = file_in_br.readLine()) != null) {
                                content += line + "\n";
                            }
                            // Always close files.
                            file_in_br.close();
                        } catch (FileNotFoundException e) {
                            System.out.println("File not found!\nPlease try again and enter a valid/existing file name.");
                            continue;
                        }

                        int HashOfFile = (Math.abs(name.hashCode()) % maxIDSpace);
                        String pathway = "";

                        node n1 = new node();

                        if (upperLimit < (maxIDSpace / 2)) {
                            //if node is in first half
                            if ((Math.abs(upperLimit - HashOfFile)) > (maxIDSpace / 2)) {
                                //go to predecessor
                                pathway = n1.InsertFile(HashOfFile, name, content, pathway, "pre");
                            } else {
                                //go to successor
                                pathway = n1.InsertFile(HashOfFile, name, content, pathway, "suc");
                            }
                        } else {
                            //if node is in srcond half
                            if ((Math.abs(upperLimit - HashOfFile)) > (maxIDSpace / 2)) {
                                //go to successor
                                pathway = n1.InsertFile(HashOfFile, name, content, pathway, "suc");
                            } else {
                                //go to predecessor
                                pathway = n1.InsertFile(HashOfFile, name, content, pathway, "pre");
                            }
                        }

                        System.out.println("path selected: " + pathway);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //if 'insert' command request is issued by node when not a part of the network
                } else if (s.length == 2 && s[0].equals("insert") && part_of_network == false) {
                    System.out.println("Invalid input, not a part of the network.");

                } else if (s.length == 2 && s[0].equals("search") && part_of_network == true) {
                    String name = s[1];
                    int HashOfFile = (Math.abs(name.hashCode()) % maxIDSpace);
                    String pathway = localmachineName + "(" + upperLimit + ") -->";
                    String nextnode;
                    boolean file_at_current_node = false;
                    if (lowerLimit <= upperLimit) {
                        for (int i = lowerLimit; i <= upperLimit; i++) {
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            for (String f : files.get(i)) {
                                if (f.equals(name)) {
                                    file_at_current_node = true;
                                }
                            }
                        }
                    } else {
                        for (int i = lowerLimit; i < maxIDSpace; i++) {
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            for (String f : files.get(i)) {
                                if (f.equals(name)) {
                                    file_at_current_node = true;
                                }
                            }
                        }
                        for (int i = upperLimit; i > 0; i--) {
                            if (!files.containsKey(i)) {
                                files.put(i, new ArrayList<String>());
                            }
                            for (String f : files.get(i)) {
                                if (f.equals(name)) {
                                    file_at_current_node = true;
                                }
                            }
                        }
                    }

                    if (file_at_current_node == false) {
                        System.out.println("HashOfFile: " + HashOfFile);
                        if (upperLimit < (maxIDSpace / 2)) {
                            //if node is in first half

                            if ((Math.abs(upperLimit - HashOfFile)) > (maxIDSpace / 2)) {
                                //go to predecessor

                                nextnode = predecessor;
                                Registry reg = LocateRegistry.getRegistry(nextnode, port);
                                nodeIF node = (nodeIF) reg.lookup("node");
                                pathway = node.searchFile(HashOfFile, name, nodeAddress, upperLimit, pathway, "pre");
                                System.out.println(pathway);

                            } else {
                                //go to successor

                                nextnode = successor;
                                Registry reg = LocateRegistry.getRegistry(nextnode, port);
                                nodeIF node = (nodeIF) reg.lookup("node");
                                pathway = node.searchFile(HashOfFile, name, nodeAddress, upperLimit, pathway, "suc");
                                System.out.println(pathway);

                            }
                        } else {
                            //if node is in srcond half

                            if ((Math.abs(upperLimit - HashOfFile)) > (maxIDSpace / 2)) {
                                //go to successor
                                nextnode = successor;
                                Registry reg = LocateRegistry.getRegistry(nextnode, port);
                                nodeIF node = (nodeIF) reg.lookup("node");
                                pathway = node.searchFile(HashOfFile, name, nodeAddress, upperLimit, pathway, "suc");
                                System.out.println(pathway);

                            } else {
                                //go to predecessor
                                nextnode = predecessor;
                                Registry reg = LocateRegistry.getRegistry(nextnode, port);
                                nodeIF node = (nodeIF) reg.lookup("node");
                                pathway = node.searchFile(HashOfFile, name, nodeAddress, upperLimit, pathway, "pre");
                                System.out.println(pathway);

                            }
                        }
                    } else {
                        System.out.println("File " + name + " is present at " + localmachineName + " itself");
                    }
                    
                } else if (s.length == 2 && s[0].equals("search") && part_of_network == false) {
                    System.out.println("Invalid input, not a part of the network.");

                } else {
                    System.out.println("Invalid Input.");
                }

            }
        } catch (NotBoundException | AccessException ex) {
            Logger.getLogger(node.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

}
