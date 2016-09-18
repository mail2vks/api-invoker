package org.vivek.api.invoker;

import org.vivek.api.invoker.config.ApiData;
import org.vivek.api.invoker.config.Config;
import org.vivek.api.invoker.service.APIInvocationService;
import org.vivek.api.invoker.service.ApiExecutionContext;
import org.vivek.api.invoker.service.HeaderProviderService;
import org.vivek.api.invoker.service.PayloadBuilderService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by vivek on 17/09/16.
 */
@Component
public class CommandRunner implements CommandLineRunner {

    @Resource
    private APIInvocationService apiInvocationService;

    @Resource
    private Config config;

    @Resource
    private HeaderProviderService headerProviderService;

    @Resource
    private PayloadBuilderService payloadBuilderService;

    @Override
    public void run(String... args) throws Exception {
        final AtomicInteger index = new AtomicInteger(0);
        final Map<Integer, ApiData> supportedAPIs = config.getApidata().stream().map(apiData -> new ImmutablePair<>(index.incrementAndGet(), apiData))
                .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));

        Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name());
        printSupportedAPI(supportedAPIs);

        int option = scanner.nextInt();
        ApiData selectedAPI = supportedAPIs.get(option);
        if (selectedAPI == null) {
            System.out.println("Could not find supporting api. Exiting");
        } else {
            System.out.println("Selected one is " + selectedAPI + " for " + config.getBaseURL());
            ApiExecutionContext apiExecutionContext = ApiExecutionContext.builder()
                    .uri(URI.create(config.getBaseURL() + selectedAPI.getEndpoint()))
                    .httpMethod(HttpMethod.valueOf(selectedAPI.getHttpMethod()))
                    .httpHeaders(headerProviderService.getHeaders(selectedAPI.getHeader()))
                    .payload(payloadBuilderService.getPayLoad(selectedAPI))
                    .build();
            apiInvocationService.execute(selectedAPI, apiExecutionContext);
        }
    }

    private void printSupportedAPI(Map<Integer, ApiData> supportedAPIs) {
        supportedAPIs.forEach((k, v) -> System.out.println(k + ". " + v.getName()));
    }
}
