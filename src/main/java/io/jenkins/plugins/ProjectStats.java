package io.jenkins.plugins;

import java.util.LinkedHashMap;

public class ProjectStats {

    LinkedHashMap<String, Integer> numberOfLines;

    public LinkedHashMap<String, Integer> getNumberOfLines() {
        return numberOfLines;
    }

    public ProjectStats(LinkedHashMap<String, Integer> numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    public ProjectStats() {
        // Default constructor
    }
}
