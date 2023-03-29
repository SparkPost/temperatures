package com.temperatures.logic;

// Begin imports

import com.temperatures.helper.Helper;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.MeterView;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import com.temperatures.cargo.LineOfText;
import com.temperatures.cargo.ParsedRecord;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// End imports

public class ParserProcess extends ProcessFunction<LineOfText, ParsedRecord>  {

// Begin declarations
	private transient Meter processRate;
	private static final Logger LOG = LoggerFactory.getLogger(ParserProcess.class);

	private static final long serialVersionUID = 1L;
// End declarations
	
	public ParserProcess() {
		super();
	}
	
	@Override
	public void open(Configuration config) throws Exception {

		// Begin open logic

		super.open(config);
		processRate = getRuntimeContext().getMetricGroup()
				.addGroup("kinesisanalytics")
				.addGroup("Program", "Temperature")
				.meter("rate", new MeterView(60));
		
		ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
		
//		int max = parameters.getInt("Parser.max", 30);


		// End open logic
		
	}


	@Override
	public void processElement(LineOfText value,
			ProcessFunction<LineOfText, ParsedRecord>.Context context, Collector<ParsedRecord> collector)
			throws Exception {

		// Emit objects using:
		//
		//     collector.collect( obj );

		// Begin process logic
		ParsedRecord parsedRecord = Helper.parseRecord(value);
		System.out.println(this.getClass().getName() + " : " + parsedRecord);
		collector.collect(parsedRecord);
		processRate.markEvent();
		// End process logic
		
	}


	@Override
	public void close() throws Exception {

		// Begin close logic

		super.close();

		// End close logic

	}

	// Begin custom methods
	
	
	// End custom methods
	
}
