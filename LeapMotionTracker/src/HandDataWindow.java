import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;

public class HandDataWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	public JLabel lblErrorSymbol;
	public JLabel lblHandNotPresent;
	public JLabel lblData;
	public JLabel lblConfidence;
	public JSeparator sepConfidenceSep;
	
	/**
	 * Create the frame.
	 */
	public HandDataWindow(boolean is_left) {
		setResizable(false);
		
		JLabel lblHand = new JLabel("<HAND TITLE GOES HERE>");
		
		if (is_left){
			//Sets the title.
			setTitle("Leap Motion Tracker - [Left Hand]");
			
			//Changes label.
			lblHand.setText("Left Hand:");
		} else {
			//Sets the title.
			setTitle("Leap Motion Tracker - [Right Hand]");
			
			//Changes label.
			lblHand.setText("Right Hand:");
		}
		
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 318, 270);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblHand.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblHand.setBounds(10, 11, 292, 26);
		contentPane.add(lblHand);
		
		lblErrorSymbol = new JLabel("");
		lblErrorSymbol.setHorizontalAlignment(SwingConstants.CENTER);
		lblErrorSymbol.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		lblErrorSymbol.setBounds(10, 87, 292, 32);
		contentPane.add(lblErrorSymbol);
		
		lblHandNotPresent = new JLabel("Hand Not Present");
		lblHandNotPresent.setFont(new Font("Tahoma", Font.ITALIC, 16));
		lblHandNotPresent.setHorizontalAlignment(SwingConstants.CENTER);
		lblHandNotPresent.setBounds(10, 49, 292, 181);
		contentPane.add(lblHandNotPresent);
		
		lblData = new JLabel("<MOTION_DATA>");
		lblData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblData.setVerticalAlignment(SwingConstants.TOP);
		lblData.setBounds(10, 48, 292, 129);
		lblData.setVisible(false);
		contentPane.add(lblData);
		
		sepConfidenceSep = new JSeparator();
		sepConfidenceSep.setBounds(10, 185, 292, 2);
		sepConfidenceSep.setVisible(false);
		contentPane.add(sepConfidenceSep);
		
		lblConfidence = new JLabel("Confidence: <VAL>%");
		lblConfidence.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblConfidence.setHorizontalAlignment(SwingConstants.CENTER);
		lblConfidence.setBounds(10, 198, 292, 32);
		lblConfidence.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
		lblConfidence.setVisible(false);
		contentPane.add(lblConfidence);
	}
	
	public void updateConfidence(int confidenceVal){
		//First, select icon for confidence.
		if (confidenceVal > 67){
			lblConfidence.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
		} else if (confidenceVal > 33){
			lblConfidence.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		} else {
			lblConfidence.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
		}
		
		//Changes the colour value.
		lblConfidence.setForeground(getColourValue(confidenceVal));
		
		//Finally, updates the confidence.
		lblConfidence.setText("Confidence: " + confidenceVal + "%");
		}

	/**
	 * Returns a colour between green and red based on
	 * an integer between 0 and 100.
	 * @param value The value of the integer.
	 * @return The colour value.
	 */
	private Color getColourValue(int value) {
		//Converts the value into a number between 0 and 1.
		double convertedValue = (double) value / 100;
		
		//Converts value into colours.
	    return Color.getHSBColor((float) (convertedValue * 0.4), (float) 0.9, (float) 0.9);
	}
}
