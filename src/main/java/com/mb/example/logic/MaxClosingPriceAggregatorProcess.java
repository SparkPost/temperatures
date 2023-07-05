package com.mb.example.logic;

import com.mb.example.state.MaxClosingPriceAggregatorProcessKeyState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * ref: https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/dev/datastream/operators/process_function/
 */
public  class MaxClosingPriceAggregatorProcess extends
        KeyedProcessFunction<String, Tuple3<String, String, Double>, Tuple3<String, String, Double>> {

    private ValueState<MaxClosingPriceAggregatorProcessKeyState> state;
    private ValueStateDescriptor<MaxClosingPriceAggregatorProcessKeyState> stateDescriptor;

    public MaxClosingPriceAggregatorProcess() {
        super();
    }

    @Override
    public void open(Configuration config) throws Exception {
        super.open(config);

        stateDescriptor =
                new ValueStateDescriptor<MaxClosingPriceAggregatorProcessKeyState>(
                        "MaxClosingPriceAggregatorProcessKeyState",
                        TypeInformation.of(MaxClosingPriceAggregatorProcessKeyState.class));
        stateDescriptor.setQueryable("query-name");
        state = getRuntimeContext().getState(stateDescriptor);
    }
    @Override
    public void processElement(Tuple3<String, String, Double> input,
                               Context context,
                               Collector<Tuple3<String, String, Double>> collector) throws Exception {
        MaxClosingPriceAggregatorProcessKeyState item = state.value();

        if (item == null) {
//            item.update(Tuple2.of(input.f1, input.f2));
            Long timer = context.timerService().currentProcessingTime() + 1*1000L;
//			Long timer = System.currentTimeMillis() + 1*1000L;
            context.timerService().registerProcessingTimeTimer(timer);
            System.out.println("Timer Registered");
            item = new MaxClosingPriceAggregatorProcessKeyState();
            item.setTimer(timer);
            item.setKey(input.f0);
        } else {
//            System.out.println("Timer  Not Registered");
        }
        if (input.f2 > item.getMaxClosingPrice()) {
            item.setCount(item.getCount()+1);
            item.setMaxClosingPrice(input.f2);
            item.setMaxClosingDateStr(input.f1);
            state.update(item);
        }
    }


    @Override
    public void onTimer(long timestamp,
                        KeyedProcessFunction<String, Tuple3<String, String, Double>, Tuple3<String, String, Double>>.OnTimerContext context,
                        Collector<Tuple3<String, String, Double>> out) throws Exception {
        super.onTimer(timestamp, context, out);
        MaxClosingPriceAggregatorProcessKeyState item = state.value();
        if (item != null) {
            out.collect(new Tuple3<>(item.getKey(), item.getMaxClosingDateStr(), item.getMaxClosingPrice()));
            System.out.println(this.getClass().getName() + ": " + "publish" );
            state.clear();
        }
    }



    @Override
    public void close() throws Exception {
        // Begin close logic
        super.close();
        // End close logic

    }

}