package org.example.viewmodel;

public class LongestProject {
    private String projectName;
    private int duration;

    public LongestProject(String projectName, int duration) {
        this.projectName = projectName;
        this.duration = duration;
    }

    public String getName() {
        return projectName;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "[projectName='" + projectName + "', duration=" + duration + "]";
    }

}
