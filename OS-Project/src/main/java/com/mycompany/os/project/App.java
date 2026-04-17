package com.mycompany.os.project;

import com.mycompany.os.project.src.Timeline;
import com.mycompany.os.project.src.process;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 * JavaFX App
 */

public class App extends Application {
    volatile boolean isPaused = false ;
    volatile boolean dynamicSimulationStarted = false;
    
    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        ObservableList<process> processes = FXCollections.observableArrayList();
        final Object processListLock = new Object();
                                ///Configuring Process Values\\\
        ComboBox<String> executionType = new ComboBox<>();
        executionType.getItems().addAll("Static Execution", "Dynamic Execution");
        executionType.setValue("Choose Exection Type");
        
        ComboBox<String> choices = new ComboBox();
        choices.getItems().addAll("FCFS", "SJF [Preemptive]", "SJF [Non-Preemptive]", "Priority [Preemptive]","Priority [Non-Preemptive]", "Round Robbin", "Idle");
        choices.setValue("Choose Your Scheduling Method");
        
        var nameOfProcess = new Label("Type your Proceess name");
        TextField processName = new TextField();
        
        var burstOfProcessLabel = new Label("Choose Burst Of Process");
        Spinner<Integer> burstOfProcess = new Spinner<>();
        burstOfProcess.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000,1)
        );

        var arrivalTimeLabel = new Label("Choose Arrival Time");
        Spinner<Integer> arrivalTime = new Spinner<>();
        arrivalTime.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0,1000,0)
        );

        var priorityLabel = new Label("Choose Priority Of Process");
        priorityLabel.setDisable(true);
        Spinner<Integer> priority = new Spinner<>();
        priority.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000,1)
        );
        priority.setDisable(true);
        
        var quantumLabel = new Label("Choose Quantum Time");
        quantumLabel.setDisable(true);
        Spinner<Integer> quantumBox = new Spinner<>();
        quantumBox.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1,1000)
        );
        quantumBox.setDisable(true);
        
        choices.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) return;

            if ("Priority [Preemptive]".equals(newValue) || "Priority [Non-Preemptive]".equals(newValue)) {
                priority.setDisable(false);
                priorityLabel.setDisable(false);
                quantumBox.setDisable(true);
                quantumLabel.setDisable(true);
            }
            else if("Round Robbin".equals(newValue))
            {
                quantumBox.setDisable(false);
                quantumLabel.setDisable(false);
                priority.setDisable(true);
                priorityLabel.setDisable(true);
            }
            else {
                priority.setDisable(true);
                priorityLabel.setDisable(true);
                quantumBox.setDisable(true);
                quantumLabel.setDisable(true);
            }
        });
        
        var arrTimeLabel = new Label("Arrival Time");
        arrTimeLabel.setDisable(true);
        Spinner<Integer> arrivalTime = new Spinner<>();
        arrivalTime.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000,0));
        arrivalTime.setDisable(true);
       
                                ///Timer\\\
        int[] seconds = {0};
        int[] currentTime = {0};
                                                        int[] displayedTime = {0};
        Label timerLabel = new Label();
        timerLabel.setText("Time: 0s");
                                                        Runnable refreshTimerLabel = () -> Platform.runLater(() -> {
                                                            displayedTime[0] = currentTime[0];
                                                            timerLabel.setText("Time: " + displayedTime[0] + "s");
                                                        });
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                // Keep GUI timer aligned with scheduler clock.
                                                                seconds[0] = currentTime[0];
                                                                displayedTime[0] = currentTime[0];
                                                                timerLabel.setText("Time: " + displayedTime[0] + "s");
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        
                                 ////Adding Process\\\\
        Button addProcess = new Button("Add Process");
        addProcess.setOnAction(e ->
        {
            boolean isDynamicExecution = "Dynamic Execution".equals(executionType.getValue());
            int processArrivalTime;

            if (isDynamicExecution) {
                processArrivalTime = dynamicSimulationStarted ? displayedTime[0] : arrivalTime.getValue();
            } else {
                processArrivalTime = arrivalTime.getValue();
            }

            synchronized (processListLock) {
                processes.add(new process(
                    burstOfProcess.getValue(),
                    priority.getValue(),
                    processName.getText(),
                    processArrivalTime
                ));
            }

        });
        
                                    ////Execution\\\\
        HBox ganttChart = new HBox();
        ganttChart.setSpacing(1);
        ganttChart.setPadding(new javafx.geometry.Insets(10));
        HBox timeAxis = new HBox();
        timeAxis.setSpacing(0);
        timeAxis.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
        
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
        
        var metricsLabel = new Label();
        var execLabel = new Label();
        var bar = new ProgressBar(0);
        Button stopAdding = new Button("Stop");
        stopAdding.setDisable(true);
        stopAdding.setOnAction(eh -> {
            dynamicSimulationStarted = false;
            stopAdding.setDisable(true);
            execLabel.setText("No more new processes");
        });
        Button startDynamic = new Button("Start Dynamic Execution");
        startDynamic.setOnAction(eh->
        {
            dynamicSimulationStarted = true;
            stopAdding.setDisable(false);
            arrivalTime.setDisable(true);
            arrTimeLabel.setDisable(true);
            refreshTimerLabel.run();

            switch (choices.getValue())
            {
                case "FCFS":
                {
                    timeline.play();
                    new Thread(() -> {
                        int totalWT = 0;
                        int totalTAT = 0;
                        int count = 0;
                        double initSize = ((double)processes.size());
                        while (true) {
                            while (isPaused) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {}
                            }

                            process p = null;
                            synchronized (processListLock) {
                                if (processes.isEmpty() && !dynamicSimulationStarted) {
                                    break;
                                }

                                for (process candidate : processes) {
                                    if (candidate.arrivalTime <= currentTime[0]) {
                                        p = candidate;
                                        break;
                                    }
                                }
                            }

                            if (p == null) {

                                int startTime = currentTime[0];

                                Platform.runLater(() -> {
                                    Region block = new Region();
                                    block.setPrefHeight(30);
                                    block.setPrefWidth(25);
                                    block.setStyle("-fx-background-color: #cccccc; -fx-border-color: black;");

                                    StackPane cell = new StackPane(block, new Label("Idle"));
                                    ganttChart.getChildren().add(cell);

                                    Label timeLabel = new Label(String.valueOf(startTime));
                                    timeLabel.setMinWidth(25);
                                    timeAxis.getChildren().add(timeLabel);
                                });

                                try { Thread.sleep(1000); } catch (Exception e) {}

                                currentTime[0]++;
                                refreshTimerLabel.run();
                                continue;
                            }

                            final process running = p;

                            Platform.runLater(() -> {
                                execLabel.setText("Executing " + running.processName + " ...");
                            });
                            final double progress = initSize <= 0 ? 0 : (initSize - ((double) processes.size()) + 1) / initSize;
                            Platform.runLater(() -> {
                                bar.setProgress(progress);
                            });
                            
                            try {
                                while(running.burst != 0)
                                {
                                    while (isPaused) {
                                        try { Thread.sleep(100); } catch (Exception e) {}
                                    }

                                    int tickStart = currentTime[0];
                                    Platform.runLater(() -> {
                                        Region block = new Region();
                                        block.setPrefHeight(30);
                                        block.setPrefWidth(25);
                                        block.setStyle(
                                            "-fx-background-color: " + running.color + ";" +
                                            "-fx-border-color: black;" +
                                            "-fx-background-radius: 5;"
                                        );

                                        Label label = new Label(running.processName);
                                        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                                        StackPane cell = new StackPane();
                                        cell.getChildren().addAll(block, label);
                                        ganttChart.getChildren().add(cell);

                                        Label timeLabel = new Label(String.valueOf(tickStart));
                                        timeLabel.setMinWidth(25);
                                        timeLabel.setStyle("-fx-font-size: 10px;");
                                        timeAxis.getChildren().add(timeLabel);
                                    });

                                    Thread.sleep(1000);
                                    running.burst--;
                                    currentTime[0]++;
                                    refreshTimerLabel.run();
                                    Platform.runLater(processesTable::refresh);
                                }
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            if(running.burst == 0)
                            {
                                running.finishTime = currentTime[0];
                                int wt = running.getWaitingTime();
                                int tat = running.getTurnaroundTime();
                                totalWT += wt;
                                totalTAT += tat;
                                count++;
                            }

                            synchronized (processListLock) {
                                processes.remove(running);
                            }
                        }
                        if(processes.size() == 0)
                        {
                            final double avgWT = count == 0 ? 0 : totalWT / (double) count;
                            final double avgTAT = count == 0 ? 0 : totalTAT / (double) count;
                            Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            metricsLabel.setText("Average Waiting Time: " + String.valueOf(avgWT) + "\n" + "Average Turnaround Time: " + String.valueOf(avgTAT));
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
                    timeline.play();
                    new Thread(() -> {
                        int totalWT = 0;
                        int totalTAT = 0;
                        int count = 0;

                        while (true) {
                            while (isPaused) {
                                try { Thread.sleep(100); } catch (Exception e) {}
                            }

                            process best = null;
                            synchronized (processListLock) {
                                if (processes.isEmpty() && !dynamicSimulationStarted) {
                                    break;
                                }
                                for (process p : processes) {
                                    if (p.arrivalTime <= currentTime[0]) {
                                        if (best == null || p.burst < best.burst) {
                                            best = p;
                                        }
                                    }
                                }
                            }

                            if (best == null) {
                                int startTime = currentTime[0];

                                Platform.runLater(() -> {
                                    Region block = new Region();
                                    block.setPrefHeight(30);
                                    block.setPrefWidth(25);
                                    block.setStyle("-fx-background-color: #cccccc; -fx-border-color: black;");

                                    StackPane cell = new StackPane(block, new Label("Idle"));
                                    ganttChart.getChildren().add(cell);

                                    Label timeLabel = new Label(String.valueOf(startTime));
                                    timeLabel.setMinWidth(25);
                                    timeAxis.getChildren().add(timeLabel);
                                });

                                try { Thread.sleep(1000); } catch (Exception e) {}

                                currentTime[0]++;
                                refreshTimerLabel.run();
                                continue;
                            }

                            process p = best;
                            Platform.runLater(() -> execLabel.setText("Executing " + p.processName + " ..."));

                            while (p.burst != 0) {
                                while (isPaused) {
                                    try { Thread.sleep(100); } catch (Exception e) {}
                                }

                                int tickStart = currentTime[0];
                                Platform.runLater(() -> {
                                    Region block = new Region();
                                    block.setPrefHeight(30);
                                    block.setPrefWidth(25);
                                    block.setStyle("-fx-background-color: " + p.color + ";" +
                                        "-fx-border-color: black;-fx-background-radius: 5;");
                                    Label label = new Label(p.processName);
                                    label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                                    StackPane cell = new StackPane(block, label);
                                    ganttChart.getChildren().add(cell);

                                    Label timeLabel = new Label(String.valueOf(tickStart));
                                    timeLabel.setMinWidth(25);
                                    timeLabel.setStyle("-fx-font-size: 10px;");
                                    timeAxis.getChildren().add(timeLabel);
                                });

                                try { Thread.sleep(1000); } catch (Exception e) {}
                                p.burst--;
                                currentTime[0]++;
                                refreshTimerLabel.run();
                                Platform.runLater(processesTable::refresh);
                            }

                            p.finishTime = currentTime[0];
                            int wt = p.getWaitingTime();
                            int tat = p.getTurnaroundTime();
                            totalWT += wt;
                            totalTAT += tat;
                            count++;

                            synchronized (processListLock) {
                                processes.remove(p);
                            }
                        }

                        final double avgWT = count == 0 ? 0 : totalWT / (double) count;
                        final double avgTAT = count == 0 ? 0 : totalTAT / (double) count;
                        Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            metricsLabel.setText("Average Waiting Time: " + avgWT +
                                "\nAverage Turnaround Time: " + avgTAT);
                            Label end = new Label(String.valueOf(currentTime[0]));
                            end.setStyle("-fx-font-size: 10px;");
                            timeAxis.getChildren().add(end);
                        });

                        seconds[0] = 0;
                        timeline.stop();
                    }).start();
                    break;
                }
                
                case "SJF [Preemptive]":    
                {
                    timeline.play();
                    new Thread(() -> {
                        HashMap<process, Integer> remaining = new HashMap<>();
                        int totalWT = 0;
                        int totalTAT = 0;
                        int count = 0;

                        while (true) {
                            while (isPaused) {
                                try { Thread.sleep(100); } catch (Exception e) {}
                            }

                            synchronized (processListLock) {
                                for (process p : processes) {
                                    remaining.putIfAbsent(p, p.burst);
                                }
                            }

                            boolean noProcessesLeft;
                            synchronized (processListLock) {
                                noProcessesLeft = processes.isEmpty();
                            }

                            if (noProcessesLeft && !dynamicSimulationStarted) {
                                final double avgWT = count == 0 ? 0 : totalWT / (double) count;
                                final double avgTAT = count == 0 ? 0 : totalTAT / (double) count;
                                Platform.runLater(() -> {
                                    execLabel.setText("Done!");
                                    metricsLabel.setText("Average Waiting Time: " + avgWT +
                                        "\nAverage Turnaround Time: " + avgTAT);
                                    Label end = new Label(String.valueOf(currentTime[0]));
                                    timeAxis.getChildren().add(end);
                                });
                                seconds[0] = 0;
                                timeline.stop();
                                break;
                            }

                            process best = null;
                            int min = Integer.MAX_VALUE;
                            synchronized (processListLock) {
                                for (process p : processes) {
                                    if (p.arrivalTime <= currentTime[0]) {
                                        int rem = remaining.get(p);
                                        if (rem < min) {
                                            min = rem;
                                            best = p;
                                        }
                                    }
                                }
                            }

                            if (best == null) {
                                int startTime = currentTime[0];

                                Platform.runLater(() -> {
                                    Region block = new Region();
                                    block.setPrefHeight(30);
                                    block.setPrefWidth(25);
                                    block.setStyle("-fx-background-color: #cccccc; -fx-border-color: black;");

                                    StackPane cell = new StackPane(block, new Label("Idle"));
                                    ganttChart.getChildren().add(cell);

                                    Label timeLabel = new Label(String.valueOf(startTime));
                                    timeLabel.setMinWidth(25);
                                    timeAxis.getChildren().add(timeLabel);
                                });

                                try { Thread.sleep(1000); } catch (Exception e) {}

                                currentTime[0]++;
                                refreshTimerLabel.run();
                                continue;
                            }

                            process current = best;
                            Platform.runLater(() -> execLabel.setText("Executing " + current.processName + " ..."));

                            int startTime = currentTime[0];
                            remaining.put(current, remaining.get(current) - 1);
                            current.burst--;
                            currentTime[0]++;

                            Platform.runLater(() -> {
                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(25);
                                block.setStyle("-fx-background-color: " + current.color + ";" +
                                    "-fx-border-color: black;-fx-background-radius: 5;");
                                StackPane cell = new StackPane(block, new Label(current.processName));
                                ganttChart.getChildren().add(cell);
                            });

                            Platform.runLater(() -> {
                                Label timeLabel = new Label(String.valueOf(startTime));
                                timeLabel.setMinWidth(25);
                                timeAxis.getChildren().add(timeLabel);
                            });

                            long finished = remaining.values().stream().filter(v -> v == 0).count();
                            double progress = finished / (double) remaining.size();
                            Platform.runLater(() -> bar.setProgress(progress));
                            Platform.runLater(() -> processesTable.refresh());

                            if (remaining.get(current) == 0) {
                                current.finishTime = currentTime[0];
                                int wt = current.getWaitingTime();
                                int tat = current.getTurnaroundTime();
                                totalWT += wt;
                                totalTAT += tat;
                                count++;
                                synchronized (processListLock) {
                                    processes.remove(current);
                                    remaining.remove(current);
                                }
                            }

                            try { Thread.sleep(1000); } catch (Exception e) {}
                        }
                    }).start();
                    break;
                }
                case "Priority [Non-Preemptive]": {
                    Timeline timeline = new Timeline();
                    int time = 0;
                    int completed = 0;

                    while (completed < processes.size()) {
                        ArrayList<process> available = new ArrayList<>();
                        for (process p : processes) {
                            if (p.arrivaltime <= time && !p.finished) {
                                available.add(p);
                            }
                        }

                        if (available.isEmpty()) {
                            timeline.addSlot("IDLE", time, time + 1);
                            time++;
                            continue;
                        }

                        available.sort((p1, p2) -> Integer.compare(p1.priority, p2.priority));

                        process current = available.get(0);
                        current.finishtime = time + current.burst;
                        timeline.addSlot(current.processName, time, current.finishtime);
                        time = current.finishtime;
                        current.finished = true;
                        completed++;
                    }
                    break;
                }
                
                case "Priority [Preemptive]":
                {
                    Timeline timeline = new Timeline();
                    int time = 0;
                    int completed = 0;

                    int[] remaining = new int[processes.size()];
                    for (int i = 0; i < processes.size(); i++) {
                        remaining[i] = processes.get(i).burst;
                    }

                    while (completed < processes.size()) {
                        ArrayList<process> available = new ArrayList<>();
                        for (process p : processes) {
                            if (p.arrivaltime <= time && !p.finished) {
                                available.add(p);
                            }
                        }

                        if (available.isEmpty()) {
                            timeline.addSlot("IDLE", time, time + 1);
                            time++;
                            continue;
                        }

                        available.sort((p1, p2) -> Integer.compare(p1.priority, p2.priority));

                        process current = available.get(0);
                        int currentIndex = processes.indexOf(current);
                        timeline.addSlot(current.processName, time, time + 1);
                        remaining[currentIndex]--;
                        time++;

                        if (remaining[currentIndex] == 0) {
                            current.finishtime = time;
                            current.finished = true;
                            completed++;
                        }
                    }
                    timeline.mergeConsecutive();
                    break;
                }
                
                case "Round Robbin": 
                {
                    int quantum = quantumBox.getValue();
                    timeline.play();
                    new Thread(() -> {
                        double initSize = (double) processes.size();
                        int totalWT = 0;
                        int totalTAT = 0;
                        int count = 0;
                        Queue<process> readyQueue = new LinkedList<>();

                        while (true) {
                            while (isPaused) {
                                try { Thread.sleep(100); } catch (Exception e) {}
                            }

                            process p = null;
                            synchronized (processListLock) {
                                for (process candidate : processes) {
                                    if (candidate.arrivalTime <= currentTime[0] && !readyQueue.contains(candidate)) {
                                        readyQueue.offer(candidate);
                                    }
                                }

                                if (!readyQueue.isEmpty()) {
                                    p = readyQueue.poll();
                                }
                            }

                            if (p == null) {
                                boolean hasPendingWork;
                                synchronized (processListLock) {
                                    hasPendingWork = !processes.isEmpty();
                                }

                                if (!dynamicSimulationStarted && !hasPendingWork && readyQueue.isEmpty()) {
                                    break;
                                }

                                int startTime = currentTime[0];

                                Platform.runLater(() -> {
                                    Region block = new Region();
                                    block.setPrefHeight(30);
                                    block.setPrefWidth(25);
                                    block.setStyle("-fx-background-color: #cccccc; -fx-border-color: black;");

                                    StackPane cell = new StackPane(block, new Label("Idle"));
                                    ganttChart.getChildren().add(cell);

                                    Label timeLabel = new Label(String.valueOf(startTime));
                                    timeLabel.setMinWidth(25);
                                    timeAxis.getChildren().add(timeLabel);
                                });

                                try { Thread.sleep(1000); } catch (Exception e) {}

                                currentTime[0]++;
                                refreshTimerLabel.run();
                                continue;
                            }

                            final process running = p;
                            int executeTime = Math.min(running.burst, quantum);

                            Platform.runLater(() -> execLabel.setText("Executing " + running.processName + " ..."));

                            final double progress = (initSize - (double) processes.size() + 1) / initSize;
                            Platform.runLater(() -> bar.setProgress(initSize <= 0 ? 0 : progress));

                            int startTime = currentTime[0];

                            Platform.runLater(() -> {
                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(executeTime * 25);
                                block.setStyle("-fx-background-color: " + running.color + ";" +
                                    "-fx-border-color: black;-fx-background-radius: 5;");
                                Label label = new Label(running.processName);
                                label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                                StackPane cell = new StackPane();
                                cell.getChildren().addAll(block, label);
                                ganttChart.getChildren().add(cell);
                            });

                            Platform.runLater(() -> {
                                Label timeLabel = new Label(String.valueOf(startTime));
                                timeLabel.setMinWidth(executeTime * 25);
                                timeLabel.setStyle("-fx-font-size: 10px;");
                                timeAxis.getChildren().add(timeLabel);
                            });

                            for (int i = 0; i < executeTime; i++) {
                                while (isPaused) {
                                    try { Thread.sleep(100); } catch (Exception e) {}
                                }
                                try { Thread.sleep(1000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                                running.burst--;
                                currentTime[0]++;
                                refreshTimerLabel.run();
                                Platform.runLater(() -> processesTable.refresh());
                            }

                            if (running.burst > 0) {
                                readyQueue.offer(running);
                            } else {
                                running.finishTime = currentTime[0];
                                int wt = running.getWaitingTime();
                                int tat = running.getTurnaroundTime();
                                totalWT += wt;
                                totalTAT += tat;
                                count++;
                                synchronized (processListLock) {
                                    processes.remove(running);
                                }
                            }
                        }

                        final double avgWT = count == 0 ? 0 : totalWT / (double) count;
                        final double avgTAT = count == 0 ? 0 : totalTAT / (double) count;
                        Platform.runLater(() -> {
                            execLabel.setText("Done!");
                            metricsLabel.setText("Average Waiting Time: " + avgWT +
                                "\nAverage Turnaround Time: " + avgTAT);
                            Label end = new Label(String.valueOf(currentTime[0]));
                            end.setStyle("-fx-font-size: 10px;");
                            timeAxis.getChildren().add(end);
                        });

                        seconds[0] = 0;
                        timeline.stop();
                    }).start();
                    break;
                }
                
                default:
                {
                    timeline.play();

                    new Thread(() -> {
                        while (true) {

                            while (isPaused) {
                                try { Thread.sleep(100); } catch (Exception e) {}
                            }

                            int startTime = currentTime[0];

                            Platform.runLater(() -> {
                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(25);
                                block.setStyle("-fx-background-color: #cccccc; -fx-border-color: black;");

                                StackPane cell = new StackPane(block, new Label("Idle"));
                                ganttChart.getChildren().add(cell);

                                Label timeLabel = new Label(String.valueOf(startTime));
                                timeLabel.setMinWidth(25);
                                timeAxis.getChildren().add(timeLabel);
                            });

                            try { Thread.sleep(1000); } catch (Exception e) {}

                            currentTime[0]++;
                            refreshTimerLabel.run();

                            // stop if user stops dynamic execution
                            if (!dynamicSimulationStarted) break;
                        }
                    }).start();

                    break;
                }
            }
        });
        
        Button startStatic = new Button("Start Static Execution");
        startStatic.setOnAction(eh -> {
            switch(choices.getValue())
            {
                case "FCFS":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes) {
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));
                    }

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    list.sort((a, b) -> Integer.compare(a.arrivalTime, b.arrivalTime));

                    int t = 0;
                    int totalWT = 0;
                    int totalTAT = 0;

                    for (process p : list) {

                        if (t < p.arrivalTime) {

                            int idleTime = p.arrivalTime - t;

                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(idleTime * 25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(t));
                            idleLabel.setMinWidth(idleTime * 25);
                            timeAxis.getChildren().add(idleLabel);

                            t = p.arrivalTime;
                        }

                        int start = t;

                        Region block = new Region();
                        block.setPrefHeight(30);
                        block.setPrefWidth(p.burst * 25);
                        block.setStyle("-fx-background-color:" + p.color + "; -fx-border-color:black;");

                        StackPane cell = new StackPane(block, new Label(p.processName));
                        ganttChart.getChildren().add(cell);

                        Label timeLabel = new Label(String.valueOf(start));
                        timeLabel.setMinWidth(p.burst * 25);
                        timeAxis.getChildren().add(timeLabel);

                        t += p.burst;

                        p.finishTime = t;

                        int tat = p.finishTime - p.arrivalTime;
                        int wt = tat - p.burst;

                        totalWT += wt;
                        totalTAT += tat;
                    }

                    Label end = new Label(String.valueOf(t));
                    end.setMinWidth(25);
                    timeAxis.getChildren().add(end);

                    metricsLabel.setText(
                        String.format("Average Waiting Time: %.2f\nAverage Turnaround Time: %.2f",
                            totalWT / (double) list.size(),
                            totalTAT / (double) list.size())
                    );

                    break;
                }
                case "SJF [Non-Preemptive]":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes) {
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));
                    }

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    boolean[] done = new boolean[list.size()];

                    int t = 0;
                    int completed = 0;

                    int totalWT = 0;
                    int totalTAT = 0;

                    while (completed < list.size()) {

                        process best = null;
                        int idx = -1;

                        for (int i = 0; i < list.size(); i++) {

                            process p = list.get(i);

                            if (!done[i] && p.arrivalTime <= t) {

                                if (best == null ||
                                        p.burst < best.burst ||
                                        (p.burst == best.burst && p.arrivalTime < best.arrivalTime)) {
                                    best = p;
                                    idx = i;
                                }
                            }
                        }

                        if (best == null) {

                            int nextArrival = Integer.MAX_VALUE;

                            for (int i = 0; i < list.size(); i++) {
                                if (!done[i]) {
                                    nextArrival = Math.min(nextArrival, list.get(i).arrivalTime);
                                }
                            }

                            int idleTime = nextArrival - t;

                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(idleTime * 25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(t));
                            idleLabel.setMinWidth(idleTime * 25);
                            timeAxis.getChildren().add(idleLabel);

                            t = nextArrival;
                            continue;
                        }

                        int start = t;

                        Region block = new Region();
                        block.setPrefHeight(30);
                        block.setPrefWidth(best.burst * 25);
                        block.setStyle("-fx-background-color:" + best.color + "; -fx-border-color:black;");

                        StackPane cell = new StackPane(block, new Label(best.processName));
                        ganttChart.getChildren().add(cell);

                        Label timeLabel = new Label(String.valueOf(start));
                        timeLabel.setMinWidth(best.burst * 25);
                        timeLabel.setPrefWidth(best.burst * 25);
                        timeLabel.setStyle("-fx-font-size:10px;");
                        timeAxis.getChildren().add(timeLabel);

                        t += best.burst;

                        best.finishTime = t;

                        int tat = t - best.arrivalTime;
                        int wt = tat - best.burst;

                        totalWT += wt;
                        totalTAT += tat;

                        done[idx] = true;
                        completed++;
                    }

                    Label end = new Label(String.valueOf(t));
                    end.setMinWidth(25);
                    timeAxis.getChildren().add(end);

                    metricsLabel.setText(
                        "Average Waiting Time: " + (totalWT / (double) list.size()) +
                        "\nAverage Turnaround Time: " + (totalTAT / (double) list.size())
                    );

                    break;
                }
                case "SJF [Preemptive]":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes) {
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));
                    }

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    HashMap<process, Integer> rem = new HashMap<>();
                    for (process p : list) rem.put(p, p.burst);

                    boolean[] done = new boolean[list.size()];

                    int t = 0;
                    int completed = 0;

                    int totalWT = 0;
                    int totalTAT = 0;

                    process last = null;
                    int blockStart = 0;

                    while (completed < list.size()) {

                        process best = null;
                        int min = Integer.MAX_VALUE;
                        int bestIdx = -1;

                        for (int i = 0; i < list.size(); i++) {

                            process p = list.get(i);

                            if (p.arrivalTime <= t && rem.get(p) > 0) {

                                if (rem.get(p) < min) {
                                    min = rem.get(p);
                                    best = p;
                                    bestIdx = i;
                                }
                            }
                        }

                        if (best == null) {

                            if (last != null) {
                                int duration = t - blockStart;

                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(duration * 25);
                                block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                                StackPane cell = new StackPane(block, new Label(last.processName));
                                ganttChart.getChildren().add(cell);

                                Label timeLabel = new Label(String.valueOf(blockStart));
                                timeLabel.setMinWidth(duration * 25);
                                timeAxis.getChildren().add(timeLabel);

                                last = null;
                            }

                            int idleStart = t;

                            t++;

                            // build idle block dynamically
                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(idleStart));
                            idleLabel.setMinWidth(25);
                            timeAxis.getChildren().add(idleLabel);

                            continue;
                        }

                        // context switch → close previous block
                        if (best != last) {

                            if (last != null) {
                                int duration = t - blockStart;

                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(duration * 25);
                                block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                                StackPane cell = new StackPane(block, new Label(last.processName));
                                ganttChart.getChildren().add(cell);

                                Label timeLabel = new Label(String.valueOf(blockStart));
                                timeLabel.setMinWidth(duration * 25);
                                timeLabel.setPrefWidth(duration * 25);
                                timeLabel.setStyle("-fx-font-size:10px;");
                                timeAxis.getChildren().add(timeLabel);
                            }

                            last = best;
                            blockStart = t;
                        }

                        // execute 1 unit
                        rem.put(best, rem.get(best) - 1);
                        t++;

                        if (rem.get(best) == 0 && !done[bestIdx]) {

                            done[bestIdx] = true;
                            completed++;

                            best.finishTime = t;

                            int tat = t - best.arrivalTime;
                            int wt = tat - best.burst;

                            totalWT += wt;
                            totalTAT += tat;
                        }
                    }

                    // close last block
                    if (last != null) {
                        int duration = t - blockStart;

                        Region block = new Region();
                        block.setPrefHeight(30);
                        block.setPrefWidth(duration * 25);
                        block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                        StackPane cell = new StackPane(block, new Label(last.processName));
                        ganttChart.getChildren().add(cell);

                        Label timeLabel = new Label(String.valueOf(blockStart));
                        timeLabel.setMinWidth(duration * 25);
                        timeLabel.setPrefWidth(duration * 25);
                        timeLabel.setStyle("-fx-font-size:10px;");
                        timeAxis.getChildren().add(timeLabel);
                    }

                    Label end = new Label(String.valueOf(t));
                    end.setMinWidth(25);
                    timeAxis.getChildren().add(end);

                    metricsLabel.setText(
                        "Average Waiting Time: " + (totalWT / (double) list.size()) +
                        "\nAverage Turnaround Time: " + (totalTAT / (double) list.size())
                    );

                    break;
                }
                case "Priority [Non-Preemptive]":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes) {
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));
                    }

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    boolean[] done = new boolean[list.size()];

                    int t = 0;
                    int completed = 0;

                    int totalWT = 0;
                    int totalTAT = 0;

                    while (completed < list.size()) {

                        process best = null;
                        int bestIdx = -1;

                        for (int i = 0; i < list.size(); i++) {

                            process p = list.get(i);

                            if (!done[i] && p.arrivalTime <= t) {

                                if (best == null ||
                                        p.priority < best.priority ||
                                        (p.priority == best.priority && p.arrivalTime < best.arrivalTime)) {
                                    best = p;
                                    bestIdx = i;
                                }
                            }
                        }

                        if (best == null) {

                            int nextArrival = Integer.MAX_VALUE;

                            for (int i = 0; i < list.size(); i++) {
                                if (!done[i]) {
                                    nextArrival = Math.min(nextArrival, list.get(i).arrivalTime);
                                }
                            }

                            int idleTime = nextArrival - t;

                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(idleTime * 25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(t));
                            idleLabel.setMinWidth(idleTime * 25);
                            timeAxis.getChildren().add(idleLabel);

                            t = nextArrival;
                            continue;
                        }

                        int start = t;

                        Region block = new Region();
                        block.setPrefHeight(30);
                        block.setPrefWidth(best.burst * 25);
                        block.setStyle("-fx-background-color:" + best.color + "; -fx-border-color:black;");

                        StackPane cell = new StackPane(block, new Label(best.processName));
                        ganttChart.getChildren().add(cell);

                        Label timeLabel = new Label(String.valueOf(start));
                        timeLabel.setMinWidth(best.burst * 25);
                        timeLabel.setPrefWidth(best.burst * 25);
                        timeLabel.setStyle("-fx-font-size:10px;");
                        timeAxis.getChildren().add(timeLabel);

                        t += best.burst;

                        best.finishTime = t;

                        int tat = t - best.arrivalTime;
                        int wt = tat - best.burst;

                        totalWT += wt;
                        totalTAT += tat;

                        done[bestIdx] = true;
                        completed++;
                    }

                    Label end = new Label(String.valueOf(t));
                    end.setMinWidth(25);
                    timeAxis.getChildren().add(end);

                    metricsLabel.setText(
                        "Average Waiting Time: " + (totalWT / (double) list.size()) +
                        "\nAverage Turnaround Time: " + (totalTAT / (double) list.size())
                    );

                    break;
                }
                case "Priority [Preemptive]":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes) {
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));
                    }

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    HashMap<process, Integer> rem = new HashMap<>();
                    for (process p : list) {
                        rem.put(p, p.burst);
                    }

                    boolean[] done = new boolean[list.size()];

                    int t = 0;
                    int completed = 0;

                    int totalWT = 0;
                    int totalTAT = 0;

                    process last = null;
                    int blockStart = 0;

                    while (completed < list.size()) {

                        process best = null;
                        int bestPriority = Integer.MAX_VALUE;
                        int bestIdx = -1;

                        for (int i = 0; i < list.size(); i++) {

                            process p = list.get(i);

                            if (p.arrivalTime <= t && rem.get(p) > 0) {

                                if (p.priority < bestPriority) {
                                    bestPriority = p.priority;
                                    best = p;
                                    bestIdx = i;
                                }
                            }
                        }

                        if (best == null) {

                            if (last != null) {
                                int duration = t - blockStart;

                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(duration * 25);
                                block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                                StackPane cell = new StackPane(block, new Label(last.processName));
                                ganttChart.getChildren().add(cell);

                                Label timeLabel = new Label(String.valueOf(blockStart));
                                timeLabel.setMinWidth(duration * 25);
                                timeAxis.getChildren().add(timeLabel);

                                last = null;
                            }

                            int idleStart = t;

                            t++;

                            // build idle block dynamically
                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(idleStart));
                            idleLabel.setMinWidth(25);
                            timeAxis.getChildren().add(idleLabel);

                            continue;
                        }

                        // context switch handling
                        if (best != last) {

                            if (last != null) {
                                int duration = t - blockStart;

                                Region block = new Region();
                                block.setPrefHeight(30);
                                block.setPrefWidth(duration * 25);
                                block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                                StackPane cell = new StackPane(block, new Label(last.processName));
                                ganttChart.getChildren().add(cell);

                                Label timeLabel = new Label(String.valueOf(blockStart));
                                timeLabel.setMinWidth(duration * 25);
                                timeLabel.setPrefWidth(duration * 25);
                                timeLabel.setStyle("-fx-font-size:10px;");
                                timeAxis.getChildren().add(timeLabel);
                            }

                            last = best;
                            blockStart = t;
                        }

                        // execute 1 unit
                        rem.put(best, rem.get(best) - 1);
                        t++;

                        if (rem.get(best) == 0 && !done[bestIdx]) {

                            done[bestIdx] = true;
                            completed++;

                            best.finishTime = t;

                            int tat = t - best.arrivalTime;
                            int wt = tat - best.burst;

                            totalWT += wt;
                            totalTAT += tat;
                        }
                    }

                    // close last running block
                    if (last != null) {
                        int duration = t - blockStart;

                        Region block = new Region();
                        block.setPrefHeight(30);
                        block.setPrefWidth(duration * 25);
                        block.setStyle("-fx-background-color:" + last.color + "; -fx-border-color:black;");

                        StackPane cell = new StackPane(block, new Label(last.processName));
                        ganttChart.getChildren().add(cell);

                        Label timeLabel = new Label(String.valueOf(blockStart));
                        timeLabel.setMinWidth(duration * 25);
                        timeLabel.setPrefWidth(duration * 25);
                        timeLabel.setStyle("-fx-font-size:10px;");
                        timeAxis.getChildren().add(timeLabel);
                    }

                    Label end = new Label(String.valueOf(t));
                    end.setMinWidth(25);
                    timeAxis.getChildren().add(end);

                    metricsLabel.setText(
                        "Average Waiting Time: " + (totalWT / (double) list.size()) +
                        "\nAverage Turnaround Time: " + (totalTAT / (double) list.size())
                    );

                    break;
                }
                case "Round Robbin":
                {
                    ganttChart.getChildren().clear();
                    timeAxis.getChildren().clear();

                    int quantum = quantumBox.getValue();

                    ArrayList<process> list = new ArrayList<>();
                    for (process p : processes)
                        list.add(new process(p.burst, p.priority, p.processName, p.arrivalTime));

                    if (list.isEmpty()) {
                        metricsLabel.setText("No processes to execute.");
                        break;
                    }

                    list.sort((a, b) -> Integer.compare(a.arrivalTime, b.arrivalTime));

                    Queue<process> q = new LinkedList<>();
                    HashMap<process, Integer> rem = new HashMap<>();
                    boolean[] visited = new boolean[list.size()];

                    for (process p : list)
                        rem.put(p, p.burst);

                    int t = 0;
                    int i = 0;

                    int totalWT = 0, totalTAT = 0;
                    int completed = 0;

                    while (completed < list.size()) {

                        // add newly arrived processes
                        while (i < list.size() && list.get(i).arrivalTime <= t) {
                            q.add(list.get(i));
                            visited[i] = true;
                            i++;
                        }

                        if (q.isEmpty()) {

                            int nextArrival = Integer.MAX_VALUE;

                            for (int j = i; j < list.size(); j++) {
                                nextArrival = Math.min(nextArrival, list.get(j).arrivalTime);
                            }

                            int idleTime = nextArrival - t;

                            Region idle = new Region();
                            idle.setPrefHeight(30);
                            idle.setPrefWidth(idleTime * 25);
                            idle.setStyle("-fx-background-color:#cccccc; -fx-border-color:black;");

                            StackPane idleCell = new StackPane(idle, new Label("Idle"));
                            ganttChart.getChildren().add(idleCell);

                            Label idleLabel = new Label(String.valueOf(t));
                            idleLabel.setMinWidth(idleTime * 25);
                            timeAxis.getChildren().add(idleLabel);

                            t = nextArrival;
                            continue;
                        }

                        process p = q.poll();

                        int exec = Math.min(quantum, rem.get(p));
                        int start = t;

                        Platform.runLater(() -> {
                            Region block = new Region();
                            block.setPrefWidth(exec * 25);
                            block.setPrefHeight(30);
                            block.setStyle("-fx-background-color:" + p.color + ";");

                            Label label = new Label(p.processName);
                            StackPane cell = new StackPane(block, label);

                            ganttChart.getChildren().add(cell);

                            Label timeLabel = new Label(String.valueOf(start));
                            timeAxis.getChildren().add(timeLabel);
                        });

                        t += exec;
                        rem.put(p, rem.get(p) - exec);


                        while (i < list.size() && list.get(i).arrivalTime <= t) {
                            q.add(list.get(i));
                            visited[i] = true;
                            i++;
                        }

                        if (rem.get(p) > 0) {
                            q.add(p);
                        } else {
                            p.finishTime = t;

                            int tat = t - p.arrivalTime;
                            int wt = tat - p.burst;

                            totalWT += wt;
                            totalTAT += tat;

                            completed++;
                        }
                    }

                    timeAxis.getChildren().add(new Label(String.valueOf(t)));

                    metricsLabel.setText(
                        "Average Waiting Time: " + (totalWT / (double) list.size()) +
                        "\nAverage Turnaround Time: " + (totalTAT / (double) list.size())
                    );

                    break;
                }
            }
        });
        
        
        executionType.valueProperty().addListener((obs, oldVaue, newValue) -> {
            if(newValue == null)
                return;
            if("Static Execution".equals(newValue))
            {
                arrivalTime.setDisable(false);
                arrTimeLabel.setDisable(false);
                startDynamic.setDisable(true);
                stopAdding.setDisable(true);
                startStatic.setDisable(false);
            }
            else if("Dynamic Execution".equals(newValue))
            {

                boolean canEditArrival = !dynamicSimulationStarted;
                arrivalTime.setDisable(!canEditArrival);
                arrTimeLabel.setDisable(!canEditArrival);
                startDynamic.setDisable(false);
                stopAdding.setDisable(!dynamicSimulationStarted);
                startStatic.setDisable(true);
            }
            else
            {
                arrivalTime.setDisable(true);
                arrTimeLabel.setDisable(true);
                startDynamic.setDisable(true);
                stopAdding.setDisable(true);
                startStatic.setDisable(true);
            }
        });
        
        Button clear = new Button("Clear");
        clear.setOnAction(eh->
        {
            processes.clear();
            ganttChart.getChildren().clear();
            timeAxis.getChildren().clear();
            bar.setProgress(0);
            execLabel.setText("Cleared");
            currentTime[0] = 0;
            seconds[0] = 0;
            displayedTime[0] = 0;
            dynamicSimulationStarted = false;
            stopAdding.setDisable(true);
            refreshTimerLabel.run();

            if ("Dynamic Execution".equals(executionType.getValue())) {
                arrivalTime.setDisable(false);
                arrTimeLabel.setDisable(false);
            }

            timerLabel.setText("Time: 0s");
        });
        
        Button pause = new Button("Pause");
        pause.setOnAction(eh -> {
            if(!isPaused)
            {
                isPaused = true;
                timeline.pause();
                pause.setText("Resume");
            }
            else if(isPaused)
            {
                isPaused = false;
                synchronized (this) {
                    notifyAll();
                }
                timeline.play();
                pause.setText("Pause");
            }
        });
        
        
        GridPane grid = new GridPane();

        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(35);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(35);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30);

        grid.getColumnConstraints().addAll(col1, col2, col3);

        for (int i = 0; i < 13; i++) {
            RowConstraints r = new RowConstraints();
            r.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(r);
        }

        // IMPORTANT FIXES
        grid.getRowConstraints().get(10).setVgrow(Priority.ALWAYS); // gantt
        grid.getRowConstraints().get(11).setVgrow(Priority.ALWAYS); // table
        grid.getRowConstraints().get(12).setVgrow(Priority.NEVER);  // metrics OK
        grid.getRowConstraints().get(12).setMinHeight(Region.USE_PREF_SIZE);
        GridPane.setValignment(metricsLabel, VPos.CENTER);


        // ===================== INPUT CONTROLS =====================

        grid.add(executionType, 0, 0, 3, 1);
        grid.add(choices, 0, 1, 3, 1);

        grid.add(nameOfProcess, 0, 2);
        grid.add(processName, 1, 2, 2, 1);

        grid.add(burstOfProcessLabel, 0, 3);
        grid.add(burstOfProcess, 1, 3);

        grid.add(priorityLabel, 0, 4);
        grid.add(priority, 1, 4);

        grid.add(quantumLabel, 0, 5);
        grid.add(quantumBox, 1, 5);

        HBox arrivalBox = new HBox(10, arrTimeLabel, arrivalTime);
        grid.add(arrivalBox, 2, 3);

        // ===================== BUTTON ROW =====================

        HBox buttonRow = new HBox(15, clear, addProcess, startDynamic, stopAdding, startStatic, pause);
        buttonRow.setPadding(new javafx.geometry.Insets(10));
        grid.add(buttonRow, 0, 6, 3, 1);


        // ===================== STATUS =====================

        grid.add(timerLabel, 0, 7, 3, 1);
        GridPane.setHalignment(timerLabel, HPos.CENTER);

        grid.add(execLabel, 0, 8, 3, 1);
        GridPane.setHalignment(execLabel, HPos.CENTER);


        // ===================== PROGRESS BAR =====================

        bar.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(bar, Priority.ALWAYS);
        grid.add(bar, 0, 9, 3, 1);


        // ===================== GANTT + TIME AXIS (FIXED) =====================

        ganttChart.setSpacing(1);
        ganttChart.setMinHeight(35);

        timeAxis.setSpacing(0);
        timeAxis.setMinHeight(25);
        timeAxis.setStyle("-fx-alignment: center-left;");


        ganttChart.setFillHeight(true);

        // Scroll containers
        ScrollPane ganttScroll = new ScrollPane(ganttChart);
        ganttScroll.setFitToHeight(false);
        ganttScroll.setPannable(true);

        ScrollPane timeScroll = new ScrollPane(timeAxis);
        timeScroll.setFitToHeight(false);
        timeScroll.setPannable(true);

        // stack them vertically
        VBox ganttBox = new VBox(5, ganttScroll, timeScroll);

        GridPane.setHgrow(ganttBox, Priority.ALWAYS);
        GridPane.setVgrow(ganttBox, Priority.ALWAYS);

        grid.add(ganttBox, 0, 10, 3, 1);


        // ===================== TABLE =====================

        processesTable.setMaxHeight(Double.MAX_VALUE);
        processesTable.setMaxWidth(Double.MAX_VALUE);

        GridPane.setHgrow(processesTable, Priority.ALWAYS);
        GridPane.setVgrow(processesTable, Priority.ALWAYS);

        grid.add(processesTable, 0, 11, 3, 1);


        // ===================== METRICS =====================

        metricsLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10;"
        );

        metricsLabel.setWrapText(true);
        metricsLabel.setMinHeight(40);
        metricsLabel.setPrefHeight(60);
        metricsLabel.setMaxWidth(Double.MAX_VALUE);

        grid.add(metricsLabel, 0, 12, 3, 1);
        GridPane.setHgrow(metricsLabel, Priority.ALWAYS);

        


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