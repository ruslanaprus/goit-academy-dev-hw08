package org.example.model;

import java.time.LocalDate;

public class Project {
    private String name;
    private int client_id;
    private LocalDate start_date;
    private LocalDate finish_date;

    public Project(String name, int client_id, LocalDate start_date, LocalDate finish_date) {
        this.name = name;
        this.client_id = client_id;
        this.start_date = start_date;
        this.finish_date = finish_date;
    }

    public String getName() {
        return name;
    }

    public int getClient_id() {
        return client_id;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public LocalDate getFinish_date() {
        return finish_date;
    }
}
