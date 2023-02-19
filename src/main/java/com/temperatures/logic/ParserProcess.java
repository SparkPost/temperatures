package com.temperatures.logic;

// Begin importsß
import com.temperatures.helper.Helper;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import com.temperatures.cargo.LineOfText;
import com.temperatures.cargo.ParsedRecord;



import com.temperatures.state.ParserProcessCheckpointItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// End imports

public class ParserProcess extends ProcessFunction<LineOfText, ParsedRecord>  {

// Begin declarations

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
		System.out.println(parsedRecord);
		collector.collect(parsedRecord);
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
