package org.example.model;

public enum Level {
    TRAINEE("TRAINEE"),
    JUNIOR("JUNIOR"),
    MIDDLE("MIDDLE"),
    SENIOR("SENIOR");

    private final String displayName;

    Level(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}