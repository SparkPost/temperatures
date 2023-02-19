package com.temperatures.logic;

// Begin imports

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import com.temperatures.cargo.LineOfText;
import com.temperatures.state.FileReaderSourceCheckpointItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;

// End imports

public class FileReaderSource extends RichParallelSourceFunction<LineOfText> implements ParallelSourceFunction<LineOfText>  {

// Begin declarations

	private static final Logger LOG = LoggerFactory.getLogger(FileReaderSource.class);

	private static final long serialVersionUID = 1L;
	
	private boolean running = false;

	private String filePathWithName =
			"/Users/praghavan/development/ApacheFlink/project/my-flink-project/src/sample.csv";
	private FileReader fileReader = null;
	private BufferedReader buffer;

// End declarations

	public FileReaderSource() throws Exception {
		super();
	}

	@Override
	public void cancel() {
		running = false;
	}

	@Override
	public void open(Configuration config) throws Exception {
		
		// Begin open logic

		super.open(config);
		
		ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
		
//		int max = parameters.getInt("FileReader.max", 30);
		fileReader
                = new FileReader(filePathWithName);
		buffer
                = new BufferedReader(fileReader);
		// End open logic
		
	}

	@Override
	public void run(SourceContext<LineOfText> context) throws Exception {

		// Begin run logic

		running = true;
		while (running) {
			synchronized (context.getCheckpointLock()) {
				// Read a line from the file and emit it using an instance of LineOfText
				String line = buffer.readLine();
				if(line == null) {
					try {Thread.sleep(1000);
						running = false;} catch(Exception ex ) {;}
				} else {
					if (!line.contains("Region")) {
						System.out.println(line);
						context.collect(new LineOfText(line));
					}
				}
			}
		}
		// End run logic
		
	}

	@Override
	public void close() throws Exception {
		super.close();
		// Begin close logic
		try {
			if (buffer != null) { buffer.close();}
			if (fileReader != null) { fileReader.close();}
	 	} catch (Throwable t) {  }

		// End close logic
	}


	// Begin custom methods
	
	
	// End custom methods
	
}
