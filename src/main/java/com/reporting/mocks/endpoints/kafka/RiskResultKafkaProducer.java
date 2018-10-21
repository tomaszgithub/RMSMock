package com.reporting.mocks.endpoints.kafka;

import com.google.gson.Gson;
import com.reporting.mocks.configuration.ApplicationConfig;
import com.reporting.mocks.model.RiskResult;
import com.reporting.mocks.model.risks.Risk;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

public class RiskResultKafkaProducer {
    private String BOOTSTRAPSERVER = null;
    private String TOPIC = null;
    private Properties kafkaProperties = null;
    private Producer producer = null;

    public RiskResultKafkaProducer(ApplicationConfig appConfig) {
        this.TOPIC = appConfig.getIntradayRiskSetTopic();
        this.BOOTSTRAPSERVER = appConfig.getKafkaServer();

        this.kafkaProperties = new Properties();

        this.kafkaProperties.put("bootstrap.servers", this.BOOTSTRAPSERVER);
        this.kafkaProperties.put("key.serializer", "com.reporting.kafka.serialization.UUIDSerializer");
        this.kafkaProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        if (this.TOPIC != null && !this.TOPIC.isEmpty())
            this.producer = new KafkaProducer<UUID,String>(this.kafkaProperties);
    }

    public void send(RiskResult riskResult) {
        if (this.producer != null) {
            UUID riskResultID = riskResult.getRiskRunId().getId();
            Gson gson = new Gson();
            /*ProducerRecord<UUID, String> record = new ProducerRecord<>(this.TOPIC, riskResult.getRiskRunId().getId(), gson.toJson(riskResult));
            try {

                this.producer.send(record).get();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            int coutn = riskResult.getResults().size();
            for (Risk risk:riskResult.getResults()) {

                ProducerRecord<UUID, String> record = new ProducerRecord<>(this.TOPIC, riskResultID, gson.toJson(risk));
                try {

                    this.producer.send(record).get();
                    System.out.println("all " + coutn+ " RiskResultKafkaProducer send to topic" + this.TOPIC+ " " +record.value());
                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        } else{
            System.out.println("RiskResultKafkaProducer not send no producer topic");
        }
    }

}
