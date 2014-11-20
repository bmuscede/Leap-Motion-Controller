import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
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


public class UserWindow extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JTable tlbUsers;
	private JButton btnDelete;
	private JButton btnSignIn;
	private JButton btnNew;
	private JLabel lblNoUsers;
	
	/**
	 * Create the frame.
	 */
	public UserWindow() {
		setResizable(false);
		setTitle("Leap Motion Controller - [Select User] ");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 490, 310);
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
		
		btnSignIn = new JButton("Sign In...");
		btnSignIn.setBounds(346, 50, 131, 23);
		btnSignIn.addActionListener(this);
		contentPane.add(btnSignIn);
		
		btnNew = new JButton("New User...");
		btnNew.setBounds(346, 162, 131, 23);
		btnNew.addActionListener(this);
		contentPane.add(btnNew);
		
		btnDelete = new JButton("Delete User");
		btnDelete.setBounds(346, 199, 131, 23);
		btnDelete.addActionListener(this);
		contentPane.add(btnDelete);
		
		JSeparator sepOptions = new JSeparator();
		sepOptions.setBounds(10, 242, 467, 2);
		contentPane.add(sepOptions);
		
		JLabel lblProgramOptions = new JLabel("Program Options:");
		lblProgramOptions.setBounds(10, 255, 112, 14);
		contentPane.add(lblProgramOptions);
		
		JCheckBox chkLimited = new JCheckBox("Start Program in Limited Mode");
		chkLimited.setBounds(103, 251, 169, 23);
		contentPane.add(chkLimited);
		
		JCheckBox chkPermaSignIn = new JCheckBox("Always Sign in With Selected Profile");
		chkPermaSignIn.setBounds(274, 251, 203, 23);
		contentPane.add(chkPermaSignIn);
		
		//Refresh the menu.
		refreshTable();
	}
	
	public void actionPerformed(ActionEvent event) {
		//Finds the appropriate action.
		if (event.getSource().equals(btnSignIn)){
			loginHandler();
		} else if (event.getSource().equals(btnNew)){
			createNewUser();
		} else if (event.getSource().equals(btnDelete)){
			deleteSelectedUser();
		}
	}

	private void deleteSelectedUser() {
		//First, we get the selected user.
		int selectedRow = tlbUsers.getSelectedRow();
		if (selectedRow < 0) return;
		
		//Now we get data about that user.
		String userName = (String) tlbUsers.getModel().getValueAt(selectedRow, 0);
		
		//Confirms the delete.
	    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you would like to delete the user " + userName + "?\n" +
	    	    "ALL HAND MOTION DATA FOR THIS USER WILL BE LOST...", "Leap Motion Controller", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (reply != JOptionPane.YES_OPTION) {
        	return;
        }		
        
        //Now carries through with the delete.
        if (!ProgramController.deleteUser(userName)){
        	ProgramController.createDialog("<html>The username " + userName + " could not be removed!<br>Please try " +
        			"again later.", "Leap Motion Controller", JOptionPane.ERROR_MESSAGE);
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
					"again with another username!", "Leap Motion Controller", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Otherwise, notify the user.
		ProgramController.createDialog("<html>The user profile has successfully been created!<br>Username: " + userName,
				"Leap Motion Controller", JOptionPane.INFORMATION_MESSAGE);
		
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
	}

	private void loginHandler() {
		//First, we check to see if an item in the list box is selected at all.
		int rowIndex = tlbUsers.getSelectedRow();
		if (rowIndex < 0) return;
		
		//Now, we get all the appropriate values to pass on to the program controller.
		String userName = (String) tlbUsers.getModel().getValueAt(rowIndex, 0);
		
		//Finally starts the visualizer.
		ProgramController.startMainProgram(userName);
	}
}
