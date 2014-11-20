import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Dictionary;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Hand;

public class ProgramController {
	//GUI Objects
	static StatusWindow status;
	static HandDataWindow handLeft;
	static HandDataWindow handRight;
	static StatusBoxWindow vizStatus;
	static UserWindow userList;
	
	//Leap Motion Objects
	static LeapMotionController controller;
	static ProcessCommunicator messageSender;
	
	//Visualizer Variables
	static boolean visualizerFailure; 
	static Process visualizerProcess;
	
	//Database Communicator.
	static DatabaseController database;
	private static final String DB_URL = "data/data.db";
	
	//Session Variables.
	static String currentUser;
	
	//GUI Variables.
	public static final int START_BAR_HEIGHT = 48;
	
	//Collection status codes.
	public static final int CONNECTED = 1;
	public static final int NOT_CONNECTED = 0;
	public static final int HANDS_PRESENT = 2;
	public static final int PAUSED = 3;
	public static final int COLLECTING = 4;
	public static final int NOT_COLLECTING = 5;
	
	//IPC Codes
	public static final int VISUALIZER_READY = 1;
	
	//Hand Codes.
	public static final int HAND_LEFT = 0;
	public static final int HAND_RIGHT = 1;
	
	public static void main(String[] args) {
		//Provides full program functionality.
		visualizerFailure = false;
		
		//Creates the database function.
		database = new DatabaseController(DB_URL);
		
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates each of the new windows.
					status = new StatusWindow();
					handLeft = new HandDataWindow(true);
					handRight = new HandDataWindow(false);
					userList = new UserWindow();
					
					//Positions the windows accordingly.
			        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			        status.setLocation(0, 0);
			        handLeft.setLocation(0, (int) dim.getHeight() - handLeft.getHeight() - START_BAR_HEIGHT);
			        handRight.setLocation((int) dim.getWidth() - handRight.getWidth(),
			        		(int) dim.getHeight() - handRight.getHeight() - START_BAR_HEIGHT);
			        userList.setLocationRelativeTo(null);
			        
					//Shows the user list window.
					userList.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void leapStatus(int statusCode){
		//Acts on the model based on the Leap Controller.
		switch (statusCode) {
			case CONNECTED:
				//Sets the status of the status bar.
				status.lblStatus.setText("Status: Connected (Ready)");
				
				//Enables the start button.
				status.btnStartStop.setEnabled(true);
				break;
				
			case NOT_CONNECTED:
				//Sets the status of the status bar.
				status.lblStatus.setText("Status: Not Connected");
				
				//Disables everything from working.
				status.btnStartStop.setEnabled(false);
				status.sendStopMessage();
				handLeft.lblErrorSymbol.setVisible(true);
				handLeft.lblHandNotPresent.setVisible(true);
				handRight.lblErrorSymbol.setVisible(true);
				handRight.lblHandNotPresent.setVisible(true);
				break;
				
			case HANDS_PRESENT:
				//Sets the status of the status bar.
				status.lblStatus.setText("Status: Hands Present (Ready)");
				
				//Enables the start button.
				status.btnStartStop.setEnabled(true);
				break;
				
			case COLLECTING:
				//Sets the status of the status bar.
				status.lblStatus.setText("Status: Collecting Data");
				break;
			
			case PAUSED:
				//Sets the status of the status bar.
				status.lblStatus.setText("Status: Paused Data Collection");
				break;
				
			default:
				break;
		}
	}

	public static void sendMessage(int statusCode) {
		//Changes the state in the LeapMotion thread.
		switch(statusCode){
			case COLLECTING:
				controller.setCollection(true);
				controller.setPaused(false);
				break;
				
			case NOT_COLLECTING:
				controller.setCollection(false);
				controller.setPaused(false);
				break;
				
			case PAUSED:
				//Sets paused to be true.
				controller.setPaused(true);
				break;
		}
	}

	/**
	 * Sends messages to the GUI about position data.
	 * If there is no hand data from the controller, it sends NULL.
	 * @param handCode The left or right hand for the data.
	 * @param handData The data of that hand. Null means no data available. 
	 * @param f 
	 */
	public static void sendHandData(int handCode, String handData, int confidence) {
		//Sees if data is available for the frame.
		if (handData == null){
			//Checks which hand has no data.
			if (handCode == HAND_LEFT){
				//Adds no hand available.
				handLeft.lblErrorSymbol.setVisible(true);
				handLeft.lblHandNotPresent.setVisible(true);
				handLeft.lblData.setVisible(false);
				handLeft.lblConfidence.setVisible(false);
				handLeft.sepConfidenceSep.setVisible(false);
			} else {
				//Adds no hand available.
				handRight.lblErrorSymbol.setVisible(true);
				handRight.lblHandNotPresent.setVisible(true);
				handRight.lblData.setVisible(false);
				handRight.lblConfidence.setVisible(false);
				handRight.sepConfidenceSep.setVisible(false);
			}
			
			//We're done now.
			return;
		}
		
		//Now, checks which hand we're on.
		if (handCode == HAND_LEFT){
			//Adds data
			handLeft.lblErrorSymbol.setVisible(false);
			handLeft.lblHandNotPresent.setVisible(false);
			handLeft.lblData.setVisible(true);
			handLeft.lblData.setText(handData);
			handLeft.lblConfidence.setVisible(true);
			handLeft.sepConfidenceSep.setVisible(true);
			handLeft.updateConfidence(confidence);
		} else {
			//Adds no hand available.
			handRight.lblErrorSymbol.setVisible(false);
			handRight.lblHandNotPresent.setVisible(false);
			handRight.lblData.setVisible(true);
			handRight.lblData.setText(handData);
			handRight.lblConfidence.setVisible(true);
			handRight.sepConfidenceSep.setVisible(true);
			handRight.updateConfidence(confidence);
		}
	}
	
	/**
	 * Alerts the user of a specific error or gives
	 * them certain information.
	 * @param infoMessage The message to be displayed.
	 * @param titleBar The title of the box.
	 */
	public static void createDialog(String infoMessage, String titleBar, int messageType)
    {
		//Creates a message dialog.
        JOptionPane.showMessageDialog(null, 
        		infoMessage, titleBar, messageType);
    }

	/**
	 * Gets called whenever a message is received by the message sender.
	 * @param message Simple format. The first three chars are the status
	 * code of the message, the rest is optional and indicates any thing that
	 * needs to be sent. More than one option means spaces are between each of
	 * the options.
	 */
	public static void messageReceived(String message) {
		//Checks for a valid message.
		if (message.length() < 3 || visualizerFailure == true){
			return;
		}
		
		//Gets the first three characters.
		int code;
		try{
			code = Integer.parseInt(message.substring(0, 3));
		} catch (Exception e){
			//There is something wrong with the message.
			return;
		}

		//Now parses the message.
		switch(code){
			case VISUALIZER_READY:
				//The visualizer is ready.
				vizStatus.setVisible(false);
				break;
				
			default:
				return;
		}
	}
	
	public static Vector<Vector<String>> getDatabaseUsers(){
		//Creates the SQL statement.
		String sql = "SELECT User.UserName, User.FName, User.LName, COUNT(Session.UserName)" +
				" FROM User LEFT JOIN Session ON (User.UserName = Session.UserName)" +
				" GROUP BY User.UserName;";
		
		//Runs the statement.
		Vector<Vector<String>> userData = database.getData(sql);
		
		if (userData == null){
			//TODO: Implement something wrong code.
		}
		
		return userData;
	}

	public static boolean addNewUser(String userName, String firstName, String lastName) {
		String sql = "INSERT INTO User VALUES(\"" + userName + "\", \"" + firstName + "\", \"" + lastName
				+ "\");";
		
		//Runs the statement.
		return database.writeSQLStatement(sql);
	}
	
	public static boolean deleteUser(String userName) {
		String sql = "DELETE FROM User WHERE UserName = \"" + userName + "\";";
	
		//Runs the statement.
		return database.writeSQLStatement(sql);
	}
	
	public static void startMainProgram(String userName) {
		//Gets the current user.
		currentUser = userName;
		
		//Shows the next windows.
        status.setVisible(true);
		handLeft.setVisible(true);
		handRight.setVisible(true);
		userList.setVisible(false);
		
		//Builds the leap motion controller.
		controller = new LeapMotionController(database, currentUser);
		controller.start();

		//Starts the visualizer maximized.
		String workingDir = System.getProperty("user.dir");
		try{
			visualizerProcess = new ProcessBuilder(
					workingDir + "/Visualizer/Visualizer.exe").start();
			
			//If that works, create the shutdown hook.
			Runtime.getRuntime().addShutdownHook(new Thread(){
				public void run(){
					//Kills the visualizer when the program terminates.
					ProgramController.visualizerProcess.destroy();
					
					//Kills the controller.
					controller.destroyController();
				}
			});
		} catch (IOException e){
			//The program had trouble executing. Likely doesn't exist!
			visualizerFailure = true;
			createDialog("<html>The program \'Visualizer.exe\' could not be " +
					"found on your system!<br>The program will now continue " +
					"but in limited mode.</html>", 
					"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
		}
		//Initializes communication between the visualizer and this program
		if (!visualizerFailure){
			//We create a new process communicator.
			try{
				messageSender = new ProcessCommunicator();
			} catch (Exception e){
				e.printStackTrace();
				//Making the communicator failed. We assume visualizer failure.
				visualizerFailure = true;
				createDialog("<html>Something went wrong with \'Visualizer.exe\'." +
						"<br>Please check your system settings.<br>The program will continue " +
						"but in limited mode.</html>", 
						"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//Finally, adds a dialog indicating the visualizer is not ready.
		if (!visualizerFailure){
			//Creates a message box telling the user to be patient.
			vizStatus = 
					new StatusBoxWindow("<html><center>Starting the Visualizer...<br>" +
							"Please be patient.</center></html>");
			vizStatus.setVisible(true);
		}
	}
}
