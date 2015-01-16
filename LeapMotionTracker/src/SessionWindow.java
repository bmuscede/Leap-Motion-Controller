import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;


public class SessionWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 6776933146081331071L;
	
	private JPanel contentPane;
	private JTable tlbSessions;
	private JButton btnPlay;
	private JButton btnDelete;
	private JButton btnCompute;
	private JButton btnView;
	private JLabel lblNoSessions;
	private String userName;
	
	/**
	 * Create the frame.
	 * @param userName 
	 */
	public SessionWindow(String userName) {
		this.userName = userName;
		setResizable(false);
		setTitle("Leap Motion Controller - [Sessions] ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 490, 338);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel(userName + "'s Sessions:");
		lblTitle.setBounds(10, 7, 464, 28);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 16));
		contentPane.add(lblTitle);
		
		lblNoSessions = new JLabel("<html><center>No sessions present!<br>Restart the program and record a new session.</html>");
		lblNoSessions.setHorizontalAlignment(SwingConstants.CENTER);
		lblNoSessions.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNoSessions.setBounds(52, 100, 361, 67);
		lblNoSessions.setVisible(false);
		contentPane.add(lblNoSessions);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(8, 34, 466, 197);
		contentPane.add(scrollPane);
		
		tlbSessions = new JTable();
		scrollPane.setViewportView(tlbSessions);
		tlbSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tlbSessions.setShowVerticalLines(false);
		tlbSessions.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Session ID", "Session Date", "Session Length (HH:MM:SS)", "Metrics Calculated"
			}
		));
		tlbSessions.getColumnModel().getColumn(0).setPreferredWidth(37);
		tlbSessions.addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        //Gets the row clicked.
		    	int row = tlbSessions.rowAtPoint(evt.getPoint());
		    	if (row < 0) return;
		    	
		    	//Retitles the buttons for metrics.
		    	String metricValue = (String) tlbSessions.getValueAt(row, 3);
		    	if (metricValue.equals("No")){
		    		btnCompute.setText("Compute Metrics");
		    		btnView.setEnabled(false);
		    	} else {
		    		btnCompute.setText("Recompute Metrics");
		    		btnView.setEnabled(true);
		    	}
		    	
		    	//Enables the buttons.
		    	btnPlay.setEnabled(true);
		    	btnDelete.setEnabled(true);
		    	btnCompute.setEnabled(true);
		    }
		});
		
		btnPlay = new JButton("Playback Session...");
		btnPlay.setBounds(10, 251, 171, 23);
		btnPlay.addActionListener(this);
		contentPane.add(btnPlay);
		
		btnDelete = new JButton("Delete");
		btnDelete.setBounds(303, 251, 171, 23);
		btnDelete.addActionListener(this);
		contentPane.add(btnDelete);
		
		JSeparator sepOptions = new JSeparator();
		sepOptions.setBounds(10, 242, 467, 2);
		contentPane.add(sepOptions);
		
		btnCompute = new JButton("Compute Metrics");
		btnCompute.addActionListener(this);
		btnCompute.setBounds(68, 281, 171, 23);
		contentPane.add(btnCompute);
		
		btnView = new JButton("View Session Metrics");
		btnView.addActionListener(this);
		btnView.setBounds(252, 281, 171, 23);
		contentPane.add(btnView);
		
		//Refreshes the table.
		refreshTable();
	}
	
	public void refreshTable() {
		//Gets the users for the object.
		Vector<Vector<String>> sessions = PlaybackController.getSessions(userName);
		if (!checkValid(sessions)){
			refreshTable();
			return;
		}
		
		//Sees how many users are present.
		if (sessions == null || sessions.size() <= 1){
			lblNoSessions.setVisible(true);
		} else {
			lblNoSessions.setVisible(false);
		}
		
		//Finally, populates the JTable with the users.
		DefaultTableModel modelUsers = (DefaultTableModel) tlbSessions.getModel();
		modelUsers.setRowCount(0);
		for (int i = 1; i < sessions.size(); i++){
			Vector<String> currentSession = sessions.elementAt(i);
			
			//Formats the timer value.
			int timerValue = Integer.parseInt(currentSession.elementAt(2));
        	String value = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(timerValue),
    			    TimeUnit.SECONDS.toMinutes(timerValue) % TimeUnit.HOURS.toMinutes(1),
    			    timerValue % TimeUnit.MINUTES.toSeconds(1));
        	
        	//Adds the rows in.
			modelUsers.addRow(new Object[]{currentSession.elementAt(1), 
										   currentSession.elementAt(3), 
										   value,
										   currentSession.elementAt(4) != null ?
												   "Yes" : "No"});
		}
		
		disableButtons();
	}

	/**
	 * Checks to see if all the sessions are valid on disk.
	 * @param sessions All the sessions from the db.
	 */
	private boolean checkValid(Vector<Vector<String>> sessions){
		Vector<String> invalidSessions = new Vector<String>();
		String workingDir = System.getProperty("user.dir");
		
		//Loops through each of the sessions.
		for (int i = 1; i < sessions.size(); i++){
			//Checks if the file exists for that user id.
			File currentSession = new File(workingDir + "/data/" + userName + "/" + sessions.get(i).get(1));
			if (!currentSession.exists()){
				invalidSessions.add(sessions.get(i).get(1));
			}
		}
		
		//At this point we only continue if there are invalid sessions.
		if (invalidSessions.size() <= 0) return true;
		
		//Remove each of those sessions.
		String message = "";
		for (int i = 0; i < invalidSessions.size(); i++){
			//Deletes the session.
			PlaybackController.deleteSessionUser(userName, invalidSessions.elementAt(i));
			
			//Now, adds it to the message.
			if (i + 1 == invalidSessions.size() && i != 0){
				message += " and " +  invalidSessions.elementAt(i);
			} else if (i + 1 == invalidSessions.size()){
				message += invalidSessions.elementAt(i);
			} else {
				message += invalidSessions.elementAt(i) + ", ";
			}
		}
		
		//Displays an error message.
		ProgramController.createDialog("<html>The session(s) " + message + " are corrupt!<br>They have " +
				"been removed from your system!", "Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	private void disableButtons() {
		btnPlay.setEnabled(false);
		btnDelete.setEnabled(false);
		btnCompute.setEnabled(false);
		btnView.setEnabled(false);
	}

	public void actionPerformed(ActionEvent event) {
		//Finds the appropriate action.
		if (event.getSource().equals(btnPlay)){
			playHandler();
		} else if (event.getSource().equals(btnDelete)){
			deleteSelectedSession();
		} else if (event.getSource().equals(btnCompute)){
			computeHandler();
		} else if (event.getSource().equals(btnView)){
			viewHandler();
		}
	}

	private void viewHandler(){
		//We get the selected session id.
		int row = tlbSessions.getSelectedRow();
			
		//Now we get the session number.
		String session = (String) tlbSessions.getValueAt(row, 0);
		ProgramController.showMetricsData(userName, session);
	}
	
	private void computeHandler() {
		//We get the selected session id.
		int row = tlbSessions.getSelectedRow();
		
		//Now we get the session number.
		String session = (String) tlbSessions.getValueAt(row, 0);
		ProgramController.showMetrics(userName, session);
	}

	private void playHandler() {
		//First, we get the selected session.
		int selectedRow = tlbSessions.getSelectedRow();
		if (selectedRow < 0) return;
		
		//Now we get data about that user.
		String session = (String) tlbSessions.getModel().getValueAt(selectedRow, 0);
		
		//Now, we start the playback.
		PlaybackController.startPlayback(session);
	}

	private void deleteSelectedSession() {
		//First, we get the selected session.
		int selectedRow = tlbSessions.getSelectedRow();
		if (selectedRow < 0) return;
		
		//Now we get data about that user.
		String session = (String) tlbSessions.getModel().getValueAt(selectedRow, 0);
		
		//Confirms the delete.
	    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you would like to delete session #" + session + " " +
	    		"for user " + userName + "?\n" +
	    	    "ALL HAND MOTION DATA FOR THIS SESSION WILL BE LOST...", "Leap Motion Controller", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (reply != JOptionPane.YES_OPTION) {
        	return;
        }		
        
        //Now carries through with the delete.
        if (!PlaybackController.deleteSessionUser(userName, session)){
        	ProgramController.createDialog("<html>Session #" + session + " could not be removed!<br>Please try " +
        			"again later.", "Leap Motion Controller", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        refreshTable();
	}
}
