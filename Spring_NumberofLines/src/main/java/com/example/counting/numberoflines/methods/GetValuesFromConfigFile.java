package com.example.counting.numberoflines.methods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GetValuesFromConfigFile {
    private static final Logger logger = LoggerFactory.getLogger(GetValuesFromConfigFile.class);
    public static final String CONFIG_FILE = "config.properties";

    public String getReportTemplatePathFromConfig() {
        String key = "report.template.path";
        String defaultKey = "/stats.html";
        return getConfigValues(key,defaultKey);
    }

    public String getErrorMessageForFileReading() {
        String key = "error.reading.contents.from.file";
        String defaultKey = "Error reading contents from the file";
        return getConfigValues(key,defaultKey);
    }
    public String getErrorMessageForDirectoryReading() {
        String key = "error.reading.directory.contents";
        String defaultKey = "Checkout successful. Working copy is now at revision: {}";
        return getConfigValues(key,defaultKey);
    }
    public String getSuccessfulCheckoutMessage() {
        String key = "successful.checkout";
        String defaultKey = "Error getting directory content from the repository";
        return getConfigValues(key,defaultKey);
    }
    public String getUnsuccessfulCheckoutMessage() {
        String key = "unsuccessful.checkout";
        String defaultKey = "Checkout failed. The returned revision is not valid: {}";
        return getConfigValues(key,defaultKey);
    }
    public String getErrorMessageForCloningRepo() {
        String key = "error.cloning.repo";
        String defaultKey = "Something wrong while cloning the repo : {}";
        return getConfigValues(key,defaultKey);
    }
    public String getErrorMessageForGeneratingReport() {
        String key = "error.generating.report";
        String defaultKey = "Something wrong while generating the report : {}";
        return getConfigValues(key,defaultKey);
    }

    public String getConfigValues(String key,String defaultMessage) {
        Properties properties = new Properties();
        String reportTemplatePath = null;
        try(InputStream input = GetValuesFromConfigFile.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
            reportTemplatePath = properties.getProperty(key,defaultMessage);
        } catch(FileNotFoundException e){
            logger.info("File is not found at the given path. Please check the path provided: {}",e.getLocalizedMessage());
        }
        catch (IOException e) {
            logger.info("There is an issue while reading contents from the file: {}",e.getLocalizedMessage());
        }
        return reportTemplatePath;
    }
}
