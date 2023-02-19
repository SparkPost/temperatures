package com.temperatures.key;

// Begin imports

import com.temperatures.helper.Helper;
import org.apache.flink.api.java.functions.KeySelector;

import com.temperatures.cargo.ParsedRecord;

// End imports

public class ParsedRecordsKeySelector implements KeySelector<ParsedRecord, ParsedRecordsKey> {

	// Begin declarations

	private static final long serialVersionUID = 1L;

	// End declarations

	@Override
	public ParsedRecordsKey getKey(ParsedRecord value) throws Exception {

	// Begin selector logic

		// Derive and instance of ParsedRecordsKey as the key from value

		return Helper.parseRecordKey(value);
		
	// End selector logic
	
	}

}
