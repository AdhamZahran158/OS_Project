package com.mycompany.os.project.src;

public class Slot {
    public String processName;
    public int start;
    public int end;

    public Slot(String processName, int start, int end) {
        this.processName = processName;
        this.start = start;
        this.end = end;
    }
}
