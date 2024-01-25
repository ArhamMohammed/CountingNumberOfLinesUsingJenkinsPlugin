package io.jenkins.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountingLinesClient {

    private static final Logger logger = LoggerFactory.getLogger(CountingLinesClient.class);

    private CountingLinesClient() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static CompletableFuture<ProjectStats> makeAsyncApiCallForCounting(
            HttpURLConnection connectionForCountingLines) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int response = connectionForCountingLines.getResponseCode();
                logger.info("Response Code for counting: {}", response);
                ProjectStats pj = null;
                if (response == HttpURLConnection.HTTP_OK) {
                    String jsonResponse;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            connectionForCountingLines.getInputStream(), StandardCharsets.UTF_8))) {
                        jsonResponse = IOUtils.toString(reader);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    pj = objectMapper.readValue(jsonResponse, ProjectStats.class);
                }
                return pj;
            } catch (MalformedURLException e) {
                logger.info("Protocol is missing from the URL {}", e.getLocalizedMessage());
            } catch (Exception e) {
                logger.info("The exception occurred is :{}", e.getLocalizedMessage());
            }
            return null;
        });
    }
}
