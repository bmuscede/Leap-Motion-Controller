import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Files;
import java.util.Vector;

import javax.swing.JOptionPane;


public class PlaybackController {
	//GUI Variables
	static SessionWindow playback;
	static PlaybackStatusWindow status;
	
	//Database Variables
	static DatabaseController db;
	
	//Playback Variables
	static FramePlayback sessionPlayback;
	static Process visualizerProcess;
	
	//User Variables
	static String currentUser;
	
	/**
	 * Starts the playback view.
	 * Creates all the windows and starts
	 * the appropriate visualizer/options.
	 * @param userName The username of the person using it.
	 * @param database The database controller.
	 */
	public static void startPlaybackProgram(final String userName, final DatabaseController database){
		//Sets the database.
		db = database;
		
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates each of the new windows.
					playback = new SessionWindow(userName);
					
					//Positions the windows accordingly.
			        playback.setLocationRelativeTo(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Gets the current user.
				currentUser = userName;
				
				//Shows the next windows.
				playback.setVisible(true);
			}
		});
	}

	public static void receiveMessage(int code) {
		//Now parses the message.
		switch(code){
			case ProgramController.VISUALIZER_READY:
				//We need to send a "playback message back".
				ProgramController.messageSender.sendMessage(
						ProgramController.S_VISUALIZER_PLAYBACK);
				break;
			
			case ProgramController.VISUALIZER_PLAYBACK:
				//The visualizer acknowledged the request.
				status.changeMessage(PlaybackStatusWindow.READY);
				status.readForPlayback();
				break;
			
			case ProgramController.STOP_CODE:
				//The visualizer has played through the frames.
				status.changeMessage(PlaybackStatusWindow.READY);
				status.readForPlayback();
			default:
				return;
		}
	}		
	
	/**
	 * Retrieves all sessions for a particular user
	 * @param userName The username of the current user. 
	 * @return A vector containing all the database records.
	 */
	public static Vector<Vector<String>> getSessions(String userName) {
		//Creates the SQL statement.
		String sql = "SELECT * FROM Session WHERE UserName = \"" + userName + "\";";
		
		//Runs the statement.
		Vector<Vector<String>> userData = db.getData(sql);
		
		if (userData == null){
			//TODO: Implement something wrong code.
		}
		
		return userData;
	}

	public static boolean deleteSessionUser(String userName, String session) {
		String sql = "DELETE FROM Session WHERE UserName = \"" + userName + "\" " +
				"AND SessionId = " + session + ";";
		
		//Also deletes the session from disk.
		File deletedSession = new File(System.getProperty("user.dir") + "/data/" + userName + "/" + session);
		deletedSession.delete();
		
		//Runs the statement.
		return db.writeSQLStatement(sql);
	}

	public static void startPlayback(final String session) {
		//Check to see if we have invoked the start playback program command.
		if (currentUser == null) return;
		
		//Hides the old GUI
		playback.setVisible(false);
		playback = null;
		
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates each of the new windows.
					status = new PlaybackStatusWindow();
					
					//Positions the windows accordingly.
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					status.setLocation((int) (dim.getWidth() / 2) - (status.getWidth() / 2), 
							(int) dim.getHeight() - status.getHeight() - ProgramController.START_BAR_HEIGHT);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Shows the next windows.
				status.setVisible(true);
				
				//Loads in the frames.
				readFrames(session);
				
				//Gets the visualizer ready.
				String workingDir = System.getProperty("user.dir");
				try{
					visualizerProcess = new ProcessBuilder(
							workingDir + "/Visualizer/Visualizer.exe").start();
					
					//If that works, create the shutdown hook.
					Runtime.getRuntime().addShutdownHook(new Thread(){
						public void run(){
							//Kills the visualizer when the program terminates.
							visualizerProcess.destroy();
						}
					});
				} catch (Exception e){
					//The program had trouble executing. Likely doesn't exist!
					ProgramController.createDialog("<html>The program \'Visualizer.exe\' could not be " +
							"found on your system!<br>The program will now exit.", 
							"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
	}
	
	private static void readFrames(String session){
		//Obtains the frames from the database.
		Vector<byte[]> frameStream = db.getFrames(currentUser, session);
		
		//Creates a playback object.
		sessionPlayback = new FramePlayback(frameStream);
	}

	public static void play() {
		sessionPlayback.play();
	}

	public static void stopPlayback() {
		sessionPlayback.stopPlayback();		
	}
	
	public static void pause(){
		sessionPlayback.pause();
	}
}
