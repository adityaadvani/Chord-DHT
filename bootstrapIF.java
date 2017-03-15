//package chord;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Aditya Advani
 */
public interface bootstrapIF extends Remote {
    
    public void addToNetwork(String nodeIP, int IPhash) throws RemoteException;
    public void DeleteNotify(String nodeIP, int IPhash) throws RemoteException;
    public void updateLeaderNode(String node) throws RemoteException;

}
