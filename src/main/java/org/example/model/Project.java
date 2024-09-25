package org.example.model;

import java.time.LocalDate;

public class Project {
    private long id;
    private String name;
    private long client_id;
    private LocalDate start_date;
    private LocalDate finish_date;

    public Project() {
    }

    public Project(long id, String name, long client_id, LocalDate start_date, LocalDate finish_date) {
        this.id = id;
        this.name = name;
        this.client_id = client_id;
        this.start_date = start_date;
        this.finish_date = finish_date;
    }

    public Project(String name, long client_id, LocalDate start_date, LocalDate finish_date) {
        this.name = name;
        this.client_id = client_id;
        this.start_date = start_date;
        this.finish_date = finish_date;
    }

    public String getName() {
        return name;
    }

    public long getClient_id() {
        return client_id;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public LocalDate getFinish_date() {
        return finish_date;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClient_id(long client_id) {
        this.client_id = client_id;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public void setFinish_date(LocalDate finish_date) {
        this.finish_date = finish_date;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", name='" + name + ", client_id=" + client_id + ", start_date=" + start_date + ", finish_date=" + finish_date + ']';
    }
}
