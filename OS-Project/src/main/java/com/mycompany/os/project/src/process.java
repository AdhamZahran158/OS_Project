package com.mycompany.os.project.src;

public class process {
    public String processName;
    public int burst;
    public int priority;

    public int arrivaltime;
    public int finishtime;


    public boolean finished= false;

    public process(int burst, int priority, String Name )
    {
        this.burst = burst;
        this.priority = priority;
        this.processName = Name;

    }
    public double getTAT (){return (double)finishtime-arrivaltime;}
    public double getWT (){return (double)finishtime-arrivaltime-burst;}
}
