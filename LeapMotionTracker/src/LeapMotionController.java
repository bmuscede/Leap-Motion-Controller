import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import com.leapmotion.leap.*;

public class LeapMotionController extends Thread{
	//Whether tracking is paused or not.
	private boolean collecting;
	//Whether the system is ready to START tracking.
	private boolean ready;
	//Whether the system is paused on tracking.
	private boolean paused;
	//Thread for the running of the controller.
	private Thread controllerThread;
	//Timer for left and right hand data output.
	private int leftOutputTime;
	private int rightOutputTime;
	
	//Tracking controller
	Controller leapController;
	MainListener leapListener;
	
	//Database controller
	private DatabaseController database;
	private String currentUser;
	private String currentSession;
	
	//Output time for data smoothening.
	private final int FRAME_OUTPUT_TIME = 50;
	
 	/**
	 * Constructor for the leap motion controller class.
	 */
	public LeapMotionController(DatabaseController db, String currentUser){
		//Copies the database object.
		database = db;
		this.currentUser = currentUser;
		
		//Sets collection to paused.
		collecting = false;
		ready = false;
		paused = false;
	}
	
	/**
	 * Stops Leap Motion services to ensure proper functioning.
	 */
	public void destroyController(){
		//Stops the listener service.
		leapController.removeListener(leapListener);
	}

	/**
	 * Sets up the Leap Motion Controller to listen
	 * for motions and then infinitely loops to keep
	 * input going.
	 */
	public void run() {
		//Now sets up Leap Motion specifics.
		leapController = new Controller();
		leapListener = new MainListener();
		leapController.addListener(leapListener);
		
		//Keeps the thread looping infinitely.
		while(true);
	}
	
	/**
	 * Invokes the run method by forking to a new thread.
	 * This can be used to invoke a new Leap Motion
	 * listener. 
	 */
	public void start(){
		//Checks to see if start hasn't already been invoked.
		if (controllerThread == null){
			//Creates a new thread and starts it.
			controllerThread = new Thread(this);
			controllerThread.start();
		}
	}
	
	public void setCollection(boolean collectionStatus){
		//Ensures a valid collection status has been passed to the Leap Motion.
		if (ready && collectionStatus){
			//We create a new session for this collection.
			currentSession = database.getNextSession(currentUser);
			database.writeSession(currentUser, currentSession);
			
			//We now allow for collection.
			collecting = collectionStatus;
			
			//Start the database thread.
			database.start();
		} else if (!collectionStatus){
			collecting = collectionStatus;
			paused = false;
			
			//Closes the session id.
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			database.updateSessionTime(currentUser, currentSession, 
					ProcedureController.status.timerValue, dateFormat.format(date));
			
			//Creates the new dialog.
			int[] values = database.stoppedCollecting();
			ProgramController.createProgressBar(values[0], values[1]);
		}
	}
	

	public void setPaused(boolean pauseStatus) {
		//Only allows for pause status changes if system is collecting.
		if (ready && collecting){
			//Flushes the buffer on pause.
			paused = pauseStatus;
		}
	}

	class MainListener extends Listener {
		/**
		 * Activates when the leap motion controller is activated.
		 */
		public void onConnect(Controller leapController) {
			//We want to send a message back to the main controller.
			ProcedureController.leapStatus(ProcedureController.CONNECTED);
			ready = true;
			
			//Sets the output times to -1.
			leftOutputTime = -1;
			rightOutputTime = -1;
		}
		
		/**
		 * Activates when the Leap Motion controller is not connected.
		 */
		public void onDisconnect(Controller leapController) {
			//We want to send a message back to the main controller.
			ProcedureController.leapStatus(ProcedureController.NOT_CONNECTED);
			ready = false;
			collecting = false;
			paused = false;
		}
		
		/**
		 * Fires very rapidly. Extends multiple classes.
		 */
		public void onFrame(Controller leapController){		
			//First, gets the frame
			Frame currentFrame = leapController.frame();

			//First, updates the message appropriately.
			updateStatusMessage(currentFrame);
			
			//Next, since hands are present, we now output motion data.
			outputMotionData(currentFrame);
			
			//Finally, saves the frame if the controller is running.
			if (ready && collecting && !paused){
				saveFrame(currentFrame);	
			}
		}
		
		/**
		 * This method saves this frame to a database under a current user.
		 * @param currentFrame The frame to be saved.
		 */
		private void saveFrame(Frame currentFrame) {
			//We call the database controller to handle this.
			database.writeFrame(currentUser, currentSession, currentFrame.serialize());
		}

		private void updateStatusMessage(Frame currentFrame) {
			if (database.isSaving()){
				//System is saving.
				ProcedureController.leapStatus(ProcedureController.SAVING);
			}
			else if (ready && collecting && paused){
				//System is paused.
				ProcedureController.leapStatus(ProcedureController.PAUSED);
			} else if (ready && collecting && !paused){
				//System is collecting.
				ProcedureController.leapStatus(ProcedureController.COLLECTING);
			} else  if (!checkHandStatus(currentFrame.hands()) && ready){
				//System is ready but not collecting.
				ProcedureController.leapStatus(ProcedureController.CONNECTED);
				return;
			} else if (ready) {
				//Hands are present.
				ProcedureController.leapStatus(ProcedureController.HANDS_PRESENT);
			} else if (!ready){
				//System has gone offline.
				ProcedureController.leapStatus(ProcedureController.NOT_CONNECTED);
			}
		}

		/**
		 * Collects hand motion data and displays it on the screen.
		 * Essentially passes a frame to the program controller which
		 * then formats it. Does not hamper execution of this thread.
		 */
		private void outputMotionData(Frame current) {
			boolean leftSet = false;
			boolean rightSet = false;
			
			//First, gets the hands for the data.
			HandList hands = current.hands();
			
			//Processes each of the hands.
			for (int i = 0; i < hands.count(); i++){
				//Get the current hand.
				Hand currentHand = hands.get(i);
				
				if (currentHand.isLeft()){
					//A left hand is detected.
					leftSet = true;
					leftOutputTime++;
					
					//Only output every 100 frames.
					if (leftOutputTime % FRAME_OUTPUT_TIME != 0){
						continue;
					}
					
					String handData = generateHandData(currentHand);
					ProcedureController.sendHandData(ProcedureController.HAND_LEFT,
							handData, (int) (currentHand.confidence() * 100));
				} else {
					//A right hand is detected.
					rightSet = true;
					rightOutputTime++;
					
					//Only output every 100 frames.
					if (rightOutputTime % FRAME_OUTPUT_TIME != 0){
						continue;
					}
					
					String handData = generateHandData(currentHand);
					ProcedureController.sendHandData(ProcedureController.HAND_RIGHT,
							handData, (int) (currentHand.confidence() * 100));
				}
			}
			
			//Sees if hand data was sent for left and right.
			if (!leftSet){
				leftOutputTime = -1;
				ProcedureController.sendHandData(ProcedureController.HAND_LEFT, null, 0);
			}
			if (!rightSet){
				rightOutputTime = -1;
				ProcedureController.sendHandData(ProcedureController.HAND_RIGHT, null, 0);
			}
		}

		private String generateHandData(Hand currentHand) {
			//Generates the string to use.
			String handData = String.format("<html>Palm Velocity: %.2f",
					 currentHand.palmVelocity().magnitude());
			for (int i = 0; i < currentHand.fingers().count(); i++){
				//Gets the current finger.
				Finger currentFinger = currentHand.fingers().get(i);
				
				handData += String.format("<br>Finger %d: X - %.2f, Y - %.2f," +
						" Z - %.2f", i + 1, currentFinger.tipPosition().getX(),
						currentFinger.tipPosition().getY(), currentFinger.tipPosition().getZ());
			}
			handData += "</html>";
			
			return handData;
		}

		/**
		 * Helper method to check for hands.
		 */
		private boolean checkHandStatus(HandList hands){
			//First, checks the hand number.
			if (hands.count() >= 1 && hands.count() <= 2) return true;
			
			//Either too many hands or no hands detected.
			return false;
		}
	}
}