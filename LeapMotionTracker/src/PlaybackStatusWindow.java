import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

public class PlaybackStatusWindow extends JFrame {

	private JPanel contentPane;
	private boolean play;
	private final JButton btnPlayPause;
	private final JButton btnStop;
	private JLabel lblMessage;
	
	//Message Codes
	static final int LOADING = 0;
	static final int READY = 1;
	static final int PLAY = 2;
	static final int PAUSED = 3;
	
	//Messages
	static final String LOADING_MSG = "Loading Visualizer";
	static final String READY_MSG = "Ready for Playback";
	static final String PLAY_MSG = "Playing Session";
	static final String PAUSED_MSG = "Session Paused";
	
	/**
	 * Create the frame.
	 */
	public PlaybackStatusWindow() {
		setAlwaysOnTop(true);
		play = false;
		setTitle("Leap Motion Tracker - [Playback]");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 410, 125);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel pnlPlayback = new JPanel();
		pnlPlayback.setBorder(new TitledBorder(null, "Playback Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlPlayback.setBounds(10, 11, 153, 74);
		contentPane.add(pnlPlayback);
		pnlPlayback.setLayout(null);
		
		btnPlayPause = new JButton("");
		btnPlayPause.setEnabled(false);
		btnPlayPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStop.setEnabled(true);
				if (play){
					PlaybackController.pause();
					btnPlayPause.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\play.png"));
					play = false;
					changeMessage(PAUSED);
				} else {
					PlaybackController.play();
					btnPlayPause.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\pause.png"));
					play = true;
					changeMessage(PLAY);
				}
			}
		});
		btnPlayPause.setBounds(10, 15, 57, 54);
		pnlPlayback.add(btnPlayPause);
		btnPlayPause.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\play.png"));
		
		btnStop = new JButton("");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Resets everything.
				btnStop.setEnabled(false);
				play = false;
				btnPlayPause.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\play.png"));
				PlaybackController.stopPlayback();
				changeMessage(READY);
			}
		});
		btnStop.setEnabled(false);
		btnStop.setBounds(88, 15, 56, 54);
		pnlPlayback.add(btnStop);
		btnStop.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\stop.png"));
		
		JLabel lblStatus = new JLabel("Status:");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 21));
		lblStatus.setBounds(173, 11, 221, 38);
		contentPane.add(lblStatus);
		
		lblMessage = new JLabel("Loading Visualizer");
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		lblMessage.setFont(new Font("Tahoma", Font.ITALIC, 21));
		lblMessage.setBounds(173, 47, 221, 38);
		contentPane.add(lblMessage);
	}
	
	public void readForPlayback(){
		//Converts to play state.
		play = false;
		btnPlayPause.setEnabled(true);
		btnStop.setEnabled(false);
		btnPlayPause.setIcon(new ImageIcon("C:\\Users\\Bryan\\SkyDrive\\School Work\\Computer Science 4490Z\\Leap Motion Projects\\LeapMotionTracker\\img\\play.png"));
	}
	
	public void changeMessage(int statusCode){
		switch(statusCode){
			case LOADING:
				lblMessage.setText(LOADING_MSG);
				break;
			case READY:
				lblMessage.setText(READY_MSG);
				break;
			case PLAY:
				lblMessage.setText(PLAY_MSG);
				break;
			case PAUSED:
				lblMessage.setText(PAUSED_MSG);
				break;
		}
	}
}
