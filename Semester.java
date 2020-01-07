package gt.model;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Semester {
	
	private ArrayList<Course> semester = new ArrayList<>(); // A semester is just a collection of Courses
	private final static String storageFile = "SavedData"; // Name of the file we will use to save and load info					
	public ArrayList<Course> getSemester(){ return semester; }		
		
	public double calculateAvgGrade() { 
		double totalGrade = 0;
		for(Course c : getSemester())
			totalGrade += c.getProjectedGrade(c.getGrades());
		double avgGrade = totalGrade/getSemester().size();
		return Double.isNaN(avgGrade) ? 0: avgGrade;
	}
	
	public boolean checkNameExists(String newCourseName) {
		for(Course c : getSemester()) {
			if( newCourseName.equals(c.getName())) return false;
		}
		return true;
	}
	
	public double getCompletedAmount() { 
		double completed = 0;
		for(Course c : getSemester())
			completed += c.weightCompleted();
		return completed;
	}
	
	public double getProgressRatio() {
		if(getSemester().isEmpty()) return 0; // So NaN is not returned when no courses are made
		double totalPercent = getSemester().size()*100;
		double progressPercent = getCompletedAmount();
		return progressPercent / totalPercent; 
	}
	
	public ArrayList<Course> getNonNullSemester(){
		ArrayList<Course> subList = new ArrayList<>();
		for(Course c : getSemester()) {
			if(!c.makeNonNullList().isEmpty()) {
				subList.add(c);
			}
		}
		return subList;
	}
		
	// Gets the index in the ArrayList of Course objects given the name of the respective course
	public int getCourseIndex(String name) {
		for(int i =0; i < getSemester().size(); i++)
			if(getSemester().get(i).getName().equals(name)) return i;
		return -1; // Returns -1 if no course with the given name exists
	}
					
	public boolean save() {
		try (FileOutputStream fos = new FileOutputStream(storageFile);
			 ObjectOutputStream oos = new ObjectOutputStream(fos);) {
			for (Course course : getSemester()) 
				oos.writeObject(course);
		} catch(IOException ex){ return false; }
		return true;
	}
	
	public boolean load() {
		File file = new File(storageFile);
		if( !file.exists() ) return false; // Code checks for existence of the file at the start before anything is done
		
		getSemester().clear(); // To replace the current ArrayList it needs to be cleared before Appointments are loaded from the file
		try (FileInputStream fis = new FileInputStream(storageFile);
			 ObjectInputStream ois = new ObjectInputStream(fis);) {
			while(true) // Appointments are read from the file until EOFException is triggered 
				getSemester().add( (Course) (ois.readObject()) );	
		} catch (EOFException ex) {
			return true;
		} catch (IOException | ClassNotFoundException ex) {
			return false; 
		}
	}
		
}
