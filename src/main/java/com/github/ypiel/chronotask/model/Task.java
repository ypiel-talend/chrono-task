package com.github.ypiel.chronotask.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ypiel.chronotask.business.AutoTaskAction;
import com.github.ypiel.chronotask.business.IntervalAutoTaskAction;

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
    private List<String> tags = new ArrayList<>();
    private List<Task> subTasks = new ArrayList<>(5);
    private List<DurationByDate> durationsByDate = new ArrayList<>(10);
    private Class autoTaskAction = IntervalAutoTaskAction.class;

    @JsonIgnore
    public boolean isValid(){
        return order > 0 && id.length() > 3;
    }

    @JsonIgnore
    public String getViewId(){
        if(isIdUrl()){
            int lastSegment = this.getId().lastIndexOf('/');
            return this.getId().substring(lastSegment + 1);
        }
        return this.getId();
    }

    @JsonIgnore
    public boolean isIdUrl() {
        return this.getId().startsWith("http");
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
