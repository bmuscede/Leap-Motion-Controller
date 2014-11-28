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
import javax.swing.ImageIcon;


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
		setBounds(100, 100, 255, 116);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNotice = new JLabel(alertMessage);
		lblNotice.setHorizontalAlignment(SwingConstants.CENTER);
		lblNotice.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblNotice.setBounds(10, 39, 228, 39);
		contentPane.add(lblNotice);
		
		JLabel label = new JLabel("");
		label.setIcon(new ImageIcon(StatusBoxWindow.class.getResource("/com/sun/java/swing/plaf/windows/icons/Inform.gif")));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Tahoma", Font.PLAIN, 16));
		label.setBounds(10, 2, 228, 39);
		contentPane.add(label);
	}
}
