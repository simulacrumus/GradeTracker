package gt.custom;

import javafx.util.converter.DoubleStringConverter;

public class CustomDoubleStringConverter extends DoubleStringConverter{
	@Override
	public Double fromString(String value) {
        try { 
        	return super.fromString(value);
        } catch(NumberFormatException e) {
        	return Double.NaN;
        }
	}
	@Override
	public String toString(Double value) {
		if(value == null || Double.isNaN(value) ) {
			return "-"; // How we present empty values in the table
		}
		return super.toString(value);
	}
}
