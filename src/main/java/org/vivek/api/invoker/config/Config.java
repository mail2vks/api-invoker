package org.vivek.api.invoker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by vivek on 16/09/16.
 */
@Component
@ConfigurationProperties
@Data
public class Config {

    private List<ApiData> apidata;

    private String baseURL;

}
