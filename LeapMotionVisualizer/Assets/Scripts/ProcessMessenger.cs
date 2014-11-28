using System;
using System.Threading;
using System.Collections;
using System.Net;
using System.Net.Sockets;
using System.IO;
using UnityEngine;
using System.Collections.Generic;
using System.Linq;

public class ProcessMessenger : MonoBehaviour {
	private const string LOCAL_HOST = "127.0.0.1"; //The address of the local host.
	private const int DEFAULT_SEND_SOCKET = 50000; //For the client.
	private const int DEFAULT_RECEIVE_SOCKET = 40000; //For the server.
	
	//IPC CODES
	private const string STARTUP_CODE = "001"; //The startup code sent to Java.
	private const string PLAYBACK_MODE = "002"; //The playback mode code sent to Unity
	private const string PLAYBACK_ACK = "003"; //The ack code for playback mode sent to Java.
	public const string STOP_CODE = "004"; //The stop code for playback mode sent to Java.

	//Indicator codes.
	private bool playback_mode;
	private bool playback_send_coordinates;

	//GameObjects
	GameObject handController;
	HandController hcScript;

	//Playback Objects.
	Queue<byte[]> frameBuffer = new Queue<byte[]>();

	// Use this for initialization
	void Start () {
		//First, gets objects running in the program
		handController = GameObject.Find ("HandController");
		hcScript = (HandController)handController.GetComponent (typeof(HandController));

		//Sets up the booleans
		playback_mode = false;
		playback_send_coordinates = false;

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

				if (playback_send_coordinates == false){
				  StreamReader reader = new StreamReader(networkStream);
				  string message = reader.ReadLine();

				  //Looks at the message that was received.
				  receivedMessage(message);
			    } else {
				  byte[] frameSerial = ReadStream(networkStream);

				  //Locks the variable.
				  lock(frameBuffer){
				    frameBuffer.Enqueue(frameSerial);
				  }
			    }
			} catch (Exception ex){
				//There was a problem parsing the message.
			}

			//Finally, closes the connection.
			connectingSocket.Close();
		}
	}

	private byte[] ReadStream(NetworkStream ns){
		List<byte> bl = new List<byte>();
		byte[] receivedBytes = new byte[128];
		while (true){
			int bytesRead = ns.Read(receivedBytes, 0, receivedBytes.Length);
			if (bytesRead == receivedBytes.Length){
				bl.AddRange(receivedBytes);
			} else {
				bl.AddRange(receivedBytes.Take(bytesRead));
				break;
			}
		}
		return bl.ToArray();
	}

	// Update is called once per frame
	void Update () {
		if (playback_mode == true) {
			//Sets up the HandController to play back frames.
			hcScript.NotifyPlayback();

			//Sets the program for the next step.
			playback_send_coordinates = true;
			playback_mode = false;
		}

		if (playback_send_coordinates == true) {
			//Locks the variable.
			lock(frameBuffer){
				if (frameBuffer.Count > 0){
				  //Checks to see if null byte.
				  byte[] current = frameBuffer.Dequeue();

				  if (current.Length == 1 && current[0] == 0){
						hcScript.NotifyStop();
				  } else if (current.Length == 2 &&
					           current[0] == 0 && current[1] == 0){
						hcScript.PausePlayback(true);
				  } else if (current.Length == 3 &&
					           current[0] == 0 && current[1] == 0  &&
					           current[2] == 0){
						hcScript.PausePlayback(false);
				  } else {
			            hcScript.SendFrame (current);
				  }
				}
			}
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
