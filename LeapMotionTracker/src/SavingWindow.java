import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dialog.ModalExclusionType;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import java.awt.Font;


public class SavingWindow extends JFrame {
	private static final long serialVersionUID = 1472356562129423186L;
	
	private JPanel contentPane;
	int highEnd;
	private JProgressBar prgSaving;
	private JLabel lblFrames;
	
	/**
	 * Create the frame.
	 */
	public SavingWindow(int lowEnd, int highEnd) {
		this.highEnd = highEnd;
		setResizable(false);
		setTitle("Leap Motion Controller - [Please Wait...]");
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 140);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblSaving = new JLabel("Saving Motion Data");
		lblSaving.setHorizontalAlignment(SwingConstants.CENTER);
		lblSaving.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblSaving.setBounds(10, 11, 474, 28);
		contentPane.add(lblSaving);
		
		lblFrames = new JLabel(lowEnd + " / " + highEnd + " Frames Written");
		lblFrames.setHorizontalAlignment(SwingConstants.CENTER);
		lblFrames.setBounds(10, 92, 474, 14);
		contentPane.add(lblFrames);
		
		prgSaving = new JProgressBar();
		prgSaving.setMinimum(lowEnd);
		prgSaving.setMaximum(highEnd);
		prgSaving.setBounds(10, 50, 474, 38);
		contentPane.add(prgSaving);
	}
	
	public void updateValue(int newLow){
		lblFrames.setText(newLow + " / " + highEnd + " Frames Written");
		prgSaving.setValue(newLow);
	}
}
