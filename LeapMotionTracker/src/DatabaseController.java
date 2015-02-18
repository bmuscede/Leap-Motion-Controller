import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;

import javax.print.attribute.Size2DSyntax;

import org.apache.commons.io.IOUtils;

public class DatabaseController extends Thread {
	private final String DATABASE_PREFIX = "jdbc:sqlite:";
	private Connection conn;
	private int frameID; //The id of the frame currently written.
	private int frameAddedID; //The id of the last frame added to buff.
	
	//Threading methods
	private Thread dbThread;
	private volatile Vector<byte[]> frameBuffer;
	private volatile boolean stpCol;
	
	//For writing frames
	private String framesUserID;
	private String framesSession;
	
 	//The default session id.
	private final String DEFAULT_SESSION = "0";
	
	/**
	 * This constructor registers a SQLite3 controller that then connects to the
	 * database. 
	 * @param dbURL The location (path of the sqlite database)
	 */
	public DatabaseController(String dbURL) {
		try{
			//Registers the SQLite3 driver.
			Class.forName("org.sqlite.JDBC");
			
			//Now connects to the database.
			conn = DriverManager.getConnection(DATABASE_PREFIX + dbURL);	
			
			//Sets a 0 frame ID.
			frameID = 0;
			frameAddedID = 0;
			
			//Creates the foreign key pragma.
			try {
				Statement query = conn.createStatement();
				query.executeUpdate("PRAGMA foreign_keys = ON;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			//Sets up the buffers.
			frameBuffer = new Vector<byte[]>();
			stpCol = false;
			
			//Sets up the session info.
			framesUserID = null;
			framesSession = null;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Passes information so that the frame writer knows which file to
	 * write to.
	 * @param userName The user ID of the user currently using.
	 * @param sessionNo The session number of the current session.
	 */
	public void setSessionInfo(String userName, String sessionNo){
		framesUserID = userName;
		framesSession = sessionNo;
	}
	
	/**
	 * Sets up the database controller so that it 
	 * continually writes to the database. This is
	 * done so long as there are items in the database.
	 */
	public void run() {
		//First, creates a new file.
		String path = System.getProperty("user.dir") + "/data/"
				+ framesUserID + "/" + framesSession;
		
		FileOutputStream fileWriter = null;
		try {
			//Checks whether the file can be opened. (IT WILL OVERWRITE)
			fileWriter = new FileOutputStream(path);
		} catch (FileNotFoundException e1) {
			//The file cannot be opened.
			e1.printStackTrace();
			return;
		}
		
		while(true){
			//We see if there are frames to write.
			if (frameBuffer.size() > 0){				
				try {
					//Writes the frame to the file.
					fileWriter.write(ByteBuffer.allocate(4)
							.putInt(frameBuffer.get(0).length).array());
					fileWriter.write(frameBuffer.get(0));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//Remove the frame.
				frameBuffer.remove(0);
				
				//Increment the frame ID.
				frameID++;
			}
			
			//Now we see if there are things that need to be done.
			if (stpCol){
				//Reports it to the GUI
				ProgramController.updateProgressBar(frameID);
				
				//Sees if we're done.
				if (frameID == frameAddedID){
					//Says we're done.
					stpCol = false;
					
					try {
						//Closes the file.
						fileWriter.close();
					} catch (IOException e) {
						//There was a problem closing.
						e.printStackTrace();
					}
					
					//Updates the GUI.
					ProgramController.closeProgressBar();
					break;
				}
			}
		}
	}
	
	/**
	 * Invokes the run method by forking to a new thread.
	 * This can be used to invoke a new Leap Motion
	 * listener. 
	 */
	public void start(){
		//Only runs if this has been set.
		if (framesUserID == null) return;
		
		//Checks to see if start hasn't already been invoked.
		if (dbThread == null){
			//Creates a new thread and starts it.
			dbThread = new Thread(this);
			dbThread.start();
		} else {
			if (!stpCol){
				//Sets a 0 frame ID.
				frameID = 0;
				frameAddedID = 0;
				
				//Flush the buffer.
				frameBuffer.clear();
				dbThread = new Thread(this);
				dbThread.start();
			}
		}
	}
	
	public int[] stoppedCollecting() {
		//Gets the high and low end of the values.
		int[] values = new int[2];
		values[0] = frameID;
		values[1] = frameAddedID;
		
		//Notifies that things have stopped.
		stpCol = true;
		
		return values;
	}
	
	/**
	 * Creates a new session to store collection data to.
	 * @param userID The user ID of the user.
	 * @param sessionNo The session number.
	 * @return A boolean indicating success or failure.
	 */
	public boolean writeSession(String userID, String sessionNo){
		String sessionStatement = "INSERT INTO Session(UserName, SessionId, STime, SDate) VALUES(\"" +
				userID + "\", " + sessionNo + ", 0, 0);";
		
		//Now writes it into the db.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sessionStatement);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Writes a frame to the database under a specific user and session.
	 * This will allow data to be segmented depending on the user and
	 * which session they're working on.
	 * @param userID The ID for the current user working.
	 * @param session The session ID for the current session of the user working.
	 * @param currentFrame The frame to be written.
	 */
	public boolean writeFrame(byte[] frameData){
		frameBuffer.add(frameData);
		frameAddedID++;
		
		return true;
	}
	
	/**
	 * Writes an SQL statement that is general.
	 * @param sql The sql statement to be executed.
	 * @return Whether or not the execution of the statement was successful.
	 */
	public boolean writeSQLStatement(String sql){
		//Executes the passed SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		//If there is no error, returns true.
		return true;
	}

	/**
	 * Runs a retrieval SQL statement on the database.
	 * @param sql The SQL statement to run.
	 * @return A vector of a vector containing the data.
	 */
	public Vector<Vector<String>> getData(String sql){
		//Creates a vector for the results.
		Vector<Vector<String>> results = new Vector<Vector<String>>();
		
		try {
			//Creates a statement object and executes the query.
			Statement queryStatement = conn.createStatement();
			ResultSet rs = queryStatement.executeQuery(sql);
			
			//Gets the number of columns.
			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();
			
			//Gets each of the column names.
			Vector<String> catalogNames = new Vector<String>();
			for (int i = 1; i <= columns; i++){
				catalogNames.add(rsmd.getColumnLabel(i));
			}
			results.add(catalogNames);
			
			//Loops through the results and displays the data.
			while(rs.next()){
				//Loops through and saves the columns into a vector.
				Vector<String> row = new Vector<String>();
				for (int i = 1; i <= columns; i++){
					row.add(rs.getString(i));
				}
				
				//Now adds the row vector into the results vector.
				results.add(row);
			}
		} catch (SQLException e) {
			return null;
		}
		
		//Returns the result set.
		return results;
	}
	
	/**
	 * Gets the next available session number for a user.
	 * @param userID The user ID for the lookup.
	 * @return The next available session number for that user.
	 */
	public String getNextSession(String userID) {
		String sqlStatement = "SELECT MAX(SessionID) + 1 FROM Session WHERE UserName = \"" + userID + "\";";
	
		ResultSet rs;
		String session;
		try {
			//Executes the query and gets the result set.
			Statement queryStatement = conn.createStatement();
			rs = queryStatement.executeQuery(sqlStatement);
			
			//Now, gets the data.
			rs.next();
			session = rs.getString(1);
		} catch (SQLException e) {
			//No record exists.
			e.printStackTrace();
			return DEFAULT_SESSION;
		}
		
		//Ensures there is no null value returned.
		if (session == null) session = DEFAULT_SESSION;
		return session;
	}

	/**
	 * Updates a session with a new time.
	 * @param userID The user ID of the session.
	 * @param sessionID The session ID for the session.
	 * @param time The time of the procedure.
	 * @param string 
	 * @return A boolean indicating success or failure.
	 */
	public boolean updateSessionTime(String userID, String sessionID, int time, String date) {
		//Creates the sql statement.
		String sql = "UPDATE Session SET STime = " + time + ",SDate = \"" + date + "\" " +
				"WHERE UserName = \"" + userID +
				"\" AND SessionId = " + sessionID + ";";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		//If there is no error, returns true.
		return true;
	}

	public boolean isSaving() {
		return stpCol;
	}

	public Vector<byte[]> getFrames(String currentUser, String session) {
		InputStream sessionFile = null;
		try {
			sessionFile = new FileInputStream(System.getProperty("user.dir") +
					"/data/" + currentUser + "/" + session);
		} catch (FileNotFoundException e1) {
			//The file was not found!
			e1.printStackTrace();
			return null;
		}
		
		//Reads all the bytes into one array.
		byte[] allBytes = null;
		try {
			allBytes = IOUtils.toByteArray(sessionFile);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		//Creates a vector to hold all serialized bytes.
		Vector<byte[]> bytes = new Vector<byte[]>();
		
		//Now, we need to loop through everything to get the frames.
		int current = 0;
		while(current < allBytes.length){
			//Get the size data.
			byte[] size = Arrays.copyOfRange(allBytes, current, current + 4);		
			int nextFrameSize = new BigInteger(size).intValue();
			
			//Now, we copy the next set of data.
			current += 4;
			byte[] frame = Arrays.copyOfRange(allBytes, current, current + nextFrameSize);
			bytes.add(frame);
			current += nextFrameSize;
		}
	
		//Finally, returns the bytes.
		return bytes;
	}

	/**
	 * This function returns a metric id or null for a specific session.
	 * This is to ensure nothing is overwritten. 
	 * @param userName The userName for the user.
	 * @param sessionID The session id for the user.
	 * @return The id for the metric.
	 */
	public String checkForMetricID(String userName, String sessionID){
		//Now we need to get the auto id.
		String sql = "SELECT MetricID FROM Session " +
				"WHERE UserName = \"" + userName + "\" AND SessionId = " + sessionID + ";";
		
		ResultSet rs;
		String id;
		//Executes the SQL.
		try {
			//Executes the query and gets the result set.
			Statement queryStatement = conn.createStatement();
			rs = queryStatement.executeQuery(sql);
			
			//Now, gets the data.
			rs.next();
			id = rs.getString(1);
		} catch (SQLException e) {
			//Again, we just return.
			e.printStackTrace();
			return null;
		}
		
		return id;
	}
	
	/**
	 * This function writes the hand motion metric data to the database.
	 * Currently only raw metrics are stored. Averaged data will come
	 * later on.
	 * @param userName The username for the user.
	 * @param sessionID The session id.
	 * @param computedHandMotions The computed hand motion numbers.
	 * @param computedLeftMotions The computed left hand motion.
	 * @param computedRightMotions The computed right hand motion.
	 */
	public void writeMetrics(String userName, String sessionID, String skillLevel,
			int[] computedHandMotions, int[] computedLeftMotions, int[] computedRightMotions,
			float[] computedHandVelocity, float[] computedLeftVelocity, float[] computedRightVelocity) {
		//We get the skill level id.
		int skillLevelID = getSkillLevelID(skillLevel);
		
		//We first check for an insert or update.
		String checkID = checkForMetricID(userName, sessionID);
		if (checkID != null) {
			updateMetrics(userName, sessionID, checkID, skillLevelID,
					computedHandMotions, computedLeftMotions, computedRightMotions,
					computedHandVelocity, computedLeftVelocity, computedRightVelocity);
			return;
		}
		
		//We first need to generate our SQL statement.
		String sql = "INSERT INTO SessionMetrics VALUES(null, " + 
				skillLevelID + ", " +
				computedHandMotions[0] + ", " + 
				computedLeftMotions[0] + ", " + 
				computedLeftMotions[1] + ", " + 
				computedLeftMotions[2] + ", " + 
				computedLeftMotions[3] + ", " + 
				computedLeftMotions[4] + ", " + 
				computedHandMotions[1] + ", " + 
				computedRightMotions[0] + ", " + 
				computedRightMotions[1] + ", " + 
				computedRightMotions[2] + ", " + 
				computedRightMotions[3] + ", " + 
				computedRightMotions[4] + ", " +
				computedHandVelocity[0] + ", " + 
				computedLeftVelocity[0] + ", " + 
				computedLeftVelocity[1] + ", " + 
				computedLeftVelocity[2] + ", " + 
				computedLeftVelocity[3] + ", " + 
				computedLeftVelocity[4] + ", " + 
				computedHandVelocity[1] + ", " + 
				computedRightVelocity[0] + ", " + 
				computedRightVelocity[1] + ", " + 
				computedRightVelocity[2] + ", " + 
				computedRightVelocity[3] + ", " + 
				computedRightVelocity[4] + ");";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			//Just doesn't do it!
			e.printStackTrace();
			return;
		}
		
		//Now we need to get the auto id.
		sql = "SELECT last_insert_rowid();";
		
		ResultSet rs;
		String id;
		//Executes the SQL.
		try {
			//Executes the query and gets the result set.
			Statement queryStatement = conn.createStatement();
			rs = queryStatement.executeQuery(sql);
			
			//Now, gets the data.
			rs.next();
			id = rs.getString(1);
		} catch (SQLException e) {
			//Again, we just return.
			e.printStackTrace();
			return;
		}
		
		//Finally, we insert the auto id into the appropriate session.
		sql = "UPDATE Session SET MetricID = " + id + 
				" WHERE UserName = \"" + userName + "\" AND SessionId = " + sessionID + ";";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			//If there is a problem, it won't do it.
			e.printStackTrace();
			return;
		}
		
		//Gets the updated average.
		calculateAverage(skillLevelID, computedHandMotions, computedLeftMotions, computedRightMotions,
						 computedHandVelocity, computedLeftVelocity, computedRightVelocity);
	}

	/**
	 * This method updates the average for the current skill level. It
	 * checks to see if such a skill level exists and then updates the 
	 * rolling average.
	 * @param skillLevelID The skill level ID.
	 * @param computedHandMotions The hand motion data.
	 * @param computedLeftMotions The left hand data.
	 * @param computedRightMotions The right hand data.
	 * @param computedHandVelocity The hand velocity.
	 * @param computedLeftVelocity The left velocity.
	 * @param computedRightVelocity The right velocity.
	 */
	private void calculateAverage(int skillLevelID, int[] computedHandMotions,
			int[] computedLeftMotions, int[] computedRightMotions,
			float[] computedHandVelocity, float[] computedLeftVelocity,
			float[] computedRightVelocity) {
		//First, gets past data.
		String sql = "SELECT * FROM MetricAverage WHERE SkillID = " + skillLevelID + ";";
		Vector<Vector<String>> results = getData(sql);
		
		//Check to see if an entry exists.
		if (results.size() == 1 || results.elementAt(1) == null){
			createNewAverage(skillLevelID, computedHandMotions, computedLeftMotions, computedRightMotions,
					computedHandVelocity, computedLeftVelocity, computedRightVelocity);
			return;
		}
		
		//Now, gets the divide value.
		int divideVal = Integer.parseInt(results.elementAt(1).elementAt(1)) + 1;
		Vector<String> values = results.elementAt(1);
		
		//We generate our SQL
		sql = "UPDATE SessionMetrics SET " +
				"NumReference = " + divideVal + ", " +
				"LeftMotions = " + ((Float.parseFloat(values.elementAt(2)) + computedHandMotions[0]) / divideVal) + ", " +
				"LeftThumbMotions = " + ((Float.parseFloat(values.elementAt(3)) + computedLeftMotions[0]) / divideVal) + ", " +
				"LeftIndexMotions = " + ((Float.parseFloat(values.elementAt(4)) + computedLeftMotions[1]) / divideVal) + ", " +
				"LeftMiddleMotions = " + ((Float.parseFloat(values.elementAt(5)) + computedLeftMotions[2]) / divideVal) + ", " +
				"LeftRingMotions = " + ((Float.parseFloat(values.elementAt(6)) + computedLeftMotions[3]) / divideVal) + ", " +
				"LeftPinkyMotions = " + ((Float.parseFloat(values.elementAt(7)) + computedLeftMotions[4]) / divideVal) + ", " +
				"RightMotions = " + ((Float.parseFloat(values.elementAt(8)) + computedHandMotions[1]) / divideVal) + ", " +
				"RightThumbMotions = " + ((Float.parseFloat(values.elementAt(9)) + computedRightMotions[0]) / divideVal) + ", " +
				"RightIndexMotions = " + ((Float.parseFloat(values.elementAt(10)) + computedRightMotions[1]) / divideVal) + ", " +
				"RightMiddleMotions = " + ((Float.parseFloat(values.elementAt(11)) + computedRightMotions[2]) / divideVal) + ", " +
				"RightRingMotions = " + ((Float.parseFloat(values.elementAt(12)) + computedRightMotions[3]) / divideVal) + ", " +
				"RightPinkyMotions = " + ((Float.parseFloat(values.elementAt(13)) + computedRightMotions[4]) / divideVal) + ", " +
				"LeftVelocity = " + ((Float.parseFloat(values.elementAt(14)) + computedHandVelocity[0]) / divideVal) + ", " +
				"LeftThumbVelocity = " + ((Float.parseFloat(values.elementAt(15)) + computedLeftVelocity[0]) / divideVal) + ", " +
				"LeftIndexVelocity = " + ((Float.parseFloat(values.elementAt(16)) + computedLeftVelocity[1]) / divideVal) + ", " +
				"LeftMiddleVelocity = " + ((Float.parseFloat(values.elementAt(17)) + computedLeftVelocity[2]) / divideVal) + ", " +
				"LeftRingVelocity = " + ((Float.parseFloat(values.elementAt(18)) + computedLeftVelocity[3]) / divideVal) + ", " +
				"LeftPinkyVelocity = " + ((Float.parseFloat(values.elementAt(19)) + computedLeftVelocity[4]) / divideVal) + ", " +
				"RightVelocity = " + ((Float.parseFloat(values.elementAt(20)) + computedHandVelocity[1]) / divideVal) + ", " +
				"RightThumbVelocity = " + ((Float.parseFloat(values.elementAt(21)) + computedRightVelocity[0]) / divideVal) + ", " +
				"RightIndexVelocity = " + ((Float.parseFloat(values.elementAt(22)) + computedRightVelocity[1]) / divideVal) + ", " +
				"RightMiddleVelocity = " + ((Float.parseFloat(values.elementAt(23)) + computedRightVelocity[2]) / divideVal) + ", " +
				"RightRingVelocity = " + ((Float.parseFloat(values.elementAt(24)) + computedRightVelocity[3]) / divideVal) + ", " +
				"RightPinkyVelocity = " + ((Float.parseFloat(values.elementAt(25)) + computedRightVelocity[4]) / divideVal) +
				" WHERE SkillID = " + skillLevelID + ";";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			//Just doesn't do it!
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Creates a new average if one doesn't exist.
	 * @param skillLevelID The skill level id.
	 * @param computedHandMotions The hand motions.
	 * @param computedLeftMotions The left hand motions.
	 * @param computedRightMotions The right hand motions.
	 * @param computedHandVelocity The hand velocity.
	 * @param computedLeftVelocity The left hand velocity.
	 * @param computedRightVelocity The right hand velocity.
	 */
	private void createNewAverage(int skillLevelID, int[] computedHandMotions,
			int[] computedLeftMotions, int[] computedRightMotions,
			float[] computedHandVelocity, float[] computedLeftVelocity,
			float[] computedRightVelocity) {
		//We first need to generate our SQL statement.
		String sql = "INSERT INTO MetricAverage VALUES(" +
				skillLevelID + ", 1," +
				computedHandMotions[0] + ", " + 
				computedLeftMotions[0] + ", " + 
				computedLeftMotions[1] + ", " + 
				computedLeftMotions[2] + ", " + 
				computedLeftMotions[3] + ", " + 
				computedLeftMotions[4] + ", " + 
				computedHandMotions[1] + ", " + 
				computedRightMotions[0] + ", " + 
				computedRightMotions[1] + ", " + 
				computedRightMotions[2] + ", " + 
				computedRightMotions[3] + ", " + 
				computedRightMotions[4] + ", " +
				computedHandVelocity[0] + ", " + 
				computedLeftVelocity[0] + ", " + 
				computedLeftVelocity[1] + ", " + 
				computedLeftVelocity[2] + ", " + 
				computedLeftVelocity[3] + ", " + 
				computedLeftVelocity[4] + ", " + 
				computedHandVelocity[1] + ", " + 
				computedRightVelocity[0] + ", " + 
				computedRightVelocity[1] + ", " + 
				computedRightVelocity[2] + ", " + 
				computedRightVelocity[3] + ", " + 
				computedRightVelocity[4] + ");";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			//Just doesn't do it!
			e.printStackTrace();
			return;
		}
	}

	/**
	 * This helper function executes an update if there is already
	 * data metrics computed. That's all it does.
	 * @param userName The user of the session.
	 * @param session The session id
	 * @param metricID The metric id for that metric
	 * @param computedHandMotions The hand motions
	 * @param computedLeftMotions The left hand motions
	 * @param computedRightMotions The right hand motions
	 */
	private void updateMetrics(String userName, String session, String metricID, int skillID,
			int[] computedHandMotions, int[] computedLeftMotions, int[] computedRightMotions,
			float[] computedHandVelocity, float[] computedLeftVelocity, float[] computedRightVelocity){
		//Remove past average first.
		removePastAverage(metricID, skillID);
		
		//We generate our SQL
		String sql = "UPDATE SessionMetrics SET " +
				"SkillID = " + skillID + ", " + 
				"LeftMotions = " + computedHandMotions[0] + ", " +
				"LeftThumbMotions = " + computedLeftMotions[0] + ", " +
				"LeftIndexMotions = " + computedLeftMotions[1] + ", " +
				"LeftMiddleMotions = " + computedLeftMotions[2] + ", " +
				"LeftRingMotions = " + computedLeftMotions[3] + ", " +
				"LeftPinkyMotions = " + computedLeftMotions[4] + ", " +
				"RightMotions = " + computedHandMotions[1] + ", " +
				"RightThumbMotions = " + computedRightMotions[0] + ", " +
				"RightIndexMotions = " + computedRightMotions[1] + ", " +
				"RightMiddleMotions = " + computedRightMotions[2] + ", " +
				"RightRingMotions = " + computedRightMotions[3] + ", " +
				"RightPinkyMotions = " + computedRightMotions[4] + ", " +
				"LeftVelocity = " + computedHandVelocity[0] + ", " +
				"LeftThumbVelocity = " + computedLeftVelocity[0] + ", " +
				"LeftIndexVelocity = " + computedLeftVelocity[1] + ", " +
				"LeftMiddleVelocity = " + computedLeftVelocity[2] + ", " +
				"LeftRingVelocity = " + computedLeftVelocity[3] + ", " +
				"LeftPinkyVelocity = " + computedLeftVelocity[4] + ", " +
				"RightVelocity = " + computedHandVelocity[1] + ", " +
				"RightThumbVelocity = " + computedRightVelocity[0] + ", " +
				"RightIndexVelocity = " + computedRightVelocity[1] + ", " +
				"RightMiddleVelocity = " + computedRightVelocity[2] + ", " +
				"RightRingVelocity = " + computedRightVelocity[3] + ", " +
				"RightPinkyVelocity = " + computedRightVelocity[4] +
				" WHERE MetricID = " + metricID + ";";
		
		//Executes the generated SQL statement.
		try {
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch (SQLException e) {
			//Just doesn't do it!
			e.printStackTrace();
			return;
		}
		
		//Now, updates the average.
		calculateAverage(skillID, computedHandMotions, computedLeftMotions, computedRightMotions,
				computedHandVelocity, computedLeftVelocity, computedRightVelocity);
	}

	/**
	 * This helper method removes data from the rolling average.
	 * @param metricID The id of the metric set to remove.
	 * @param skillID The skill id to remove from.
	 */
	private void removePastAverage(String metricID, int skillID) {
		//Generates an SQL statement for the metrics.
		String sql = "SELECT * FROM SessionMetrics WHERE MetricID = " + metricID + ";";
		Vector<String> metricData = getData(sql).elementAt(1);
		
		//Generates the SQL for the average.
		sql = "SELECT * FROM MetricAverage WHERE SkillID = " + skillID + ";";
		Vector<String> averageData = getData(sql).elementAt(1);
		
		//Sets the number of data items.
		int reference = Integer.parseInt(averageData.elementAt(1));
		
		//Generates the update statement.
		sql = "UPDATE MetricAverage SET " +
		"NumReference = " + (reference - 1) + ", " +
		"LeftMotions = " + ((Float.parseFloat(averageData.elementAt(2)) - Float.parseFloat(metricData.elementAt(2))) * reference) + ", " +
		"LeftThumbMotions = " + ((Float.parseFloat(averageData.elementAt(3)) - Float.parseFloat(metricData.elementAt(3))) * reference) + ", " +
		"LeftIndexMotions = " + ((Float.parseFloat(averageData.elementAt(4)) - Float.parseFloat(metricData.elementAt(4))) * reference) + ", " +
		"LeftMiddleMotions = " + ((Float.parseFloat(averageData.elementAt(5)) - Float.parseFloat(metricData.elementAt(5))) * reference) + ", " +
		"LeftRingMotions = " + ((Float.parseFloat(averageData.elementAt(6)) - Float.parseFloat(metricData.elementAt(6))) * reference) + ", " +
		"LeftPinkyMotions = " + ((Float.parseFloat(averageData.elementAt(7)) - Float.parseFloat(metricData.elementAt(7))) * reference) + ", " +
		"RightMotions = " + ((Float.parseFloat(averageData.elementAt(8)) - Float.parseFloat(metricData.elementAt(8))) * reference) + ", " +
		"RightThumbMotions = " + ((Float.parseFloat(averageData.elementAt(9)) - Float.parseFloat(metricData.elementAt(9))) * reference) + ", " +
		"RightIndexMotions = " + ((Float.parseFloat(averageData.elementAt(10)) - Float.parseFloat(metricData.elementAt(10))) * reference) + ", " +
		"RightMiddleMotions = " + ((Float.parseFloat(averageData.elementAt(11)) - Float.parseFloat(metricData.elementAt(11))) * reference) + ", " +
		"RightRingMotions = " + ((Float.parseFloat(averageData.elementAt(12)) - Float.parseFloat(metricData.elementAt(12))) * reference) + ", " +
		"RightPinkyMotions = " + ((Float.parseFloat(averageData.elementAt(13)) - Float.parseFloat(metricData.elementAt(13))) * reference) + ", " +
		"LeftVelocity = " + ((Float.parseFloat(averageData.elementAt(14)) - Float.parseFloat(metricData.elementAt(14))) * reference) + ", " +
		"LeftThumbVelocity = " + ((Float.parseFloat(averageData.elementAt(15)) - Float.parseFloat(metricData.elementAt(15))) * reference) + ", " +
		"LeftIndexVelocity = " + ((Float.parseFloat(averageData.elementAt(16)) - Float.parseFloat(metricData.elementAt(16))) * reference) + ", " +
		"LeftMiddleVelocity = " + ((Float.parseFloat(averageData.elementAt(17)) - Float.parseFloat(metricData.elementAt(17))) * reference) + ", " +
		"LeftRingVelocity = " + ((Float.parseFloat(averageData.elementAt(18)) - Float.parseFloat(metricData.elementAt(18))) * reference) + ", " +
		"LeftPinkyVelocity = " + ((Float.parseFloat(averageData.elementAt(19)) - Float.parseFloat(metricData.elementAt(19))) * reference) + ", " +
		"RightVelocity = " + ((Float.parseFloat(averageData.elementAt(20)) - Float.parseFloat(metricData.elementAt(20))) * reference) + ", " +
		"RightThumbVelocity = " + ((Float.parseFloat(averageData.elementAt(21)) - Float.parseFloat(metricData.elementAt(21))) * reference) + ", " +
		"RightIndexVelocity = " + ((Float.parseFloat(averageData.elementAt(22)) - Float.parseFloat(metricData.elementAt(22))) * reference) + ", " +
		"RightMiddleVelocity = " + ((Float.parseFloat(averageData.elementAt(23)) - Float.parseFloat(metricData.elementAt(23))) * reference) + ", " +
		"RightRingVelocity = " + ((Float.parseFloat(averageData.elementAt(24)) - Float.parseFloat(metricData.elementAt(24))) * reference) + ", " +
		"RightPinkyVelocity = " + ((Float.parseFloat(averageData.elementAt(25)) - Float.parseFloat(metricData.elementAt(25))) * reference) +
		" WHERE SkillID = " + skillID + ";";
	}

	public Vector<String> getSkillLevels() {
		//Creates the SQL statement.
		String sql = "SELECT SkillName FROM SkillLevel ORDER BY SkillVal ASC;";
		
		//Runs it.
		Vector<Vector<String>> results = getData(sql);
		
		Vector<String> names = new Vector<String>();
		for(int i = 1; i < results.size(); i++){
			names.add(results.elementAt(i).elementAt(0));
		}
		
		//Returns.
		return names;
	}

	public void createNewSkillLevel(String newSkillLevel) {
		//First we need to get the lowest skill level.
		int lowest = getLowestSkillLevel();
		
		String sql = "INSERT INTO SkillLevel VALUES(null,\"" + newSkillLevel + "\"," + (lowest + 1) + ");";
		
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return;
		}
	}

	private int getLowestSkillLevel() {
		//Creates the sql statement.
		String sql = "SELECT MAX(SkillVal) FROM SkillLevel;";
		
		//Runs it
		Vector<Vector<String>> results = getData(sql);
		if (results.size() < 1 || results.elementAt(1).elementAt(0) == null) return -1;
		
		return Integer.parseInt(results.elementAt(1).elementAt(0));
	}

	public int increaseSkillLevel(String selected) {
		//Gets the current skill value.
		String sql = "Select SkillName, SkillVal FROM SkillLevel ORDER BY SkillVal ASC;";
		
		//Runs it.
		Vector<Vector<String>> results = getData(sql);
		int i = 0;
		for (i = 1; i < results.size(); i++){
			if (results.elementAt(i).elementAt(0).equals(selected)) break;
		}
		if (i == 1) return -1;
		
		//Otherwise, we get the skill val and swap.
		sql = "UPDATE SkillLevel SET SkillVal = " + results.elementAt(i).elementAt(1) + " WHERE SkillName = \"" +
				results.elementAt(i - 1).elementAt(0) + "\";";
		
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return -2;
		}
		
		sql = "UPDATE SkillLevel SET SkillVal = " + results.elementAt(i - 1).elementAt(1) + " WHERE SkillName = \"" +
				results.elementAt(i).elementAt(0) + "\";";
		
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return -2;
		}
		
		return 0;
	}

	public int decreaseSkillLevel(String selected) {
		//Gets the current skill value.
		String sql = "Select SkillName, SkillVal FROM SkillLevel ORDER BY SkillVal ASC;";
		
		//Runs it.
		Vector<Vector<String>> results = getData(sql);
		int i = 0;
		for (i = 1; i < results.size(); i++){
			if (results.elementAt(i).elementAt(0).equals(selected)) break;
		}
		if (i == results.size() - 1) return -1;
		
		//Otherwise, we get the skill val and swap.
		sql = "UPDATE SkillLevel SET SkillVal = " + results.elementAt(i).elementAt(1) + " WHERE SkillName = \"" +
				results.elementAt(i + 1).elementAt(0) + "\";";
		
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return -2;
		}
		
		sql = "UPDATE SkillLevel SET SkillVal = " + results.elementAt(i + 1).elementAt(1) + " WHERE SkillName = \"" +
				results.elementAt(i).elementAt(0) + "\";";
		
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return -2;
		}
		
		return 0;
	}

	public void removeSkillLevel(String selected) {
		String sql = "DELETE FROM SkillLevel WHERE SkillName = \"" + selected + "\";";
				
		//Executes the generated SQL statement.
		try{
			Statement query = conn.createStatement();
			query.executeUpdate(sql);
		} catch(SQLException e){
			e.printStackTrace();
			return;
		}
	}
	
	
	private int getSkillLevelID(String skillLevel) {
		//Gets the data.
		String sql = "SELECT SkillID FROM SkillLevel WHERE SkillName = \"" + skillLevel + "\";";
		Vector<Vector<String>> data = getData(sql);
		
		try {
			return Integer.parseInt(data.elementAt(1).elementAt(0));
		} catch (IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		
		return -1;
	}

	public String getSkillLevelName(String skillID) {
		//Gets the data.
		String sql = "SELECT SkillName FROM SkillLevel WHERE SkillID = " + skillID + ";";
		Vector<Vector<String>> data = getData(sql);
		
		try {
			return data.elementAt(1).elementAt(0);
		} catch (IndexOutOfBoundsException e){
			e.printStackTrace();
		}
		
		return null;
	}
}