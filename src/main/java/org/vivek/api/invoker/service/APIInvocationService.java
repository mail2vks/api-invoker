package org.vivek.api.invoker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.vivek.api.invoker.config.ApiData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.vivek.api.invoker.util.Utils.objectMapper;

/**
 * Created by vivek on 16/09/16.
 */
@Service
public class APIInvocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    private void invokeAPI(ApiData selectedAPI, ApiExecutionContext apiExecutionContext) {
        Path path = Paths.get(selectedAPI.getName() + "_response_" + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + ".csv");
        RateLimiter rateLimiter = RateLimiter.create(1);
        switch (apiExecutionContext.getHttpMethod()) {
            case GET:
                ResponseEntity<String> rv = handleGET(apiExecutionContext);
                List<Pair<Map<String, String>, ResponseEntity<String>>> rawDataToWrite = new ArrayList<>();
                rawDataToWrite.add(new ImmutablePair<>(Collections.EMPTY_MAP, rv));
                writeResponse(selectedAPI, path, apiExecutionContext, rawDataToWrite);
                break;
            case POST:
                BlockingQueue<Pair<Map<String, String>, ResponseEntity<String>>> blockingQueue = new ArrayBlockingQueue<>(10);
                apiExecutionContext.getPayload().forEach(payload -> {
                    HttpEntity<String> httpEntity = null;
                    try {
                        httpEntity = new HttpEntity<>(objectMapper.writeValueAsString(payload), apiExecutionContext.getHttpHeaders());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    rateLimiter.acquire();
                    ResponseEntity<String> postReponse = handlePost(apiExecutionContext, httpEntity);
                    if (blockingQueue.remainingCapacity() == 0) {
                        List<Pair<Map<String, String>, ResponseEntity<String>>> rawData = new ArrayList<>();
                        blockingQueue.drainTo(rawData);
                        writeResponse(selectedAPI, path, apiExecutionContext, rawData);
                    } else {
                        blockingQueue.add(new ImmutablePair<>(payload, postReponse));
                    }
                });
                if (!blockingQueue.isEmpty()) {
                    List<Pair<Map<String, String>, ResponseEntity<String>>> rawData = new ArrayList<>();
                    blockingQueue.drainTo(rawData);
                    writeResponse(selectedAPI, path, apiExecutionContext, rawData);
                }

                break;
            default:
                throw new UnsupportedOperationException("Method " + apiExecutionContext.getHttpMethod() + " not supported");
        }
    }

    private void writeResponse(ApiData selectedAPI, Path path, ApiExecutionContext apiExecutionContext, List<Pair<Map<String, String>, ResponseEntity<String>>> rawData) {
        System.out.println("***************** Dumping Response in File + " + path.toString() + "**********************");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            CsvMapWriter csvMapWriter = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);
            rawData.forEach(pair -> {
                if (MediaType.APPLICATION_JSON_UTF8.equals(pair.getRight().getHeaders().getContentType())
                        || MediaType.APPLICATION_JSON.equals(pair.getRight().getHeaders().getContentType())) {
                    System.out.println("Got JSON Response. Dumping after flattening");
                    try {
                        Map<String, Object> dataToWrite = new HashMap<>();
                        dataToWrite.putAll(pair.getLeft());
                        String[] headersToWrite = selectedAPI.getCsvHeadersAsArray();
                        String body = pair.getRight().getBody();
                        System.out.println(body);
                        HashMap<String, Object> jsonResponseAsMap = objectMapper.readValue(body, new TypeReference<HashMap<String, Object>>() {
                        });
                        HashMap<String, Object> finalMap = new HashMap<>();
                        jsonResponseAsMap.entrySet().stream().flatMap(APIInvocationService::flatten)
                                .collect(Collectors.toList()).forEach(entry -> {
                            finalMap.putIfAbsent(entry.getKey(), entry.getValue());
                        });
                        dataToWrite.putAll(finalMap);
                        if (csvMapWriter.getLineNumber() == 0)
                            csvMapWriter.writeHeader(ArrayUtils.addAll(headersToWrite, (String[]) finalMap.keySet().toArray(new String[]{})));
                        csvMapWriter.write(dataToWrite, ArrayUtils.addAll(headersToWrite, (String[]) finalMap.keySet().toArray(new String[]{})));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            csvMapWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @HystrixCommand
    private ResponseEntity<String> handlePost(ApiExecutionContext apiExecutionContext, HttpEntity<String> httpEntity) {
        return restTemplate.postForEntity(apiExecutionContext.getUri(), httpEntity, String.class);
    }

    @HystrixCommand
    private ResponseEntity<String> handleGET(ApiExecutionContext apiExecutionContext) {
        return restTemplate.getForEntity(apiExecutionContext.getUri(), String.class);
    }

    public void execute(ApiData selectedAPI, ApiExecutionContext apiExecutionContext) {
        invokeAPI(selectedAPI, apiExecutionContext);
    }

    public static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof Map<?, ?>) {
            return ((Map<String, Object>) entry.getValue()).entrySet().stream().flatMap(APIInvocationService::flatten);
        }
        return Stream.of(entry);
    }
}
