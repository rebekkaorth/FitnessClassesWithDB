import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class JdbcGui extends JFrame implements ActionListener {
	private JPanel panel; 
	private JToggleButton coursesInDBToggle, membersBookedCourseToggle, addBookingToggle;
	private JLabel allCourseToggleDescriptionLabel, courseBookedLabel, memberNameLabel, courseToBookLabel;
	private JTextField courseBookedField, memberNameField, courseToBookField;
	private JButton enterButton; 
	private JTextArea outputArea; 
	
	//jdbc object 
	Connector connector; 
	
	public JdbcGui () {
		
		setSize(800, 580);
		setTitle("JDBC Gymbooking DB");
		
		
		this.connector = new Connector ("m_17_2312288o","m_17_2312288o", "2312288o");
		connector.startConnection();
		
		layoutComponents(); 
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		addWindowListener(new WindowAdapter () {
			@Override
			public void windowClosing (WindowEvent windowEvent) {
				connector.endConnection(); 
			}
		});
		
		
	}
	
	private void layoutComponents() {
		//create panel for selection section 
				JPanel panel = new JPanel(); 
		
				//add panel for course selection 
				JPanel topPanelNorth = new JPanel(); 
				//add explanation label to explain what happens when user selects this toggle 
				allCourseToggleDescriptionLabel = new JLabel ("select to view all courses");
				topPanelNorth.add(allCourseToggleDescriptionLabel);
				//add coursesInDBToggle toggle button
				coursesInDBToggle = new JToggleButton("all courses in database", false);
				coursesInDBToggle.addActionListener(this); 
				topPanelNorth.add(coursesInDBToggle);
				
				//add panel for members booked on course 
				JPanel topPanelCenter= new JPanel(); 
				//add membersBookedCourseToggle toggle button 
				membersBookedCourseToggle = new JToggleButton("members booked on course", false); 
				membersBookedCourseToggle.addActionListener(this); 
				topPanelCenter.add(membersBookedCourseToggle); 
				//add text field of course 
				courseBookedLabel = new JLabel("CourseID: "); 
				topPanelCenter.add(courseBookedLabel); 
				courseBookedField = new JTextField(20); 
				topPanelCenter.add(courseBookedField);
				
				
				//add panel for new bookings 
				JPanel topPanelSouth = new JPanel(); 
				//add addBookingToogle toggle button 
				addBookingToggle = new JToggleButton("add new booking", false); 
				addBookingToggle.addActionListener(this); 
				topPanelSouth.add(addBookingToggle); 
				//add text fields of member and course 
				memberNameLabel = new JLabel("MemberID: "); 
				topPanelSouth.add(memberNameLabel);
				memberNameField = new JTextField(10);
				topPanelSouth.add(memberNameField);
				courseToBookLabel = new JLabel("CourseID: ");
				topPanelSouth.add(courseToBookLabel);
				courseToBookField = new JTextField(10); 
				topPanelSouth.add(courseToBookField); 
				
				
				//add panel to topPanel
				panel.add(topPanelNorth); 
				//add panel to topPanel 
				panel.add(topPanelCenter); 
				//add panel to topPanel 
				panel.add(topPanelSouth); 
				
				JPanel buttonPanel = new JPanel();
				//adding button to start executing 
				enterButton = new JButton("ENTER"); 
				enterButton.addActionListener(this);
				buttonPanel.add(enterButton); 
				panel.add(buttonPanel);
				
				//adding text field to show output / result 
				JPanel textAreaPanel = new JPanel ();
				outputArea = new JTextArea(25, 70);
				outputArea.setEditable(false);
				textAreaPanel.add(outputArea); 
				panel.add(textAreaPanel); 

				this.add(panel);
	}
	
	public void actionPerformed (ActionEvent e) {
		if(e.getSource() == coursesInDBToggle) {
			//select toggle button
			coursesInDBToggle.setSelected(true);
			//unselect other toggle buttons
			membersBookedCourseToggle.setSelected(false);
			addBookingToggle.setSelected(false);
			//disable other text fields
			courseBookedField.setEnabled(false);
			memberNameField.setEnabled(false); 
			courseToBookField.setEnabled(false);
		} else if (e.getSource() == membersBookedCourseToggle) {
			//select toggle button
			membersBookedCourseToggle.setSelected(true);
			//unselect other toggle buttons
			coursesInDBToggle.setSelected(false);
			addBookingToggle.setSelected(false);
			//enable text field
			courseBookedField.setEnabled(true);
			//disable other text fields
			memberNameField.setEnabled(false); 
			courseToBookField.setEnabled(false);
		} else if (e.getSource() == addBookingToggle) {
			//select toggle button
			addBookingToggle.setSelected(true);
			//unselect other toggle buttons
			coursesInDBToggle.setSelected(false);
			membersBookedCourseToggle.setSelected(false);
			//enable text fields 
			memberNameField.setEnabled(true); 
			courseToBookField.setEnabled(true);
			//disable other text fields
			courseBookedField.setEnabled(false);
		} else if (e.getSource() == enterButton && coursesInDBToggle.isSelected()==true) { //when "all courses in database toggle" is selected and enter button is pressed
			//call JDBC method 
			outputArea.setText(connector.viewAllCourses());
			setFieldsBackToStart();
			
		} else if (e.getSource() == enterButton && membersBookedCourseToggle.isSelected()==true) { //when "members on course" toggle is selected and enter button is pressed
			//call JDBC method 
			outputArea.setText(connector.viewMembersOnCourse(courseBookedField.getText()));
			setFieldsBackToStart();
			
		} else if (e.getSource() == enterButton && addBookingToggle.isSelected()==true) { //when "add booking" toggle is selected and enter button is pressed 
			//call JDBC method
			outputArea.setText(connector.insertBooking(memberNameField.getText(), courseToBookField.getText()));
			setFieldsBackToStart(); 
			
		}

	}
	
	private void setFieldsBackToStart () {
		//unselect all toggle buttons and enable all text fields 
		coursesInDBToggle.setSelected(false);
		membersBookedCourseToggle.setSelected(false);
		addBookingToggle.setSelected(false);

		courseBookedField.setEnabled(true);
		memberNameField.setEnabled(true); 
		courseToBookField.setEnabled(true);
		//set text in text fields to ""
		courseBookedField.setText("");
		memberNameField.setText(""); 
		courseToBookField.setText("");
		
		
	}
}

