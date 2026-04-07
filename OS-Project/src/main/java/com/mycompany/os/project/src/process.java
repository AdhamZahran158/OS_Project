package com.mycompany.os.project.src;

public class process {
    public String processName;
    public int burst;
    public int priority;
    public process(int burst, int priority, String Name)
    {
        this.burst = burst;
        this.priority = priority;
        this.processName = Name;
    }
}
