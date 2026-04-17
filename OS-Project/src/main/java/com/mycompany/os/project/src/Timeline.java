package com.mycompany.os.project.src;

import com.mycompany.os.project.src.Slot;

import java.util.ArrayList;

public class Timeline {
    public ArrayList<Slot> slots = new ArrayList<>();

    public void addSlot(String processName, int start, int end) {
        slots.add(new Slot(processName, start, end));
    }

    public void mergeConsecutive() {
        for (int i = slots.size() - 1; i > 0; i--) {
            Slot curr = slots.get(i);
            Slot prev = slots.get(i - 1);
            if (curr.processName.equals(prev.processName) && prev.end == curr.start) {
                prev.end = curr.end;
                slots.remove(i);
            }
        }
    }

    public int getTotalTime() {
        if (slots.isEmpty()) return 0;
        return slots.get(slots.size() - 1).end;
    }
}