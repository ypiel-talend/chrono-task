package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NotesEditor extends VBox {

    private double scrollpos = 0.0;

    private static final String WEBVIEW_CSS;
    static {
        try {
            InputStream cssStream = NotesEditor.class.getResourceAsStream("/webview.css");
            if (cssStream == null) {
                throw new IOException("CSS file not found");
            }
            WEBVIEW_CSS = new String(cssStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load CSS file", e);
        }
    }

    private Task task;
    private final Label lblNotesInfo;
    private final CodeArea codeArea;
    private final WebEngine webEngine;

    public NotesEditor() {
        super();
        lblNotesInfo = new Label("Notes of ...");
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Restore the scroll position
                    System.out.println("************** Set scroll position: " + scrollpos);
                    webEngine.executeScript("window.scrollTo(0, " + scrollpos + ");");
                }
            }
        });

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);

        SplitPane splitPaneNotes = new SplitPane();
        splitPaneNotes.orientationProperty().set(Orientation.VERTICAL);
        splitPaneNotes.getItems().addAll(scrollPane, webView);

        HBox menu = new HBox(lblNotesInfo);
        this.setVgrow(splitPaneNotes, Priority.ALWAYS);
        this.getChildren().addAll(menu, splitPaneNotes);

        Timeline autoRenderHTML = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            updateView();
            save();
        }));
        autoRenderHTML.setCycleCount(Timeline.INDEFINITE);
        autoRenderHTML.play();
    }

    public void save() {
        if (task == null) {
            return;
        }

        task.setNotes(codeArea.getText());
    }

    private void updateView() {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(codeArea.getText());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        StringBuilder sb = new StringBuilder("<html><head>");
        sb.append(WEBVIEW_CSS);
        sb.append("</head><body>");
        sb.append(html);
        sb.append("</body></html>");
        html = sb.toString();


        // Get the current scroll position
        Object scrollY = webEngine.executeScript("window.pageYOffset || document.documentElement.scrollTop;");
        // Store the scroll position as a double
        this.scrollpos = scrollY instanceof Number ? ((Number) scrollY).doubleValue() : 0.0;
        webEngine.loadContent(html);
    }

    public void setTask(Task task) {
        save();

        if (!task.isValid()) {
            this.removeTask();
            return;
        }

        this.task = task;
        this.codeArea.replaceText(task.getNotes());
        lblNotesInfo.setText("Notes of " + task.getViewId());
    }

    public void removeTask() {
        this.task = null;
        this.codeArea.replaceText("_No task selected_");
        lblNotesInfo.setText("Notes of ...");
    }

}
