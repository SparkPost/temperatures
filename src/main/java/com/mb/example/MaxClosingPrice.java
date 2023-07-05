package com.mb.example;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.mb.example.logic.MaxClosingPriceAggregatorProcess;
import com.temperatures.StreamingJob;
import org.apache.flink.api.common.functions.*;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.FileProcessingMode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class MaxClosingPrice {

    public static void main(String[] args) throws Exception {

        //
//        final StreamExecutionEnvironment env =
//                StreamExecutionEnvironment.getExecutionEnvironment();
        Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
        ArrayList<Properties> groups = new ArrayList<Properties>(applicationProperties.values());
        Properties props = new Properties();
        org.apache.flink.configuration.Configuration conf = new org.apache.flink.configuration.Configuration();
        conf.setInteger(org.apache.flink.configuration.RestOptions.PORT, 8085);
        int defaultLocalParallelism = Runtime.getRuntime().availableProcessors();
        conf.setString("taskmanager.memory.network.max", "1gb");
        conf.setBoolean("queryable-state.enable", true);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(defaultLocalParallelism, conf);

        //



//        DataStream<String> streamFilesAsString =
//                env.readTextFile("src/main/resources/MSFT_2020.csv");
         // FileReader source
        String filePath =	"/Users/paramraghavan/dev/mb/temperatures/src/stockprice/";
        //Define the text input format based on the directory
        TextInputFormat csvFormat = new TextInputFormat(
                new org.apache.flink.core.fs.Path(filePath));

        //Create a Datastream based on the directory
        DataStream<String> streamFilesAsString
                = env.readFile(csvFormat,
                filePath,    //Director to monitor
                FileProcessingMode.PROCESS_CONTINUOUSLY,
                1000); //monitor interval


        SingleOutputStreamOperator<String> operator_filter = streamFilesAsString.filter(
                (FilterFunction<String>) line ->
                        !line.contains("Date,Open,High,Low,Close,Adj Close,Volume,Name"));

        DataStream<String> streamToStockRecords = operator_filter;

//        DataStream<String> stockRecords = streamFilesAsString.filter(
//                (FilterFunction<String>) line ->
//                        !line.contains("Date,Open,High,Low,Close,Adj Close,Volume,Name"));

        SingleOutputStreamOperator<Tuple3<String, String, Double>> operator_closingPrices = streamToStockRecords.map(
                new MapFunction<String, Tuple3<String, String, Double>>() {
                    @Override
                    public Tuple3<String, String, Double> map(String s) throws Exception {
                        String[] tokens = s.split(",");

                        return new Tuple3<>(tokens[7], tokens[0], Double.parseDouble(tokens[5]));
                    }
                });
        operator_closingPrices.setParallelism(1);

        DataStream<Tuple3<String, String, Double>> streamToClosingPrices = operator_closingPrices;

        // key is string
        KeyedStream<Tuple3<String, String, Double>, String>
                keyedStreamToClosingPriceAgg = streamToClosingPrices.keyBy(value -> value.f0);

        SingleOutputStreamOperator<Tuple3<String, String, Double>> operator_MaxClosingPriceAgg =
                keyedStreamToClosingPriceAgg.process( new MaxClosingPriceAggregatorProcess());
        operator_MaxClosingPriceAgg.setParallelism(1);
        operator_MaxClosingPriceAgg.print();
        env.execute();

        JobGraph jobGraph = env.getStreamGraph().getJobGraph();
        System.out.println("[info] Job ID: " + jobGraph.getJobID());
    }

}