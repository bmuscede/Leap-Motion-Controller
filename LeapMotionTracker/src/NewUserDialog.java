import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingConstants;
import java.awt.Color;


public class NewUserDialog extends JDialog implements ActionListener {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtUserName;
	private JTextField txtFirstName;
	private JTextField txtLastName;
	private JLabel lblError;
	
	private boolean dataReady;
	
	/**
	 * Create the dialog.
	 */
	public NewUserDialog() {
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setModal(true);
		setTitle("Create New User...");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 403, 227);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblUserName = new JLabel("User Name:");
		lblUserName.setBounds(10, 29, 97, 17);
		contentPanel.add(lblUserName);
		lblUserName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		txtUserName = new JTextField();
		txtUserName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtUserName.setBounds(117, 24, 260, 28);
		contentPanel.add(txtUserName);
		txtUserName.setColumns(10);
		
		JLabel lblFirstName = new JLabel("First Name:");
		lblFirstName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblFirstName.setBounds(10, 69, 97, 14);
		contentPanel.add(lblFirstName);
		
		txtFirstName = new JTextField();
		txtFirstName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtFirstName.setColumns(10);
		txtFirstName.setBounds(117, 61, 260, 28);
		contentPanel.add(txtFirstName);
		
		JLabel lblLastName = new JLabel("Last Name:");
		lblLastName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLastName.setBounds(10, 108, 97, 14);
		contentPanel.add(lblLastName);
		
		txtLastName = new JTextField();
		txtLastName.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLastName.setColumns(10);
		txtLastName.setBounds(117, 100, 260, 28);
		contentPanel.add(txtLastName);
		
		lblError = new JLabel("<ERROR MSG HERE>");
		lblError.setForeground(Color.RED);
		lblError.setHorizontalAlignment(SwingConstants.CENTER);
		lblError.setVisible(false);
		lblError.setBounds(10, 133, 367, 14);
		contentPanel.add(lblError);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(this);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
		
		//Tells the calling method that data cannot be collected.
		dataReady = false;
	}

	public boolean returnValue() {
		return dataReady;
	}

	public String getUserName() {
		if (!dataReady) return null;
		
		return txtUserName.getText();
	}
	
	public String getFirstName(){
		if (!dataReady) return null;
		
		return txtFirstName.getText();
	}
	
	public String getLastName(){
		if (!dataReady) return null;
		
		return txtLastName.getText();
	}

	public void actionPerformed(ActionEvent event) {
		if ("OK".equals(event.getActionCommand())){
			//We need to validate that everything entered is OK!
			if (validateForm()){
				dataReady = true;
				this.setVisible(false);
			}
		} else if ("Cancel".equals(event.getActionCommand())){
			//We simply close the window.
			this.setVisible(false);
		}
	}

	private boolean validateForm() {
		if (txtUserName.getText().length() == 0 || txtUserName.getText().length() > 15){
			lblError.setText("Error: The username must be between 1 and 15 characters.");
			lblError.setVisible(true);
			return false;
		} else if (txtFirstName.getText().length() == 0 || txtUserName.getText().length() > 15){
			lblError.setText("Error: The first name must be between 1 and 15 characters.");
			lblError.setVisible(true);
			return false;
		} else if (txtLastName.getText().length() == 0 || txtUserName.getText().length() > 30){
			lblError.setText("Error: The last name must be between 1 and 30 characters.");
			lblError.setVisible(true);
			return false;
		}
		
		return true;
	}
}
