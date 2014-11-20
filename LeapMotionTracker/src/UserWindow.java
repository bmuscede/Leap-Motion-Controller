import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;


public class UserWindow extends JFrame {

	private JPanel contentPane;
	private JTable tlbUsers;
	private JButton btnDelete;

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
		
		JLabel lblNoUsers = new JLabel("<html><center>No users present!<br>Select \"New User...\" to create a new user.</center></html>");
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
		
		JButton btnSignIn = new JButton("Sign In...");
		btnSignIn.setBounds(346, 50, 131, 23);
		contentPane.add(btnSignIn);
		
		JButton btnNew = new JButton("New User...");
		btnNew.setBounds(346, 162, 131, 23);
		contentPane.add(btnNew);
		
		btnDelete = new JButton("Delete User");
		btnDelete.setBounds(346, 199, 131, 23);
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
		
		//Gets the users for the object.
		Vector<Vector<String>> users = ProgramController.getDatabaseUsers();
		
		//Sees how many users are present.
		if (users.size() <= 1){
			lblNoUsers.setVisible(true);
		}
		
		//Finally, populates the JTable with the users.
		DefaultTableModel modelUsers = (DefaultTableModel) tlbUsers.getModel();
		for (int i = 1; i < users.size(); i++){
			Vector<String> currentUser = users.elementAt(i);
			modelUsers.addRow(new Object[]{currentUser.elementAt(0), 
										   currentUser.elementAt(1) + " " + currentUser.elementAt(2), 
										   currentUser.elementAt(3)});
		}
	}
}
