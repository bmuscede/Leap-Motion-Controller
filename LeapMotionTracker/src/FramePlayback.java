import java.util.Vector;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;

@SuppressWarnings("unused")
public class FramePlayback extends Thread{
	private Vector<Frame> frames;
	private Controller leapController;
	private int pos;
	private volatile boolean playData;
	private Thread playThread;
	
	//For sending null frames
	private final int STOP = 1;
	private final int PAUSED = 2;
	private final int PLAY = 3;
	
	public FramePlayback(Vector<byte[]> frameStream) {
		frames = new Vector<Frame>();

		//Initializes the leap controller object
		//(Needed to deserialize...for some reason?)
		leapController = new Controller();
		
		//Convert bytes to frames
		for (int i = 0; i < frameStream.size(); i++){
			//Restores the frame.
			Frame currentFrame = new Frame();
			currentFrame.deserialize(frameStream.elementAt(i));
			
			//Stores it in the frames vector.
			frames.add(currentFrame);
		}
		
		//Sets the current position.
		pos = 0;
		
		//Turns off data playback.
		playData = false;
		
		//Finally, starts the playback thread.
		this.start();
	}
	
	public void start(){
		if (playThread == null){
			//Creates a new thread.
			playThread = new Thread(this);
			playThread.start();
		} else {
			//Resets the counter.
			pos = 0;
			playData = false;
			playThread = new Thread(this);
			playThread.start();
		}
	}
	
	public void run(){
		//Constantly loops
		while (pos < frames.size()){
			//Goes forward only if play is enabled.
			if (!playData) continue;
			
			Frame current = frames.elementAt(pos);
			
			//Manages the frame
			manageFrame(current);
			
			pos++;
		}
		
		//Sends null frame.
		manageFrame(STOP);
		
		//We've finished.
		this.start();
	}
	
	private void manageFrame(int type) {
		byte[] nullFrame = new byte[type];
		for (int i = 0; i < type; i++)
			nullFrame[i] = 0;
		
		//We send null frame to visualizer.
		ProgramController.messageSender.sendFrame(nullFrame);
	}

	private void manageFrame(Frame current) {
		//First, we output the frame data.
		
		//We send it to the visualizer.
		ProgramController.messageSender.sendFrame(current.serialize());
	}

	public void play(){
		//Simply set the play boolean.
		playData = true;
		
		manageFrame(PLAY);
	}
	
	public void stopPlayback(){
		//Reset the thread!
		pos = frames.size();
	}
	
	public void pause(){
		//Simply set the play boolean.
		playData = false;
		
		//Sends a pause frame.
		manageFrame(PAUSED);
	}
}
