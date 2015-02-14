import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Window.Type;


public class ChartDialog extends JDialog {
	private final String[] chartTypes = {"Number of Movements",
										 "Average Velocity",
										 "Movement Over Time",
										 "Average Velocity Over Time"};
	private final String[] descriptions = {"<html>Shows the number of movements that each hand, finger, and palm made " +
			"overall. Important in being able to see an overall difference between different digits.</html>",
			"<html>Shows the average velocity of each hand, finger, and palm throughout the session. Important in being" +
			"able to see digits where velocity differed.</html>",
			"<html>Line chart that shows how many movements were made per second thoughout the entire session. This can" +
			" higlight areas of improvement.</html>",
			"<html>Line chart that shows the average velocity throughout the entire session. This can highlight areas of " +
			"improvement.</html>"};
	
	private final JPanel contentPanel = new JPanel();
	private JPanel pnlDescription;
	private JLabel lblDescription;
	private JCheckBox chckbxLeftHand;
	private JCheckBox chkbxAverage;
	private JCheckBox chckbxRightHand;
	private JCheckBox chckbxIndexFinger;
	private JCheckBox chckbxMiddleFinger;
	private JCheckBox chckbxRingFinger;
	private JCheckBox chckbxPinkyFinger;
	private JCheckBox chckbxThumb;
	private JCheckBox chckbxPalm;
	
	private final JComboBox<String> cmbChartTypes;
	
	private int retCode;
	
	
	public int getReturnCode(){
		return retCode;
	}
	public int getSelectedChart(){
		if (retCode != 1)
				return -1;
		
		return cmbChartTypes.getSelectedIndex();
	}
	public boolean[] getSelectedValues(){
		if (retCode != 1)
			return null;
			
		boolean[] values = new boolean[9];
		values[0] = chckbxLeftHand.isSelected();
		values[1] = chckbxRightHand.isSelected();		
		values[2] = chkbxAverage.isSelected();
		values[3] = chckbxThumb.isSelected();
		values[4] = chckbxIndexFinger.isSelected();
		values[5] = chckbxMiddleFinger.isSelected();
		values[6] = chckbxRingFinger.isSelected();
		values[7] = chckbxPinkyFinger.isSelected();
		values[8] = chckbxPalm.isSelected();
	    
		return values;
	}
	
	/**
	 * Create the dialog.
	 */
	public ChartDialog() {
		retCode = 0;
		setResizable(false);
		setType(Type.POPUP);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Chart Options...");
		setBounds(100, 100, 274, 366);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblChartType = new JLabel("Type:");
			lblChartType.setBounds(18, 13, 58, 14);
			contentPanel.add(lblChartType);
		}
		{
			cmbChartTypes = new JComboBox<String>();
			cmbChartTypes.setBounds(52, 10, 187, 20);
			cmbChartTypes.setModel(new DefaultComboBoxModel<String>(chartTypes));
			cmbChartTypes.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        //We need to change the description.
			    	lblDescription.setText(descriptions[cmbChartTypes.getSelectedIndex()]);
			    }
			});
			contentPanel.add(cmbChartTypes);
		}
		
		pnlDescription = new JPanel();
		pnlDescription.setBorder(new TitledBorder(null, "Description", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlDescription.setBounds(10, 38, 248, 108);
		contentPanel.add(pnlDescription);
		pnlDescription.setLayout(null);
		
		lblDescription = new JLabel("< Description Goes Here >");
		lblDescription.setHorizontalAlignment(SwingConstants.CENTER);
		lblDescription.setBounds(10, 21, 228, 87);
		pnlDescription.add(lblDescription);
		{
			JPanel pnlData = new JPanel();
			pnlData.setLayout(null);
			pnlData.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Data To Include", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			pnlData.setBounds(11, 149, 247, 148);
			contentPanel.add(pnlData);
			
			chckbxLeftHand = new JCheckBox("Left Hand");
			chckbxLeftHand.setSelected(true);
			chckbxLeftHand.setBounds(6, 50, 97, 23);
			pnlData.add(chckbxLeftHand);
			
			chckbxRightHand = new JCheckBox("Right Hand");
			chckbxRightHand.setSelected(true);
			chckbxRightHand.setBounds(6, 71, 97, 23);
			pnlData.add(chckbxRightHand);
			
			chkbxAverage = new JCheckBox("Average");
			chkbxAverage.setSelected(true);
			chkbxAverage.setBounds(6, 91, 97, 23);
			pnlData.add(chkbxAverage);
			
			chckbxThumb = new JCheckBox("Thumb");
			chckbxThumb.setSelected(true);
			chckbxThumb.setBounds(122, 16, 109, 23);
			pnlData.add(chckbxThumb);
			
			chckbxIndexFinger = new JCheckBox("Index Finger");
			chckbxIndexFinger.setSelected(true);
			chckbxIndexFinger.setBounds(122, 35, 109, 23);
			pnlData.add(chckbxIndexFinger);
			
			chckbxMiddleFinger = new JCheckBox("Middle Finger");
			chckbxMiddleFinger.setSelected(true);
			chckbxMiddleFinger.setBounds(122, 55, 109, 23);
			pnlData.add(chckbxMiddleFinger);
			
			chckbxRingFinger = new JCheckBox("Ring Finger");
			chckbxRingFinger.setSelected(true);
			chckbxRingFinger.setBounds(122, 77, 109, 23);
			pnlData.add(chckbxRingFinger);
			
			chckbxPinkyFinger = new JCheckBox("Pinky Finger");
			chckbxPinkyFinger.setSelected(true);
			chckbxPinkyFinger.setBounds(122, 98, 109, 23);
			pnlData.add(chckbxPinkyFinger);
			
			chckbxPalm = new JCheckBox("Palm");
			chckbxPalm.setSelected(true);
			chckbxPalm.setBounds(122, 119, 109, 23);
			pnlData.add(chckbxPalm);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//OK was selected.
						retCode = 1;
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//Cancel was selected.
						retCode = 0;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		//Gets the selected combobox.
		lblDescription.setText(descriptions[cmbChartTypes.getSelectedIndex()]);
	}
	
	public void setValues(int defaultChartType, boolean[] defaultValues) {
		//Sets the chart type.
		cmbChartTypes.setSelectedIndex(defaultChartType);
	
		//Now, sets the checkboxes.
		chckbxLeftHand.setSelected(defaultValues[0]);
		chckbxRightHand.setSelected(defaultValues[1]);
		chkbxAverage.setSelected(defaultValues[2]);
		chckbxThumb.setSelected(defaultValues[3]);
		chckbxIndexFinger.setSelected(defaultValues[4]);
		chckbxMiddleFinger.setSelected(defaultValues[5]);
		chckbxRingFinger.setSelected(defaultValues[6]);
		chckbxPinkyFinger.setSelected(defaultValues[7]);
		chckbxPalm.setSelected(defaultValues[8]);
	}
	
}
