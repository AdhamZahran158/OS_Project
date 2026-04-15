package com.mycompany.os.project.src;

import java.util.Random;

public class process {
    public String processName;
    public int burst;
    public int priority;
    public String color;
    public int arrivalTime = 0;
    public int startTime = -1;
    public int finishTime;
    public int originalBurst;
    
    public process(int burst, int priority, String Name, int arrTime)
    {
        this.burst = burst;
        this.priority = priority;
        this.processName = Name;
        this.arrivalTime = arrTime;
        this.originalBurst = burst;
        
        Random rand = new Random();

        int r = 50 + rand.nextInt(206);
        int g = 50 + rand.nextInt(206);
        int b = 50 + rand.nextInt(206);

        color =  String.format("#%02X%02X%02X", r, g, b);
    }
    
    public int getTurnaroundTime() {
    return finishTime - arrivalTime;
    }
    
    public int getWaitingTime() {
    return getTurnaroundTime() - originalBurst;
    }
}
