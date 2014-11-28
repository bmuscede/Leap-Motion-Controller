import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;


public class UserWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = -1218373856456167662L;
	private JPanel contentPane;
	private JTable tlbUsers;
	private JButton btnDelete;
	private JButton btnRecord;
	private JButton btnNew;
	private JButton btnPlayback;
	private JLabel lblNoUsers;
	
	/**
	 * Create the frame.
	 */
	public UserWindow() {
		setResizable(false);
		setTitle("Leap Motion Tracker - [Select User] ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 490, 330);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("Please Select a Profile:");
		lblTitle.setBounds(10, 7, 464, 28);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 16));
		contentPane.add(lblTitle);
		
		lblNoUsers = new JLabel("<html><center>No users present!<br>Select \"New User...\" to create a new user.</center></html>");
		lblNoUsers.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNoUsers.setBounds(52, 100, 235, 67);
		lblNoUsers.setVisible(false);
		contentPane.add(lblNoUsers);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(8, 34, 326, 197);
		contentPane.add(scrollPane);
		
		tlbUsers = new JTable();
		scrollPane.setViewportView(tlbUsers);
		tlbUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tlbUsers.setShowVerticalLines(false);
		tlbUsers.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"User ID", "Name", "Sessions"
			}
		) {
			private static final long serialVersionUID = 7509521877188570057L;
			boolean[] columnEditables = new boolean[] {
				false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tlbUsers.getColumnModel().getColumn(0).setResizable(false);
		tlbUsers.getColumnModel().getColumn(1).setResizable(false);
		tlbUsers.getColumnModel().getColumn(2).setResizable(false);
		tlbUsers.getColumnModel().getColumn(2).setPreferredWidth(35);
		tlbUsers.addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        //Gets the row clicked.
		    	int row = tlbUsers.rowAtPoint(evt.getPoint());
		    	if (row < 0) return;
		    	
		    	//Enables the buttons.
		    	btnRecord.setEnabled(true);
		    	btnDelete.setEnabled(true);
		    	
		    	//Enables playback ONLY if there are sessions.
		    	if (Integer.parseInt((String) tlbUsers.getModel().getValueAt(row, 2)) > 0)
		    		btnPlayback.setEnabled(true);
		    	else
		    		btnPlayback.setEnabled(false);
		    }
		});
		
		btnRecord = new JButton("Start Program...");
		btnRecord.setBounds(343, 46, 131, 23);
		btnRecord.addActionListener(this);
		contentPane.add(btnRecord);
		
		btnNew = new JButton("New User...");
		btnNew.setBounds(346, 162, 131, 23);
		btnNew.addActionListener(this);
		contentPane.add(btnNew);
		
		btnPlayback = new JButton("Data Playback...");
		btnPlayback.setBounds(343, 82, 131, 23);
		btnPlayback.addActionListener(this);
		contentPane.add(btnPlayback);
		
		btnDelete = new JButton("Delete User");
		btnDelete.setBounds(346, 199, 131, 23);
		btnDelete.addActionListener(this);
		contentPane.add(btnDelete);
		
		JSeparator sepOptions = new JSeparator();
		sepOptions.setBounds(10, 242, 467, 2);
		contentPane.add(sepOptions);
		
		JLabel lblProgramOptions = new JLabel("Program Options:");
		lblProgramOptions.setHorizontalAlignment(SwingConstants.LEFT);
		lblProgramOptions.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblProgramOptions.setBounds(10, 255, 147, 39);
		contentPane.add(lblProgramOptions);
		
		JCheckBox chkLimited = new JCheckBox("Start Program in Limited Mode");
		chkLimited.setEnabled(false);
		chkLimited.setBounds(157, 250, 322, 23);
		contentPane.add(chkLimited);
		
		JCheckBox chkPermaSignIn = new JCheckBox("Always Sign in With Selected Profile");
		chkPermaSignIn.setEnabled(false);
		chkPermaSignIn.setBounds(157, 275, 322, 23);
		contentPane.add(chkPermaSignIn);
		
		//Refresh the menu.
		refreshTable();
	}
	
	public void actionPerformed(ActionEvent event) {
		//Finds the appropriate action.
		if (event.getSource().equals(btnRecord)){
			loginHandler();
		} else if (event.getSource().equals(btnPlayback)){
			playbackHandler();
		} else if (event.getSource().equals(btnNew)){
			createNewUser();
		} else if (event.getSource().equals(btnDelete)){
			deleteSelectedUser();
		}
	}

	private void playbackHandler() {
		//First, we get the selected user.
		int selectedRow = tlbUsers.getSelectedRow();
		if (selectedRow < 0) return;
		
		//Now we get data about that user.
		String userName = (String) tlbUsers.getModel().getValueAt(selectedRow, 0);
		
		ProgramController.runPlaybackView(userName);
	}

	private void deleteSelectedUser() {
		//First, we get the selected user.
		int selectedRow = tlbUsers.getSelectedRow();
		if (selectedRow < 0) return;
		
		//Now we get data about that user.
		String userName = (String) tlbUsers.getModel().getValueAt(selectedRow, 0);
		
		//Confirms the delete.
	    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you would like to delete the user " + userName + "?\n" +
	    	    "ALL HAND MOTION DATA FOR THIS USER WILL BE LOST...", "Leap Motion Tracker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (reply != JOptionPane.YES_OPTION) {
        	return;
        }		
        
        //Now carries through with the delete.
        if (!ProgramController.deleteUser(userName)){
        	ProgramController.createDialog("<html>The username " + userName + " could not be removed!<br>Please try " +
        			"again later.", "Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
        	return;
        }
        refreshTable();
	}

	private void createNewUser() {
		//We show the new dialog.
		NewUserDialog dlgNew = new NewUserDialog();
		dlgNew.setVisible(true);
		
		//Now we check the status of the dialog.
		if (dlgNew.returnValue() == false) return;
		
		//Otherwise we obtain data.
		String userName = dlgNew.getUserName();
		String firstName = dlgNew.getFirstName();
		String lastName = dlgNew.getLastName();
		
		//Now we write to the database.
		if (!ProgramController.addNewUser(userName, firstName, lastName)){
			//The unique constraint failed.
			ProgramController.createDialog("<html>The username " + userName + " already exists!<br>Please try " +
					"again with another username!", "Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Otherwise, notify the user.
		ProgramController.createDialog("<html>The user profile has successfully been created!<br>Username: " + userName,
				"Leap Motion Tracker", JOptionPane.INFORMATION_MESSAGE);
		
		//Finally, we refresh the menu.
		refreshTable();
	}

	private void refreshTable() {
		//Gets the users for the object.
		Vector<Vector<String>> users = ProgramController.getDatabaseUsers();
		
		//Sees how many users are present.
		if (users.size() <= 1){
			lblNoUsers.setVisible(true);
		} else {
			lblNoUsers.setVisible(false);
		}
		
		//Finally, populates the JTable with the users.
		DefaultTableModel modelUsers = (DefaultTableModel) tlbUsers.getModel();
		modelUsers.setRowCount(0);
		for (int i = 1; i < users.size(); i++){
			Vector<String> currentUser = users.elementAt(i);
			modelUsers.addRow(new Object[]{currentUser.elementAt(0), 
										   currentUser.elementAt(1) + " " + currentUser.elementAt(2), 
										   currentUser.elementAt(3)});
		}
		
		disableButtons();
	}

	private void disableButtons(){
		//Checks to see if a row is selected.
		if (tlbUsers.getSelectedRow() < 0){
			btnDelete.setEnabled(false);
			btnPlayback.setEnabled(false);
			btnRecord.setEnabled(false);
		}
	}
	
	private void loginHandler() {
		//First, we check to see if an item in the list box is selected at all.
		int rowIndex = tlbUsers.getSelectedRow();
		if (rowIndex < 0) return;
		
		//Now, we get all the appropriate values to pass on to the program controller.
		String userName = (String) tlbUsers.getModel().getValueAt(rowIndex, 0);
		
		//Finally starts the visualizer.
		ProgramController.runProcedureView(userName);
	}
}
