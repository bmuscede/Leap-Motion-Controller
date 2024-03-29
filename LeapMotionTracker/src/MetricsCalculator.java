import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;

/**
 * This class computes the metrics for a session. This can be done either 
 * after a session has been computed or deferred for later. This is a fairly
 * resource intensive process
 * @author Bryan J Muscedere
 */
public class MetricsCalculator implements Runnable {	
	//Constants.
	private final boolean DEFAULT_SMOOTHENING = true;
	private final int DEFAULT_WINDOW = 3;
	private final int DEFAULT_CONFIDENCE = 0;
	private final int DEFAULT_EXAMINE_VAL = 1;
	private final int DEFAULT_SENSITIVITY = 3;
	private final float DEFAULT_VEL_IGNORE = 0;
	
	//Calculation Constants.
	private final int LEFT_HAND_OFFSET = 2;
	private final int RIGHT_HAND_OFFSET = 7;
	
	//Variables to calculate metrics.
	private boolean frameSmoothening;
	private int smootheningWindow;
	private int confidenceRemove;
	private int frameExamineValue;
	private int sensitivityValue;
	private float velocityIgnoreValue;
	
	//Informational objects.
	private String userName;
	private String sessionID;
	private Vector<Frame> frames;
	private boolean done;
	private String skillLevel;
	
	//Final Computed data.
	private int[] computedHandMotions;
	private int[] computedLeftMotions;
	private int[] computedRightMotions;
	private float[] computedHandVel;
	private float[] computedLeftVel;
	private float[] computedRightVel;
	
	//Technical objects.
	private DatabaseController db;
	private Thread calculator;
	private Controller leapController;

	/**
	 * This class computes the hand motion metrics as discussed
	 * in the 2003 University of Toronto paper. Essentially, it
	 * gets a session, loads it in, and allows parameters to be
	 * set before computing the metrics.
	 * @param userName The name of the user.
	 * @param sessionID The session number.
	 */
	public MetricsCalculator(String userName, String sessionID){
		//Gets the database reader.
		db = ProgramController.database;
		
		//Sets up the default values for calculator.
		frameSmoothening = DEFAULT_SMOOTHENING;
		smootheningWindow = DEFAULT_WINDOW;
		confidenceRemove = DEFAULT_CONFIDENCE;
		frameExamineValue = DEFAULT_EXAMINE_VAL;
		sensitivityValue = DEFAULT_SENSITIVITY;
		velocityIgnoreValue = DEFAULT_VEL_IGNORE;
		
		//Sets the skill level to null.
		skillLevel = null;
		
		//Sets the corresponding user id and session.
		this.userName = userName;
		this.sessionID = sessionID;
		
		//States that the program is not done.
		done = false;
		computedHandMotions = null;
		computedLeftMotions = null;
		computedRightMotions = null;
		
		//Finally load in the frames for the session.
		loadSessionFrames();
	}
	
	/**
	 * Sets whether or not smoothening occurs (Default
	 * is yes)
	 * @param smootheningValue True or false for smoothening.
	 */
	public void setFrameSmoothening(boolean smootheningValue){
		frameSmoothening = smootheningValue;
	}
	
	/**
	 * Sets the window size (ie the number of values to be
	 * weighted) for the smoothening operation. This is only
	 * considered if smoothening is enabled. (Default is 3).
     * @param newWindow The new window size (must be odd!)
	 */
	public void setSmootheningWindow(int newWindow){
		if (newWindow % 2 == 1)
			smootheningWindow = newWindow;
	}
	
	/**
	 * The threshold for when frames should be removed. Everything
	 * below the confidence level is removed. (Default is 0, ie
	 * no removal).
	 * @param newRemovalVal
	 */
	public void setConfidenceRemovalValue(int newRemovalVal){
		if (newRemovalVal > 100 || newRemovalVal < 0) return;
		
		confidenceRemove = newRemovalVal;
	}
	
	/**
	 * The value for how many frame are to be examined.
	 * This value determines the N value for every N frame
	 * to be examined. (Default is 1, ie every frame).
	 * @param newExamineVal The N value.
	 */
	public void setFrameExamineValue(int newExamineVal){
		if (newExamineVal > frames.size() || newExamineVal < 1) return;
		
		frameExamineValue = newExamineVal;
	}
	
	/**
	 * Sets the value for what constitutes a movement.
	 * This value will be how much the magnitude of x, y, z can
	 * differ before a movement is determined.
	 * @param newSensitivityVal The sensitivity value.
	 */
	public void setSensitivityValue(int newSensitivityVal){
		sensitivityValue = newSensitivityVal;
	}
	
	/**
	 * Sets the value for which velocities below this
	 * value will not be considered. If 0 is the value
	 * then all values will be considered.
	 * @param newIgnoreVal The new value to ignore
	 */
	public void setVelocityIgnoreValue(float newIgnoreVal){
		velocityIgnoreValue = newIgnoreVal;
	}
	
	/**
	 * Sets the selected skill level for the 
	 * metrics values.
	 */
	public void setSkillLevel(String sValue){
		skillLevel = sValue;
	}
	
	/**
	 * Notifies whether the hand motion computations
	 * are done or not.
	 * @return Whether the calculations are done.
	 */
	public boolean isDone(){
		return done;
	}
	
	/**
	 * Returns the computed hand motions.
	 * Only works when the calculator is
	 * finished.
	 * @return The number of hand motions or
	 * null if not completed.
	 */
	public int[] getHandMotions(){
		//Ensures the data is done.
		if (done == false) return null;
		
		return computedHandMotions;
	}
	
	/**
	 * Returns the computed finger motions
	 * for a specific hand. Only works
	 * if the calculator is finished.
	 * @param isLeft If you want the left hand fingers.
	 * @return Data on the fingers.
	 */
	public int[] getFingerMotions(boolean isLeft){
		//Ensures the data is done.
		if (done == false) return null;
		if (isLeft) return computedLeftMotions;
		
		return computedRightMotions;
	}
	
	/**
	 * Returns the computed hand velocity.
	 * Only works when the calculator is
	 * finished.
	 * @return The number of hand motions or
	 * null if not completed.
	 */
	public float[] getHandVelocity(){
		//Ensures the data is done.
		if (done == false) return null;
		
		return computedHandVel;
	}
	
	/**
	 * Returns the computed finger velocity
	 * for a specific hand. Only works
	 * if the calculator is finished.
	 * @param isLeft If you want the left hand fingers.
	 * @return Data on the fingers.
	 */
	public float[] getFingerVelocity(boolean isLeft){
		//Ensures the data is done.
		if (done == false) return null;
		if (isLeft) return computedLeftVel;
		
		return computedRightVel;
	}
	
	/**
	 * Simply returns the reported skill level.
	 * @return The skill level reported.
	 */
	public String getSkillLevel(){
		return skillLevel;
	}
	
	/**
	 * Starts a new thread. This ensures only one thread
	 * is running at a time.
	 */
	public void start(){
		//Only starts a new thread if there isn't an instance of it running.
		if (calculator == null){
			done = false;
			calculator = new Thread(this);
			calculator.start();
		}
	}
	
	/**
	 * Carries out the calculations. Checks which
	 * operations need to be performed and such.
	 * This is performed on another thread.
	 */
	public void run(){
		//Checks to see whether frame smoothening is set.
		if (frameSmoothening == true && smootheningWindow > 1) 
			performSmoothening();
		
		//Now checks to see if we remove any frames.
		if (confidenceRemove > 0) removeBadFrames();
		
		//Finally, we actually compute the value.
		int numMotions[] = computeNumberMotions();
		
		//Now, we compute the velocity.
		float velocity[] = computeVelocity();
		
		//Delivers the data to the global variables.
		storeHandMotions(numMotions);
		storeVelocity(velocity);
		
		//Writes to database.
		db.writeMetrics(userName, sessionID, skillLevel,
						computedHandMotions, computedLeftMotions, computedRightMotions,
						computedHandVel, computedLeftVel, computedRightVel);
		
		//States that the program is done.
		done = true;
	}
	
	private float[] computeVelocity() {
		//Creates an array for the left and right hands.
		float[] velocity = new float[12];
		int[] setSize = new int[12];
		
		//We initialize the array to 0.
		for (int i = 0; i < 12; i++){
			setSize[i] = 0;
			velocity[i] = 0;
		}
		
		//Now we iterate until there is no more.
		for (int i = 0; i < frames.size(); i+= frameExamineValue){
			//We get the frame.
			Frame current = frames.elementAt(i);
			
			//We loop through each of the hands for that finger.
			for (int j = 0; j < current.hands().count(); j++){
				Hand hand = current.hands().get(j);
				
				//We get the palm average.
				float currVel = hand.palmVelocity().magnitude();
				
				//Skip if it falls below ignore system.
				if (currVel < velocityIgnoreValue) continue;
				
				if (hand.isLeft()){		
					//Compute the running average.
					velocity[0] += currVel;
					setSize[0]++;
					velocity[0] = velocity[0] / setSize[0];
				} else {
					//Compute the running average.
					velocity[1] += currVel;
					setSize[1]++;
					velocity[1] = velocity[1] / setSize[1];
				}
				
				//Now, we iterate through the fingers.
				for (int k = 0; k < hand.fingers().count(); k++){
					Finger finger = hand.fingers().get(k);
					
					//First, we get the palm average.
					currVel = finger.tipVelocity().magnitude();
					if (currVel < velocityIgnoreValue) continue;
					
					//Now we need to figure out what to associate it with.
					int pos = 0;
					if (hand.isLeft())
						pos = finger.type().swigValue() + LEFT_HAND_OFFSET;
					else 
						pos = finger.type().swigValue() + RIGHT_HAND_OFFSET;
					
					//We then compute the average.
					velocity[pos] += currVel;
					setSize[pos]++;
					velocity[pos] = velocity[pos] / setSize[pos];
				}
			}
		}
		
		return velocity;
	}

	/**
	 * Loads in all the frames from the
	 * session that is specified in the object.
	 */
	private void loadSessionFrames(){
		//Initialize frames.
		frames = new Vector<Frame>();
		
		//Initializes the leap controller object
		//(Needed to deserialize...for some reason?)
		leapController = new Controller();
		
		//Loads in the frames.
		Vector<byte[]> byteStream = db.getFrames(userName, sessionID);
		for (int i = 0; i < byteStream.size(); i++){
			Frame currentFrame = new Frame();
			currentFrame.deserialize(byteStream.elementAt(i));
			frames.add(currentFrame);
		}
	}
	
	private void performSmoothening(){
		//Computes sub-window size
		int subWindow = smootheningWindow / 2;
		
		//Loops through the frames.
		for (int i = 0; i < frames.size(); i++){
			//Stores the main frame being smoothened.
			Frame mainFrame = frames.elementAt(i);
			
			//Creates a sublist of frames for the current operation.
			List<Frame> subList = frames.subList
					(i - subWindow < 0 ? 0 : i - subWindow,
					 i + subWindow + 1 > frames.size() ? frames.size() : i + subWindow + 1);
			
			//Once the sublist has been created, compute the weighted average for each palm and fingers.
			for (int j = 0; j < mainFrame.hands().count(); j++){
				//Generates the average for that palm's hand.
				generatePalmAverage(mainFrame.hand(j), mainFrame, subList);
			
				//Generates the average for all the fingers in the hand.
				generateFingerAverage(mainFrame.hand(j), mainFrame, subList);
			}
		}
	}
	
	private void generatePalmAverage(Hand hand, Frame main, List<Frame> subList) {
		float averageValueX = 0;
		float averageValueY = 0;
		float averageValueZ = 0;
		int windowSize = subList.size();

		//First, gets the main frame average.
		averageValueX += hand.palmPosition().getX() * 0.5;
		averageValueY += hand.palmPosition().getY() * 0.5;
		averageValueZ += hand.palmPosition().getZ() * 0.5;
		
		//Loops through each of the sublist to find the correct one.
		float tempX = 0, tempY = 0, tempZ = 0;
		for (int i = 0; i <= subList.size(); i++){
			//Sees if we are examining the current frame or at the end.
			if (i == subList.size() || subList.get(i).equals(main)){
				//We add the temp values to the average.
				averageValueX += tempX * 0.25;
				averageValueY += tempY * 0.25;
				averageValueZ += tempZ * 0.25;
				
				//Now we reset the temp values.
				tempX = 0;
				tempY = 0;
				tempZ = 0;
			} else {
				//We first need to find the correct hand.
				for (int j = 0; j < subList.get(i).hands().count(); j++){
					//Ensures we are working on the correct hand.
					if (subList.get(i).hand(j).isLeft() == hand.isLeft()){
						tempX += subList.get(i).hand(j).palmPosition().getX();
						tempY += subList.get(i).hand(j).palmPosition().getY();
						tempZ += subList.get(i).hand(j).palmPosition().getZ();
						break;
					}
				}
			}
		}
		
		//Divides by the window size.
		averageValueX = averageValueX / windowSize;
		averageValueY = averageValueY / windowSize;
		averageValueZ = averageValueZ / windowSize;
		
		//Finally, sets the palm value for the hand.
		hand.palmPosition().setX(averageValueX);
		hand.palmPosition().setY(averageValueY);
		hand.palmPosition().setZ(averageValueZ);
	}
	
	private void generateFingerAverage(Hand hand, Frame currentFrame, List<Frame> subList){
		float averageX, averageY, averageZ;
		
		//Loops through all of the fingers.
		for (int i = 0; i < hand.fingers().count(); i++){
			//Clears the average.
			averageX = 0;
			averageY = 0;
			averageZ = 0;
			
			//Gets the current finger.
			Finger currentFinger = hand.finger(i);
			
			//Adds the current finger to the average.
			averageX += currentFinger.tipPosition().getX() * 0.5;
			averageY += currentFinger.tipPosition().getY() * 0.5;
			averageZ += currentFinger.tipPosition().getZ() * 0.5;
			
			//Now we loop through all the sub frames (for their average).
			float tempX = 0, tempY = 0, tempZ = 0;
			for (int j = 0; j <= subList.size(); j++){
				Frame subFrame = subList.get(j);
				
				//Check to make sure this isn't main frame or we aren't done.
				if (j == subList.size() || subFrame.equals(currentFrame)){
					//We add the current average to the average.
					averageX += tempX * 0.25;
					averageY += tempY * 0.25;
					averageZ += tempZ * 0.25;
					
					//Now we clear the average.
					tempX = 0;
					tempY = 0;
					tempZ = 0;
					continue;
				}
				
				//Otherwise, we loop to find the correct hand in that frame.
				for (int k = 0; k < subFrame.hands().count(); k++){
					//Check to make sure the hand is the correct hand.
					if (subFrame.hand(k).isLeft() != hand.isLeft()) continue;
					
					//Now we find the finger.
					for (int l = 0; l < subFrame.hand(k).fingers().count(); l++){
						//Check to see if its the same type of finger.
						if (subFrame.hand(k).finger(l).type().equals(currentFinger.type())){
							//Now we add to the average.
							tempX += subFrame.hand(k).finger(l).tipPosition().getX();
							tempY += subFrame.hand(k).finger(l).tipPosition().getY();
							tempZ += subFrame.hand(k).finger(l).tipPosition().getZ();
						}
					}
				}
			}
			
			//We divide by the window size.
			averageX = averageX / subList.size();
			averageY = averageY / subList.size();
			averageZ = averageZ / subList.size();
			
			//Finally, we add the average to the finger.
			currentFinger.tipPosition().setX(averageX);
			currentFinger.tipPosition().setY(averageY);
			currentFinger.tipPosition().setZ(averageZ);
		}
	}
	
	private void removeBadFrames(){
		//Reduces confidence to float value.
		float confidenceCheck = ((float) confidenceRemove) / 100;
		
		int i = 0;
		
		//Loop through all the frames.
		while (i < frames.size()){
			//Gets the current frame.
			Frame current = frames.elementAt(i);
			
			//Loops through each of the hands now.
			for (int j = 0; j < current.hands().count(); j++){
				if (current.hand(j).confidence() < confidenceCheck){
					//Removes the hand due to bad confidence.
					frames.remove(i);
					i--;
					break;
				}
			}
			
			//Increments i.
			i++;
		}
	}
	
	private int[] computeNumberMotions(){
		//Creates an array for the left and right hands.
		int[] handMotions = new int[12];
		for (int i = 0; i < 12; i++){
			handMotions[i] = 0;
		}
		
		//Now we iterate until there is no more.
		for (int i = 0; i < frames.size(); i+= frameExamineValue){
			//We get the frame.
			Frame current = frames.elementAt(i);
			
			//Gets the frame to examine.
			Frame next = getNextFrame(i);
			if (next == null) continue;
			
			//Now we see if there are hands.
			HandList hands = current.hands();
			for (int j = 0; j < hands.count(); j++){
				Hand currentHand = hands.get(j);
				
				//Gets the motion for a specific hand.
				boolean isMotion = parseHand(currentHand, next, currentHand.isLeft());
				
				//Gets the hand motions.
				if (isMotion && currentHand.isLeft()) handMotions[0]++;
				else if (isMotion && currentHand.isRight()) handMotions[1]++;
				
				//Next, we loop through each of the fingers.
				FingerList fingers = currentHand.fingers();
				for (int k = 0; k < fingers.count(); k++){
					Finger currentFinger = fingers.get(k);
					
					//Gets the motion for a specific finger.
					isMotion = parseFinger(currentHand, currentFinger, next, currentHand.isLeft());
					
					//Gets the finger motions.
					if (isMotion && currentHand.isLeft()) 
						handMotions[currentFinger.type().swigValue() + LEFT_HAND_OFFSET]++;
					else if (isMotion && currentHand.isRight()) 
						handMotions[currentFinger.type().swigValue() + RIGHT_HAND_OFFSET]++;
				}
			}
		}
		
		return handMotions;
	}
	
	/**
	 * Gets the next directed frame. Returns
	 * null on error.
	 * @currentVal The current frame number.
	 * @return The next frame.
	 */
	private Frame getNextFrame(int currentVal){
		Frame next = null;
		
		try{
			next = frames.elementAt(currentVal + frameExamineValue);
		} catch (IndexOutOfBoundsException e){
			next = frames.elementAt(frames.size() - 1);
			
			//Checks to make sure we aren't comparing the same frame.
			if (frames.elementAt(currentVal).equals(next)){
				next = null;
			}
		}
		
		return next;
	}
	
	/**
	 * Gets the relevant hand of the next frame.
	 * @param currentHand The current hand being comparedd.
	 * @param nextFrame The next frame.
	 * @return The next hand.
	 */
	private Hand getNextHand(Hand currentHand, Frame nextFrame){
		Hand nextHand = null;
		
		//First, attempts to locate from ID.
		nextHand = nextFrame.hand(currentHand.id());
		if (nextHand.isValid()) return nextHand;
		
		//If invalid, the hand changed id's since last update.
		//Loop to find similar hand.
		for (int i = 0; i < nextFrame.hands().count(); i++){
			if (nextFrame.hand(i).isLeft() == currentHand.isLeft()){
				nextHand = nextFrame.hand(i);
				break;
			}
		}
		
		return nextHand;
	}
	
	/**
	 * This method retrieves the finger of the next frame.
	 * @param currentFinger The current finger being analyzed.
	 * @param nextHand The next hand of the finger.
	 * @return The next finger.
	 */
	private Finger getNextFinger(Finger currentFinger, Hand nextHand){
		Finger nextFinger = null;
		
		//Safeguard against null pointer exception.
		if (nextHand == null || nextHand.isValid() == false) return nextFinger;
		
		//First, we try to locate based on the previous finger.
		nextFinger = nextHand.finger(currentFinger.id());
		if (nextFinger.isValid()) return nextFinger;
		
		//If this doesn't work, we manually locate.
		for (int i = 0; i < nextHand.fingers().count(); i++){
			if (nextHand.fingers().get(i).type() == currentFinger.type()){
				nextFinger = nextHand.fingers().get(i);
				break;
			}
		}
		
		return nextFinger;
	}
	
	private boolean parseFinger(Hand currentHand, Finger currentFinger, Frame next, boolean left) {
		//We start by finding the appropriate finger in the next frame.
		Hand nextHand = getNextHand(currentHand, next);
		Finger nextFinger = getNextFinger(currentFinger, nextHand);
		
		//See if we found the next finger.
		if (nextFinger == null || nextFinger.isValid() == false) return false;
		
		//If we have, we now compute the x,y,z diff.
		float xDiff = Math.abs(currentFinger.tipPosition().getX() 
				- nextFinger.tipPosition().getX());
		float yDiff = Math.abs(currentFinger.tipPosition().getY() 
				- nextFinger.tipPosition().getY());
		float zDiff = Math.abs(currentFinger.tipPosition().getZ() 
				- nextFinger.tipPosition().getZ());
		
		//Finally, we calculate the 3D vector for the difference.
		float magnitude = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2) + Math.pow(zDiff, 2));
		
		//We now need to compare the magnitude to the acceptable difference.
		if (magnitude > sensitivityValue) return true;
		
		return false;
	}

	private boolean parseHand(Hand current, Frame next, boolean leftHand){
		//We start by finding the hand in the next frame.
		Hand nextHand = getNextHand(current, next);
		
		//See if we couldn't find it.
		if (nextHand == null) return false;
		
		//Now we get x,y,z difference.
		float xDiff = Math.abs(current.palmPosition().getX() 
				- nextHand.palmPosition().getX());
		float yDiff = Math.abs(current.palmPosition().getY() 
				- nextHand.palmPosition().getY());
		float zDiff = Math.abs(current.palmPosition().getZ() 
				- nextHand.palmPosition().getZ());
		
		//Finally, we calculate the 3D vector for the difference.
		float magnitude = (float) Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2) + Math.pow(zDiff, 2));
		
		//We now need to compare the magnitude to the acceptable difference.
		if (magnitude > sensitivityValue) return true;
		
		return false;
	}
	
	private void storeHandMotions(int[] numMotions){
		computedHandMotions = new int[2];
		computedLeftMotions = new int[5];
		computedRightMotions = new int[5];
		
		for (int i = 0; i < 2; i++){
			computedHandMotions[i] = numMotions[i];
		}
		for (int i = 2; i < 7; i++){
			computedLeftMotions[i - 2] = numMotions[i];
		}
		for (int i = 7; i < 12; i++){
			computedRightMotions[i - 7] = numMotions[i];
		}
	}
	
	private void storeVelocity(float[] velocity){
		computedHandVel = new float[2];
		computedLeftVel = new float[5];
		computedRightVel = new float[5];
		
		for (int i = 0; i < 2; i++){
			computedHandVel[i] = velocity[i];
		}
		for (int i = 2; i < 7; i++){
			computedLeftVel[i - 2] = velocity[i];
		}
		for (int i = 7; i < 12; i++){
			computedRightVel[i - 7] = velocity[i];
		}
	}
}
