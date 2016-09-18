package org.vivek.api.invoker.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Created by vivek on 17/09/16.
 */
@Service
public class HeaderProviderService {

    public HttpHeaders getHeaders(String headerData) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (StringUtils.isNotBlank(headerData)) {
            Arrays.stream(headerData.split(","))
                    .filter(StringUtils::isNotEmpty)
                    .map(header -> getHttpHeader(header))
                    .forEach(pair -> httpHeaders.add(pair.getLeft(), pair.getRight()));
        }
        return httpHeaders;
    }

    private Pair<String, String> getHttpHeader(String header) {
        Pair<String, String> rv = null;
        switch (header) {
            case "json":
                rv = new ImmutablePair<>(HttpHeaders.CONTENT_TYPE, "application/json");
                break;
            default:
                throw new UnsupportedOperationException("Unknown header " + header);
        }
        return rv;
    }
}
