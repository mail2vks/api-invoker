package org.vivek.api.invoker.service;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by vivek on 17/09/16.
 */
@Builder
@Data
public class ApiExecutionContext {

    URI uri;
    HttpMethod httpMethod;
    HttpHeaders httpHeaders;
    LinkedList<Map<String, String>> payload;
}
