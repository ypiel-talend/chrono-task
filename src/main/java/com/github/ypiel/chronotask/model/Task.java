package com.github.ypiel.chronotask.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.ypiel.chronotask.ChronoTask;
import com.github.ypiel.chronotask.business.IntervalAutoTaskAction;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class Task implements Serializable, Comparable<Task> {
    private int order = 0;
    private String jira = "";
    private String shortDescription = "";
    private String notes = "";
    private Category category = Category.Fix;
    private List<String> tags = new ArrayList<>();
    private List<DurationByDate> durationsByDate = new ArrayList<>(10);
    private Class autoTaskAction = IntervalAutoTaskAction.class;

    @JsonIgnore
    public boolean isValid(){
        return order > 0 && !shortDescription.trim().isEmpty();
    }

    @JsonIgnore
    public String getViewId(){
        if(isIdUrl()){
            int lastSegment = this.getJira().lastIndexOf('/');
            return this.getJira().substring(lastSegment + 1);
        }
        return this.getJira();
    }

    @JsonIgnore
    public boolean isIdUrl() {
        return this.getJira().startsWith("http");
    }

    @JsonIgnore
    public boolean isDone() {
        return this.getTags().contains(ChronoTask.DONE_STATUS);
    }

    @Override
    public int compareTo(Task other) {
        if (this.isDone() && !other.isDone()) {
            return -1;
        }
        else if (!this.isDone() && other.isDone()) {
            return 1;
        }
        else {
            return Integer.compare(this.getOrder(), other.getOrder());
        }
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
