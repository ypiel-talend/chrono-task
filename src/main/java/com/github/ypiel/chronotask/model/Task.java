package com.github.ypiel.chronotask.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class Task implements Serializable {
    private int order = 0;
    private String id = "";
    private String shortDescription = "";
    private String notes = "";
    private Status status = Status.New;
    private List<Task> subTasks = new ArrayList<>(10);
    private List<DurationByDate> durationsByDate = new ArrayList<>(10);

    public boolean isValid(){
        return order > 0 && id.length() > 3;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DurationByDate implements Serializable {
        private LocalDate date = LocalDate.now();
        private Duration duration = Duration.ZERO;
        private String notes = "";
    }

}
