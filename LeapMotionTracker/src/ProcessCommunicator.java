import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class allows processes to send messages
 * back and forth between each other. As of now,
 * for platform independence, sockets are used
 * on the local host to pass messages.
 * @author Bryan J Muscedere
 *
 */
public class ProcessCommunicator extends Thread{
	private static final int DEFAULT_SEND_SOCKET = 40000; //For the client.
	private static final int DEFAULT_RECEIVE_SOCKET = 50000; //For the server.
	private ServerSocket receivingManager; //Server for receiving messages.
	private Socket receivingClient; //Client socket information for receiving messages.
	private Socket sendingManager; //Client for sending messages.
	
	/**
	 * Creates a socket on the local host in
	 * which processes can communicate.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public ProcessCommunicator() throws UnknownHostException, IOException{
		//First, initializes the client for sending messages.
		sendingManager = null;
		
		//Next, we want to create the server for receiving messages.
		receivingManager = new ServerSocket(DEFAULT_RECEIVE_SOCKET);
		this.start();
	}
	
	/**
	 * This is where the process communicator waits for incoming
	 * messages. If a request to connect is received, the 
	 * program signals the Program Controller.
	 */
	public void run(){
		while(true){
			try {
				//Waits for a client to connect.
				receivingClient = receivingManager.accept();
				
				//Creates stream readers to receive data.
				BufferedReader input = new BufferedReader(new InputStreamReader(receivingClient.getInputStream()));
				
				//Gets the message and terminates the connection.
				String message = input.readLine();
				receivingClient.close();
				
				//Sends the message now to the controller.
				ProgramController.messageReceived(message);
			} catch (IOException e) {
				//If we hit this, we do nothing.
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a message to the server. Accepts a string
	 * message and passes it over. Either returns true
	 * or false depending on success.
	 * @param message The message to send to the server.
	 * @return A boolean indicating success.
	 */
	public boolean sendMessage(String message){
		//First, we initialize our connection.
		try {
			//First, initializes a connection to the sever.
			sendingManager = new Socket(InetAddress.getByName(null), DEFAULT_SEND_SOCKET);
			OutputStream messageSendStream = sendingManager.getOutputStream();
			
			//Next, using our output stream, we just send our message.
			messageSendStream.write(message.getBytes());
		} catch (UnknownHostException e) {
			//Something went wrong here.
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			//There was a problem with the streams.
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
}
