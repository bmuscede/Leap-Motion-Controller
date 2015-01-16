import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MetricSettingsWindow extends JDialog implements ItemListener, ActionListener {
	private static final long serialVersionUID = 3522834592795748276L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtConfidence;
	private JTextField txtWindowSize;
	private JLabel lblWindowSize;
	private JCheckBox chkPerform;
	private JCheckBox chkDelete;
	private JLabel lblConfidence;
	private JLabel lblPercent;
	private JButton okButton;
	private JButton cancelButton;
	private JCheckBox chkObserve;
	private JCheckBox chkMagnitude;
	private JLabel lblCompareEvery;
	private JLabel lblFrames;
	private JLabel lblSensitivity;
	
	private String userName;
	private String session;
	private JTextField txtFrameOrder;
	private JTextField txtSensitivity;
	
	/**
	 * Create the dialog.
	 */
	public MetricSettingsWindow(String userName, String session) {
		//Saves to a global variable.
		this.userName = userName;
		this.session = session;
	
		setTitle("Leap Motion Tracker - [Metric Settings]");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblMetricSettings = new JLabel("Metric Settings:");
		lblMetricSettings.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMetricSettings.setBounds(10, 11, 238, 28);
		contentPanel.add(lblMetricSettings);
		
		JPanel pnlPreprocessing = new JPanel();
		pnlPreprocessing.setBorder(new TitledBorder(null, "Preprocessing Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlPreprocessing.setBounds(10, 50, 414, 75);
		contentPanel.add(pnlPreprocessing);
		pnlPreprocessing.setLayout(null);
		
		chkPerform = new JCheckBox("Perform Data Smoothening");
		chkPerform.addItemListener(this);
		chkPerform.setBounds(6, 19, 182, 23);
		pnlPreprocessing.add(chkPerform);
		
		lblWindowSize = new JLabel("Window Size:");
		lblWindowSize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWindowSize.setVisible(false);
		lblWindowSize.setBounds(194, 23, 128, 14);
		pnlPreprocessing.add(lblWindowSize);
		
		txtWindowSize = new JTextField();
		txtWindowSize.setColumns(10);
		txtWindowSize.setVisible(false);
		txtWindowSize.setBounds(327, 20, 66, 20);
		pnlPreprocessing.add(txtWindowSize);
		
		chkDelete = new JCheckBox("Delete Bad Frames");
		chkDelete.addItemListener(this);
		chkDelete.setBounds(6, 45, 182, 23);
		pnlPreprocessing.add(chkDelete);
		
		lblConfidence = new JLabel("Delete Frames Below:");
		lblConfidence.setHorizontalAlignment(SwingConstants.RIGHT);
		lblConfidence.setBounds(167, 49, 155, 14);
		lblConfidence.setVisible(false);
		pnlPreprocessing.add(lblConfidence);
		
		txtConfidence = new JTextField();
		txtConfidence.setBounds(327, 46, 51, 20);
		pnlPreprocessing.add(txtConfidence);
		txtConfidence.setVisible(false);
		txtConfidence.setColumns(10);
		
		lblPercent = new JLabel("%");
		lblPercent.setBounds(380, 49, 13, 14);
		lblPercent.setVisible(false);
		pnlPreprocessing.add(lblPercent);
		
		JPanel pnlProcessing = new JPanel();
		pnlProcessing.setLayout(null);
		pnlProcessing.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Processing Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlProcessing.setBounds(10, 136, 414, 81);
		contentPanel.add(pnlProcessing);
		
		chkObserve = new JCheckBox("Analyze Select Frames");
		chkObserve.setBounds(6, 19, 193, 23);
		chkObserve.addItemListener(this);
		pnlProcessing.add(chkObserve);
		
		lblCompareEvery = new JLabel("Compare Every");
		lblCompareEvery.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCompareEvery.setBounds(205, 23, 91, 14);
		lblCompareEvery.setVisible(false);
		pnlProcessing.add(lblCompareEvery);
		
		txtFrameOrder = new JTextField();
		txtFrameOrder.setBounds(306, 20, 46, 20);
		pnlProcessing.add(txtFrameOrder);
		txtFrameOrder.setVisible(false);
		txtFrameOrder.setColumns(10);
		
		lblFrames = new JLabel("Frames");
		lblFrames.setBounds(358, 23, 46, 14);
		lblFrames.setVisible(false);
		pnlProcessing.add(lblFrames);
		
		chkMagnitude = new JCheckBox("Set Movement Sensitivity");
		chkMagnitude.setBounds(6, 45, 193, 23);
		chkMagnitude.addItemListener(this);
		pnlProcessing.add(chkMagnitude);
		
		lblSensitivity = new JLabel("Sensitivity:");
		lblSensitivity.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSensitivity.setBounds(205, 49, 91, 14);
		lblSensitivity.setVisible(false);
		pnlProcessing.add(lblSensitivity);
		
		txtSensitivity = new JTextField();
		txtSensitivity.setBounds(306, 46, 98, 20);
		pnlProcessing.add(txtSensitivity);
		txtSensitivity.setVisible(false);
		txtSensitivity.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Compute Metrics...");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				okButton.addActionListener(this);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(this);
				buttonPane.add(cancelButton);
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		//Gets the source.
		Object source = e.getSource();
		
		//Now we see what got checked/or unchecked.
		if (source == chkPerform){
			if (chkPerform.isSelected()){
				//We show the options for it.
				lblWindowSize.setVisible(true);
				txtWindowSize.setVisible(true);
			} else {
				//We hide the options.
				lblWindowSize.setVisible(false);
				txtWindowSize.setVisible(false);
			}
		} else if (source == chkDelete){
			if (chkDelete.isSelected()){
				//We show the options for it.
				lblConfidence.setVisible(true);
				txtConfidence.setVisible(true);
				lblPercent.setVisible(true);
			} else {
				//We hide the options for it.
				lblConfidence.setVisible(false);
				txtConfidence.setVisible(false);
				lblPercent.setVisible(false);
			}
		} else if (source == chkObserve){
			//We show the options for it.
			if (chkObserve.isSelected()){
				lblCompareEvery.setVisible(true);
				lblFrames.setVisible(true);
				txtFrameOrder.setVisible(true);
			} else {
				lblCompareEvery.setVisible(false);
				lblFrames.setVisible(false);
				txtFrameOrder.setVisible(false);
			}
		} else if (source == chkMagnitude){
			//We hide the options for it.
			if (chkMagnitude.isSelected()){
				lblSensitivity.setVisible(true);
				txtSensitivity.setVisible(true);
			} else {
				lblSensitivity.setVisible(false);
				txtSensitivity.setVisible(false);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//Get the source.
		Object source = arg0.getSource();
		
		if (source == cancelButton){
			//Destroys the window.
			this.dispose(); 
		} else if (source == okButton){
			okHandler();
		}
	}
	
	private void okHandler(){
		int smootheningWindowSize = 0;
		int confidenceLevel = 0;
		int frameOrder = 0;
		int sensitivityValue = 0;
		
		//We first look at the smoothening.
		boolean smoothening = chkPerform.isSelected();

		if (smoothening){
			//We need to get the window size.
			try{
				smootheningWindowSize = Integer.parseInt(txtWindowSize.getText());
				
				//Check if number is odd.
				if (smootheningWindowSize % 2 == 0 ||
						smootheningWindowSize < 1) throw new NumberFormatException();
			} catch (NumberFormatException e) {
				//There was an error with the value.
				ProgramController.createDialog("<html>The window size must be a valid odd integer!" +
						"<br>Please correct this value.</html>", 
						"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//Next we look at confidence percentage.
		boolean confidence = chkDelete.isSelected();
		
		if (confidence){
			//We need to get the confidence level.
			try{
				confidenceLevel = Integer.parseInt(txtConfidence.getText());
				
				//Check if number is valid.
				if (confidenceLevel < 1 || confidenceLevel > 100) throw new NumberFormatException();
			} catch (NumberFormatException e){
				//There was an error with the value.
				ProgramController.createDialog("<html>The confidence level must be a number between 1 and 100!" +
						"<br>Please correct this value.</html>", 
						"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//Next, we look at the frame observe value.
		boolean observe = chkObserve.isSelected();
		
		if (observe){
			//We need to get the frame pattern.
			try{
				frameOrder = Integer.parseInt(txtFrameOrder.getText());
				
				//Check if number is valid.
				if (frameOrder < 1) throw new NumberFormatException();
			} catch (NumberFormatException e){
				//There was an error with the value.
				ProgramController.createDialog("<html>The frame order must be a number greater than 0!" +
						"<br>Please correct this value.</html>", 
						"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//Next, we look at the sensitivity value.
		boolean sensitivity = chkObserve.isSelected();
		 
		if (sensitivity){
			//We need to get the frame pattern.
			try{
				sensitivityValue = Integer.parseInt(txtSensitivity.getText());
				
				//Check if number is valid.
				if (sensitivityValue < 1) throw new NumberFormatException();
			} catch (NumberFormatException e){
				//There was an error with the value.
				ProgramController.createDialog("<html>The sensitivity value must be a number greater than 0!" +
						"<br>Please correct this value.</html>", 
						"Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//Finally, we send all data to the ProgramController.
		ProgramController.computeMetrics(userName, session, smoothening, confidence, observe, sensitivity,
				smootheningWindowSize, confidenceLevel, frameOrder, sensitivityValue);
	}
}
