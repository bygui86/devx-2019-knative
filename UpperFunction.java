package com.example.springupper;

import io.cloudevents.CloudEvent;
import io.cloudevents.format.Wire;
import io.cloudevents.v03.AttributesImpl;
import io.cloudevents.v03.CloudEventBuilder;
import io.cloudevents.v03.http.Marshallers;
import io.cloudevents.v03.http.Unmarshallers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@RestController
public class UpperFunction {

    public static void main(String[] args) {
        SpringApplication.run(UpperFunction.class, args);
    }

    @RequestMapping
    public ResponseEntity<String> upper(@RequestHeader HttpHeaders headers, UriComponentsBuilder uriComponentsBuilder, @RequestBody String body) {
        Map<String, Object> reqHeaders = new HashMap<>(headers.toSingleValueMap());
        reqHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json");

        CloudEvent<AttributesImpl, Data> event =
                Unmarshallers.binary(Data.class)
                        .withHeaders(() -> reqHeaders)
                        .withPayload(() -> body)
                        .unmarshal();

        Data respData = event.getData().get();
        respData.message = respData.message.toUpperCase();

        CloudEvent<AttributesImpl, Data> respEvent =
                CloudEventBuilder.<Data>builder()
                        .withType("spring-upper")
                        .withSource(uriComponentsBuilder.build().toUri())
                        .withId(UUID.randomUUID().toString())
                        .withTime(ZonedDateTime.now())
                        .withDatacontenttype("application/json")
                        .withData(respData)
                        .build();

        Wire<String, String, String> wire = Marshallers.<Data>binary()
                .withEvent(() -> respEvent)
                .marshal();

        MultiValueMap<String, String> respHeaders = new LinkedMultiValueMap<>();
        respHeaders.setAll(wire.getHeaders());

        return new ResponseEntity<>(wire.getPayload().get(), respHeaders, HttpStatus.OK);
    }

    public static class Data {
        public String message;
    }

}
