package org.example.model;

import java.time.LocalDate;

public class Worker {
    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private Level level;
    private int salary;

    public Worker(String name, LocalDate dateOfBirth, String email, Level level, int salary) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.level = level;
        this.salary = salary;
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
}