package gt.custom;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javafx.util.converter.LocalDateStringConverter;

public class CustomLocalDateStringConverter extends LocalDateStringConverter{
	@Override
	public LocalDate fromString(String value) {
        try { 
        	return super.fromString(value);
        } catch(DateTimeParseException e) {
        	return null;
        }
	}	
	@Override
	public String toString(LocalDate date) {
		if(date == null ) {
			return "-"; // How we present empty values in the table
		}
		return super.toString(date);
	}

}
