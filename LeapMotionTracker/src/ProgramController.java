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
	static UserWindow userList;
	static SavingWindow progBar;
	
	//Database Communicator.
	static DatabaseController database;
	private static final String DB_URL = "data/data.db";
	
	//Indicators of programs running.
	static boolean procedureView;
	static boolean playbackView;
	
	//GUI Variables.
	public static final int START_BAR_HEIGHT = 48;
	
	//IPC
	public static ProcessCommunicator messageSender;
	public static final int VISUALIZER_READY = 1;
	public static final int VISUALIZER_PLAYBACK = 2;
	
	public static void main(String[] args) {
		//Creates the database function.
		database = new DatabaseController(DB_URL);
		
		//No programs are running.
		playbackView = false;
		procedureView = false;
		
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates the user list window.
					userList = new UserWindow();
					
					//Positions the window accordingly.
			        userList.setLocationRelativeTo(null);
			        
					//Shows the user list window.
					userList.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//We create a new process communicator.
		try{
			messageSender = new ProcessCommunicator();
		} catch (Exception e){
			e.printStackTrace();
			//The communicator failed for some reason.
			//TODO: MAKE THIS BETTER
			ProgramController.createDialog("<html>There was a problem starting the process communicator." +
					"<br>Please check your network settings or contact your system administrator.<br>" +
					"The program will now terminate.</html>", 
					"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	/**
	 * Runs the playback view.
	 * @param userName The user name of the view.
	 */
	public static void runPlaybackView(String userName) {
		//First, alerts that it's a playback view.
		playbackView = true;
		
		//Hides the user list.
		userList.setVisible(false);
		
		//Starts playback view
		PlaybackController.startPlaybackProgram(userName, database);
	}
	
	/**
	 * Runs when the user selects for the
	 * procedure view to be run.
	 */
	public static void runProcedureView(String user){
		//First, alerts that it's a procedure view.
		procedureView = true;
		
		//Hides the user list.
		userList.setVisible(false);
		
		//Activates the Procedure controller.
		ProcedureController.startProcedureProgram(user, database);
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
		if (message.length() < 3){
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

		if (procedureView) ProcedureController.receiveMessage(code);
		if (playbackView) PlaybackController.receiveMessage(code);
	}
	
	/**
	 * Retrieves all database users 
	 * @return A vector containing all the database records.
	 */
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

	/**
	 * Creates a new user and adds it to the database.
	 * @param userName The new username. Must be unique.
	 * @param firstName The first name of the user.
	 * @param lastName The last name of the user.
	 * @return Success code.
	 */
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
	
	public static void createProgressBar(final int low, final int high) {
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates the new progress bar window.
					progBar = new SavingWindow(low, high);
					
					//Positions the windows accordingly.
			        progBar.setLocationRelativeTo(null);
			        
					//Shows the user list window.
					progBar.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void updateProgressBar(int newLow) {
		//Check that there is actually an instance.
		if (progBar == null){
			return;
		}
		
		progBar.updateValue(newLow);
	}

	public static void closeProgressBar() {
		//Destroys the progress bar.
		progBar.setVisible(false);
		progBar = null;
	}
	
	public static Vector<Vector<String>> getSessions(String userName) {
		// TODO Auto-generated method stub
		return null;
	}
}
