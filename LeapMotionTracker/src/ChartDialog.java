import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;


public class ChartDialog extends JDialog {
	private final String[] chartTypes = {"Number of Movements",
										 "Average Velocity",
										 "Movement Over Time",
										 "Average Velocity Over Time"};
	private final String[] descriptions = {"Shows the number of movements that each hand, finger, and palm made" +
			"overall. Important in being able to see an overall difference between different digits.",
			"Shows the average velocity of each hand, finger, and palm throughout the session. Important in being" +
			"able to see digits where velocity differed.",
			"Line chart that shows how many movements were made per second thoughout the entire session. This can" +
			" higlight areas of improvement.",
			"Line chart that shows the average velocity throughout the entire session. This can highlight areas of " +
			"improvement."};
	
	private final JPanel contentPanel = new JPanel();
	private JPanel pnlDescription;
	private JLabel lblDescription;
	private JCheckBox chckbxLeftHand;
	private JCheckBox chkbxAverage;
	private JCheckBox chckbxRightHand;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ChartDialog dialog = new ChartDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ChartDialog() {
		setBounds(100, 100, 274, 358);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblChartType = new JLabel("Chart Type:");
			lblChartType.setBounds(18, 13, 58, 14);
			contentPanel.add(lblChartType);
		}
		{
			JComboBox cmbChartTypes = new JComboBox();
			cmbChartTypes.setBounds(81, 10, 158, 20);
			cmbChartTypes.setModel(new DefaultComboBoxModel(chartTypes));
			contentPanel.add(cmbChartTypes);
		}
		
		pnlDescription = new JPanel();
		pnlDescription.setBorder(new TitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlDescription.setBounds(10, 38, 238, 77);
		contentPanel.add(pnlDescription);
		pnlDescription.setLayout(null);
		
		lblDescription = new JLabel("< Description Goes Here >");
		lblDescription.setHorizontalAlignment(SwingConstants.CENTER);
		lblDescription.setBounds(10, 21, 218, 45);
		pnlDescription.add(lblDescription);
		{
			JPanel panel_1 = new JPanel();
			panel_1.setLayout(null);
			panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Data To Include", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel_1.setBounds(10, 126, 238, 156);
			contentPanel.add(panel_1);
			
			chckbxLeftHand = new JCheckBox("Left Hand");
			chckbxLeftHand.setSelected(true);
			chckbxLeftHand.setBounds(6, 50, 97, 23);
			panel_1.add(chckbxLeftHand);
			
			chckbxRightHand = new JCheckBox("Right Hand");
			chckbxRightHand.setSelected(true);
			chckbxRightHand.setBounds(6, 71, 97, 23);
			panel_1.add(chckbxRightHand);
			
			chkbxAverage = new JCheckBox("Average");
			chkbxAverage.setSelected(true);
			chkbxAverage.setBounds(6, 91, 97, 23);
			panel_1.add(chkbxAverage);
			
			JCheckBox chckbxPalm = new JCheckBox("Palm");
			chckbxPalm.setSelected(true);
			chckbxPalm.setBounds(135, 20, 97, 23);
			panel_1.add(chckbxPalm);
			
			JCheckBox chckbxIndexFinger = new JCheckBox("Index Finger");
			chckbxIndexFinger.setSelected(true);
			chckbxIndexFinger.setBounds(135, 41, 97, 23);
			panel_1.add(chckbxIndexFinger);
			
			JCheckBox chckbxMiddleFinger = new JCheckBox("Middle Finger");
			chckbxMiddleFinger.setSelected(true);
			chckbxMiddleFinger.setBounds(135, 61, 97, 23);
			panel_1.add(chckbxMiddleFinger);
			
			JCheckBox chckbxThumb = new JCheckBox("Thumb");
			chckbxThumb.setSelected(true);
			chckbxThumb.setBounds(135, 124, 97, 23);
			panel_1.add(chckbxThumb);
			
			JCheckBox chckbxPinkyFinger = new JCheckBox("Pinky Finger");
			chckbxPinkyFinger.setSelected(true);
			chckbxPinkyFinger.setBounds(135, 104, 97, 23);
			panel_1.add(chckbxPinkyFinger);
			
			JCheckBox chckbxRingFinger = new JCheckBox("Ring Finger");
			chckbxRingFinger.setSelected(true);
			chckbxRingFinger.setBounds(135, 83, 97, 23);
			panel_1.add(chckbxRingFinger);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
