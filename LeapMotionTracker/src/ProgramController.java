import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

public class ProgramController {
	//GUI Objects
	static UserWindow userList;
	static SavingWindow progBar;
	static MetricSettingsWindow window;
	
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
	public static final int VISUALIZER_READY = 001; //Message that the visualizer is ready.
	public static final String S_VISUALIZER_PLAYBACK = "002"; //Sent to the visualizer for playback.
	public static final int VISUALIZER_PLAYBACK = 003; //Playback ack received from visualizer.
	public static final int STOP_CODE = 004; //Stop code received from visualizer.
	
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
		if (!database.writeSQLStatement(sql)){
			return false;
		}
		
		//Now, we create a new user file.
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir + "/data/" + userName);
		
		//Makes the user directory.
		file.mkdir();
		
		return true;
	}
	
	public static boolean deleteUser(String userName) {
		String sql = "DELETE FROM User WHERE UserName = \"" + userName + "\";";
	
		//We delete the user and sessions from disk.
		try {
			String workingDirectory = System.getProperty("user.dir");
			FileUtils.deleteDirectory(new File(workingDirectory + "/data/" + userName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		if (progBar == null) return;
		progBar.setVisible(false);
		progBar = null;
	}
	
	public static Vector<Vector<String>> getSessions(String userName) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void showMetrics(String userName, String session) {
		window = new MetricSettingsWindow(userName, session);
		window.setModal(true);
		window.setVisible(true);
	}
	
	public static void computeMetrics(String userName, String session,
			boolean smoothening, boolean confidence, boolean observe, boolean sensitivity, 
			int smootheningVal, int confidenceVal, int frameOrder, int sensitivityValue){
		//We create a new metrics object.
		MetricsCalculator calculate = new MetricsCalculator(userName, session);
		
		//We now set metrics settings.
		calculate.setFrameSmoothening(smoothening);
		calculate.setSmootheningWindow(smootheningVal);
		if (confidence){
			calculate.setConfidenceRemovalValue(confidenceVal);
		}
		if (observe){
			calculate.setFrameExamineValue(frameOrder);
		}
		if (sensitivity){
			calculate.setSensitivityValue(sensitivityValue);
		}
		//Finally, we start the calculations.
		calculate.start();
		
		//Close the window.
		window.dispose();
		window = null;
		
		//Now we create the next window.
		MetricsStatusWindow status = new MetricsStatusWindow(userName, session);
		status.waitingForData(calculate);
		status.setVisible(true);
	}

	public static void showMetricsData(String userName, String session) {
		//We first need to get the MetricID.
		String ID = database.checkForMetricID(userName, session);
		
		if (ID == null){
			ProgramController.createDialog("<html>Something went wrong!<br>The data does not exist. Try" +
					" recomputing the metric!", "Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Now, we need to retreive the data.
		Vector<Vector<String>> data = database.getData("SELECT * FROM SessionMetrics WHERE MetricID = " + ID + ";");
		
		//Finally we start the status window.
		MetricsStatusWindow status = new MetricsStatusWindow(userName, session);
		status.setUpDataDatabase(data.elementAt(1));
		status.setVisible(true);
	}
}
