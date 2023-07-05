package com.mb.example;

import com.mb.example.state.MaxClosingPriceAggregatorProcessKeyState;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.queryablestate.client.QueryableStateClient;

import java.util.concurrent.CompletableFuture;

public class QueryStateClient {

    public static void main(String[] args) throws Exception {
        QueryableStateClient client = new QueryableStateClient("127.0.0.1", 9069);

// the state descriptor of the state to be fetched.
        ValueStateDescriptor<Tuple2<Long, Long>> descriptor =
                new ValueStateDescriptor<>(
                        "average",
                        TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {}));
        long key = 2l;
        //  http://localhost:8086/jobs
        //  http://localhost:8086/jobs/overview
        CompletableFuture<ValueState<Tuple2<Long, Long>>> resultFuture =
                client.getKvState(JobID.fromHexString("3a430f6aa092a3ca38b221502074aab3"), "query-name", key, BasicTypeInfo.LONG_TYPE_INFO, descriptor);
//
//// now handle the returned value
        resultFuture.thenAccept(response -> {
            try {
                //Tuple2<Long, Long> res = response.get();
                System.out.println("Inside : " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
