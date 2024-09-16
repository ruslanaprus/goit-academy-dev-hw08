package org.example.viewmodel;

public class ProjectPriceInfo {
    private String projectName;
    private int projectPrice;

    public ProjectPriceInfo(String projectName, int price) {
        this.projectName = projectName;
        this.projectPrice = price;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getPrice() {
        return projectPrice;
    }

    @Override
    public String toString() {
        return "[projectName='" + projectName + "', projectPrice=" + projectPrice + ']';
    }
}
