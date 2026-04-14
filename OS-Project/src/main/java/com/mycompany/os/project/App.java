package com.mycompany.os.project;

import com.mycompany.os.project.src.process;
import java.util.ArrayList;
import java.util.HashMap;
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

public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        ObservableList<process> processes = FXCollections.observableArrayList();

        // Configuring Process Values
        ComboBox<String> choices = new ComboBox();
        choices.getItems().addAll("FCFS", "SJF [Preemptive]", "SJF [Non-Preemptive]",
                "Priority [Preemptive]", "Priority [Non-Preemptive]", "Round Robbin");
        choices.setValue("Choose Your Scheduling Method");

        var nameOfProcess = new Label("Type your Process name");
        TextField processName = new TextField();

        var burstOfProcessLabel = new Label("Choose Burst Of Process");
        Spinner<Integer> burstOfProcess = new Spinner<>();
        burstOfProcess.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1)
        );

        var priorityLabel = new Label("Choose Priority Of Process");
        priorityLabel.setDisable(true);
        Spinner<Integer> priority = new Spinner<>();
        priority.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000)
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

        // Timer
        int[] seconds = {0};
        Label timerLabel = new Label();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    seconds[0]++;
                    timerLabel.setText("Time: " + seconds[0] + "s");
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Adding Process
        Button addProcess = new Button("Add Process");
        addProcess.setOnAction(e -> {
            processes.add(new process(burstOfProcess.getValue(), priority.getValue(),
                    processName.getText(), seconds[0]));
        });

        // Execution
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
        start.setOnAction(eh -> {
            switch (choices.getValue()) {
                case "FCFS": {
                    timeline.play();
                    new Thread(() -> {
                        double initSize = ((double) processes.size());
                        while (!processes.isEmpty()) {
                            var p = processes.get(0);

                            Platform.runLater(() -> {
                                execLabel.setText("Executing " + p.processName + " ...");
                            });
                            final double progress = (initSize - ((double) processes.size()) + 1) / initSize;
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
                        if (processes.size() == 0) {
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
                case "SJF [Non-Preemptive]": {
                    timeline.play();

                    new Thread(() -> {
                        ArrayList<process> remaining = new ArrayList<>(processes);
                        double initSize = remaining.size();

                        while (!remaining.isEmpty()) {
                            final int now = currentTime[0];
                            ArrayList<process> available = new ArrayList<>();
                            for (process p : remaining) {
                                if (p.arrivalTime <= now) available.add(p);
                            }

                            if (available.isEmpty()) {
                                currentTime[0]++;
                                try { Thread.sleep(1000L); } catch (Exception e) {}
                                continue;
                            }

                            available.sort((a, b) -> a.burst - b.burst);
                            process p = available.get(0);
                            remaining.remove(p);

                            int startTime = currentTime[0];
                            currentTime[0] += p.burst;

                            double progress = (initSize - remaining.size()) / initSize;

                            Platform.runLater(() -> {
                                execLabel.setText("Executing " + p.processName);
                                bar.setProgress(progress);

                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(p.burst * 25);
                                block.setStyle("-fx-background-color:" + p.color);
                                ganttChart.getChildren().add(new StackPane(block, new Label(p.processName)));
                                timeAxis.getChildren().add(new Label(String.valueOf(startTime)));

                                // Update the table by removing the process from the observable list
                                processes.remove(p);
                            });

                            try { Thread.sleep(p.burst * 1000L); } catch (Exception ex) {}
                        }

                        Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            bar.setProgress(1.0);
                            timeAxis.getChildren().add(new Label(String.valueOf(currentTime[0])));
                        });
                        timeline.stop();
                    }).start();
                    break;
                }
                case "SJF [Preemptive]": {
                    timeline.play();

                    new Thread(() -> {
                        ArrayList<process> remaining = new ArrayList<>(processes);
                        int n = remaining.size();

                        HashMap<process, Integer> rem = new HashMap<>();
                        for (process p : remaining) {
                            rem.put(p, p.burst);
                        }

                        process current = null;
                        int lastSwitchTime = currentTime[0];

                        while (!remaining.isEmpty()) {
                            final int now = currentTime[0];

                            process best = null;
                            int bestTime = Integer.MAX_VALUE;

                            for (process p : remaining) {
                                if (p.arrivalTime <= now) {
                                    int rt = rem.get(p);
                                    if (rt < bestTime) {
                                        bestTime = rt;
                                        best = p;
                                    }
                                }
                            }

                            // No process available - idle
                            if (best == null) {
                                if (current != null) {
                                    process finalCurrent = current;
                                    int segmentStart = lastSwitchTime;
                                    int segmentEnd = now;
                                    Platform.runLater(() -> createGanttBlock(finalCurrent, segmentStart, segmentEnd, ganttChart, timeAxis));
                                    current = null;
                                }
                                try { Thread.sleep(1000); } catch (Exception e) {}
                                currentTime[0]++;
                                continue;
                            }

                            // Context switch occurred
                            if (current != best) {
                                if (current != null) {
                                    process finalCurrent = current;
                                    int segmentStart = lastSwitchTime;
                                    int segmentEnd = now;
                                    Platform.runLater(() -> createGanttBlock(finalCurrent, segmentStart, segmentEnd, ganttChart, timeAxis));
                                }
                                current = best;
                                lastSwitchTime = now;
                                process finalCurrent1 = current;
                                Platform.runLater(() -> execLabel.setText("Executing " + finalCurrent1.processName));
                            }

                            // Execute 1 time unit
                            rem.put(best, rem.get(best) - 1);
                            currentTime[0]++;

                            double progress = (double) (n - remaining.size()) / n;
                            Platform.runLater(() -> bar.setProgress(progress));

                            // Process completed
                            if (rem.get(best) == 0) {
                                int finishStart = lastSwitchTime;
                                int finishEnd = currentTime[0];
                                process finished = best;
                                Platform.runLater(() -> createGanttBlock(finished, finishStart, finishEnd, ganttChart, timeAxis));
                                remaining.remove(best);
                                // Also remove from the observable list for the table
                                Platform.runLater(() -> processes.remove(finished));
                                current = null;
                            }

                            try { Thread.sleep(1000); } catch (Exception e) {}
                        }

                        Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            bar.setProgress(1.0);
                            timeAxis.getChildren().add(new Label(String.valueOf(currentTime[0])));
                        });
                        timeline.stop();
                    }).start();
                    break;
                }
                case "Priority [Non-Preemptive]": {
                    break;
                }
                case "Priority [Preemptive]": {
                    break;
                }
                case "Round Robbin": {
                    break;
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(20);
        grid.setPadding(new javafx.geometry.Insets(20));

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

    /**
     * Helper method to create a Gantt chart block for a process execution segment.
     * @param p         The process being executed
     * @param startTime Start time of the segment
     * @param endTime   End time of the segment
     * @param chart     The HBox representing the Gantt chart
     * @param axis      The HBox representing the time axis
     */
    private void createGanttBlock(process p, int startTime, int endTime, HBox chart, HBox axis) {
        int duration = endTime - startTime;
        if (duration <= 0) return;

        Region block = new Region();
        block.setPrefHeight(30);
        block.setPrefWidth(duration * 25);
        block.setStyle(
                "-fx-background-color: " + p.color + ";" +
                        "-fx-border-color: black;" +
                        "-fx-background-radius: 5;"
        );

        Label label = new Label(p.processName);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        chart.getChildren().add(new StackPane(block, label));
        axis.getChildren().add(new Label(String.valueOf(startTime)));
    }

    public static void main(String[] args) {
        launch();
    }
}