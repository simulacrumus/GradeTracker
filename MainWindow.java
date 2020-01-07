package gt.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import gt.custom.*;
import gt.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class MainWindow extends Application {
	
	private Semester semester = new Semester(); 
	private final String[] CATEGORIES = {"TESTS", "LABS", "ASSIGNMENTS", "OTHER"}; // Array holding all the defined categories
	private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM");  // Date format used in program
	private final DecimalFormat DF = new DecimalFormat("#.#"); 
	// Main panels
	private VBox leftPanel = new VBox(), topCourses = new VBox();
	private BorderPane rightPanel = new BorderPane();
	// Bar charts
	private BarChart<Number, String> courseChart = makeCourseChart();
	private BarChart<String, Number> dbChart = makeDBChart();
	// Upcoming panel
	private ScrollPane scrollUpcoming = new ScrollPane();
	private VBox upcomingPanel = makeUpcomingPanel();
	// Loading bar
	private Label completedBar = new Label();
	private Label totalBar = new Label();
	private ProgressBar progressBar = new ProgressBar();
	private Label progressBarTitle = new Label();
	private VBox loadingBar = makeLoadingBar();	
	// Metrics Nodes
	private Label contentProjected = new Label(), contentCurrentGrade = new Label(), contentRemaining = new Label(),
			contentMarksLost = new Label(), contentGoalGradeDifference = new Label();
	private Label topProjected = new Label("PROJECTED GRADE"), topCurrentGrade = new Label("CURRENT GRADE"),
			topRemaining = new Label("REMAINING MARKS"), topMarksLost = new Label("MARKS LOST"), topGoalGradeDifference = new Label("GOAL DIFFERENCE");
	private GridPane metrics = makeMetricsTable();
	// Images
	private Image imgTrash = new Image(getClass().getResourceAsStream("trash.png"));
	private Image imgEdit = new Image(getClass().getResourceAsStream("edit.png"));
	private Image upArrow = new Image(getClass().getResourceAsStream("up-arrow.png"));
	private Image downArrow = new Image(getClass().getResourceAsStream("down-arrow.png"));

	// Launch Method
	public static void main(String[] args) { launch(args); }
	
	public void start(Stage primaryStage) {
		semester.load(); // Load on start
		leftPanel.setMinSize(250, 800);
		leftPanel.setId("leftPanel");
		
		// Mac and Windows require slightly different sizes so the code adjusts based on users OS
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
        	topCourses.setMinHeight(762);
		}else if(System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
			topCourses.setMinHeight(750);
		}
		
		for(Course c : semester.getSemester()) { topCourses.getChildren().add(makeCourseHBox(c)); }

		HBox bottomBtns = new HBox();
		bottomBtns.setMaxHeight(30);

		Button addCourseBtn = makeBtn( "ADD COURSE", e -> makeCourseWindow() );
		addCourseBtn.setId("addCourseBtn");
		addCourseBtn.setMinSize(125,30);
		addCourseBtn.setTooltip(makeToolTip("Add Course"));

		Button dashBoardBtn = makeBtn( "DASHBOARD", e -> displayDashBoard() );
		dashBoardBtn.setId("dashboardBtn");
		dashBoardBtn.setTooltip(makeToolTip("Dashboard"));
		dashBoardBtn.setMinSize(125,30);
		bottomBtns.setAlignment(Pos.CENTER);
		bottomBtns.getChildren().addAll(addCourseBtn, dashBoardBtn);
		leftPanel.getChildren().addAll(topCourses, bottomBtns, getAbout());
		
		rightPanel.setMinSize(1100, 800);
		rightPanel.setId("rightPanel");

		displayDashBoard(); // Display dashboard on start
		HBox mainLayout =  new HBox(); // Holds the left and right panels
		mainLayout.getChildren().addAll(leftPanel, rightPanel);		
		Scene scene = new Scene(mainLayout, 1350, 800);
		URL url = this.getClass().getResource("style.css");
		scene.getStylesheets().add(url.toExternalForm());
	    primaryStage.setScene(scene);
	    primaryStage.setTitle("Grade Tracker");
	    primaryStage.setResizable(false);
	    primaryStage.show();
	}
	
	// Makes some of the GridPane of the add/edit course windows
	private GridPane makeWindowGridPane(boolean isAddWindow) {
		GridPane gp = new GridPane();
		String extraLabelText = isAddWindow ? "Enter " : ""; // If its the add window just add the word "Enter" at the start of the label
		gp.setId("updateWindow");
		gp.setPadding(new Insets(20,10,10,10));
		gp.setAlignment(Pos.BASELINE_CENTER);
		gp.setVgap(15);
		gp.setHgap(15);
		
		Label nameLabel = new Label(extraLabelText + "Name");
		nameLabel.setId("windowLabel");
		gp.add(nameLabel, 0, 0);
				
		Label profLabel = new Label(extraLabelText + "Professor Name");
		profLabel.setId("windowLabel");
		gp.add(profLabel, 0, 1);
		
		Label goalLabel = new Label(extraLabelText + "Goal Grade"); 
		goalLabel.setId("windowLabel");
		gp.add(goalLabel, 0, 2); 
		return gp;
	}
	
	// Makes some of the stage of add/edit course windows
	private Stage makeWindowStage(GridPane gp, String title) {
		Stage courseWindow = new Stage();
		courseWindow.setTitle("Add Course");
		courseWindow.initModality(Modality.APPLICATION_MODAL);
		courseWindow.setResizable(false);
		Scene courseWindowScene = new Scene(gp, 450, 210);
		URL url = this.getClass().getResource("style.css");
		courseWindowScene.getStylesheets().add(url.toExternalForm());
		courseWindow.setScene(courseWindowScene);
		return courseWindow;
	}
	
	private void makeCourseWindow() {
		GridPane gp = makeWindowGridPane(true);

		TextField nameText = new TextField();
		gp.add(nameText, 1, 0);
		TextField profText = new TextField();
		gp.add(profText, 1, 1);
		TextField goalText = new TextField();
		gp.add(goalText, 1, 2);
		
		Button addBtn = new Button("Add Course");
		addBtn.setDefaultButton(true);
		addBtn.setId("windowBtn");
		GridPane.setHalignment(addBtn, HPos.RIGHT);
		gp.add(addBtn, 1, 3);
		
		Stage courseAddWindow = makeWindowStage(gp, "Add Course");
		
		addBtn.setOnAction((e -> {
			try {
				if (!semester.checkNameExists(nameText.getText()))
					throw new CustomException("Course Name already exists", "Duplicate Course Name");
				Course newCourse = new Course(nameText.getText(), profText.getText(),
						Double.parseDouble(goalText.getText()));
				semester.getSemester().add(newCourse);
				semester.save();
				topCourses.getChildren().add(makeCourseHBox(newCourse));
				displayMainCourseInfo(newCourse); // After a course is made display its page
				updateCourseChart(newCourse, "ALL", newCourse.getGrades());
				courseAddWindow.close(); // Close window after adding a course
			} catch (NumberFormatException ex) {
				new CustomException("Goal Grade must be a number", "Bad Goal Grade input").DisplayErrMessage();
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
			}
		}));
		
		courseAddWindow.show();
	}
	
	private void editCourseWindow(Course c) {
		GridPane gp = makeWindowGridPane(false);
		
		TextField nameText = new TextField();
		nameText.setText(c.getName());
		gp.add(nameText, 1, 0);

		TextField profText = new TextField();
		profText.setText(c.getProfName());
		gp.add(profText, 1, 1);
		
		TextField goalText = new TextField();
		goalText.setText(String.valueOf(c.getGoalGrade()));
		gp.add(goalText, 1, 2);
		
		Button editBtn = new Button("UPDATE");
		editBtn.setDefaultButton(true);
		editBtn.setId("windowBtn");
		gp.add(editBtn, 1, 3);
		GridPane.setHalignment(editBtn, HPos.RIGHT);
		
		Stage courseMakerWindow = makeWindowStage(gp, "Edit Course");
		editBtn.setOnAction(((e -> {
			try {
				c.setName(nameText.getText());
				c.setProfName(profText.getText());
				c.setGoalGrade(Double.parseDouble(goalText.getText()));
				semester.save();
				displayMainCourseInfo(c); // After a course is made display its page
				updateCourseChart(c, "ALL", c.getGrades());
				topCourses.getChildren().clear(); // Clear the current collection of HBoxes before we add the modifed list
				for (Course co : semester.getSemester()) {
					topCourses.getChildren().add(makeCourseHBox(co));
				}
				courseMakerWindow.close(); // Close window after adding a course
			} catch (NumberFormatException ex) {
				new CustomException("Goal Grade must be a number", "Bad Goal Grade input").DisplayErrMessage();
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
			}
		})));
		

		courseMakerWindow.show();
	}
	
	private HBox makeCourseHBox(Course c) {
		HBox courseHBox = new HBox(-5);
		courseHBox.setAlignment(Pos.CENTER_LEFT);
		courseHBox.setId("courseBox");
		courseHBox.setPadding(new Insets(10,0 ,10,0));

		Button courseName = new Button();
		courseName.setTooltip(makeToolTip(c.getName()));
		courseName.setId("courseName");
		courseName.setAlignment(Pos.CENTER_LEFT);
		courseName.setMinWidth(150);
		courseHBox.getChildren().addAll(courseName);
		courseName.setText(c.getName());
		courseHBox.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			Button edit = new Button();
			edit.setOnAction(e -> editCourseWindow(c) );
			Button delete = new Button();
			edit.setTooltip(makeToolTip("Edit"));
			edit.setGraphic(new ImageView(imgEdit));
			edit.setBackground(null);
			edit.setAlignment(Pos.CENTER_RIGHT);
			delete = makeBtn("", (em -> {
				if(deleteConfirm() == ButtonType.OK) {
					semester.getSemester().remove(semester.getCourseIndex(c.getName()));
					updateUpcomingPanel(c);
					semester.save();
					removeHBox(courseHBox);
				}
			}));
			delete.setTooltip(makeToolTip("Delete"));
			delete.setGraphic(new ImageView(imgTrash));
			delete.setBackground(null);
			delete.setAlignment(Pos.CENTER_LEFT);
			if(courseHBox.getChildren().size()>1)
				courseHBox.getChildren().remove(1,3);
			courseHBox.getChildren().add(1, edit);
			courseHBox.getChildren().add(2, delete);

			if(!newValue){
				if(courseHBox.getChildren().size()>1)
					courseHBox.getChildren().remove(1,3);

			}
		});
		courseName.setOnAction(e -> {
			updateCourseChart(c, "ALL", c.getGrades());
			displayMainCourseInfo(c);
			courseHBox.requestFocus();
			});

		return courseHBox;
	}
	
	private void removeHBox(HBox hBox) {
		displayDashBoard(); // When a course is removed go back to the dashboard
	    topCourses.getChildren().remove(hBox);
	}
	
	private void displayDashBoard() {
		rightPanel.setRight(null); // We currently have nothing on the dashboard right so for now clear it to get rid of the upcoming panel
		updateLoadingBar();
		updateDBChart();
		Label dashboardLabelAverage = new Label("AVERAGE SEMESTER GRADE: "+DF.format(semester.calculateAvgGrade())+"% "+Course.getLetterGrade(semester.calculateAvgGrade()));

		dashboardLabelAverage.setId("dashboardLabel");
		dashboardLabelAverage.setFont(new Font("Arial", 25));
		dashboardLabelAverage.setAlignment(Pos.CENTER);

		Label dashboardLabelPercent = new Label("SEMESTER PROGRESS "+ DF.format(semester.getProgressRatio()*100) + "%");
		dashboardLabelPercent.setId("dashboardLabel");
		dashboardLabelPercent.setFont(new Font("Arial", 25));
		dashboardLabelPercent.setAlignment(Pos.CENTER);

		VBox dbTopHolder = new VBox();
		dbTopHolder.setAlignment(Pos.CENTER);
		dbTopHolder.setId("dbTopHolder");
		dbTopHolder.setPadding(new Insets(16, 0, 15, 0));
		dbTopHolder.setSpacing(10);
		dbTopHolder.getChildren().addAll(dashboardLabelAverage,dashboardLabelPercent,loadingBar);
		dbTopHolder.setMinWidth(1150);
		rightPanel.setTop(dbTopHolder);
		rightPanel.setCenter(dbChart);
		rightPanel.setBottom(makeCourseUpcomingDisplay());
	}
	
	private VBox makeLoadingBar() {
		VBox loadingBarHolder = new VBox();
		loadingBarHolder.setId("loadingBarHolder");
		progressBarTitle.setId("progressTitle");
		progressBarTitle.setMinHeight(30);
		progressBar.setId("progressBar");
		progressBar.setMinSize(1000, 20);
		progressBar.setMaxHeight(20);
		loadingBarHolder.getChildren().addAll(progressBar);
		return loadingBarHolder;
	}
	
	private void updateLoadingBar() {
		completedBar.setMinSize(totalBar.getMinWidth()*semester.getProgressRatio(), totalBar.getMinHeight());
		completedBar.setMaxSize(totalBar.getMinWidth()*semester.getProgressRatio(), totalBar.getMinHeight());
		progressBar.setProgress(semester.getProgressRatio());
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
				new KeyFrame(Duration.seconds(1), e-> {
				}, new KeyValue(progressBar.progressProperty(), semester.getProgressRatio()))
		);
		timeline.setCycleCount(1);
		timeline.play();
	}
	
	private BarChart<String, Number> makeDBChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setUpperBound(100);
        BarChart <String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setAnimated(false);
        bc.setTitle("Semester Summary");
        bc.setStyle("CHART_COLOR_1: #DB9501; CHART_COLOR_2: #4B7447;");
        xAxis.setLabel("Course Name");
        yAxis.setLabel("Grade");                            
        return bc;
    }
	
	private void updateDBChart() {
		dbChart.getData().clear();
		XYChart.Series<String, Number> projectedSeries = new XYChart.Series<>();
		projectedSeries.setName("PROJECTED GRADE");
		XYChart.Series<String, Number> goalSeries = new XYChart.Series<>();
		goalSeries.setName("GOAL GRADE");

		for (Course c : semester.getSemester()) {
			projectedSeries.getData().add(new XYChart.Data<String, Number>(c.getName(), c.getProjectedGrade(c.getGrades())));
			goalSeries.getData().add(new XYChart.Data<String, Number>(c.getName(), c.getGoalGrade()));
		}
		dbChart.getData().add(goalSeries);	
		dbChart.getData().add(projectedSeries);
	}
	
	private HBox makeCourseUpcomingDisplay() { 
		HBox courseUpcoming = new HBox();
		courseUpcoming.setAlignment(Pos.CENTER);
		courseUpcoming.setMinHeight(150);
		courseUpcoming.setSpacing(10);
		courseUpcoming.setId("dashboardCourseUpcomingHBox");
		courseUpcoming.setPadding(new Insets(10, 10, 10,10));
		ArrayList<Course> subSemester = semester.getNonNullSemester();
		subSemester.sort((Course c1, Course c2) -> c1.findNearestComponent().getDueDate().compareTo(c2.findNearestComponent().getDueDate()) );
		for (Course c : subSemester) {
			VBox upcomingComponentHolder = new VBox();
			upcomingComponentHolder.setId("dashboardUpcomingVBox");
			Label componentLabel = new Label(c.getName());
			componentLabel.setId("dashboardCourseName");
			Label componentName = new Label(c.findNearestComponent().getName());
			componentName.setId("dashboardCourseDate");
			Label componentDate = new Label(FORMATTER.format(c.findNearestComponent().getDueDate()));
			componentDate.setId("dashboardCourseDate");
			upcomingComponentHolder.getChildren().addAll(componentLabel, componentName, componentDate);
			courseUpcoming.getChildren().add(upcomingComponentHolder);
		}
		return courseUpcoming;
	}
				
	private void displayMainCourseInfo(Course c) {
		Label courseTitle = new Label( c.getName() + "\n" + c.getProfName().toUpperCase() );
		courseTitle.setFont(new Font("Arial", 25));
		courseTitle.setId("courseTitle");
		courseTitle.setPadding(new Insets(20, 50, 20, 50));
		courseTitle.setMinSize(1150, 80);
		courseTitle.setAlignment(Pos.CENTER);


		rightPanel.setTop(courseTitle);
		displayGradesTable(false, c.getName(), "ALL");
		updateMetricsTable(c);
		VBox content = new VBox();
		HBox tabs = getCategoryTabs(CATEGORIES, c);
		content.getChildren().addAll(tabs, metrics, courseChart);
		rightPanel.setCenter(content);
		updateUpcomingPanel(c);
		rightPanel.setRight(upcomingPanel);
	}
	
	private HBox getCategoryTabs(String[] categories, Course c) {
		HBox categoryTabs = new HBox();
		categoryTabs.setId("categoryTabs");
		categoryTabs.setMinWidth(600);
		Button allTabBtn = makeBtn("ALL", e -> {
			displayGradesTable(false, c.getName(), "ALL");
			updateCourseChart(c, "ALL", c.getGrades());
		});
		allTabBtn.setId("sectionBtnAll");
		allTabBtn.setTooltip(makeToolTip("ALL"));
		allTabBtn.setMinHeight(30);
		categoryTabs.getChildren().add(allTabBtn);
		for (String tabName : categories) {
				Button tabBtn = makeBtn(tabName, e -> {
				displayGradesTable(true, c.getName(), tabName);
				updateCourseChart(c, tabName, c.makeSubList(tabName));
			});
			tabBtn.setId("sectionBtns");
			tabBtn.setMinHeight(30);
			tabBtn.setTooltip(makeToolTip(tabName));
			categoryTabs.getChildren().add(tabBtn);
		}
		return categoryTabs;
	}
	
	private GridPane makeMetricsTable() {
		GridPane gp = new GridPane();
		gp.setId("metrics");
		gp.setPadding(new Insets(10, 10, 10, 10));
		gp.setHgap(20);
		gp.setVgap(10);
		topProjected.setId("gridpaneTitle");
		topCurrentGrade.setId("gridpaneTitle");
		topRemaining.setId("gridpaneTitle");
		topMarksLost.setId("gridpaneTitle");
		topGoalGradeDifference.setId("gridpaneTitle");

		contentProjected.setId("gridpaneText");
		contentCurrentGrade.setId("gridpaneText");
		contentRemaining.setId("gridpaneText");
		contentMarksLost.setId("gridpaneText");
		contentGoalGradeDifference.setId("gridpaneText");
		gp.add(topProjected, 0, 0); gp.add(topCurrentGrade, 1, 0); gp.add(topRemaining, 2, 0); gp.add(topMarksLost, 3, 0); gp.add(topGoalGradeDifference, 4, 0);
		gp.add(contentProjected, 0, 1); gp.add(contentCurrentGrade, 1, 1); gp.add(contentRemaining, 2, 1); gp.add(contentMarksLost, 3, 1); gp.add(contentGoalGradeDifference, 4, 1);
		return gp;
	}
	
	private void updateMetricsTable(Course c) {
		contentProjected.setText(String.valueOf(DF.format(c.getProjectedGrade(c.getGrades()))) + "% " + Course.getLetterGrade(c.getProjectedGrade(c.getGrades())));
		contentCurrentGrade.setText(String.valueOf(DF.format(c.getCurrentGrade(c.getGrades())))+ "% " + Course.getLetterGrade(c.getCurrentGrade(c.getGrades())));
		contentRemaining.setText(String.valueOf(DF.format(c.getRemainingMarks()))+ "% ");
		contentMarksLost.setText(String.valueOf(DF.format(c.getMarksLost()))+ "% ");
		contentGoalGradeDifference.setText(String.valueOf(DF.format(Math.abs(c.getGoalGradeDifference())))+"%");
		if(c.getGoalGradeDifference() >= 0)
			contentGoalGradeDifference.setGraphic(new ImageView(upArrow));
		else 
			contentGoalGradeDifference.setGraphic(new ImageView(downArrow));
	}
				
	private BarChart<Number, String> makeCourseChart() {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(100);
        BarChart <Number, String> bc = new BarChart<>(xAxis, yAxis);
        bc.setAnimated(false);
        bc.setLegendVisible(false);
        bc.setTitle("Grades Summary");
        xAxis.setLabel("Grade");
        xAxis.tickLabelFontProperty().set(Font.font(16));
        yAxis.tickLabelFontProperty().set(Font.font(16));
        bc.setPadding(new Insets(5, 25, 0, 25));
        return bc;
    }
				
	private void updateCourseChart(Course c, String name, ArrayList<GradeComponent> gc) {
		courseChart.getData().clear();
		XYChart.Series<Number, String> courseSeries = new XYChart.Series<>();
		XYChart.Data<Number, String> projectedData = new XYChart.Data<>(c.getProjectedGrade(gc), "Projected (" + name + ")");
		Label projectedLabel = new Label();
		projectedLabel.setStyle("-fx-background-color: #4B7447;");
		projectedData.setNode(projectedLabel);

		XYChart.Data<Number, String> goalData = new XYChart.Data<>(c.getGoalGrade(), String.format("%35s", "Goal Grade"));
		Label goalLabel = new Label();
		goalLabel.setStyle("-fx-background-color: #DB9501;");
		goalData.setNode(goalLabel);

		courseSeries.getData().add(projectedData);
		courseSeries.getData().add(goalData);
		courseChart.getData().add(courseSeries);
	}
	
	private Label makeColToolTip(String colName, String toolTipText) {
		Label colLabel = new Label(colName);
	    colLabel.setTooltip(makeToolTip(toolTipText));
	    return colLabel;
	}
	
	private void displayGradesTable(boolean canAdd, String courseName, String category) {
        VBox tableHolder = new VBox();
        ArrayList<GradeComponent> grades = getCourse(courseName).makeSubList(category);
        tableHolder.setId("tableHolder");
        tableHolder.setMinWidth(rightPanel.getWidth());
		tableHolder.setMaxHeight(400);
		
		TableView<GradeComponent> gradesTable = new TableView<>();
		gradesTable.setId("componentsTable");
		gradesTable.setEditable(true); 
		gradesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		TableColumn<GradeComponent, String> nameCol = makeColumn(null, "name", true, new DefaultStringConverter());
		nameCol.setOnEditCommit(e -> {
			try {
				gradesTable.getSelectionModel().getSelectedItem().setName(e.getNewValue());
				updateAndSave(courseName, category);
				displayGradesTable(canAdd, courseName, category);
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
				displayGradesTable(canAdd, courseName, category);
			}
		});
	    nameCol.setGraphic(makeColToolTip("NAME", "Identify the assignments you would like to keep track of. Assignments can include, tests, labs, or any other \r\n" + 
	    		" related coursework."));
	    
		TableColumn<GradeComponent, String> categoryCol = makeColumn(null, "category", false, null);
		categoryCol.setGraphic(makeColToolTip("CATEGORY", "Refers to the type of coursework. Hybrid, Lab, Test, or any other general category\r\n" + 
				"for grouping coursework, are example categories"));

		TableColumn<GradeComponent, Double> weightCol = makeColumn(null, "weight", true, new CustomDoubleStringConverter());
		weightCol.setOnEditCommit(e -> {
			try {
				if ( e.getNewValue() == null ) {
					gradesTable.getSelectionModel().getSelectedItem().setWeight(Double.NaN);
				} else if ( e.getNewValue().isNaN() ) {
					throw new CustomException("Weight, Score and Max Score must be numbers", "Non numbers input");
				} else {
					if( Double.isNaN(e.getOldValue()) ) {
						checkWeightFull(getCourse(courseName).getTotalWeight() - 0,  e.getNewValue());
					}else {
						checkWeightFull(getCourse(courseName).getTotalWeight() - e.getOldValue(),  e.getNewValue());
					}
					gradesTable.getSelectionModel().getSelectedItem().setWeight(e.getNewValue());
				}
				updateAndSave(courseName, category);
				displayGradesTable(canAdd, courseName, category);
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
				displayGradesTable(canAdd, courseName, category);
			}
		});
		weightCol.setGraphic(makeColToolTip("WEIGHT", "Refers to the importance of the particular assignment. The weight should be input as a number representing a percentage. An example \r\n" + 
				"weight could be a final exam that holds a value of 30%"));
		
		TableColumn<GradeComponent, String> percentScoreCol = makeColumn(null, "percentScore", false, null);
		percentScoreCol.setCellValueFactory(cellData -> Bindings.format("%s%s", (!Double.isNaN(cellData.getValue().getPercentScore())) ? String.valueOf(DF.format(cellData.getValue().getPercentScore())) : "-", 
				!Double.isNaN( cellData.getValue().getPercentScore() ) ? "%" : "")); // Format 
		percentScoreCol.setGraphic(makeColToolTip("GRADE", "The grade percentage attained"));
		
		TableColumn<GradeComponent, Double> scoreCol = makeColumn(null, "score", true, new CustomDoubleStringConverter());
		scoreCol.setOnEditCommit(e -> {
			try {
				if ( e.getNewValue() == null ) {
					gradesTable.getSelectionModel().getSelectedItem().setScore(Double.NaN);
					gradesTable.getSelectionModel().getSelectedItem().setMaxScore(Double.NaN);
				} else if ( e.getNewValue().isNaN() ) {
					throw new CustomException("Weight, Score and Max Score must be numbers", "Non numbers input");
				} else {
					gradesTable.getSelectionModel().getSelectedItem().setScore(e.getNewValue());
				}	
				updateAndSave(courseName, category);
				displayGradesTable(canAdd, courseName, category);
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
				displayGradesTable(canAdd, courseName, category);
			}
		});
		scoreCol.setGraphic(makeColToolTip("SCORE", "The mark attained for the particualar assignment, input as a number. A mark of 8/10 should be input as 8"));
		
		TableColumn<GradeComponent, Double> maxScoreCol = makeColumn(null, "maxScore", true, new CustomDoubleStringConverter());
		maxScoreCol.setOnEditCommit(e -> {
			try {
				if ( e.getNewValue() == null ) {
					gradesTable.getSelectionModel().getSelectedItem().setMaxScore(Double.NaN);
					gradesTable.getSelectionModel().getSelectedItem().setScore(Double.NaN);
				} else if ( e.getNewValue().isNaN() ) {
					throw new CustomException("Weight, Score and Max Score must be numbers", "Non numbers input");
				} else {
					gradesTable.getSelectionModel().getSelectedItem().setMaxScore(e.getNewValue());
				}	
				updateAndSave(courseName, category);
				displayGradesTable(canAdd, courseName, category);
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
				displayGradesTable(canAdd, courseName, category);
			}
			});
		maxScoreCol.setGraphic(makeColToolTip("MAX SCORE", "The maximum score that can be reached for a particular assignment. For example, an assignment can be marked out of \r\n" + 
				"50 marks. The score attained may be 44/50, The 44 should be placed into the preceding column labled as Score, and the 50 should \r\n" + 
				"be placed in this column as the Max Score"));

		TableColumn<GradeComponent, String> weightAchievedCol = makeColumn(null, "weightAchieved", false, null);
		weightAchievedCol.setCellValueFactory(cellData -> Bindings.format("%s%s", (!Double.isNaN(cellData.getValue().getWeightAchieved())) ? String.valueOf(DF.format(cellData.getValue().getWeightAchieved())) : "-", 
				!Double.isNaN( cellData.getValue().getWeightAchieved() ) ? "%" : "")); // Format  
		weightAchievedCol.setGraphic(makeColToolTip("WEIGHT ACHIEVED", "The percentage captured of the available total. If an assignment has a weight of 15%, a Score of 34, and a Max\r\n" + 
				"Score of 40, then the weight achieved would be 12.75%"));
		
		TableColumn<GradeComponent, LocalDate> dueDateCol = makeColumn(null, "dueDate", true, new CustomLocalDateStringConverter());
		dueDateCol.setOnEditCommit(e -> {
			try {
				gradesTable.getSelectionModel().getSelectedItem().setDueDate((e.getNewValue()));
				updateAndSave(courseName, category);
				displayGradesTable(canAdd, courseName, category);
			} catch (CustomException ex) {
				ex.DisplayErrMessage();
				displayGradesTable(canAdd, courseName, category);
			}
		});
		dueDateCol.setGraphic(makeColToolTip("DUE DATE", "The date where an assignment is due"));

		gradesTable.setItems(FXCollections.observableArrayList(grades)); 

		if(!canAdd) {  gradesTable.getColumns().addAll(new ArrayList<>(Arrays.asList(categoryCol, nameCol, weightCol, percentScoreCol, weightAchievedCol, dueDateCol)));  } // This column is only added for the all table
		else gradesTable.getColumns().addAll(new ArrayList<>(Arrays.asList(nameCol, weightCol, scoreCol, maxScoreCol, weightAchievedCol, dueDateCol))); 
		tableHolder.getChildren().add(gradesTable);
		
		gradesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		gradesTable.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.DELETE) || e.getCode().equals(KeyCode.BACK_SPACE)) {
				ObservableList<GradeComponent> selectedGrades = gradesTable.getSelectionModel().getSelectedItems();
				if (deleteConfirm() == ButtonType.OK) {
					getCourse(courseName).getGrades().removeAll(selectedGrades);
					gradesTable.getItems().removeAll(selectedGrades);
					updateAndSave(courseName, category);		
				}

			}
		});
		
		if(canAdd) {
			TextField nameInput = makeTableModField("NAME", "nameInput");
			TextField scoreInput = makeTableModField("SCORE", "scoreInput");
			TextField maxScoreInput = makeTableModField("MAX SCORE", "maxScoreInput");
			TextField weightInput = makeTableModField("WEIGHT", "weightInput");
			DatePicker dueDatePicker = new DatePicker();
			dueDatePicker.setId("dueDatePicker");
			dueDatePicker.setMinWidth(rightPanel.getWidth()/6);
			dueDatePicker.getEditor().setDisable(true);
			dueDatePicker.setPromptText("DUE DATE");
				
			Button addGradeBtn = makeBtn("ADD", e -> {
				try {
					GradeComponent gc = new GradeComponent();
					gc.setName(nameInput.getText());
					if(!weightInput.getText().isEmpty()) gc.setWeight(Double.parseDouble(weightInput.getText()));
					else gc.setWeight(Double.NaN);
					
					if(!scoreInput.getText().isEmpty()) gc.setScore(Double.parseDouble(scoreInput.getText()));
					else gc.setScore(Double.NaN);
					
					if(!maxScoreInput.getText().isEmpty()) gc.setMaxScore(Double.parseDouble(maxScoreInput.getText()));
					else gc.setMaxScore(Double.NaN);
					
					if(dueDatePicker.getValue() != null) {
						gc.setDueDate(dueDatePicker.getValue());
					}
					checkWeightFull(getCourse(courseName).getTotalWeight(), gc.getWeight());
					gc.setCategory(category);
					getCourse(courseName).getGrades().add(gc);
					updateAndSave(courseName, category);
					gradesTable.getItems().add(gc);
					nameInput.clear(); weightInput.clear(); scoreInput.clear(); maxScoreInput.clear(); dueDatePicker.setValue(null);
				} catch (NumberFormatException ex) { new CustomException("Weight, Score and Max Score must be numbers", "Non numbers input").DisplayErrMessage();
				} catch (CustomException ex) { ex.DisplayErrMessage(); }
			});
			addGradeBtn.setDefaultButton(true);
			addGradeBtn.setId("addGradeBtn");
			addGradeBtn.setMinWidth(rightPanel.getWidth()/6);
			
			HBox addTableRow = new HBox();
			addTableRow.setMinWidth(rightPanel.getWidth());
			addTableRow.getChildren().addAll(nameInput, weightInput, scoreInput, maxScoreInput, dueDatePicker, addGradeBtn);
			tableHolder.getChildren().addAll(addTableRow);
		}
	
		rightPanel.setBottom(tableHolder);
	}
		
	private <T> TableColumn<GradeComponent, T> makeColumn(String colName, String varName, boolean editable, StringConverter<T> converter){
		TableColumn<GradeComponent, T> newCol = new TableColumn<>(colName);
		newCol.setMinWidth(100);
		newCol.setCellValueFactory(new PropertyValueFactory<>(varName));
		if(editable) newCol.setCellFactory(TextFieldTableCell.forTableColumn(converter));
		return newCol;
	}
	
	private TextField makeTableModField(String prompt, String id) {
		TextField field = new TextField();
		field.setId(id);
		field.setPromptText(prompt);
		field.setMinWidth(rightPanel.getWidth()/6);
		return field;
	}
	
	private VBox makeUpcomingPanel() {
        VBox upcomingPanel = new VBox();
        upcomingPanel.setId("upcomingPanel");
        upcomingPanel.setMinSize(250,250);
        Label upcomingTitle = new Label("UPCOMING");
        upcomingTitle.setId("upcomingTitle");
        upcomingTitle.setAlignment(Pos.CENTER);
		upcomingTitle.setMinSize(250,30);

        scrollUpcoming.setMinWidth(250);
		scrollUpcoming.setId("scrollUpcoming");
		scrollUpcoming.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        upcomingPanel.getChildren().addAll(upcomingTitle, scrollUpcoming);
        return upcomingPanel;
    }
	
	private void updateUpcomingPanel(Course c) {
		VBox upcomingComponentHolder = new VBox();
		upcomingComponentHolder.setId("upcomingComponentHolder");
		ArrayList<GradeComponent> nn = c.makeNonNullList();
		nn.sort( (GradeComponent gc1, GradeComponent gc2) -> gc1.getDueDate().compareTo(gc2.getDueDate()) );
		for (GradeComponent gc : nn) {
			if( !gc.getDueDate().isBefore(LocalDate.now()) && Double.isNaN(gc.getWeightAchieved()) ) {
				Label upcomingComponentName = new Label(gc.getName());
				upcomingComponentName.setId("upcomingComponentName");
				upcomingComponentName.setMinSize(250, 20);
				upcomingComponentName.setAlignment(Pos.CENTER);
				Label upcomingComponentDate = new Label(FORMATTER.format(gc.getDueDate()));
				upcomingComponentDate.setId("upcomingComponentDate");
				upcomingComponentDate.setMinSize(250, 20);
				upcomingComponentDate.setAlignment(Pos.CENTER);
				upcomingComponentHolder.getChildren().addAll(upcomingComponentName, upcomingComponentDate);	
			}
		}
		scrollUpcoming.setContent(upcomingComponentHolder);
	}
	
	private void updateAndSave(String courseName, String category) {
		updateMetricsTable(getCourse(courseName));
		updateCourseChart(getCourse(courseName), category, getCourse(courseName).makeSubList(category));
		updateUpcomingPanel(getCourse(courseName));
		semester.save();
	}
	
	private Course getCourse(String courseName) { return semester.getSemester().get(semester.getCourseIndex(courseName)); } // Just makes it easier to call the method
	
	private Button makeBtn(String text, EventHandler<ActionEvent> e) {
		Button btn = new Button(text);
		btn.setOnAction(e);
		return btn;
	}

	private Tooltip makeToolTip(String text) {
		Tooltip toolTip = new Tooltip(text);
		toolTip.getStyleClass().add("tooltip");
		return toolTip;
	}
	
	private ButtonType deleteConfirm() {
		Alert errorAlert = new Alert(AlertType.CONFIRMATION);
		errorAlert.getDialogPane().backgroundProperty().set(new Background(new BackgroundFill(Color.web("89a7a9"), null, null)));
		errorAlert.setHeaderText("Confirm Deletion");
		errorAlert.setContentText("Are you sure you want to delete?");
		errorAlert.getResult();
		return errorAlert.showAndWait().get();
	}
	
	private void checkWeightFull(double totalWeight, double newWeight) {
		if( totalWeight + newWeight > 100)
			throw new CustomException("Total course weight cannot exceed 100%", "Component weight value too large");
	}	
	
	private HBox getAbout(){
		HBox aboutBox = new HBox();
		aboutBox.setAlignment(Pos.CENTER);
		aboutBox.setId("aboutBox");
		aboutBox.setMinSize(250, 20);
		Button aboutBtn = new Button("About");
		aboutBtn.setId("aboutBtn");
		aboutBtn.setMinSize(250, 20);
		aboutBtn.setOnAction(e->{
			Stage aboutStage = new Stage();
			HBox aboutLayout = new HBox();
			aboutLayout.setId("aboutLayout");

			Label aboutText = new Label();
			aboutText.setPadding(new Insets(10, 25, 10, 25));
			aboutText.setId("aboutText");
			aboutText.setText("            WELCOME TO GRADETRACKER!\n" +
					"\n" +
					"AUTHORS: \n" +
					"Ra'ad Sweidan (https://github.com/RS-Coder95), \n" +
					"Emrah Kinay (https://github.com/simulacrumus), \n" +
					"Jeph Francois (https://github.com/franic220)\n" +
					"APPLICATION NAME: GradeTracker\n" +
					"VERSION: 1.0\n" +
					"\n" +
					"\n" +
					"This application is designed with the sole intention of improving the \n" +
					"lives of students everywhere, regardless of their program of choice. \n" +
					"Throughout the semester courses tend to move at a very fast paced, \n" +
					"making it far too easy for students to become disorganized. \n" +
					"Forgetting an assignment's due date, not knowing the value of a assignment, \n" +
					"and not knowing one's current grade in a course are just some\n" +
					"of the problems that this application provides solutions for.\n" +
					"Utilizing Object Oriented Programming methodologies (Encapsulation, \n" +
					"Polymorphism, Inheritance) combined with the knowledge attained \n" +
					"from discussions held with numerous students in various programs, \n" +
					"this application provides an all in one solution to the common problems \n" +
					"that impact all students. Start by setting a goal grade to keep track of \n" +
					"how close or how far you are from achieving your goal grade for \n" +
					"the course throughout the semester. Track coursework, grades received, \n" +
					"and due dates. View your progress throughout the semester \n" +
					"at any given time by visiting your dashboard, in here you'll be able to \n" +
					"compare your grades in one course with your grades in another. \n" +
					"View your current grade and projected grade in a course by visiting the \n" +
					"course directly.  \n" +
					"\n" +
					"Thank you for taking the time to use GradeTracker, we hope you find it \n" +
					"as useful as we have!");

			aboutText.setAlignment(Pos.CENTER);
			aboutLayout.getChildren().add(aboutText);
			Scene scene = new Scene(aboutLayout, 550, 700);
			URL url = this.getClass().getResource("style.css");
			scene.getStylesheets().add(url.toExternalForm());
			aboutStage.setScene(scene);
			aboutStage.setTitle("About GradeTracker");
			aboutStage.setResizable(false);
			aboutStage.initModality(Modality.APPLICATION_MODAL);
			aboutStage.show();
	});
		aboutBox.getChildren().add(aboutBtn);
		return aboutBox;
	}
}
			
