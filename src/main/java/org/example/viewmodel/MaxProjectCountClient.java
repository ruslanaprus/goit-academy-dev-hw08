package org.example.viewmodel;

public class MaxProjectCountClient {
    private String clientName;
    private int projectCount;

    public MaxProjectCountClient(String clientName, int projectCount) {
        this.clientName = clientName;
        this.projectCount = projectCount;
    }

    public String getClientName() {
        return clientName;
    }

    public int getProjectCount() {
        return projectCount;
    }

    @Override
    public String toString() {
        return "[clientName='" + clientName + "', projectCount=" + projectCount + ']';
    }
}