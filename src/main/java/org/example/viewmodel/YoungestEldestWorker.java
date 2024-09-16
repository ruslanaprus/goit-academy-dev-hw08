package org.example.viewmodel;


import java.time.LocalDate;

public class YoungestEldestWorker {
    private String type;
    private String name;
    private LocalDate birthday;

    public YoungestEldestWorker(String type, String name, LocalDate birthday) {
        this.type = type;
        this.name = name;
        this.birthday = birthday;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    @Override
    public String toString() {
        return "[type='" + type + "', name='" + name + "', birthday='" + birthday + ']';
    }
}
