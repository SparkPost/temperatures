package com.temperatures.helper;

import com.temperatures.cargo.LineOfText;
import com.temperatures.cargo.ParsedRecord;

public class Helper {
    // "Region,Country,State,City,Month,Day,Year,AvgTemperature";
    public static ParsedRecord parseRecord(LineOfText lText) {
        String[] tokens = lText.getText().split(",");
        return
                new ParsedRecord(tokens[0],tokens[1], tokens[2], tokens[3],
                        Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]),
                        Double.parseDouble(tokens[7]));
    }
}
