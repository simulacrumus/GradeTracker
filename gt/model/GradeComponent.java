package gt.model;

import java.io.Serializable;
import java.time.LocalDate;

import gt.custom.CustomException;

public class GradeComponent implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String name, category;
    private LocalDate dueDate;
    private double weight, score, maxScore, percentScore, weightAchieved;

    public String getName() { return name; }
    public void setName(String name) { 
    	if (name == null || name.trim().isEmpty()) throw new CustomException("Please enter a value for Name", "No value Input");
    	this.name = name;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) {
    	if (weight <= 0) throw new CustomException("Weight cannot be equal or less than 0", "Bad weight value");
    	this.weight = weight; 
    }
    
    public double getScore() { return score; }
    public void setScore(double score) {
    	if (score < 0) throw new CustomException("Score cannot be less than 0", "Bad score value");
    	this.score = score; 
    }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) {
    	if (maxScore <= 0) throw new CustomException("Max score must be greater than 0", "Bad max score value");
    	this.maxScore = maxScore;
    }

    public double getWeightAchieved() { return (getScore()/getMaxScore())*getWeight(); }
    public void setWeightAchieved(double weightAchieved) { this.weightAchieved = weightAchieved; }
    
    public double getPercentScore() { return (getScore()/getMaxScore())*100; }
    public void setPercentScore(double percentScore) { this.percentScore = percentScore; }
    
}
