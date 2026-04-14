package com.mycompany.os.project;

import com.mycompany.os.project.src.Timeline;
import com.mycompany.os.project.src.process;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();
        ArrayList<process> processes = new ArrayList<>();
        
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
        
        Button addProcess = new Button("Add Process");
        addProcess.setOnAction(e ->
        {
            if(priority.getValue()== -1)
            {
                processes.add(new process(burstOfProcess.getValue(),0,processName.getText()));
            }
            else
            {
                processes.add(new process(burstOfProcess.getValue(),priority.getValue(),processName.getText()));
            }
        });
        
        var execLabel = new Label();
        var bar = new ProgressBar(0);
        Button start = new Button("Start Execution");
        start.setOnAction(eh->
        {
            switch (choices.getValue())
            {
                case "FCFS":
                {
                    new Thread(() -> {
                        for (int i = 0; i< processes.size(); i++) {
                            var p = processes.get(i);
                            Platform.runLater(() -> {
                                execLabel.setText("Executing " + p.processName + " ...");
                            });
                            final double progress = (i+1)/((double)processes.size());
                            Platform.runLater(() -> {
                                bar.setProgress(progress);
                            });

                            try {
                                Thread.sleep(p.burst * 1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
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
                    break;
                }
            }
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(20); 
        grid.setVgap(15); 
        grid.setPadding(new javafx.geometry.Insets(20, 20, 20, 20)); 
        grid.add(choices, 1, 0);
        grid.add(nameOfProcess, 0, 1);
        grid.add(processName, 1, 1);
        grid.add(burstOfProcessLabel, 0, 2);
        grid.add(burstOfProcess, 1, 2);
        grid.add(priorityLabel, 0, 3);
        grid.add(priority, 1, 3);
        grid.add(addProcess, 1, 4); 
        grid.add(start, 2, 4);      
        grid.add(bar,  0, 7, 3, 1);
        grid.add(execLabel, 0, 6, 3, 1); 

        GridPane.setHalignment(addProcess, javafx.geometry.HPos.CENTER);
        GridPane.setHalignment(start, javafx.geometry.HPos.CENTER);
        GridPane.setHalignment(execLabel, javafx.geometry.HPos.CENTER);
        GridPane.setHalignment(bar, javafx.geometry.HPos.CENTER);

        Scene scene = new Scene(grid, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}