import java.io.*;
import java.rmi.*;
import java.util.*;

/*!
 * \brief Defines the chord functionality.
 */

public interface ChordMessageInterface extends Remote {
/*!
 * \brief get the last written time of an file
 * \param guid guid of the file
 * \return time of the last written
 */
public Date getLastWritten(Long guid) throws RemoteException;

/*!
 * \brief transfer the key from lastWritten Hashmap to fileBusy Hashmap
 * \param ip ip of the other user
 * \param port of other user
 * \return the chord
 */
public void transferKey(Long guid, Date log) throws RemoteException;

/*!
 * \brief check if file exists
 * \param guid guid of the file
 * \return true if file exists/false if it does not
 */
public boolean fileExists(Long guid) throws RemoteException;

/*!
 * \brief check if file can be committed
 * \param guid guid of the file
 * \param Date the last date/time the user read the file
 * \return true if file can commit/false if it can not
 */
public boolean canCommit(Long guid, Date userLastRead) throws RemoteException;

/*!
 * \brief commit the file
 * \param guid guid of the file
 * \param Date the date/time that the commit occurs
 */
public void commit(Long guid, Date write) throws RemoteException;

/*!
 * \brief abort the commit
 * \param guid guid of the file
 */
public void abort(Long guid) throws RemoteException;

/*!
 * \brief gets the prefedd
 * \return the predecessor
 */
public ChordMessageInterface getPredecessor()  throws RemoteException;

/*!
 * \brief	find the successor of an obj
 * \param       key key of the obj
 * \return the sucessor
 */
ChordMessageInterface locateSuccessor(long key) throws RemoteException;

/*!
 * \brief	func to find the closest preceding node
 * \param       key	key of the obj
 * \return the successor of the obj
 */
ChordMessageInterface closestPrecedingNode(long key) throws RemoteException;

/*!
 * \brief	func to join the ring
 * \param ip	ip of the connecting predessor
 * \param port	port of the connecting prefessor
 */
public void joinRing(String Ip, int port)  throws RemoteException;

/*!
 * \brief
 * \param
 * \return
 */
public void notify(ChordMessageInterface j) throws RemoteException;

/*!
 * \brief checks for status of this chord
 * \return state of the chord in boolean
 */
public boolean isAlive() throws RemoteException;

/*!
 * \brief get guid oh node
 * \return guid
 */
public long getId() throws RemoteException;

/*!
 * \brief change the sucessor
 * \param s sucessor
 */
public void setSuccessor(ChordMessageInterface s) throws RemoteException;

/*!
 * \brief change the predecessor
 * \param p predecessor
 */
public void setPredecessor(ChordMessageInterface p) throws RemoteException;

/*****************************/

/**
 * \brief cancels the timer
 **********************************/
public void cancelTimer() throws RemoteException;

/*****************************/

/**
 * \brief restarts the timer
 **********************************/
public void restartTimer() throws RemoteException;

/*!
 * \brief write the file
 * \param guidObject	guid of the file
 * \param stream		the file
 */
public void put(long guidObject, InputStream file) throws IOException, RemoteException;

/*!
 * \brief retrieve a file in the ring
 * \param guidobject the file to retrieve
 * \return the file
 */
public InputStream get(long guidObject) throws IOException, RemoteException;

/*!
 * \brief delete a file in the ring
 * \param guidobject the file to delete
 */
public void delete(long guidObject) throws IOException, RemoteException;
}
