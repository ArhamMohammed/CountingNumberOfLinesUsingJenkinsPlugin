package io.jenkins.plugins;

import java.util.Map;

public class ProjectStats {

    Map<String, Integer> numberOfLines;

    public Map<String, Integer> getNumberOfLines() {
        return numberOfLines;
    }

    public ProjectStats(Map<String, Integer> numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    public ProjectStats() {
        // Default constructor
    }
}
