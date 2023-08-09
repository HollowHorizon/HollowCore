package ru.hollowhorizon.hc.common.events.action;

public class ActionPackage {
    private final String action;
    private int delay;

    public ActionPackage(String action, int delay) {
        this.action = action;
        this.delay = delay;
    }

    public boolean tick() {
        delay -= 1;
        return delay == 0;
    }

    public String getAction() {
        return action;
    }

    public int getDelay() {
        return delay;
    }
}
