package org.vivek.api.invoker.service;

import org.vivek.api.invoker.config.ApiData;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by vivek on 17/09/16.
 */
@Service
public class PayloadBuilderService {

    public LinkedList<Map<String, String>> getPayLoad(ApiData selectedAPI) {
        if (HttpMethod.POST.equals(HttpMethod.valueOf(selectedAPI.getHttpMethod()))) {
            return preparePayload(selectedAPI);
        }
        return new LinkedList<>();
    }

    private LinkedList<Map<String, String>> preparePayload(ApiData selectedAPI) {
        String fileNameToSearch = selectedAPI.getName() + ".csv";
        LinkedList<Map<String, String>> payloadData = new LinkedList<>();
        try (BufferedReader fileReader = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource(fileNameToSearch).toURI()));
             CsvMapReader csvMapReader = new CsvMapReader(fileReader, CsvPreference.STANDARD_PREFERENCE)) {
            csvMapReader.getHeader(true);
            Map<String, String> rowData;
            while ((rowData = csvMapReader.read(selectedAPI.getCsvHeadersAsArray())) != null) {
                payloadData.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return payloadData;
    }
}
