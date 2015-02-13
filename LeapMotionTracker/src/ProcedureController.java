import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JOptionPane;


public class ProcedureController {
	//GUI Objects
	static StatusWindow status;
	static HandDataWindow handLeft;
	static HandDataWindow handRight;
	static StatusBoxWindow vizStatus;
	
	//Leap Motion Objects
	static LeapMotionController controller;
	static ProcessCommunicator messageSender;
	
	//Visualizer Variables
	static boolean visualizerFailure; 
	static Process visualizerProcess;
	
	//Session Variables.
	static String currentUser;
	
	//Collection status codes.
	public static final int CONNECTED = 1;
	public static final int NOT_CONNECTED = 0;
	public static final int HANDS_PRESENT = 2;
	public static final int PAUSED = 3;
	public static final int COLLECTING = 4;
	public static final int NOT_COLLECTING = 5;
	public static final int SAVING = 6;
	public static final int RESUME = 7;
	
	//Hand Codes.
	public static final int HAND_LEFT = 0;
	public static final int HAND_RIGHT = 1;

	/**
	 * Starts the procedure view.
	 * Creates all the windows and starts
	 * the appropriate visualizer/options.
	 * @param userName The username of the person using it.
	 * @param limited 
	 */
	public static void startProcedureProgram(final String userName, final DatabaseController database, 
			final boolean limited){
		//Provides full program functionality.
		visualizerFailure = false;
		
		
		//Starts the GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {					
					//Creates each of the new windows.
					status = new StatusWindow();
					handLeft = new HandDataWindow(true);
					handRight = new HandDataWindow(false);
					if (!limited){
						vizStatus = new StatusBoxWindow("<html><center>Starting the Visualizer...<br>" +
									"Please wait.</center></html>");
					}
					
					//Positions the windows accordingly.
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					handLeft.setLocation(0, 
				    		(int) dim.getHeight() - handLeft.getHeight() - ProgramController.START_BAR_HEIGHT);
				    handRight.setLocation((int) dim.getWidth() - handRight.getWidth(),
				      		(int) dim.getHeight() - handRight.getHeight() - ProgramController.START_BAR_HEIGHT);
					if (limited){
						status.setLocationRelativeTo(null);
					} else {
						status.setLocation(0, 0);
					    vizStatus.setLocationRelativeTo(null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//Gets the current user.
				currentUser = userName;
				
				//Shows the next windows.
		        status.setVisible(true);
		        status.setUser(currentUser);
				handLeft.setVisible(true);
				handRight.setVisible(true);
				
				//Builds the leap motion controller.
				controller = new LeapMotionController(database, currentUser);
				controller.start();

				//Starts the visualizer maximized.
				if (!limited){
					String workingDir = System.getProperty("user.dir");
					try{
						visualizerProcess = new ProcessBuilder(
								workingDir + "/Visualizer/Visualizer.exe").start();
						
						//If that works, create the shutdown hook.
						Runtime.getRuntime().addShutdownHook(new Thread(){
							public void run(){
								//Kills the visualizer when the program terminates.
								ProcedureController.visualizerProcess.destroy();
								
								//Kills the controller.
								controller.destroyController();
							}
						});
					} catch (IOException e){
						//The program had trouble executing. Likely doesn't exist!
						visualizerFailure = true;
						ProgramController.createDialog("<html>The program \'Visualizer.exe\' could not be " +
								"found on your system!<br>The program will now continue " +
								"but in limited mode.</html>", 
								"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
					}
				}
								
				//Finally, adds a dialog indicating the visualizer is not ready.
				if (!visualizerFailure && !limited){
					//Creates a message box telling the user to be patient.
					vizStatus.setVisible(true);
				}
			}
		});
	}
	
	/**
	 * Prints leap motion status codes to the screen.
	 * @param statusCode The status code and action.
	 */
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
				
			case SAVING:
				status.lblStatus.setText("Status: Saving...");
				
				//Disables the buttons.
				status.btnStartStop.setEnabled(false);
			default:
				break;
		}
	}
	
	/**
	 * Affects the LeapMotion Controller.
	 * Can be used to send a message.
	 * @param statusCode The code of the message.
	 */
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
			
			case RESUME:
				//Resumes tracking.
				controller.setPaused(false);
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
	
	public static void receiveMessage(int code){
		//Now parses the message.
		switch(code){
			case ProgramController.VISUALIZER_READY:
				//The visualizer is ready.
				vizStatus.setVisible(false);
				break;
				
			default:
				return;
		}
	}
}
