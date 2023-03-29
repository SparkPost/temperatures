package com.temperatures;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.temperatures.cargo.*;
import com.temperatures.key.*;
import com.temperatures.logic.*;

import org.apache.flink.streaming.api.functions.source.FileProcessingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;

public class StreamingJob {

	private static final Logger LOG = LoggerFactory.getLogger(StreamingJob.class);

	public static void main(String[] args) {

		LOG.info("Beginning StreamingJob");

		try {
			ParameterTool parameter = null;
			
			Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();
			ArrayList<Properties> groups = new ArrayList<Properties>(applicationProperties.values());
			
			InputStream is = StreamingJob.class.getResourceAsStream("StreamingJob.properties");
			Properties props = new Properties();
			props.load(is);
			is.close();

// Begin custom property logic

			groups.add(0, props);
			
// End custom property logic
				 
			is = union(groups);
			parameter = ParameterTool.fromPropertiesFile(is);

//			} else {
//				
//				if (args.length > 0) {
//					String propertiesFile = args[0];
//					try {
//						parameter = ParameterTool.fromPropertiesFile(propertiesFile);
//					} catch (Exception e) {
//						System.err.println("Error while reading property file "+propertiesFile+" : "+e.getLocalizedMessage());
//						return;
//					}
//				} else {
//					try {
//						parameter = ParameterTool.fromPropertiesFile(StreamingJob.class.getResourceAsStream("StreamingJob.properties"));
//					} catch (Exception e) {
//						System.err.println("Error while reading resource property file 'com/gp/StreamingJob.properties' : "+e.getLocalizedMessage());
//						return;
//					}
//				}
//
//			
//			for (String key: parameter.getUnrequestedParameters()) {
//				String value = String.valueOf(parameter.get(key));
//				LOG.error("parm: "+key+" ---> "+value);
//			}

//			StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();
//			see.setMaxParallelism(parameter.getInt("maxParallelism", 1000));
			// https://jedong.medium.com/flink-jvm-metrics-with-jmx-adb805f1003a
			org.apache.flink.configuration.Configuration conf = new org.apache.flink.configuration.Configuration();
			conf.setInteger(org.apache.flink.configuration.RestOptions.PORT, 8082);
			int defaultLocalParallelism = Runtime.getRuntime().availableProcessors();
			conf.setString("taskmanager.memory.network.max", "1gb");
			// default port 9249, make sure Prometheus docker image is running, metrics-reporter/prometheus/run.sh
			// https://nightlies.apache.org/flink/flink-docs-release-1.13/docs/deployment/metric_reporters/#prometheus
			conf.setString("metrics.reporter.prom.factory.class", "org.apache.flink.metrics.prometheus.PrometheusReporterFactory");


			StreamExecutionEnvironment see = StreamExecutionEnvironment.createLocalEnvironment(defaultLocalParallelism, conf);

			see.disableOperatorChaining();
			
			if (parameter.getBoolean("checkpoint.enabled", false)) {
				
				long interval = parameter.getLong("checkpoint.interval", 60000L);
				see.enableCheckpointing(interval);
				if (parameter.has("checkpoint.data.uri")) {
	 				String checkpointDataUri = parameter.get("checkpoint.data.uri");
 					see.setStateBackend((StateBackend)new FsStateBackend(checkpointDataUri));
				}
				
			}
			

			// FileReader source
			//String filePath =	"/Users/praghavan/development/ApacheFlink/project/my-flink-project/src/";
			String filePath =	"/Users/praghavan/test1/";
            //Define the text input format based on the directory
            TextInputFormat auditFormat = new TextInputFormat(
                    new org.apache.flink.core.fs.Path(filePath));

            //Create a Datastream based on the directory
            DataStream<String> streamFilesAsString
                        = see.readFile(auditFormat,
                            filePath,    //Director to monitor
                            FileProcessingMode.PROCESS_CONTINUOUSLY,
                            1000); //monitor interval

			SingleOutputStreamOperator<LineOfText> operator_LineOfTextParser= streamFilesAsString.process(new LineOfTextParserProcess());
			operator_LineOfTextParser.uid("LineOfTextStream");
			operator_LineOfTextParser.name("LineOfTextStream");

			DataStream<LineOfText> streamToParser = operator_LineOfTextParser;
			// Parser process
 			SingleOutputStreamOperator<ParsedRecord> operator_Parser = streamToParser.process(new ParserProcess());

			operator_Parser.uid("Parser");
			operator_Parser.name("Parser");
			operator_Parser.setParallelism(parameter.getInt("Parser.parallelism", 1));

			DataStream<ParsedRecord> streamToAggregator = operator_Parser;
			// Aggregator process
			KeyedStream<ParsedRecord, ParsedRecordsKey> keyedStreamToAggregator = streamToAggregator.keyBy(new ParsedRecordsKeySelector());
     		SingleOutputStreamOperator<Result> operator_Aggregator = keyedStreamToAggregator.process(new AggregatorProcess());


			operator_Aggregator.uid("Aggregator");
			operator_Aggregator.name("Aggregator");
			operator_Aggregator.setParallelism(parameter.getInt("Aggregator.parallelism", 1));


			// Reporter sink
			DataStream<Result> streamToReporter = operator_Aggregator;
			DataStreamSink<Result> sink_Reporter = streamToReporter.addSink(new ReporterSink());
			sink_Reporter.uid("Reporter");
			sink_Reporter.name("Reporter");
			sink_Reporter.setParallelism(parameter.getInt("Reporter.parallelism", 1));


			
			see.getConfig().setGlobalJobParameters(parameter);
			see.execute("temperatures");

		} catch (Exception e) {
		
			LOG.error("Error building topology in StreamingJob", e);
		
		}
		
	}
	
	private static InputStream union(ArrayList<Properties> groups) throws IOException {
		Properties all = new Properties();
		for (Properties p: groups) {
			for (Object key: p.keySet()) {
				Object value = p.get(key);
				all.put(key, value);
			}			
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (Object key: all.keySet()) {
			Object value = all.get(key);
			String line = key.toString() + ": " + value + "\n"; 
			baos.write(line.getBytes());
		}			
		
		return new ByteArrayInputStream(baos.toByteArray());
	}
	
}
