package com.simulator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate kafkaTemplate;

    @Value("${topic}")
    private String topic;

    @Value("${tcore_id}")
    private String tcore_id;

    @Value("${file_path}")
    private String file_path;

    @Scheduled(initialDelay = 0, fixedDelayString = "${scheduled_cron}") // 60000 : 60초
    public void produceMessage() {

        // 현재 timestamp
        Calendar c = Calendar.getInstance();
        Long time = (c.getTimeInMillis());
    
        // tcore_id를 application에서 "," 기준으로 추출
        String[] tcoreIdArr = tcore_id.split(",");
        List<String> tcoreIdList = new ArrayList<>();
        for (int i = 0; i < tcoreIdArr.length; i++) {
            tcoreIdList.add(tcoreIdArr[i]);
        }

        // conf 파일 읽어오기
        File dir = new File(file_path);
        File files[] = dir.listFiles();

        List<JSONObject> jsonList = new ArrayList<JSONObject>();

        for (int i = 0; i < files.length; i++) {
            JSONParser parser = new JSONParser();
            try {
                FileReader reader = new FileReader(files[i]);
                Object obj = parser.parse(reader);
                JSONObject jsonObject = (JSONObject) obj;
                reader.close();
                // conf 파일 안에 있는 json 파일들
                jsonList.add(jsonObject);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        log.info("Produce Message - BEGIN");
        // tcore_id 개수만큼 반복
        for(int i = 0; i < tcoreIdList.size(); i++) {
            // conf안에 있는 json파일 개수만큼 반복
            for(int j = 0; j < jsonList.size(); j++) {
                HashMap<String,Object> obj = (HashMap<String, Object>) jsonList.get(j).get("tags");
                // tcore_id와 timestamp 값을 변경해준다
                if (obj != null && obj.get("tcore_id") != null) {
                    obj.put("tcore_id", tcoreIdList.get(i));
                    jsonList.get(j).put("tags", obj);
                }
                if (jsonList.get(j).get("timestamp") != null){jsonList.get(j).put("timestamp", time);}

                String message = jsonList.get(j).toJSONString();
                // 원하는 topic으로 msg를 kafka producer
                // 여기서 topic은 application.properties에서 설정 가능
                ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate.send(topic, message);
                listenableFuture.addCallback(new ListenableFutureCallback<Object>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("ERROR Kafka error happend", ex);
                    }
        
                    @Override
                    public void onSuccess(Object result) {
                        log.info("SUCCESS!! This is the reulst: {}", result);
                    }
                });
        
            }
        }
        log.info("Produce Message - END");
    }
}
