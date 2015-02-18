import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Window.Type;
import net.miginfocom.swing.MigLayout;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.HorizontalAlignment;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileFilter;
import java.io.IOException;

public class MetricsStatusWindow extends JFrame {
	private JPanel contentPane;
	private JPanel pnlLoading;
	private JTabbedPane tabbedPane;
	private JLabel lblLeftHandData;
	private JLabel lblLeftMove;
	private JLabel lblLeftVelocity;
	private JLabel lblLeftMoveData;
	private JLabel lblLeftVelocityData;
	private JLabel lblLeftFingerZero;
	private JLabel lblLeftFingerTwo;
	private JLabel lblLeftFingerThree;
	private JLabel lblLeftFingerFour;
	private JLabel lblLeftIndividualFingers;
	private JLabel lblLeftFingerOne;
	private JLabel lblLeftMovementsZero;
	private JLabel lblLeftVelocityZero;
	private JLabel lblLeftMovementsOne;
	private JLabel lblLeftVelocityOne;
	private JLabel lblLeftMovementsTwo;
	private JLabel lblLeftVelocityTwo;
	private JLabel lblLeftMovementsThree;
	private JLabel lblLeftVelocityThree;
	private JLabel lblLeftMovementsFour;
	private JLabel lblLeftVelocityFour;
	private JLabel lblRightHandData;
	private JLabel lblRightMove;
	private JLabel lblRightVelocity;
	private JSeparator separator_1;
	private JLabel lblRightIndividualFingers;
	private JLabel lblRightFingerZero;
	private JLabel lblRightMovementsZero;
	private JLabel lblRightVelocityZero;
	private JLabel lblRightFingerOne;
	private JLabel lblRightMovementsOne;
	private JLabel lblRightVelocityOne;
	private JLabel lblRightFingerTwo;
	private JLabel lblRightMovementsTwo;
	private JLabel lblRightVelocityTwo;
	private JLabel lblRightFingerThree;
	private JLabel lblRightMovementsThree;
	private JLabel lblRightVelocityThree;
	private JLabel lblRightFingerFour;
	private JLabel lblRightMovementsFour;
	private JLabel lblRightVelocityFour;
	private JLabel lblRightMoveData;
	private JLabel lblRightVelocityData;
	private ChartPanel pnlChart;
	private JFreeChart freeChart;
	private JButton btnModifyChart;
	private JButton btnSaveChart;
	private ChartDialog chartSetting;
	
	//User Variables.
	private String userName;
	private String session;
	
	//Calculator Variables
	private MetricsCalculator calculator;
	private Timer calcTimer;
	
	//Finger Types
	private final String[] FINGERS = {"Thumb", 
			                          "Index Finger",
			                          "Middle Finger",
			                          "Ring Finger",
			                          "Pinky Finger",
			                          "Palm"};
	private JLabel[] LEFT_INFO_GROUP;
	private JLabel[] LEFT_DATA_MOVEMENT_GROUP;
	private JLabel[] LEFT_DATA_VELOCITY_GROUP;
	private JLabel[] RIGHT_INFO_GROUP;
	private JLabel[] RIGHT_DATA_MOVEMENT_GROUP;
	private JLabel[] RIGHT_DATA_VELOCITY_GROUP;
	
	//Chart Variables
	public enum ChartType {
		MOTION_BAR, VEL_BAR, MOTION_LNE, VEL_LNE
	}
	private final String[] BAR_CATEGORIES = {
			"Left Hand",
			"Right Hand",
			"Average"
	};
	private boolean[] defaultValues;
	private int defaultChartType;
	
	//Stores intermediate data.
	private float[] leftMotions;
	private float[] rightMotions;
	private float[] leftVelocity;
	private float[] rightVelocity;
	private JPanel pnlScore;
	private JLabel lblReported;
	private JSeparator separator_2;
	private JLabel lblMetrics;
	
	/**
	 * Create the frame.
	 */
	public MetricsStatusWindow(String userName, String session) {
		defaultChartType = 0;
		defaultValues = new boolean[9];
		for (int i = 0; i < defaultValues.length; i++){
			defaultValues[i] = true;
		}
		
		setResizable(false);
		setType(Type.UTILITY);
		setTitle("Leap Motion Tracker - [" + userName + " - Session " + session +"]");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 444, 271);
		contentPane.add(tabbedPane);
		
		pnlScore = new JPanel();
		tabbedPane.addTab("Score\r\n", null, pnlScore, null);
		pnlScore.setLayout(null);
		
		lblReported = new JLabel("<html><strong>Reported Skill Level:</strong>");
		lblReported.setBounds(10, 11, 419, 14);
		pnlScore.add(lblReported);
		
		separator_2 = new JSeparator();
		separator_2.setBounds(10, 36, 419, 2);
		pnlScore.add(separator_2);
		
		lblMetrics = new JLabel("<html><center>Your metrics are [NUM]% [better/worse] than your skill level's average.</center></html>");
		lblMetrics.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblMetrics.setHorizontalAlignment(SwingConstants.CENTER);
		lblMetrics.setBounds(10, 79, 419, 40);
		pnlScore.add(lblMetrics);
		
		JPanel pnlLeft = new JPanel();
		tabbedPane.addTab("Left Hand Data", null, pnlLeft, null);
		pnlLeft.setLayout(null);
		
		lblLeftHandData = new JLabel("Left Hand Data:");
		lblLeftHandData.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblLeftHandData.setBounds(10, 11, 419, 31);
		pnlLeft.add(lblLeftHandData);
		
		lblLeftMove = new JLabel("<html>Number of <b>Overall</b> Hand Movements:</html>");
		lblLeftMove.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLeftMove.setBounds(20, 53, 239, 22);
		pnlLeft.add(lblLeftMove);
		
		lblLeftMoveData = new JLabel("?");
		lblLeftMoveData.setToolTipText("");
		lblLeftMoveData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLeftMoveData.setBounds(269, 53, 160, 22);
		pnlLeft.add(lblLeftMoveData);
		
		lblLeftVelocity = new JLabel("<html><b>Overall</b> Average Hand Motion Velocity:</html>");
		lblLeftVelocity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLeftVelocity.setBounds(20, 80, 239, 22);
		pnlLeft.add(lblLeftVelocity);
		
		lblLeftVelocityData = new JLabel("?");
		lblLeftVelocityData.setToolTipText("");
		lblLeftVelocityData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLeftVelocityData.setBounds(269, 80, 160, 22);
		pnlLeft.add(lblLeftVelocityData);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 115, 419, 2);
		pnlLeft.add(separator);
		
		lblLeftIndividualFingers = new JLabel("Individual Fingers:");
		lblLeftIndividualFingers.setVerticalAlignment(SwingConstants.TOP);
		lblLeftIndividualFingers.setHorizontalAlignment(SwingConstants.LEFT);
		lblLeftIndividualFingers.setFont(new Font("Tahoma", Font.ITALIC, 15));
		lblLeftIndividualFingers.setBounds(10, 122, 419, 22);
		pnlLeft.add(lblLeftIndividualFingers);
		
		lblLeftFingerZero = new JLabel("Finger 0:");
		lblLeftFingerZero.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLeftFingerZero.setBounds(10, 142, 79, 14);
		pnlLeft.add(lblLeftFingerZero);
		
		lblLeftMovementsZero = new JLabel("Movement: ?");
		lblLeftMovementsZero.setBounds(10, 160, 140, 14);
		pnlLeft.add(lblLeftMovementsZero);
		
		lblLeftVelocityZero = new JLabel("Velocity: ?");
		lblLeftVelocityZero.setBounds(10, 177, 140, 14);
		pnlLeft.add(lblLeftVelocityZero);
		
		lblLeftFingerOne = new JLabel("Finger 1:");
		lblLeftFingerOne.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLeftFingerOne.setBounds(10, 194, 79, 14);
		pnlLeft.add(lblLeftFingerOne);
		
		lblLeftMovementsOne = new JLabel("Movement: ?");
		lblLeftMovementsOne.setBounds(10, 209, 139, 14);
		pnlLeft.add(lblLeftMovementsOne);
		
		lblLeftVelocityOne = new JLabel("Velocity: ?");
		lblLeftVelocityOne.setBounds(10, 225, 140, 14);
		pnlLeft.add(lblLeftVelocityOne);
		
		lblLeftFingerTwo = new JLabel("Finger 2:");
		lblLeftFingerTwo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLeftFingerTwo.setBounds(149, 142, 79, 14);
		pnlLeft.add(lblLeftFingerTwo);
		
		lblLeftMovementsTwo = new JLabel("Movement: ?");
		lblLeftMovementsTwo.setBounds(149, 160, 120, 14);
		pnlLeft.add(lblLeftMovementsTwo);
		
		lblLeftVelocityTwo = new JLabel("Velocity: ?");
		lblLeftVelocityTwo.setBounds(149, 177, 120, 14);
		pnlLeft.add(lblLeftVelocityTwo);
		
		lblLeftFingerThree = new JLabel("Finger 3:");
		lblLeftFingerThree.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLeftFingerThree.setBounds(149, 194, 79, 14);
		pnlLeft.add(lblLeftFingerThree);
		
		lblLeftMovementsThree = new JLabel("Movement: ?");
		lblLeftMovementsThree.setBounds(149, 208, 120, 14);
		pnlLeft.add(lblLeftMovementsThree);
		
		lblLeftVelocityThree = new JLabel("Velocity: ?");
		lblLeftVelocityThree.setBounds(149, 225, 120, 14);
		pnlLeft.add(lblLeftVelocityThree);
		
		lblLeftFingerFour = new JLabel("Finger 4:");
		lblLeftFingerFour.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLeftFingerFour.setBounds(281, 142, 79, 14);
		pnlLeft.add(lblLeftFingerFour);
		
		JPanel pnlRight = new JPanel();
		tabbedPane.addTab("Right Hand Data", null, pnlRight, null);
		pnlRight.setLayout(null);
		
		lblRightHandData = new JLabel("Right Hand Data:");
		lblRightHandData.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblRightHandData.setBounds(10, 11, 419, 31);
		pnlRight.add(lblRightHandData);
		
		lblRightMove = new JLabel("<html>Number of <b>Overall</b> Hand Movements:</html>");
		lblRightMove.setHorizontalAlignment(SwingConstants.TRAILING);
		lblRightMove.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblRightMove.setBounds(20, 53, 239, 22);
		pnlRight.add(lblRightMove);
		
		lblRightMoveData = new JLabel("?");
		lblRightMoveData.setToolTipText("");
		lblRightMoveData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblRightMoveData.setBounds(269, 53, 160, 22);
		pnlRight.add(lblRightMoveData);
		
		lblRightVelocity = new JLabel("<html><b>Overall</b> Average Hand Motion Velocity:</html>");
		lblRightVelocity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblRightVelocity.setBounds(20, 80, 239, 22);
		pnlRight.add(lblRightVelocity);
		
		lblRightVelocityData = new JLabel("?");
		lblRightVelocityData.setToolTipText("");
		lblRightVelocityData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblRightVelocityData.setBounds(269, 80, 160, 22);
		pnlRight.add(lblRightVelocityData);
		
		separator_1 = new JSeparator();
		separator_1.setBounds(10, 115, 419, 2);
		pnlRight.add(separator_1);
		
		lblRightIndividualFingers = new JLabel("Individual Fingers:");
		lblRightIndividualFingers.setVerticalAlignment(SwingConstants.TOP);
		lblRightIndividualFingers.setHorizontalAlignment(SwingConstants.LEFT);
		lblRightIndividualFingers.setFont(new Font("Tahoma", Font.ITALIC, 15));
		lblRightIndividualFingers.setBounds(10, 122, 419, 22);
		pnlRight.add(lblRightIndividualFingers);
		
		lblRightFingerZero = new JLabel("Finger 0:");
		lblRightFingerZero.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRightFingerZero.setBounds(10, 142, 89, 14);
		pnlRight.add(lblRightFingerZero);
		
		lblRightMovementsZero = new JLabel("Movement: ?");
		lblRightMovementsZero.setBounds(10, 160, 140, 14);
		pnlRight.add(lblRightMovementsZero);
		
		lblRightVelocityZero = new JLabel("Velocity: ?");
		lblRightVelocityZero.setBounds(10, 177, 140, 14);
		pnlRight.add(lblRightVelocityZero);
		
		lblRightFingerOne = new JLabel("Finger 1:");
		lblRightFingerOne.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRightFingerOne.setBounds(10, 194, 79, 14);
		pnlRight.add(lblRightFingerOne);
		
		lblRightMovementsOne = new JLabel("Movement: ?");
		lblRightMovementsOne.setBounds(10, 209, 139, 14);
		pnlRight.add(lblRightMovementsOne);
		
		lblRightVelocityOne = new JLabel("Velocity: ?");
		lblRightVelocityOne.setBounds(10, 225, 140, 14);
		pnlRight.add(lblRightVelocityOne);
		
		lblRightFingerTwo = new JLabel("Finger 2:");
		lblRightFingerTwo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRightFingerTwo.setBounds(149, 142, 79, 14);
		pnlRight.add(lblRightFingerTwo);
		
		lblRightMovementsTwo = new JLabel("Movement: ?");
		lblRightMovementsTwo.setBounds(149, 160, 120, 14);
		pnlRight.add(lblRightMovementsTwo);
		
		lblRightVelocityTwo = new JLabel("Velocity: ?");
		lblRightVelocityTwo.setBounds(149, 177, 120, 14);
		pnlRight.add(lblRightVelocityTwo);
		
		lblRightFingerThree = new JLabel("Finger 3:");
		lblRightFingerThree.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRightFingerThree.setBounds(149, 194, 79, 14);
		pnlRight.add(lblRightFingerThree);
		
		lblRightMovementsThree = new JLabel("Movement: ?");
		lblRightMovementsThree.setBounds(149, 208, 120, 14);
		pnlRight.add(lblRightMovementsThree);
		
		lblRightVelocityThree = new JLabel("Velocity: ?");
		lblRightVelocityThree.setBounds(149, 225, 120, 14);
		pnlRight.add(lblRightVelocityThree);
		
		lblRightFingerFour = new JLabel("Finger 4:");
		lblRightFingerFour.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRightFingerFour.setBounds(281, 142, 79, 14);
		pnlRight.add(lblRightFingerFour);
		
		lblRightMovementsFour = new JLabel("Movement: ?");
		lblRightMovementsFour.setBounds(281, 160, 120, 14);
		pnlRight.add(lblRightMovementsFour);
		
		lblRightVelocityFour = new JLabel("Velocity ?");
		lblRightVelocityFour.setBounds(281, 177, 120, 14);
		pnlRight.add(lblRightVelocityFour);
		
		JPanel pnlGraph = new JPanel();
		tabbedPane.addTab("Visualize Data", null, pnlGraph, null);
		pnlGraph.setLayout(null);
		
		pnlChart = new ChartPanel(freeChart);
		pnlChart.setBounds(0, 0, 439, 218);
		pnlGraph.add(pnlChart);
		pnlChart.setLayout(null);
		
		btnSaveChart = new JButton("Save Chart...");
		btnSaveChart.setBounds(8, 222, 121, 16);
		pnlGraph.add(btnSaveChart);
		
		btnModifyChart = new JButton("Modify Chart...");
		btnModifyChart.setBounds(308, 222, 121, 16);
		pnlGraph.add(btnModifyChart);
		btnModifyChart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//We first need to call the chart dialog.
				chartSetting = new ChartDialog();
				
				//Sets the default values.
				chartSetting.setValues(defaultChartType, defaultValues);
			
				chartSetting.setModal(true);
				chartSetting.setAutoRequestFocus(true);
				chartSetting.setVisible(true);

				//Check the return code.
				if (chartSetting.getReturnCode() != 1)
					return;
				
				//Now we obtain values.
				int chartType = chartSetting.getSelectedChart();
				boolean[] values = chartSetting.getSelectedValues();
				
				defaultChartType = chartType;
				defaultValues = values;
				
				//Call the redraw method.
				generateChart(ChartType.values()[chartType], values);
			}
		});
		btnSaveChart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Prompts the user for a save file.
				JFileChooser dialog = new JFileChooser();
				
				//Sets the file filter.
				dialog.removeChoosableFileFilter(dialog.getFileFilter());
				dialog.addChoosableFileFilter(new FileNameExtensionFilter("JPG File (.jpg)", "jpg"));
				dialog.addChoosableFileFilter(new FileNameExtensionFilter("JPEG File (.jpeg)", "jpeg"));
				
				int retCode = dialog.showSaveDialog(MetricsStatusWindow.this);
				
				//Check the return code.
				if (retCode != 0)
					return;
				
				//We generate an output file.
				String path = dialog.getSelectedFile().getAbsolutePath();
				if (dialog.getFileFilter().getDescription().equals("JPG File (.jpg)") &&
						!path.endsWith("jpg")){
					path += ".jpg";
				} else if ((dialog.getFileFilter().getDescription().equals("JPEG File (.jpeg)") &&
						!path.endsWith("jpeg"))){
					path += ".jpeg";
				}
				
				BufferedImage image = ScreenImage.createImage(pnlChart);
				try {
					ScreenImage.writeImage(image, path);
				} catch (IOException e) {
					e.printStackTrace();
					ProgramController.createDialog("There was a problem creating the image.\nAborted!", 
												   "Leap Motion Tracker", JOptionPane.ERROR_MESSAGE);
				}
				
				//Notifies of success.
				ProgramController.createDialog("Success!\nFile saved as\n" + path, 
						"Leap Motion Tracker", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		pnlLoading = new JPanel();
		pnlLoading.setVisible(false);
		pnlLoading.setBounds(0, 0, 444, 271);
		contentPane.add(pnlLoading);
		pnlLoading.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setIcon(new ImageIcon(MetricsStatusWindow.class.getResource("/javax/swing/plaf/metal/icons/Inform.gif")));
		lblNewLabel.setBounds(10, 72, 424, 51);
		pnlLoading.add(lblNewLabel);
		
		JLabel lblPleaseWait = new JLabel("Please Wait!");
		lblPleaseWait.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblPleaseWait.setHorizontalAlignment(SwingConstants.CENTER);
		lblPleaseWait.setBounds(10, 103, 424, 65);
		pnlLoading.add(lblPleaseWait);
		
		JLabel lblCrunchingTheLatest = new JLabel("Crunching The Latest Numbers....");
		lblCrunchingTheLatest.setHorizontalAlignment(SwingConstants.CENTER);
		lblCrunchingTheLatest.setFont(new Font("Tahoma", Font.ITALIC, 16));
		lblCrunchingTheLatest.setBounds(10, 154, 424, 25);
		pnlLoading.add(lblCrunchingTheLatest);
		
		lblLeftMovementsFour = new JLabel("Movement: ?");
		lblLeftMovementsFour.setBounds(281, 160, 120, 14);
		pnlLeft.add(lblLeftMovementsFour);
		
		lblLeftVelocityFour = new JLabel("Velocity ?");
		lblLeftVelocityFour.setBounds(281, 177, 120, 14);
		pnlLeft.add(lblLeftVelocityFour);
		
		//Creates object groups.
		//TODO: MAKE WAAAAAY MORE EFFICIENT
		LEFT_INFO_GROUP = new JLabel[5];
		LEFT_INFO_GROUP[0] = lblLeftFingerZero;
		LEFT_INFO_GROUP[1] = lblLeftFingerOne;
		LEFT_INFO_GROUP[2] = lblLeftFingerTwo;
		LEFT_INFO_GROUP[3] = lblLeftFingerThree;
		LEFT_INFO_GROUP[4] = lblLeftFingerFour;
		
		LEFT_DATA_MOVEMENT_GROUP = new JLabel[5];
		LEFT_DATA_MOVEMENT_GROUP[0] = lblLeftMovementsZero;
		LEFT_DATA_MOVEMENT_GROUP[1] = lblLeftMovementsOne;
		LEFT_DATA_MOVEMENT_GROUP[2] = lblLeftMovementsTwo;
		LEFT_DATA_MOVEMENT_GROUP[3] = lblLeftMovementsThree;
		LEFT_DATA_MOVEMENT_GROUP[4] = lblLeftMovementsFour;
		
		LEFT_DATA_VELOCITY_GROUP = new JLabel[5];
		LEFT_DATA_VELOCITY_GROUP[0] = lblLeftVelocityZero;
		LEFT_DATA_VELOCITY_GROUP[1] = lblLeftVelocityOne;
		LEFT_DATA_VELOCITY_GROUP[2] = lblLeftVelocityTwo;
		LEFT_DATA_VELOCITY_GROUP[3] = lblLeftVelocityThree;
		LEFT_DATA_VELOCITY_GROUP[4] = lblLeftVelocityFour;
		
		RIGHT_INFO_GROUP = new JLabel[5];
		RIGHT_INFO_GROUP[0] = lblRightFingerZero;
		RIGHT_INFO_GROUP[1] = lblRightFingerOne;
		RIGHT_INFO_GROUP[2] = lblRightFingerTwo;
		RIGHT_INFO_GROUP[3] = lblRightFingerThree;
		RIGHT_INFO_GROUP[4] = lblRightFingerFour;
		
		RIGHT_DATA_MOVEMENT_GROUP = new JLabel[5];
		RIGHT_DATA_MOVEMENT_GROUP[0] = lblRightMovementsZero;
		RIGHT_DATA_MOVEMENT_GROUP[1] = lblRightMovementsOne;
		RIGHT_DATA_MOVEMENT_GROUP[2] = lblRightMovementsTwo;
		RIGHT_DATA_MOVEMENT_GROUP[3] = lblRightMovementsThree;
		RIGHT_DATA_MOVEMENT_GROUP[4] = lblRightMovementsFour;
		
		RIGHT_DATA_VELOCITY_GROUP = new JLabel[5];
		RIGHT_DATA_VELOCITY_GROUP[0] = lblRightVelocityZero;
		RIGHT_DATA_VELOCITY_GROUP[1] = lblRightVelocityOne;
		RIGHT_DATA_VELOCITY_GROUP[2] = lblRightVelocityTwo;
		RIGHT_DATA_VELOCITY_GROUP[3] = lblRightVelocityThree;
		RIGHT_DATA_VELOCITY_GROUP[4] = lblRightVelocityFour;
	}
	
	public void waitingForData(MetricsCalculator calculator){
		//First, we hide the data panel.
		tabbedPane.setVisible(false);
		pnlLoading.setVisible(true);
		
		//Now we set up a timer to listen for incoming data.
		this.calculator = calculator;
		calcTimer = new Timer();
		calcTimer.schedule(new Tick(), 1000, 1000);
	}
	
	private void generateChart(ChartType type, boolean[] data) {
		//Develops the dataset.
		 Dataset ds = createDataset(type, data);
		 
		 //Creates general chart look and feel.
		 if (type == ChartType.MOTION_BAR || type == ChartType.VEL_BAR){
	         freeChart = ChartFactory.createBarChart(type == ChartType.MOTION_BAR ? "Hand Motions Per Hand" :
					 								 	"Velocity Per Hand", 	
	        		 								 "",
	                 								 type == ChartType.MOTION_BAR ? "# of Motions" :
	                 									 "Average Velocity", 
	                 								 (CategoryDataset) ds, 
	                 								 PlotOrientation.VERTICAL, 
	                 								 true, 
	                 								 true,
	                 								 false);
		 } else {
			 
		 }
         
         //Redraws the chart.
		 freeChart.setBackgroundPaint(pnlLoading.getBackground());
         pnlChart.setChart(freeChart);
	}

	private Dataset createDataset(ChartType type, boolean[] values) {
		//Creates a new dataset.
		Dataset data = null;

        //Determines what type of dataset is created.
        if (type == ChartType.VEL_BAR || type == ChartType.MOTION_BAR){
        	DefaultCategoryDataset ds = new DefaultCategoryDataset();
        	
        	//Loops to generate the data.
        	for (int i = 0; i < BAR_CATEGORIES.length - 1; i++){
        		//Skip the category if not selected.
        		if (values[i] == false) continue;
        		
        		int j;
        		for (j = 0; j < FINGERS.length; j++){
        			//We look to see if we skip our current value.
        			if (values[j + 3] == false) continue;
        			
        			if (type == ChartType.MOTION_BAR)
        				ds.addValue((i == 0) ? leftMotions[j] : rightMotions[j] , FINGERS[j], BAR_CATEGORIES[i]);
        			else
        				ds.addValue((i == 0) ? leftVelocity[j] : rightVelocity[j] , FINGERS[j], BAR_CATEGORIES[i]);
        		}
        	}
        	
        	//Sets this to be the data.
        	data = ds;
        } else {
        	//We need to start by generating the data.    		
        	if (type == ChartType.MOTION_LNE){        		
        		
        	} else {
        		
        	}
        }
        
        return data;
	}

	private void setUpData(){
		//We pull the category from the calculator.
		lblReported.setText(lblReported.getText() + " <i>" + calculator.getSkillLevel() + "</i></html>");
		
		//Next we pull data from the calculator.
		int[] handMotions = calculator.getHandMotions();
		int[] leftFingerMotions = calculator.getFingerMotions(true);
		int[] rightFingerMotions = calculator.getFingerMotions(false);
		float[] handVelocity = calculator.getHandVelocity();
		float[] leftFingerVelocity = calculator.getFingerVelocity(true);
		float[] rightFingerVelocity = calculator.getFingerVelocity(false);
		
		//First, we set up left hand.
		lblLeftMoveData.setText(Integer.toString(handMotions[0]));
		lblLeftVelocityData.setText(Float.toString(handVelocity[0]));
		for (int i = 0; i < leftFingerMotions.length; i++){
			//Sets the internal labels.
			LEFT_INFO_GROUP[i].setText(FINGERS[i] + ":");
			LEFT_DATA_MOVEMENT_GROUP[i].setText("Movements: " + leftFingerMotions[i]);
			LEFT_DATA_VELOCITY_GROUP[i].setText("Velocity: " + leftFingerVelocity[i]);
		}
		
		//Next, we set up the right hand.
		lblRightMoveData.setText(Integer.toString(handMotions[1]));
		lblRightVelocityData.setText(Float.toString(handVelocity[1]));
		for (int i = 0; i < rightFingerMotions.length; i++){
			//Sets the internal labels.
			RIGHT_INFO_GROUP[i].setText(FINGERS[i] + ":");
			RIGHT_DATA_MOVEMENT_GROUP[i].setText("Movements: " + rightFingerMotions[i]);
			RIGHT_DATA_VELOCITY_GROUP[i].setText("Velocity: " + rightFingerVelocity[i]);
		}
		
		//Store the data values.
		storeDataValues();
		
		//Finally, we generate the chart.
		generateChart(ChartType.MOTION_BAR, defaultValues);
		
		//We hide the loading panel and show the data panel.
		tabbedPane.setVisible(true);
		pnlLoading.setVisible(false);
		
	}
	
	public void setUpDataDatabase(Vector<String> values){
		//Get the skill level from the database.
		lblReported.setText(lblReported.getText() + " <i>" + 
				ProgramController.database.getSkillLevelName(values.elementAt(1)) + "</i></html>");
		
		//We first want to set up the finger data.
		for (int i = 0; i < 5; i++){
			LEFT_INFO_GROUP[i].setText(FINGERS[i] + ":");
			LEFT_DATA_MOVEMENT_GROUP[i].setText("Movements: " + values.elementAt(i + 3));
			LEFT_DATA_VELOCITY_GROUP[i].setText("Velocity: " + values.elementAt(i + 15));
			RIGHT_INFO_GROUP[i].setText(FINGERS[i] + ":");
			RIGHT_DATA_MOVEMENT_GROUP[i].setText("Movements: " + values.elementAt(i + 9));
			RIGHT_DATA_VELOCITY_GROUP[i].setText("Velocity: " + values.elementAt(i + 21));
		}
		
		//Next we add the hand values first.
		lblLeftMoveData.setText(values.elementAt(2));
		lblLeftVelocityData.setText(values.elementAt(14));
		lblRightMoveData.setText(values.elementAt(8));
		lblRightVelocityData.setText(values.elementAt(20));
		
		//We store the data values.
		storeDataValues(values);
		
		//Finally, we generate the chart.
		generateChart(ChartType.MOTION_BAR, defaultValues);
		
		//We hide the loading panel and show the data panel.
		tabbedPane.setVisible(true);
		pnlLoading.setVisible(false);
	}
	
	private void storeDataValues(Vector<String> values) {
		leftMotions = new float[6];
		rightMotions = new float[6];
		leftVelocity = new float[6];
		rightVelocity = new float[6];
		
		//Stores the data.
		for (int i = 0; i < leftMotions.length - 1; i++){
			leftMotions[i] = Float.parseFloat(values.elementAt(i + 2));
			rightMotions[i] = Float.parseFloat(values.elementAt(i + 8));
			leftVelocity[i] = Float.parseFloat(values.elementAt(i + 14));
			rightVelocity[i] = Float.parseFloat(values.elementAt(i + 20));			
		}
		leftMotions[5] = Float.parseFloat(values.elementAt(1));
		rightMotions[5] = Float.parseFloat(values.elementAt(7));
		leftVelocity[5] = Float.parseFloat(values.elementAt(14));
		rightVelocity[5] = Float.parseFloat(values.elementAt(19));
	}
	private void storeDataValues(){
		leftMotions = new float[6];
		rightMotions = new float[6];
		leftVelocity = new float[6];
		rightVelocity = new float[6];
		
		//Does the palm motion data.
		int[] intermediate = calculator.getHandMotions();
		leftMotions[5] = intermediate[0];
		rightMotions[5] = intermediate[1];
		
		//Now does the finger motion data.
		intermediate = calculator.getFingerMotions(true);
		int[] rightIntermediate = calculator.getFingerMotions(false);
		for (int i = 0; i < intermediate.length; i++){
			leftMotions[i] = intermediate[i];
			rightMotions[i] = rightIntermediate[i];
		}
		
		//Does the velocity.
		leftVelocity[5] = calculator.getHandVelocity()[0];
		rightVelocity[5] = calculator.getHandVelocity()[1];
		for (int i = 0; i < calculator.getFingerVelocity(true).length; i++){
			leftVelocity[i] = calculator.getFingerVelocity(true)[i];
			rightVelocity[i] = calculator.getFingerVelocity(false)[i];
		}
	}

	class Tick extends TimerTask {
        public void run() {
        	//We check for data from the calculator.
        	if (calculator.isDone()){
        		//Stops the timer.
        		calcTimer.cancel();
        		
        		//Notify GUI of completion.
        		PlaybackController.refreshSessionWindow();
        		
        		//Sets up the data.
        		setUpData();
        	}
        }
	}
}
