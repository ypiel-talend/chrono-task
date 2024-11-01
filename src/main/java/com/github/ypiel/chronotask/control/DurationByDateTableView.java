package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import static com.github.ypiel.chronotask.model.Task.DurationByDate;

public class DurationByDateTableView extends TableView<DurationByDate> {
public DurationByDateTableView() {
    super();

    TableColumn<DurationByDate, String> dateTableColumn = new TableColumn<>("Date");
    dateTableColumn.setCellValueFactory(cellData -> {
        LocalDate date = cellData.getValue().getDate();
        return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    });

    TableColumn<DurationByDate, String> durationTableColumn = new TableColumn<>("Durée");
    durationTableColumn.setCellValueFactory(cellData -> {
        Duration duration = cellData.getValue().getDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.toSeconds() % 60;
        return new SimpleStringProperty(String.format("%d:%d:%s", hours, minutes, seconds));
    });

    TableColumn<DurationByDate, String> notesColumn = new TableColumn<>("Notes");
    notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
    notesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    notesColumn.setOnEditCommit(event -> {
        DurationByDate DurationByDate = event.getRowValue();
        DurationByDate.setNotes(event.getNewValue());
    });

    this.getColumns().addAll(dateTableColumn, durationTableColumn, notesColumn);
    this.setEditable(true);
}

    public void setDurationsByDate(List<DurationByDate> durationByDateList) {
        this.setItems(FXCollections.observableArrayList(durationByDateList));
    }
}
