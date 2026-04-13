package com.mycompany.os.project;

import com.mycompany.os.project.src.process;
import java.util.ArrayList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        ObservableList<process> processes = FXCollections.observableArrayList();
                                ///Configuring Process Values\\\
        ComboBox<String> choices = new ComboBox();
        choices.getItems().addAll("FCFS", "SJF [Preemptive]", "SJF [Non-Preemptive]", "Priority [Preemptive]","Priority [Non-Preemptive]", "Round Robbin");
        choices.setValue("Choose Your Scheduling Method");
        
        var nameOfProcess = new Label("Type your Proceess name");
        TextField processName = new TextField();
        
        var burstOfProcessLabel = new Label("Choose Burst Of Process");
        Spinner<Integer> burstOfProcess = new Spinner<>();
        burstOfProcess.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000,1)
        );
        
        var priorityLabel = new Label("Choose Priority Of Process");
        priorityLabel.setDisable(true);
        Spinner<Integer> priority = new Spinner<>();
        priority.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000)
        );
        priority.setDisable(true);
        choices.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            if ("Priority [Preemptive]".equals(newValue) || "Priority [Non-Preemptive]".equals(newValue)) {
                priority.setDisable(false);
                priorityLabel.setDisable(false);
            } else {
                priority.setDisable(true);
                priorityLabel.setDisable(true);
            }
        });
       
                                ///Timer\\\
        int[] seconds = {0};
        Label timerLabel = new Label();
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                seconds[0]++;
                timerLabel.setText("Time: " + seconds[0] + "s");
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        
                                 ////Adding Process\\\\
        Button addProcess = new Button("Add Process");
        addProcess.setOnAction(e ->
        {
                processes.add(new process(burstOfProcess.getValue(),priority.getValue(),processName.getText(),seconds[0]));
        });
        
                                    ////Execution\\\\
        HBox ganttChart = new HBox();
        ganttChart.setSpacing(1);
        ganttChart.setPadding(new javafx.geometry.Insets(10));
        HBox timeAxis = new HBox();
        timeAxis.setSpacing(0);
        timeAxis.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        int[] currentTime = {0};
        
        TableView<process> processesTable = new TableView();
        TableColumn<process, String> nameCol = new TableColumn<>("Process Name");
        nameCol.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleStringProperty(cellData.getValue().processName)
        );
        TableColumn<process, Integer> burstCol = new TableColumn<>("Burst time (Sec)");
        burstCol.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().burst).asObject()
        );
        TableColumn<process, Integer> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().priority).asObject()
        );
        TableColumn<process, Integer> arrivalCol = new TableColumn<>("Arrival Time (Sec)");
        arrivalCol.setCellValueFactory(cellData ->
        new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().arrivalTime).asObject()
        );
        nameCol.setPrefWidth(400);
        burstCol.setPrefWidth(200);
        priorityCol.setPrefWidth(200);
        arrivalCol.setPrefWidth(200);
        processesTable.getColumns().addAll(nameCol, burstCol, priorityCol, arrivalCol);
        processesTable.setItems(processes);
        
        var execLabel = new Label();
        var bar = new ProgressBar(0);
        Button start = new Button("Start Execution");
        start.setOnAction(eh->
        {
            switch (choices.getValue())
            {
                case "FCFS":
                {
                    timeline.play();
                    new Thread(() -> {
                        double initSize = ((double)processes.size());
                        while (!processes.isEmpty()) {
                            var p = processes.get(0);

                            Platform.runLater(() -> {
                                execLabel.setText("Executing " + p.processName + " ...");
                            });
                            final double progress = (initSize-((double)processes.size())+1)/initSize;
                            Platform.runLater(() -> {
                                bar.setProgress(progress);
                            });
                            
                            Platform.runLater(() -> {
                            Region block = new Region();

                            block.setPrefHeight(30);
                            block.setPrefWidth(p.burst * 25); 

                            block.setStyle(
                                "-fx-background-color: " + p.color + ";" +
                                "-fx-border-color: black;" +
                                "-fx-background-radius: 5;"
                            );

                            Label label = new Label(p.processName);
                            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                            StackPane cell = new StackPane();
                            cell.getChildren().addAll(block, label);

                            ganttChart.getChildren().add(cell);
                            });
                            int startTime = currentTime[0];
                            currentTime[0] += p.burst;

                            Platform.runLater(() -> {
                                Label timeLabel = new Label(String.valueOf(startTime));
                                timeLabel.setMinWidth(p.burst * 25);
                                timeLabel.setStyle("-fx-font-size: 10px;");

                                timeAxis.getChildren().add(timeLabel);
                            });

                            try {
                                Thread.sleep(p.burst * 1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            processes.remove(0);
                        }
                        if(processes.size() == 0)
                        {
                            Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            });
                            Platform.runLater(() -> {
                                Label end = new Label(String.valueOf(currentTime[0]));
                                end.setStyle("-fx-font-size: 10px;");
                                timeAxis.getChildren().add(end);
                            });
                            seconds[0] = 0;
                            timeline.stop();
                        }
                    }).start();
                    
                    break;
                }
                case "SJF [Non-Preemptive]":
                {
                    break;
                }
                case "SJF [Preemptive]":
                {
                    break;
                }
                case "Priority [Non-Preemptive]":
                {
                    break;
                }
                case "Priority [Preemptive]":
                {
                    break;
                }
                case "Round Robbin":
                {
                    break;
                }
            }
        });
        
        
        GridPane grid = new GridPane();

        grid.setHgap(25);
        grid.setVgap(20);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(34);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        grid.add(choices, 1, 0);

        grid.add(nameOfProcess, 0, 1);
        grid.add(processName, 1, 1);

        grid.add(burstOfProcessLabel, 0, 2);
        grid.add(burstOfProcess, 1, 2);

        grid.add(priorityLabel, 0, 3);
        grid.add(priority, 1, 3);

        grid.add(addProcess, 1, 4);
        grid.add(start, 2, 4);

        grid.add(timerLabel, 0, 5, 3, 1);
        GridPane.setHalignment(timerLabel, HPos.CENTER);

        grid.add(execLabel, 0, 6, 3, 1);
        GridPane.setHalignment(execLabel, HPos.CENTER);

        grid.add(bar, 0, 7, 3, 1);
        bar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(bar, Priority.ALWAYS);

        grid.add(ganttChart, 0, 8, 3, 1);
        grid.add(timeAxis, 0, 9, 3, 1);

        grid.add(processesTable, 0, 10, 3, 1);
        processesTable.setPrefHeight(220);
        processesTable.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(processesTable, Priority.ALWAYS);

        GridPane.setHalignment(addProcess, HPos.CENTER);
        GridPane.setHalignment(start, HPos.CENTER);
        Scene scene = new Scene(grid, 1100, 750);

        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}