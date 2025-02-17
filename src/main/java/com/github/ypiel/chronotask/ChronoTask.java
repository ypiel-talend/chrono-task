package com.github.ypiel.chronotask;


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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.ypiel.chronotask.business.AutoTaskAction;
import com.github.ypiel.chronotask.business.DurationManager;
import com.github.ypiel.chronotask.control.DurationByDateTableView;
import com.github.ypiel.chronotask.control.NotesEditor;
import com.github.ypiel.chronotask.control.TaskTableView;
import com.github.ypiel.chronotask.model.Task;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ChronoTask extends Application implements AutoTaskAction.Destination {

    public final static String[] mainTopics = {"TDI", "QCS", "TCK", "PROCESS", "CONNECTIVITY CONVERGENCE", "CODE REVIEW", "MEETING"};

    private static final String SAVE_DIR = System.getProperty("chrono.task.dir", System.getProperty("user.home") + "/chrono-task");
    private static final String SAVE_FILE = Paths.get(SAVE_DIR, "chrono-task.json").toString();

    private final DurationManager durationManager = new DurationManager();

    private TaskTableView taskTableView;
    private TaskTableView todoTableView;

    private ToggleButton btPause;

    private ObjectMapper jacksonMapper;

    private final AtomicBoolean autoSaveEnabled = new AtomicBoolean(true);

    private Optional<Timeline> autoTaskActionTimeline = Optional.empty();

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initSerialization();

        final List<Task> tasks = load();
        this.taskTableView = new TaskTableView(tasks);
        ToggleButton tbHideClosed = new ToggleButton("Hide closed");
        taskTableView.hideClosedProperty().bindBidirectional(tbHideClosed.selectedProperty());
        tbHideClosed.setSelected(true);

        durationManager.start();

        final NotesEditor notesEditor = new NotesEditor();

        final DurationByDateTableView durationByDateTableView = new DurationByDateTableView();

        todoTableView = new TaskTableView();
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
                store(taskTableView.getAllItems());
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

        btPause = new ToggleButton("Pause");
        btPause.setOnAction(event -> {
            doPause();
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

        TextField tfFilter = new TextField();
        taskTableView.getFilterProperty().bind(tfFilter.textProperty());
        Label lblFilter = new Label("Filter: ");

        Spinner<Integer> forceDurationSpinner = new Spinner<>();
        SpinnerValueFactory<Integer> forceDurationSpinnerValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 100000, -1, 1);
        forceDurationSpinner.setValueFactory(forceDurationSpinnerValueFactory);
        forceDurationSpinner.setEditable(true);
        Label lblForceDuration = new Label("Force duration (minutes)");
        Button btForceDuration = new Button("Force duration");
        btForceDuration.setOnAction(event -> {
            Task toUpdate = todoTableView.getSelectionModel().getSelectedItem();
            if (toUpdate == null) {
                toUpdate = taskTableView.getSelectionModel().getSelectedItem();
            }

            long spinnerValue = forceDurationSpinner.getValue();
            if (toUpdate != null && spinnerValue >= 0) {
                log.info(String.format("Force duration for task %s", toUpdate.getShortDescription()));
                LocalDate now = LocalDate.now();
                toUpdate.getDurationsByDate().stream()
                        .filter(d -> d.getDate().equals(now))
                        .findAny()
                        .ifPresent(d -> d.setDuration(java.time.Duration.ofMinutes(spinnerValue)));
            }

            forceDurationSpinner.getValueFactory().setValue(-1);

        });


        splitPane.getItems().addAll(leftVBox, rightVBox, notesEditor);
        Separator separatorB = new Separator(Orientation.VERTICAL);
        separatorB.setStyle("-fx-padding: 0 5 0 5;");
        Separator separatorA = new Separator(Orientation.VERTICAL);
        separatorA.setStyle("-fx-padding: 0 5 0 5;");

        HBox bottom = new HBox(btPause, tbHideClosed, currentTasks, dayDuration, separatorA, lblForceDuration, forceDurationSpinner, btForceDuration, separatorB, lblFilter, tfFilter) {
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
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, 2, 1);
        spinner.setValueFactory(valueFactory);
        CheckBox cbDetailled = new CheckBox("Detailled");
        cbDetailled.setSelected(true);
        Button btExport = new Button("Export");
        btExport.setOnAction(event -> {
            exportAction(datePicker, spinner, taExport, cbDetailled.isSelected());
        });

        VBox vbExport = new VBox(new HBox(datePicker, new Label("(minimum of"), spinner, new Label(" minutes)"), cbDetailled, btExport), taExport);
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

    private void doPause() {
        if (btPause.isSelected()) {
            durationManager.pause();
            autoSaveEnabled.set(false);
        } else {
            durationManager.resume();
            autoSaveEnabled.set(true);
        }
    }

    private void exportAction(DatePicker datePicker, Spinner<Integer> spinner, TextArea taExport, boolean detailled) {
        LocalDate localDate = datePicker.getValue();
        java.time.Duration mini = java.time.Duration.ofMinutes(spinner.getValue());

        String export = detailled ?
                taskDurationFor(taskTableView.getAllItems(), localDate, mini, false) :
                taskFor(taskTableView.getAllItems(), localDate, mini, false);

        taExport.setText(export);
    }

    private String taskFor(List<Task> tasks, LocalDate date, java.time.Duration mini, boolean tab) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Task t : tasks) {
            Optional<Task.DurationByDate> durationByDate = t.getDurationsByDate().stream().filter(d -> d.getDate().equals(date)).findAny();

            if (!durationByDate.isPresent() || durationByDate.get().getDuration().compareTo(mini) < 0) {
                continue;
            }
            if (!first) {
                sb.append("\n");
            }
            sb.append(tab ? "    - " : "- ")
                    .append(t.getId()).append(": ").append(t.getShortDescription());

            List<Task> subTasks = t.getSubTasks();
            String subs = taskFor(subTasks, date, mini, true);
            sb.append(subs.trim().isEmpty() ? "" : "\n" + subs);

            first = false;
        }

        return sb.toString();
    }

    private String taskDurationFor(List<Task> tasks, LocalDate date, java.time.Duration mini, boolean tab) {
        StringBuilder sb = new StringBuilder();

        java.time.Duration dayDuration = java.time.Duration.ZERO;
        boolean first = true;
        for (Task t : tasks) {
            Optional<Task.DurationByDate> durationByDate = t.getDurationsByDate().stream().filter(d -> d.getDate().equals(date)).findAny();

            if (!durationByDate.isPresent() || durationByDate.get().getDuration().compareTo(mini) < 0) {
                continue;
            }

            java.time.Duration duration = durationByDate.get().getDuration();
            if (!first) {
                sb.append("\n");
            }

            dayDuration = dayDuration.plus(duration);
            sb.append(tab ? "    " : "").append(formatDuration(duration)).append(" - ")
                    .append(t.getViewId()).append(": ").append(t.getShortDescription());

            List<Task> subTasks = t.getSubTasks();
            String subs = taskDurationFor(subTasks, date, mini, true);
            sb.append(subs.trim().isEmpty() ? "" : "\n" + subs);

            first = false;
        }

        if (!tab) {
            sb.append("\n\nWorking day: ").append(formatDuration(dayDuration));
        }

        return sb.toString();
    }

    private void updateDayDuration(Label dayDuration) {
        LocalDate now = LocalDate.now();
        Optional<java.time.Duration> total = taskTableView.getAllItems().stream().flatMap(t -> t.getDurationsByDate().stream())
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

        // Fix a bug about adding a todo to a task
        Task selectedTask = taskTableView.getSelectionModel().getSelectedItem();
        if (selectedTask != null && newValue != null) {
            Optional<Task> todo = selectedTask.getSubTasks().stream()
                    .filter(t -> t.equals(newValue)).findAny();
            if (!todo.isPresent()) {
                selectedTask.getSubTasks().add(newValue);
            }
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
        }

        if (newValue != null) {
            durationByDateTableView.setDurationsByDate(newValue.getDurationsByDate());
            newValue.setDurationsByDate(durationByDateTableView.getItems());

            todoTableView.setTasks(newValue.getSubTasks());
            newValue.setSubTasks(todoTableView.getAllItems());
            startAutoTaskAction(newValue, stage);
            if (newValue.isValid()) {
                durationManager.addTasks(newValue);
                notesEditor.setTask(newValue);
            }
        }
    }

    private void startAutoTaskAction(Task task, Stage stage) {
        try {
            AutoTaskAction autoTaskAction = (AutoTaskAction) task.getAutoTaskAction().getDeclaredConstructor().newInstance();
            autoTaskAction.setDestination(this);
            if (autoTaskActionTimeline.isPresent()) {
                autoTaskActionTimeline.get().stop();
            }

            if (task != null && task.isValid()) {

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

        store(taskTableView.getAllItems());

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

    @Override
    public Task getSelectedMainTask() {
        return taskTableView.getSelectionModel().getSelectedItem();
    }

    @Override
    public Task getSelectedTodo() {
        return todoTableView.getSelectionModel().getSelectedItem();
    }

    @Override
    public void moveToFront() {
        this.stage.toFront();
    }

    @Override
    public void unselectAll() {
        taskTableView.getSelectionModel().clearSelection();
        todoTableView.getSelectionModel().clearSelection();
    }

    @Override
    public void pause() {
        btPause.setSelected(true);
        doPause();
    }

    @Override
    public void resume() {
        btPause.setSelected(false);
        doPause();
    }

    @Override
    public boolean isPaused() {
        return btPause.isSelected();
    }
}
