package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
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

    private Task task;
    private final Label lblNotesInfo;
    private final CodeArea codeArea;
    private final WebEngine webEngine;

    public NotesEditor() {
        super();
        lblNotesInfo = new Label("Notes of ...");
        Button btnSave = new Button("Save");
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        btnSave.setOnAction(e -> {
            if (task != null) {
                log.info("Save notes of task {}", task.getViewId());
                task.setNotes(codeArea.getText());
            }
        });

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);

        SplitPane splitPaneNotes = new SplitPane();
        splitPaneNotes.orientationProperty().set(Orientation.VERTICAL);
        splitPaneNotes.getItems().addAll(scrollPane, webView);

        HBox menu = new HBox(lblNotesInfo, btnSave);
        this.setVgrow(splitPaneNotes, Priority.ALWAYS);
        this.getChildren().addAll(menu, splitPaneNotes);

        Timeline autoRenderHTML = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            updateView();
        }));
        autoRenderHTML.setCycleCount(Timeline.INDEFINITE);
        autoRenderHTML.play();
    }

    private void updateView() {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(codeArea.getText());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        String css = "<style>" +
                "body { " +
                "  font-family: 'Arial', sans-serif; " +
                "  padding: 20px; " +
                "}" +
                "code { background-color: #F0F0F0; }" +
                "h1 { color: #2e6c80; }" +
                "blockquote {\n" +
                "  margin-left: 1em;\n" +
                "  border-left: 5px gray solid;\n" +
                "padding-left: 10px;\n" +
                "  background-color: #F0F0F0;\n" +
                "}" +
                "</style>";

        html = "<html><head>" + css + "</head><body>" + html + "</body></html>";

        System.out.println("============================");
        System.out.println(html);
        System.out.println("============================");

        webEngine.loadContent(html);
    }

    public void setTask(Task task) {
        this.task = task;
        this.codeArea.replaceText(task.getNotes());
        lblNotesInfo.setText("Notes of " + task.getViewId());
    }

    public void removeTask() {
        this.task = null;
        lblNotesInfo.setText("Notes of ...");
    }

}
