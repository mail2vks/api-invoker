package org.vivek.api.invoker.config;

import lombok.Data;

@Data
public class ApiData {

    private String name;
    private String endpoint;
    private String header;
    private String httpMethod;
    private String csvHeaders;

    public String[] getCsvHeadersAsArray() {
        return csvHeaders != null ? csvHeaders.split(",") : new String[]{};
    }
}
