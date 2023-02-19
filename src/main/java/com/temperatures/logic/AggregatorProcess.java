package com.temperatures.logic;

// Begin imports

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

import com.temperatures.cargo.ParsedRecord;
import com.temperatures.cargo.Result;

import com.temperatures.key.ParsedRecordsKey;
import com.temperatures.state.AggregatorProcessKeyState;
import com.temperatures.state.AggregatorProcessCheckpointItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// End imports

public class AggregatorProcess extends KeyedProcessFunction<ParsedRecordsKey, ParsedRecord, Result>  {

// Begin declarations

	private static final Logger LOG = LoggerFactory.getLogger(AggregatorProcess.class);

	private static final long serialVersionUID = 1L;

	private ValueStateDescriptor<AggregatorProcessKeyState> stateDescriptor;
	private ValueState<AggregatorProcessKeyState> 			state;
// End declarations
	
	public AggregatorProcess() {
		super();
	}
	
	@Override
	public void open(Configuration config) throws Exception {

		// Begin open logic

		super.open(config);
		
		ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
		
//		int max = parameters.getInt("Aggregator.max", 30);


		stateDescriptor = new ValueStateDescriptor<AggregatorProcessKeyState>(
				"AggregatorProcessKeyState", 
				TypeInformation.of(AggregatorProcessKeyState.class));
		state = getRuntimeContext().getState(stateDescriptor);

		// End open logic
		
	}


	@Override
	public void processElement(ParsedRecord value,
			KeyedProcessFunction<ParsedRecordsKey, ParsedRecord, Result>.Context context, Collector<Result> collector)
			throws Exception {

		// Emit objects using:
		//
		//     collector.collect( obj );

		// Begin process logic
		if (value.getRegion().equalsIgnoreCase("Region")) {
			// skip this entity
			return;
		}

		AggregatorProcessKeyState item = state.value();

		if (item == null) {
			Long timer = context.timerService().currentProcessingTime() + 10*10l;
			context.timerService().registerProcessingTimeTimer(timer);
			item = new AggregatorProcessKeyState();
			item.setTimer(timer);
			item.setKey(value.getKey());
		}
		item.setCount(item.getCount() + 1);
		double newSumOfAvgTemp = (item.getAvgTemp() + value.getAvgTemperature());
		item.setAvgTemp(newSumOfAvgTemp);
		state.update(item);
		//collector.collect(new Result(item.getCount(), item.getAvgTemp()/ item.getCount(), item.getKey()));
		// End process logic
		
	}

	@Override
	public void onTimer(long timestamp, KeyedProcessFunction<ParsedRecordsKey, ParsedRecord, Result>.OnTimerContext ctx, Collector<Result> out) throws Exception {
		super.onTimer(timestamp, ctx, out);
		System.out.println("OnTimer");
		AggregatorProcessKeyState item = state.value();
		out.collect(new Result(item.getCount(), item.getAvgTemp()/ item.getCount(), item.getKey()));
		//delete the timer
		item.setTimer(null);
		Long timer = ctx.timerService().currentProcessingTime() + 10*10l;
		ctx.timerService().registerProcessingTimeTimer(timer);
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
