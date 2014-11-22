import java.awt.EventQueue;
import java.util.Vector;


public class PlaybackController {
	//GUI Variables
	static SessionWindow playback;
	
	//Database Variables
	static DatabaseController db;
	
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
			
		//Runs the statement.
		return db.writeSQLStatement(sql);
	}
}
