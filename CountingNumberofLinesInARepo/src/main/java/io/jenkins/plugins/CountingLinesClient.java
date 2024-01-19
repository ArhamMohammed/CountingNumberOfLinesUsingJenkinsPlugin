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
    private static Logger logger = LoggerFactory.getLogger(CountingLinesClient.class);

    public static CompletableFuture<ProjectStats> makeAsyncApiCallForCounting(
            HttpURLConnection connectionForCountingLines) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //                if (urlPassed.equals(StringUtils.EMPTY)) {
                //                    //                    logger.debug("The protocol is empty. Please check");
                //                    //                    logger.debug("Protocol: "+urlPassed);
                //                    System.out.println("The protocol is empty. Please check");
                //                    System.out.println("Protocol: " + urlPassed);
                //                    return null;
                //                } else if (root.equals(StringUtils.EMPTY)) {
                //                    //                    logger.debug("The root is empty. Please check");
                //                    //                    logger.debug("Root: "+root);
                //                    System.out.println("The root is empty. Please check");
                //                    System.out.println("Root: " + root);
                //                    return null;
                //                } else if (key.equals(StringUtils.EMPTY)) {
                //                    logger.debug("Please check the key provided, as it is EMPTY");
                //                    System.out.println("Please check the key provided, as it is EMPTY");
                //                    return null;
                //                }
                int response = connectionForCountingLines.getResponseCode();
                //                logger.debug("Response Code for counting: " + response);
                //                System.out.println("Response Code for counting: " + response);
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
                //                else if (response == 404 || response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                //                    logger.debug("Please check the complete URL provided ");
                //                    logger.debug("Protocol: " + urlPassed);
                //                    logger.debug("Root: " + root);
                //                    System.out.println(
                //                            "The resource which you have provided is unavailable. Please check the URL
                // provided ");
                //                    System.out.println("Protocol: " + urlPassed);
                //                    System.out.println("Root: " + root);
                //                } else {
                //                    logger.debug("Error occurred " + url);
                //                    System.out.println("Error occurred " + url);
                //                }
                //                Thread.sleep(delay);
                //                System.out.println(pj.getNumberOfLines());
                System.out.println("The value after counting the lines = " + pj.getNumberOfLines());
                return pj;
            } catch (MalformedURLException e) {
                System.out.println("Protocol is missing from the URL ");
                System.out.println(e.getLocalizedMessage());
            } catch (Exception e) {
                System.out.println("The exception occurred is :" + e);
            }
            return null;
        });
    }
}
