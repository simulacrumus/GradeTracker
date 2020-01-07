package gt.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

import gt.custom.CustomException;

public class Course implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String name, profName;
    private double goalGrade;
    private ArrayList<GradeComponent> grades = new ArrayList <>();

    public Course(String name, String profName, double goalGrade){
    	setName(name);
    	setProfName(profName);
    	setGoalGrade(goalGrade);
    }

    public String getName(){ return name; }
    public void setName(String name) {
    	if (name == null || name.trim().isEmpty() ) throw new CustomException("Please enter a value for Course Name", "No value Input");
    	this.name = name.toUpperCase(); 
    }

    public String getProfName(){ return profName; }
    public void setProfName(String profName){ 
    	if (profName == null || profName.trim().isEmpty()) throw new CustomException("Please enter a value for Proffesor Name", "No value Input");
    	this.profName = profName; 
    }

    public double getGoalGrade(){ return goalGrade; }
    public void setGoalGrade(double goalGrade){ 
    	if (goalGrade <= 0) throw new CustomException("Goal Grade cannot be less than or equal to 0", "Bad Goal Grade input");
    	if (goalGrade > 100) throw new CustomException("Goal Grade cannot be greater than 100", "Bad Goal Grade input");
    	this.goalGrade = goalGrade; 
    }

    public ArrayList<GradeComponent> getGrades(){ return grades; }
    
    public double getCurrentGrade(ArrayList<GradeComponent> gc){
        double totalGrades = 0;
        for (int i = 0; i < gc.size(); i++) 
        	if(!Double.isNaN(gc.get(i).getWeightAchieved())) totalGrades += gc.get(i).getWeightAchieved();
        return totalGrades;
    }

    public double getProjectedGrade(ArrayList<GradeComponent> gc){
        double weightAchieved = 0, weight = 0;
        for (int i = 0; i < gc.size(); i++) {
        	if( !Double.isNaN(gc.get(i).getWeightAchieved()) ) {
            	weightAchieved += gc.get(i).getWeightAchieved();
                weight += gc.get(i).getWeight();
        	}

        }
        return Double.isNaN(weightAchieved / weight) ? 0 : (weightAchieved / weight) * 100  ;
    }

    public double getRemainingMarks(){
        double weight = 0;
        for (int i = 0 ; i < getGrades().size(); i++)
        	if(!Double.isNaN(getGrades().get(i).getWeightAchieved())) weight += getGrades().get(i).getWeight();
        return 100 - weight;
    }
    
    public double getMarksLost(){
        double weight = 0, weightAchieved = 0;
        for (int i = 0; i < getGrades().size(); i++) {
        	if(!Double.isNaN(getGrades().get(i).getWeightAchieved())) {
            	weightAchieved += getGrades().get(i).getWeightAchieved();
                weight += getGrades().get(i).getWeight();
        	}  	
        }
        return weight - weightAchieved;
    }
    
    public GradeComponent findNearestComponent() {
    	ArrayList<GradeComponent> nn = makeNonNullList();
    	nn.sort((GradeComponent gc1, GradeComponent gc2) -> gc1.getDueDate().compareTo(gc2.getDueDate()));
    	return nn.get(0);
    }
    
    public double getGoalGradeDifference(){ return getProjectedGrade(getGrades()) - getGoalGrade(); }
    
    public static String getLetterGrade(double grade){
        if(grade >= 90) {return "A+";}
        else if(grade >= 85) {return "A";}
        else if(grade >= 80) {return "A-";}
        else if(grade >= 77) {return "B+";}
        else if(grade >= 73) {return "B";}
        else if(grade >= 70) {return "B-";}
        else if(grade >= 67) {return "C+";}
        else if(grade >= 63) {return "C";}
        else if(grade >= 60) {return "C-";}
        else if(grade >= 57) {return  "D+";}
        else if(grade >= 53) {return "D";}
        else if(grade >= 50) {return"D-";}
        else {return"F";}
    }
    	
	public ArrayList<GradeComponent> makeSubList(String category){
		if(category.equals("ALL")) return getGrades(); // If this method is used with "ALL" no sublist is made
		ArrayList<GradeComponent> subList = new ArrayList<>();
		for(GradeComponent gc : getGrades()) 
			if( gc.getCategory().equals(category) ) subList.add(gc);
		return subList;
	}
	
	public ArrayList<GradeComponent> makeNonNullList() {
		ArrayList<GradeComponent> subList = new ArrayList<>();
		for( GradeComponent gc : getGrades() ) {
			if(gc.getDueDate() != null && Double.isNaN(gc.getWeightAchieved())) {
				subList.add(gc);
			}
		}		
		return subList;
	}
	
	
	public double weightCompleted() {
		double weightCompleted = 0;
		for( GradeComponent gc : getGrades() ) {
			if( gc.getDueDate() == null) {
				if(!Double.isNaN(gc.getWeightAchieved())) {
					weightCompleted += gc.getWeight();
				}
			}else if( gc.getDueDate().isBefore(LocalDate.now()) || !Double.isNaN(gc.getWeightAchieved())) {
				weightCompleted += gc.getWeight();
			}
		}
		return weightCompleted;
	}
	

	public double getTotalWeight() {
		double currentWeight = 0;
		for(GradeComponent gc : getGrades()) 
			if(!Double.isNaN(gc.getWeight())) {
				currentWeight += gc.getWeight();
			}	
		return currentWeight;
	}
					
}
