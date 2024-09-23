package org.example.model;

import java.time.LocalDate;

public class Worker {
    private long id;
    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private Level level;
    private int salary;

    public Worker() {}

    public Worker(Long id, String name, LocalDate dateOfBirth, String email, Level level, int salary) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.level = level;
        this.salary = salary;
    }

    public Worker(String name, LocalDate dateOfBirth, String email, Level level, int salary) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.level = level;
        this.salary = salary;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public Level getLevel() {
        return level;
    }

    public int getSalary() {
        return salary;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "[id=" + id + ", name='" + name + "', dateOfBirth=" + dateOfBirth + ", email='" + email + "', level=" + level + ", salary=" + salary + "]";
    }
}