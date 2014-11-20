import java.sql.*;
import java.util.Vector;

import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;

public class DatabaseController {
	private final String DATABASE_PREFIX = "jdbc:sqlite:";
	private Connection conn;
	private int frameID;
	private Vector<String> statementBuffer;
	
	//The default session id.
	private final String DEFAULT_SESSION = "0";
	
	/**
	 * This constructor registers a SQLite3 controller that then connects to the
	 * database. 
	 * @param dbURL The location (path of the sqlite database)
	 */
	public DatabaseController(String dbURL) {
		statementBuffer = new Vector<String>();
		
		try{
			//Registers the SQLite3 driver.
			Class.forName("org.sqlite.JDBC");
			
			//Now connects to the database.
			conn = DriverManager.getConnection(DATABASE_PREFIX + dbURL);	
			
			//Sets a 0 frame ID.
			frameID = 0;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new session to store collection data to.
	 * @param userID The user ID of the user.
	 * @param sessionNo The session number.
	 * @return A boolean indicating success or failure.
	 */
	public boolean writeSession(String userID, String sessionNo){
		String sessionStatement = "INSERT INTO Session VALUES(" +
				userID + ", " + sessionNo + ", 0);";
		
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
	public void writeFrame(String userID, String session, Frame currentFrame){
		//First, generates an SQL statement for the user and frame.
		String frameStatement = "INSERT INTO Frame VALUES(";
		
		//Now generates some of the main values for the frame.
		frameStatement += userID + ", " + session + ", " + frameID + ", " + 
				currentFrame.currentFramesPerSecond() + ", " + currentFrame.hands().count() + ");";
		
		//For each of the hands, creates new hand data.
		for (int i = 0; i < currentFrame.hands().count(); i++){
			Hand currentHand = currentFrame.hand(i);
			
			//Creates the initial statement for the hand.
			String handStatement = "INSERT INTO Hand VALUES(" + userID + ", " + session + ", " + frameID +
					", " + i + ", \"" + currentHand.isLeft() + "\", " + currentHand.confidence() + ", " +
					currentHand.fingers().count() + ");";
			
			//Now, creates new finger data for each of the fingers.
			for (int j = 0; j < currentHand.fingers().count(); j++){
				Finger currentFinger = currentHand.finger(j);
				
				//Creates the initial statement for the finger.
				String fingerStatement = "INSERT INTO Finger VALUES(" + userID + ", " + session + ", " + frameID +
						", " + i + ", " + j + ", " + currentFinger.tipPosition().getX() + ", " + 
						currentFinger.tipPosition().getY() + ", " + currentFinger.tipPosition().getZ() + ");";
				
				//Now, adds it to the statement buffer.
				statementBuffer.add(fingerStatement);
			}
			
			//Now, writes that in the database.
			statementBuffer.add(handStatement);
		}
		
		//Next, we execute the SQL statement.
		statementBuffer.add(frameStatement);
		
		//Since there was no error, return true.
		frameID++;
	}
	
	public boolean flushFrames(){
		//We start by looping through the vector.
		for(int i = 0; i < statementBuffer.size(); i++){
			//Get the current string out of the buffer and write to db.
			String currStatement = statementBuffer.get(i);
			
			try {
				Statement query = conn.createStatement();
				query.executeUpdate(currStatement);
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		//Clears the entire buffer.
		statementBuffer.removeAllElements();
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
		String sqlStatement = "SELECT MAX(SessionID) + 1 FROM Session WHERE UId = " + userID;
	
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
	 * @return A boolean indicating success or failure.
	 */
	public boolean updateSessionTime(String userID, String sessionID, int time) {
		//Creates the sql statement.
		String sql = "UPDATE Session SET STime = " + time + " WHERE UId = " + userID +
				" AND SessionId = " + sessionID + ";";
		
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
}