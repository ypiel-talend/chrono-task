package com.github.ypiel.chronotask;


import com.github.ypiel.chronotask.business.DurationManager;
import com.github.ypiel.chronotask.control.DurationByDateTableView;
import com.github.ypiel.chronotask.control.TaskTableView;
import com.github.ypiel.chronotask.model.Status;
import com.github.ypiel.chronotask.model.Task;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ChronoTask extends Application {

    private final DurationManager durationManager = new DurationManager();
    ;

    public static List<Task> buildTasksList() {
        List<Task> taskList = new ArrayList<>();
        Task task1 = new Task();
        task1.setOrder(1);
        task1.setId("T1xx");
        task1.setShortDescription("Task 1");
        task1.setNotes("This is the first task");
        task1.setStatus(Status.New);
        taskList.add(task1);

        Task task2 = new Task();
        task2.setOrder(2);
        task2.setId("T2xx");
        task2.setShortDescription("Task 2");
        task2.setNotes("This is the second task");
        task2.setStatus(Status.InProgress);
        task2.getDurationsByDate().add(new Task.DurationByDate());
        task2.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1980, Month.JULY, 19), java.time.Duration.ofSeconds(60 * 60 * 3 + 60 * 25 + 45), ""));
        task2.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1978, Month.FEBRUARY, 11), java.time.Duration.ofHours(6), ""));
        task2.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1973, Month.MAY, 5), java.time.Duration.ofHours(8), ""));

        Task t1 = new Task();
        t1.setOrder(1);
        t1.setId("T2.1");
        t1.setShortDescription("Task 2.1");
        t1.setNotes("This is the first sub task of the second task");
        t1.setStatus(Status.InProgress);
        t1.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1973, Month.MAY, 5), java.time.Duration.ofHours(8), "Yolo 1"));
        t1.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1978, Month.FEBRUARY, 11), java.time.Duration.ofHours(6), "Yolo 2"));
        task2.getSubTasks().add(t1);

        Task t2 = new Task();
        t2.setOrder(2);
        t2.setId("T2.2");
        t2.setShortDescription("Task 2.2");
        t2.setNotes("This is the second sub task of the second task");
        t2.setStatus(Status.InProgress);
        task2.getSubTasks().add(t2);

        taskList.add(task2);

        Task task3 = new Task();
        task3.setOrder(3);
        task3.setId("T3xx");
        task3.setShortDescription("Task 3");
        task3.setNotes("This is the third task");
        task3.setStatus(Status.OnHold);
        task3.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1945, Month.AUGUST, 6), java.time.Duration.ofHours(4), ""));
        task3.getDurationsByDate().add(new Task.DurationByDate(LocalDate.of(1947, Month.AUGUST, 16), java.time.Duration.ofHours(6), ""));

        Task t3sb1 = new Task();
        t3sb1.setOrder(1);
        t3sb1.setId("T3.1");
        t3sb1.setShortDescription("Task 3.1");
        t3sb1.setNotes("This is the first sub task of the third task");
        t3sb1.setStatus(Status.OnHold);
        task3.getSubTasks().add(t3sb1);

        taskList.add(task3);

        Task task4 = new Task();
        task4.setOrder(4);
        task4.setId("T4xx");
        task4.setShortDescription("Task 4");
        task4.setNotes("This is the fourth task");
        task4.setStatus(Status.Closed);
        taskList.add(task4);

        return taskList;
    }

    @Override
    public void start(Stage primaryStage) {
        final List<Task> tasks = buildTasksList();

        durationManager.start();

        final TaskTableView taskTableView = new TaskTableView(tasks);
        final DurationByDateTableView durationByDateTableView = new DurationByDateTableView();

        final TaskTableView todoTableView = new TaskTableView();
        final DurationByDateTableView todoDurationByDateTableView = new DurationByDateTableView();

        taskTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (observable.getValue() == null) {
                durationByDateTableView.setDurationsByDate(Collections.emptyList());
            }

            if (oldValue != null) {
                durationManager.removeTasks(oldValue);
                //oldValue.setSubTasks(todoTableView.getItems().stream().filter(Task::isValid).collect(Collectors.toList()));
            }

            if (newValue != null) {
                durationByDateTableView.setDurationsByDate(newValue.getDurationsByDate());
                newValue.setDurationsByDate(durationByDateTableView.getItems());

                todoTableView.setTasks(newValue.getSubTasks());
                newValue.setSubTasks(todoTableView.getItems());
                if (newValue.isValid()) {
                    durationManager.addTasks(newValue);
                }
            }
        });

        Timeline timelineRefresh = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            durationByDateTableView.refresh();
        }));
        timelineRefresh.setCycleCount(Timeline.INDEFINITE);
        timelineRefresh.play();

        todoTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (observable.getValue() == null) {
                todoDurationByDateTableView.setDurationsByDate(Collections.emptyList());
            }

            if (oldValue != null) {
                durationManager.removeTasks(oldValue);
            }

            if (newValue != null) {
                todoDurationByDateTableView.setDurationsByDate(newValue.getDurationsByDate());
                newValue.setDurationsByDate(todoDurationByDateTableView.getItems());
                if (newValue.isValid()) {
                    durationManager.addTasks(newValue);
                }
            }
        });


        Timeline timelineTodoRefresh = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            todoDurationByDateTableView.refresh();
        }));
        timelineTodoRefresh.setCycleCount(Timeline.INDEFINITE);
        timelineTodoRefresh.play();

        Button btPause = new Button("Pause");
        btPause.setOnAction(event -> {
            if (btPause.getText().equals("Pause")) {
                durationManager.pause();
                btPause.setText("Resume");
            } else {
                durationManager.resume();
                btPause.setText("Pause");
            }
        });

        Label lblDayDuration = new Label("Day duration");


        SplitPane splitPane = new SplitPane();
        VBox leftVBox = new VBox();
        VBox.setVgrow(taskTableView, Priority.ALWAYS);
        VBox.setVgrow(durationByDateTableView, Priority.ALWAYS);
        taskTableView.maxHeightProperty().bind(leftVBox.heightProperty().multiply(2.0 / 3.0));
        durationByDateTableView.maxHeightProperty().bind(leftVBox.heightProperty().multiply(1.0 / 3.0));
        leftVBox.getChildren().addAll(taskTableView, durationByDateTableView);

        VBox rightVBox = new VBox();
        VBox.setVgrow(todoTableView, Priority.ALWAYS);
        VBox.setVgrow(todoDurationByDateTableView, Priority.ALWAYS);
        todoTableView.maxHeightProperty().bind(rightVBox.heightProperty().multiply(2.0 / 3.0));
        todoDurationByDateTableView.maxHeightProperty().bind(rightVBox.heightProperty().multiply(1.0 / 3.0));
        rightVBox.getChildren().addAll(todoTableView, todoDurationByDateTableView);

        splitPane.getItems().addAll(leftVBox, rightVBox);
        HBox bottom = new HBox(btPause, lblDayDuration) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double maxHeight = Math.max(btPause.getHeight(), lblDayDuration.getHeight());
                setMinHeight(maxHeight);
                setPrefHeight(maxHeight);
                setMaxHeight(maxHeight);
            }
        };

        VBox main = new VBox(splitPane, bottom);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        VBox.setVgrow(bottom, Priority.ALWAYS);
        Scene scene = new Scene(main, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Manager");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (durationManager != null) {
            durationManager.stop();
        }

        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
