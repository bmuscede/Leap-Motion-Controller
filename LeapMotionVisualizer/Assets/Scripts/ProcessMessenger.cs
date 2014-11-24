using System;
using System.Threading;
using System.Collections;
using System.Net;
using System.Net.Sockets;
using System.IO;
using UnityEngine;
using System.Collections;

public class ProcessMessenger : MonoBehaviour {
	private const string LOCAL_HOST = "127.0.0.1"; //The address of the local host.
	private const int DEFAULT_SEND_SOCKET = 50000; //For the client.
	private const int DEFAULT_RECEIVE_SOCKET = 40000; //For the server.
	
	//IPC CODES
	private const string STARTUP_CODE = "001"; //The startup code sent to Java.
	private const string PLAYBACK_MODE = "002"; //The playback mode code sent to Unity
	private const string PLAYBACK_ACK = "003"; //The ack code for playback mode sent to Java.

	//Indicator codes.
	private bool playback_mode;

	//GameObjects
	GameObject handController;
	GameObject playbackController;

	// Use this for initialization
	void Start () {
		//First, gets objects running in the program
		handController = GameObject.Find ("HandController");
		
		//Now creates a new client socket thread.
		sendMessage (STARTUP_CODE);

		//Creates a new server thread and starts the server.
		Loom.RunAsync (() => {
			runServer ();
		});
	}
	
	public bool sendMessage(string contents){
		System.Net.Sockets.TcpClient 
			clientSocket = new System.Net.Sockets.TcpClient ();

		try {
			//Connects to the localhost.
			clientSocket.Connect (LOCAL_HOST, DEFAULT_SEND_SOCKET);
			
			//Now we send the initial startup message.
			NetworkStream clientMessage = clientSocket.GetStream ();

			byte[] message = System.Text.Encoding.ASCII.GetBytes (contents + "\n");
			clientMessage.Write (message, 0, message.Length);
			clientMessage.Flush ();
		} catch (SocketException ex) {
			//If this is the case, we have a problem connecting to the server.
			return false;
		}
		
		return true;
	}
	
	//The main method for the server.
	private void runServer(){
		//Creates a TCPListener on the receive socket.
		TcpListener serverSocket = new TcpListener(DEFAULT_RECEIVE_SOCKET);
		
		//Connecting socket for connecting agent.
		TcpClient connectingSocket = default(TcpClient);
		
		//Now, starts the server.
		serverSocket.Start ();
		
		//Loops until the program terminates.
		while (true) {
			//Waits for a connection.
			connectingSocket = serverSocket.AcceptTcpClient();
			
			//Gets data sent from the client.
			try{
				//Gets the stream and reads from it.
				NetworkStream networkStream = connectingSocket.GetStream();
				StreamReader reader = new StreamReader(networkStream);
				string message = reader.ReadLine();

				//Looks at the message that was received.
				Debug.Log (message);
				receivedMessage(message);
			} catch (Exception ex){
				//There was a problem parsing the message.
			}

			//Finally, closes the connection.
			connectingSocket.Close();
		}
	}
	
	// Update is called once per frame
	void Update () {
		if (playback_mode == true) {
			Destroy (handController);
		}
	}

	//Manages codes that are sent to it.
	public void receivedMessage(string message){
		//Gets a message from the Process Communicator.
		if (message.Length != 3) return;

		//Finds which code was sent.
		if (string.Compare (message, PLAYBACK_MODE) == 0) {
			//Sends an ack back to the controller.
			sendMessage (PLAYBACK_ACK);

			//We now destroy the hand controller.
			playback_mode= true;
		}
	}
}
