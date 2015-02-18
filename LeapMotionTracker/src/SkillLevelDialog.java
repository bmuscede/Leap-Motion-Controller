import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Window.Type;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class SkillLevelDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JList lstLevels;
	private DefaultListModel listModel;
	
	/**
	 * Create the dialog.
	 */
	public SkillLevelDialog() {
		//Gets the skill levels.
		Vector<String> skills = ProgramController.database.getSkillLevels();
		
		setTitle("Leap Motion Tracker - [Skill Levels]");
		setType(Type.POPUP);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setBounds(100, 100, 310, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 173, 206);
		contentPanel.add(scrollPane);
		
		listModel = new DefaultListModel();
		for (int i = 0; i < skills.size(); i++)
			listModel.addElement(skills.elementAt(i));
		
		lstLevels = new JList(listModel);
		scrollPane.setViewportView(lstLevels);
		
		JButton btnAdd = new JButton("Add...");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Prompts the user to enter a skill level name.
				String newSkillLevel = JOptionPane.showInputDialog("Enter a new skill level name:");
				
				//Check the value.
				if (newSkillLevel == null || newSkillLevel.equals("")) return;
				
				createNewSkillLevel(newSkillLevel);
			}
		});
		btnAdd.setBounds(193, 23, 91, 23);
		contentPanel.add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Gets the selected object.
				String selected = (String) lstLevels.getSelectedValue();
				
				//Check the value.
				if (selected == null || selected.equals("")) return;
				
				removeSelected(selected);				
			}
		});
		btnRemove.setBounds(193, 54, 91, 23);
		contentPanel.add(btnRemove);
		
		JButton btnIncreaseSkill = new JButton("Increase");
		btnIncreaseSkill.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Get the selected object.
				String selected = (String) lstLevels.getSelectedValue();
				
				increaseSkill(selected, lstLevels.getSelectedIndex());
			}
		});
		btnIncreaseSkill.setBounds(193, 149, 91, 23);
		contentPanel.add(btnIncreaseSkill);
		
		JButton btnDecreaseSkill = new JButton("Decrease");
		btnDecreaseSkill.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Get the selected object.
				String selected = (String) lstLevels.getSelectedValue();
				
				decreaseSkill(selected, lstLevels.getSelectedIndex());
			}
		});
		btnDecreaseSkill.setBounds(193, 183, 91, 23);
		contentPanel.add(btnDecreaseSkill);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
	

	private void createNewSkillLevel(String newSkillLevel) {
		//First, we write it to the database.
		ProgramController.database.createNewSkillLevel(newSkillLevel);
		
		updateSkills();
	}
	
	private void updateSkills(){
		//We get a list of new skill levels.
		Vector<String> skillLevels = ProgramController.database.getSkillLevels();
		listModel = new DefaultListModel();
		for(int i = 0; i < skillLevels.size(); i++)
			listModel.addElement(skillLevels.elementAt(i));
		
		lstLevels.setModel(listModel);
	}
	
	private void removeSelected(String selected) {
		ProgramController.database.removeSkillLevel(selected);
		
		updateSkills();
	}
	
	private void increaseSkill(String selected, int selIndex) {
		//Increase the skill level.
		int val = ProgramController.database.increaseSkillLevel(selected);
		
		updateSkills();
		
		lstLevels.setSelectedIndex((val == 0)? selIndex - 1 : selIndex);
	}
	
	private void decreaseSkill(String selected, int selIndex) {
		//Increase the skill level.
		int val = ProgramController.database.decreaseSkillLevel(selected);
		
		updateSkills();
		
		lstLevels.setSelectedIndex((val == 0)? selIndex + 1 : selIndex);
	}
}