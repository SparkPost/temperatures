package com.temperatures.logic;

// Begin imports

import java.io.Serializable;
import java.util.Date;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import com.temperatures.cargo.Result;
import com.temperatures.state.ReporterSinkCheckpointItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// End imports

public class ReporterSink extends RichSinkFunction<Result> implements Serializable  {

	// Begin declarations

	private static final Logger LOG = LoggerFactory.getLogger(ReporterSink.class);

	private static final long serialVersionUID = 1L;
	private long start, end;

	// End declarations
	
	public ReporterSink() throws Exception {
		super();
	}
	
	@Override
	public void open(Configuration config) throws Exception {

		// Begin open logic
		start = System.currentTimeMillis();
		super.open(config);
		
		ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
		
//		int max = parameters.getInt("Reporter.max", 30);

		// End open logic
		
	}
	
	@Override
	public void invoke(Result value, Context context) throws Exception {

		// Begin invoke logic

		System.out.println(value.toString());

		// End invoke logic
		
	}

	@Override
	public void close() throws Exception {

		// Begin close logic
		end = System.currentTimeMillis();
  		System.out.print("Execution time is " + (end - start) / 1000d + " seconds");
		super.close();

		// End close logic

	}


	// Begin custom methods
	
	
	// End custom methods
	
}
