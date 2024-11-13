package com.github.ypiel.chronotask;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.ypiel.chronotask.business.AutoTaskAction;
import com.github.ypiel.chronotask.business.DurationManager;
import com.github.ypiel.chronotask.control.DurationByDateTableView;
import com.github.ypiel.chronotask.control.NotesEditor;
import com.github.ypiel.chronotask.control.TaskTableView;
import com.github.ypiel.chronotask.model.Task;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ChronoTask extends Application {

    public final static String[] mainTopics = {"TDI", "QCS", "TCK", "PROCESS", "CONNECTIVITY CONVERGENCE", "CODE REVIEW", "MEETING"};

    private static final String SAVE_DIR = System.getProperty("chrono.task.dir", System.getProperty("user.home") + "/chrono-task");
    private static final String SAVE_FILE = Paths.get(SAVE_DIR, "chrono-task.json").toString();

    private final DurationManager durationManager = new DurationManager();

    private TaskTableView taskTableView;

    private ObjectMapper jacksonMapper;

    private final AtomicBoolean autoSaveEnabled = new AtomicBoolean(true);

    private Optional<Timeline> autoTaskActionTimeline = Optional.empty();

    /*public static List<Task> buildTasksList() {
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
    }*/

    @Override
    public void start(Stage primaryStage) {
        initSerialization();
        final List<Task> tasks = load();

        durationManager.start();

        final NotesEditor notesEditor = new NotesEditor();

        this.taskTableView = new TaskTableView(tasks);
        final DurationByDateTableView durationByDateTableView = new DurationByDateTableView();

        final TaskTableView todoTableView = new TaskTableView();
        final DurationByDateTableView todoDurationByDateTableView = new DurationByDateTableView();

        taskTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            taskTableSelection(observable, oldValue, newValue, durationByDateTableView, notesEditor, todoTableView);
        });

        Timeline timelineRefresh = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            durationByDateTableView.refresh();
        }));
        timelineRefresh.setCycleCount(Timeline.INDEFINITE);
        timelineRefresh.play();

        // Auto-save every minute
        Timeline autoSave = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            if (autoSaveEnabled.get()) {
                store(taskTableView.getItems());
            }
        }));
        autoSave.setCycleCount(Timeline.INDEFINITE);
        autoSave.play();

        todoTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            todoTableSelection(observable, oldValue, newValue, todoDurationByDateTableView, notesEditor);
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
                autoSaveEnabled.set(false);
                btPause.setText("Resume");
            } else {
                durationManager.resume();
                autoSaveEnabled.set(true);
                btPause.setText("Pause");
            }
        });

        Label currentTasks = new Label("");
        Label dayDuration = new Label("");
        Timeline dayDurationRefresh = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            updateDayDuration(dayDuration);
        }));
        dayDurationRefresh.setCycleCount(Timeline.INDEFINITE);
        dayDurationRefresh.play();

        durationManager.addListener(new DurationManager.DurationManagerListener() {
            @Override
            public void onTaskDurationAddTask(DurationManager durationManager, Task task) {
                currentTasks.setText(durationManager.toString());
            }

            @Override
            public void onTaskDurationRemoveTask(DurationManager durationManager, Task task) {
                currentTasks.setText(durationManager.toString());
            }

            @Override
            public void onTaskDurationStart(DurationManager durationManager) {
                currentTasks.setText(durationManager.toString());
            }

            @Override
            public void onTaskDurationStop(DurationManager durationManager) {
                currentTasks.setText(durationManager.toString());
            }

            @Override
            public void onTaskDurationPause(DurationManager durationManager) {
                currentTasks.setText(durationManager.toString());
            }

            @Override
            public void onTaskDurationResume(DurationManager durationManager) {
                currentTasks.setText(durationManager.toString());
            }
        });


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

        splitPane.getItems().addAll(leftVBox, rightVBox, notesEditor);
        HBox bottom = new HBox(btPause, currentTasks, dayDuration) {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double maxHeight = Math.max(btPause.getHeight(), currentTasks.getHeight());
                setMinHeight(maxHeight);
                setPrefHeight(maxHeight);
                setMaxHeight(maxHeight);
            }
        };

        TextArea taExport = new TextArea();

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, 20, 1);
        spinner.setValueFactory(valueFactory);
        Button btExport = new Button("Export");
        btExport.setOnAction(event -> {
            exportAction(datePicker, spinner, taExport);
        });

        VBox vbExport = new VBox(new HBox(datePicker, new Label("(minimum of"), spinner, new Label(" minutes)"), btExport), taExport);
        VBox.setVgrow(taExport, Priority.ALWAYS);


        TabPane tabPane = new TabPane();
        Tab workingTab = new Tab("Work", splitPane);
        Tab exportTab = new Tab("Export", vbExport);
        tabPane.getTabs().addAll(workingTab, exportTab);

        VBox main = new VBox(tabPane, bottom);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        VBox.setVgrow(bottom, Priority.ALWAYS);

        Scene scene = new Scene(main, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Manager");
        primaryStage.show();

    }

    private void exportAction(DatePicker datePicker, Spinner<Integer> spinner, TextArea taExport) {
        LocalDate localDate = datePicker.getValue();
        List<Task> found = taskTableView.getItems().stream()
                .filter(t -> t.getDurationsByDate().stream().anyMatch(d -> d.getDate().equals(localDate))).toList();
        found = found.stream().filter(t -> t.getDurationsByDate().stream().filter(d -> d.getDate()
                .equals(localDate)).findAny().get().getDuration().compareTo(java.time.Duration.ofMinutes(spinner.getValue())) >= 0).collect(Collectors.toList());
        String dailyWork = found.stream().map(t -> formatDuration(t.getDurationsByDate().stream()
                        .filter(d -> d.getDate().equals(localDate)).findAny().get().getDuration()) + " - " + t.getViewId() + ": " + t.getShortDescription())
                .collect(Collectors.joining("\n"));
        taExport.setText(dailyWork);
    }

    private void updateDayDuration(Label dayDuration) {
        LocalDate now = LocalDate.now();
        Optional<java.time.Duration> total = taskTableView.getItems().stream().flatMap(t -> t.getDurationsByDate().stream())
                .filter(d -> d.getDate().equals(now))
                .map(Task.DurationByDate::getDuration)
                .reduce(java.time.Duration::plus);
        dayDuration
                .setText(total.map(d -> "Today: " + d.toHours() + "h " + d.toMinutesPart() + "m " + d.toSecondsPart() + "s")
                        .orElse("Today: 0h 0m 0s"));
    }

    private void todoTableSelection(ObservableValue<? extends Task> observable, Task oldValue, Task newValue, DurationByDateTableView todoDurationByDateTableView, NotesEditor notesEditor) {
        if (observable.getValue() == null) {
            todoDurationByDateTableView.setDurationsByDate(Collections.emptyList());
        }

        if (oldValue != null) {
            durationManager.removeTasks(oldValue);
            notesEditor.removeTask();
        }

        if (newValue != null) {
            todoDurationByDateTableView.setDurationsByDate(newValue.getDurationsByDate());
            newValue.setDurationsByDate(todoDurationByDateTableView.getItems());
            if (newValue.isValid()) {
                durationManager.addTasks(newValue);
                notesEditor.setTask(newValue);
            }
        }
    }

    private void taskTableSelection(ObservableValue<? extends Task> observable, Task oldValue, Task newValue, DurationByDateTableView durationByDateTableView, NotesEditor notesEditor, TaskTableView todoTableView) {
        if (observable.getValue() == null) {
            durationByDateTableView.setDurationsByDate(Collections.emptyList());
        }

        if (oldValue != null) {
            durationManager.removeTasks(oldValue);
            notesEditor.removeTask();
            //oldValue.setSubTasks(todoTableView.getItems().stream().filter(Task::isValid).collect(Collectors.toList()));
        }

        if (newValue != null) {
            durationByDateTableView.setDurationsByDate(newValue.getDurationsByDate());
            newValue.setDurationsByDate(durationByDateTableView.getItems());

            todoTableView.setTasks(newValue.getSubTasks());
            newValue.setSubTasks(todoTableView.getItems());
            startAutoTaskAction(newValue);
            if (newValue.isValid()) {
                durationManager.addTasks(newValue);
                notesEditor.setTask(newValue);
            }
        }
    }

    private void startAutoTaskAction(Task task) {
        try {
            AutoTaskAction autoTaskAction = (AutoTaskAction) task.getAutoTaskAction().getDeclaredConstructor().newInstance();
            autoTaskAction.setTask(task);
            if (autoTaskActionTimeline.isPresent()) {
                autoTaskActionTimeline.get().stop();
            }

            if(task != null && task.isValid()) {

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(autoTaskAction.getInterval().getSeconds()), event -> {
                    autoTaskAction.run();
                }));
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.play();
                this.autoTaskActionTimeline = Optional.of(timeline);
            }

        } catch (Exception e) {
            log.error("Error starting auto task action", e);
        }
    }


    public static String formatDuration(java.time.Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.toSeconds() % 60;

        StringBuilder sb = new StringBuilder();

        sb.append(hours < 10 ? "0" : "").append(hours).append(":");
        sb.append(minutes < 10 ? "0" : "").append(minutes).append(":");
        sb.append(seconds < 10 ? "0" : "").append(seconds);

        return sb.toString();
    }

    private void initSerialization() {
        this.jacksonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public void stop() throws Exception {
        if (durationManager != null) {
            durationManager.stop();
        }

        store(taskTableView.getItems());

        super.stop();
    }

    @SneakyThrows
    private void store(List<Task> tasks) {
        tasks = removeInvalidTasks(tasks);
        String dayOfYear = String.valueOf(LocalDate.now().getDayOfYear());
        Files.createDirectories(Paths.get(SAVE_DIR));
        try (FileWriter file = new FileWriter(SAVE_FILE)) {
            this.jacksonMapper.writerWithDefaultPrettyPrinter().writeValue(file, tasks);
            log.info("Saving tasks to {}.", SAVE_FILE);
        }
        Files.copy(Paths.get(SAVE_FILE), Paths.get(SAVE_FILE + "." + dayOfYear), StandardCopyOption.REPLACE_EXISTING);
    }

    private List<Task> removeInvalidTasks(List<Task> tasks) {
        return tasks.stream()
                .filter(Task::isValid)
                .peek(task -> task.setSubTasks(removeInvalidTasks(task.getSubTasks())))
                .toList();
    }

    @SneakyThrows
    private List<Task> load() {
        // Backup at start
        String dayOfYear = String.valueOf(LocalDate.now().getDayOfYear());
        if (Files.exists(Path.of(SAVE_FILE), LinkOption.NOFOLLOW_LINKS)) {
            Files.copy(Paths.get(SAVE_FILE), Paths.get(SAVE_FILE + ".start." + dayOfYear), StandardCopyOption.REPLACE_EXISTING);
        }

        if (Files.exists(Path.of(SAVE_FILE), LinkOption.NOFOLLOW_LINKS)) {
            return this.jacksonMapper.readValue(Paths.get(SAVE_FILE).toFile(), this.jacksonMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
