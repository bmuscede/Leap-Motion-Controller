import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Window.Type;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;


public class StatusBoxWindow extends JDialog {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public StatusBoxWindow(String alertMessage) {
		setResizable(false);
		setModal(true);
		setLocationRelativeTo(null);
		setTitle("Leap Motion Tracker - [Notice]");
		setType(Type.POPUP);
		setAlwaysOnTop(true);
		setBounds(100, 100, 450, 116);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNotice = new JLabel(alertMessage);
		lblNotice.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNotice.setHorizontalAlignment(SwingConstants.CENTER);
		lblNotice.setBounds(10, 11, 424, 64);
		contentPane.add(lblNotice);
	}
}
