package gt.custom;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

public class CustomException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	private String displayError;
	
	public CustomException() { this("Please try again", "Bad data entered"); }
	public CustomException(String s, String a) {
		super(s);
		setDisplayError(a);
	}

	public String getDisplayError() { return displayError; }
	public void setDisplayError(String displayError) { this.displayError = displayError; }

	public Optional<ButtonType> DisplayErrMessage() {
		Alert errorAlert = new Alert(AlertType.ERROR);
		errorAlert.setTitle("Error!");
		errorAlert.setHeaderText(getDisplayError());
		errorAlert.setContentText(getMessage());
		errorAlert.getDialogPane().backgroundProperty().set(new Background(new BackgroundFill(Color.web("89a7a9"), null, null)));
		return errorAlert.showAndWait();
	}

}
