import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class StatusWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1734261250858506565L;
	
	private JPanel pnlStatusContent;
	public JLabel lblStatus;
	public JLabel lblTime;
	public JButton btnStartStop;
	public JButton btnPause;
	
	//Variables for timer execution.
	public Timer programTimer;
	public int timerValue;
	
	//Variables to keep track of state.
	public boolean start;
	public boolean paused;
	private JLabel lblProfile;
	
	/**
	 * Create the frame.
	 */
	public StatusWindow() {
		//Sets state variables.
		start = false;
		paused = false;
		
		setTitle("Leap Motion Tracker - [Status]");
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 439, 188);
		pnlStatusContent = new JPanel();
		pnlStatusContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(pnlStatusContent);
		pnlStatusContent.setLayout(null);
		
		lblStatus = new JLabel("\r\nStatus: Not Connected");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 23));
		lblStatus.setBounds(10, 0, 414, 56);
		pnlStatusContent.add(lblStatus);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 54, 414, 2);
		pnlStatusContent.add(separator);
		
		lblTime = new JLabel("00:00:00");
		lblTime.setHorizontalAlignment(SwingConstants.CENTER);
		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 42));
		lblTime.setBounds(10, 56, 414, 44);
		pnlStatusContent.add(lblTime);
		
		btnStartStop = new JButton("Start");
		btnStartStop.setEnabled(false);
		btnStartStop.setBounds(98, 114, 115, 23);
		btnStartStop.addActionListener(this);
		pnlStatusContent.add(btnStartStop);
		
		btnPause = new JButton("Pause");
		btnPause.setEnabled(false);
		btnPause.setBounds(241, 114, 100, 23);
		btnPause.addActionListener(this);
		pnlStatusContent.add(btnPause);
		
		lblProfile = new JLabel("Profile: <NAME>");
		lblProfile.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
		lblProfile.setBounds(10, 141, 414, 14);
		pnlStatusContent.add(lblProfile);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(btnStartStop)){
			startStopHandler();
		} else if (event.getSource().equals(btnPause)){
			pauseHandler();
		}
	}

	private void pauseHandler() {
		if (paused == false){
			//Sets program state.
			paused = true;
			btnPause.setText("Resume");

			//Pauses the timer.
			programTimer.cancel();
			
			//Sends message to controller.
			ProcedureController.sendMessage(ProcedureController.PAUSED);
		} else {
			//Sets program state.
			paused = false;
			btnPause.setText("Pause");
			
			//Restarts the timer.
			programTimer = new Timer();
			programTimer.schedule(new Tick(), 1000, 1000);
			
			//Sends message to controller.
			ProcedureController.sendMessage(ProcedureController.RESUME);
		}
	}

	private void startStopHandler() {
		if (start == false){
			//Sets state accordingly.
			start = true;
			btnPause.setEnabled(true);
			btnStartStop.setText("Stop");
			
			//Creates a new (fresh) timer.
			programTimer = new Timer();
			timerValue = 0;
			programTimer.schedule(new Tick(), 1000, 1000);
			
			//Sends message to controller.
			ProcedureController.sendMessage(ProcedureController.COLLECTING);
		} else {
			//Sets state accordingly.
			start = false;
			btnPause.setEnabled(false);
			paused = false;
			btnStartStop.setText("Start");
			
			//Removes the timer.
			programTimer.cancel();
			
			//Resets the timer value.
			lblTime.setText("00:00:00");
			
			//Sends message to controller.
			ProcedureController.sendMessage(ProcedureController.NOT_COLLECTING);
		}
	}
	
	public void sendStopMessage() {
		//Ensures data collection is occurring.
		if (start) startStopHandler();
		
	}
	
	class Tick extends TimerTask {
        public void run() {
        	//Increments the timer
        	timerValue++;
        	
        	//Formats the timer value.
        	String value = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(timerValue),
    			    TimeUnit.SECONDS.toMinutes(timerValue) % TimeUnit.HOURS.toMinutes(1),
    			    timerValue % TimeUnit.MINUTES.toSeconds(1));
        	lblTime.setText(value);
        }
	}

	public void setUser(String currentUser) {
		lblProfile.setText("Profile: " + currentUser);
	}
}

