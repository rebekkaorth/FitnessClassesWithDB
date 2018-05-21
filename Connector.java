import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JOptionPane;

import org.postgresql.util.PSQLException;

public class Connector {
	String databaseName; 
	String username;
	String password; 

	private Connection connection = null; 

	public Connector(String databaseName, String username, String password) {
		this.databaseName  = databaseName;
		this.username  = username;
		this.password  = password;
	}

	public void startConnection () {
		try {
			connection =
					DriverManager.getConnection("jdbc:postgresql://yacata.dcs.gla.ac.uk:5432/" +
							databaseName,username, password);
		}
		catch (SQLException e) {
			System.err.println("Connection Failed!");
			e.printStackTrace();
			return;
		}
		if (connection != null) {
			System.out.println("Connection successful");
		}
		else {
			System.err.println("Failed to make connection!");
		}
	}

	public void endConnection () {
		try {
			connection.close();
			System.out.println("Connection closed");
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Connection could not be closed – SQL exception");
		}
	}
	
	public String getResults(String column, String tableName) {
		Statement stmt = null;
		String query;
			query = " SELECT " + column + " FROM gymbooking." + tableName;	
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String result = rs.getString(column);
				return result; 
			}
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
			JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE);
		}
		return " ";
	}
	
	public String insertBooking (String memberID, String courseID) {
		Statement stmt = null;
		String nextBookingID = this.getNextBookingId();
		String date = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
		String time =  new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		String totalCost = this.getResults("cost", "course");
		String query = "INSERT INTO gymbooking.booking VALUES  ('" + nextBookingID + "', '" + memberID + "', '"+ date +"', '" + time + "', " + totalCost + ", '" + courseID + "')";
		try {
			if (checkPlacesInCourse(getMaxPlacesInCourse(courseID))) { //check if there are still places left in the course
				stmt = connection.createStatement();
				int rs = stmt.executeUpdate(query);
				String successMessage = "" + memberID + " was booked on to " + courseID;
				return successMessage;
			} 
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
			JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE);
		}
		
		return "course is full or input is wrong";
	}
	
	//get maximum number of places from gymbooking.course
	private int getMaxPlacesInCourse (String courseID) {
		Statement stmt = null;
		String query;
			query = "SELECT course.places FROM gymbooking.course WHERE course.courseid = '" + courseID + "'";	
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String result = rs.getString("places");
				int resultAsInt = Integer.parseInt(result);
				return resultAsInt; 
			}
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
		}
		
		return 0; 
	}
	
	//check if there still places free in the course to be booked. By counting members in gymbooking.booking and comparing that to that max. places
	private boolean checkPlacesInCourse (int maxPlacesInCourse) {
		int resultAsInt = 0; 
		//count members currently booked on table 
		Statement stmt = null;
		String query;
			query = "SELECT COUNT (booking.member) as counterm, booking.coursebooking FROM gymbooking.booking WHERE booking.coursebooking IS NOT NULL GROUP BY booking.coursebooking";	
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String result = rs.getString("counterm");
				if (result.equals(null)) {
					resultAsInt = 0; 
				} else {
				resultAsInt = Integer.parseInt(result);
				}
			}
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
			JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE);
		}
		if (resultAsInt < maxPlacesInCourse) {
		return true; 
		} 
		return false; 
	}
	
	//method to view all courses in the database
	public String viewAllCourses () {
		Statement stmt = null;
		String query;
			query = "SELECT course.courseid, course.nameofcourse, course.instructor, instructor.firstname, instructor.lastname , course.places, memberBookedCount.countM " + 
					"FROM gymbooking.course " + 
					"LEFT JOIN (SELECT COUNT (booking.member) as countM, booking.coursebooking " + 
					"FROM gymbooking.booking " + 
					"WHERE booking.coursebooking IS NOT NULL " + 
					"GROUP BY booking.coursebooking) as memberBookedCount " + 
					"ON course.courseid = memberBookedCount.coursebooking " + 
					"LEFT JOIN gymbooking.instructor " + 
					"ON course.instructor = instructor.instructorid";	
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			//build StringBuilder to build table of result set
			StringBuilder resultSet = new StringBuilder();
			resultSet.append("course ID");
			resultSet.append("\t");
			resultSet.append(String.format("%-30s","name of course"));
			resultSet.append("\t");
			resultSet.append("instructor ID");
			resultSet.append("\t");
			resultSet.append("firstname");
			resultSet.append("\t");
			resultSet.append("lastname");
			resultSet.append("\t");
			resultSet.append("places");
			resultSet.append("\t");
			resultSet.append("memberBookedCount");
			resultSet.append("\r\n");
			resultSet.append("\r\n");
			
			//append different rows to String 
			while (rs.next()) {
				resultSet.append(rs.getString("courseid"));
				resultSet.append("\t");
				resultSet.append(String.format("%-30s", rs.getString("nameofcourse")));
				resultSet.append("\t");
				resultSet.append( rs.getString("instructor"));
				resultSet.append("\t");
				resultSet.append(rs.getString("firstname"));
				resultSet.append("\t");
				resultSet.append(rs.getString("lastname"));
				resultSet.append("\t");
				resultSet.append(rs.getString("places"));
				resultSet.append("\t");
				resultSet.append(rs.getString("countM"));
				resultSet.append("\r\n");
			}
			String result = resultSet.toString(); 
			return result;  //return result as String formatted as a table 
		}
			catch (SQLException e ) {
				e.printStackTrace();
				System.err.println("error executing query " + query);
				JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE);
			}
			return " ";
	}
	

	//method to view all members currently booked on one course
	public String viewMembersOnCourse (String courseID) {
		Statement stmt = null;
		String query;
			query = "SELECT course.courseid, course.nameofcourse, booking.member, member.firstname, member.lastname\r\n" + 
					"	FROM gymbooking.booking \r\n" + 
					"	INNER JOIN gymbooking.member\r\n" + 
					"	ON booking.member = member.memberid\r\n" + 
					"	INNER JOIN gymbooking.course\r\n" + 
					"	ON booking.coursebooking = course.courseid \r\n" + 
					"	WHERE course.courseid = '" + courseID + "'";	
			try {
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				StringBuilder resultSet = new StringBuilder(); 
				resultSet.append("course ID"); 
				resultSet.append("\t"); 
				resultSet.append("name"); 
				resultSet.append("\t"); 
				resultSet.append("member ID"); 
				resultSet.append("\t"); 
				resultSet.append("firstname");
				resultSet.append("\t"); 
				resultSet.append("lastname"); 
				resultSet.append("\r\n"); 
				boolean test = rs.next(); //testing if resultSet returns true or false -> testing if courseID entered exists 
				if (test) {
					while (test) {
						resultSet.append(rs.getString("courseid"));
						resultSet.append("\t");
						resultSet.append(rs.getString("nameofcourse"));
						resultSet.append("\t");
						resultSet.append(rs.getString("member"));
						resultSet.append("\t");
						resultSet.append(rs.getString("firstname"));
						resultSet.append("\t");
						resultSet.append(rs.getString("lastname"));	
						resultSet.append("\r\n");
						test= rs.next();
					}
					String result = resultSet.toString(); 
					return result; 
				} else {
					return "the course does not exist or there are currently no members booked on the table";
				}
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
			JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE); 
		}
		return " ";
	}
	//method to get last booking ID in order to use for updating the ID when a new booking is insetered 
	private String getLastBookignID() {
		Statement stmt = null;
		String query;
			query = "SELECT ROW_NUMBER() OVER(ORDER BY booking.bookingid DESC), booking.bookingid " + 
					"FROM gymbooking.booking";	
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String result = rs.getString("bookingid");
				return result; 
			}
		}
		catch (SQLException e ) {
			e.printStackTrace();
			System.err.println("error executing query " + query);
			JOptionPane.showMessageDialog(null, "Please enter a correct input!", "input error", JOptionPane.ERROR_MESSAGE);
		}
		return " ";
	}
	//calculating new booking id with input from lastBookingID in order to get new booking ID used in the insert query for a new booking added to the table 
	public String getNextBookingId () {
		String nextBookingID = ""; 
		//get last booking ID from table 
		String lastBookingID = this.getLastBookignID(); 
		int lastBookingIDasInt = Integer.parseInt(lastBookingID.substring(1, lastBookingID.length()));
		lastBookingIDasInt++; 
		nextBookingID = "B" + String.format("%03d", lastBookingIDasInt);
		return nextBookingID;
	}
}
