package io.jenkins.plugins;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetValuesFromConfigFile {
    private static final Logger logger = LoggerFactory.getLogger(GetValuesFromConfigFile.class);
    public static final String CONFIG_FILE = "config.properties";

    public String getReportTemplatePathFromConfig() {
        String key = "report.template.path";
        String defaultKey = "/stats.html";
        return getConfigValues(key, defaultKey);
    }

    public String getConfigValues(String key, String defaultMessage) {
        Properties properties = new Properties();
        String reportTemplatePath = null;
        try (InputStream input = GetValuesFromConfigFile.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(input);
            reportTemplatePath = properties.getProperty(key, defaultMessage);
        } catch (FileNotFoundException e) {
            logger.info(
                    "File is not found at the given path. Please check the path provided: {}", e.getLocalizedMessage());
        } catch (IOException e) {
            logger.info("There is an issue while reading contents from the file: {}", e.getLocalizedMessage());
        }
        return reportTemplatePath;
    }
}
