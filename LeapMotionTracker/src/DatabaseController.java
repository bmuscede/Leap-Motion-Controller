import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Vector;

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
	private byte[] nullByte;
	
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
			
			//Sets up a null byte.
			nullByte = ByteBuffer.allocate(4).putInt(0).array();
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
					fileWriter.write(frameBuffer.get(0));
					
					//Adds an empty array of bytes.
					fileWriter.write(nullByte);
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
		String sessionStatement = "INSERT INTO Session VALUES(\"" +
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
		//Creates a vector to hold all serialized bytes.
		Vector<byte[]> bytes = new Vector<byte[]>();
		
		//Creates the statement.
		String sql = "SELECT FrameSerial FROM Frame WHERE UserName = \"" + currentUser + "\" " +
				"AND SessionId = " + session + " ORDER BY FrameNo ASC";
		
		Statement stm=null;
        try{
        	//Executes the statement.
            stm = conn.createStatement();
            ResultSet result = stm.executeQuery(sql);
		
            //Loops through the result set.
            while (result.next()){
            	bytes.add(result.getBytes("FrameSerial"));
            }
            
            //Closes the result set.
            result.close();
        } catch (SQLException e) {
        	e.printStackTrace();
        	return null;
        }
        
		//Finally, returns the bytes.
		return bytes;
	}
}