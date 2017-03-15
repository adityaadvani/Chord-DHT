//package chord;

import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Aditya Advani
 */
public interface nodeIF extends Remote {
    
    public void InsertionFirst() throws RemoteException;
    public void InsertionNewNode(String nodeAddress, int IPhash) throws RemoteException, AccessException;
    public void RemoveNode(String nodeIP, HashMap<Integer,ArrayList<String>> f, int lLimit, int uLimit) throws RemoteException;
    public String getSuccessor() throws RemoteException;
    public String getPredecessor() throws RemoteException;
    public void setSuccessor(String nodeIP) throws RemoteException;
    public void setPredecessor(String nodeIP) throws RemoteException;
    public int getIPhash() throws RemoteException;
    public void setUpperLimit(int limit) throws RemoteException;
    public int getUpperLimit() throws RemoteException;
    public void setLowerLimit(int limit) throws RemoteException;
    public int getLowerLimit() throws RemoteException;
    public String getName() throws RemoteException;
    public String InsertFile(int HashOfFile, String FileName, String Content, String pathway, String direction) throws RemoteException;
    public boolean isLeaderNode() throws RemoteException;
    public void setFileMap() throws RemoteException;
    public void addFile(int i, String file, String content) throws RemoteException;
    public void removeFile(String file) throws RemoteException;
    public String getFileContent(String file) throws RemoteException;
    public void setLeaderNode(boolean status) throws RemoteException;
    public String searchFile(int HashOfFile, String name, String nodeAddress, int upperLimit, String pathway, String direction) throws RemoteException;
}
