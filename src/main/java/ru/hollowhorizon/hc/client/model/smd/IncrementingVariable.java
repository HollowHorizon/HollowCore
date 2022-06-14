package ru.hollowhorizon.hc.client.model.smd;

import java.util.Objects;

public class IncrementingVariable {
    public float value = 0.0F;
    public float increment;
    public float limit;
    public boolean shouldReverse = false;
    public boolean shouldStayAtEnd = false;
    public boolean inReverse = false;
    private boolean atEnd = false;
    public int stayAtEndTime = -1;
    public Runnable atEndTask;

    public IncrementingVariable(float increment, float limit) {
        this.increment = increment;
        this.limit = limit;
    }

    public void tick() {
        if (this.atEnd) {
            if (this.stayAtEndTime != -1) {
                --this.stayAtEndTime;
                return;
            }

            this.atEnd = false;
        }

        float increment = this.increment;
        if (this.inReverse) {
            increment = -increment;
        }

        this.value += increment;
        if (this.value >= this.limit) {
            this.value = 0.0F;
        } else if (this.value < 0.0F) {
            this.value = this.limit;
        }

    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof IncrementingVariable)) {
            return false;
        } else {
            IncrementingVariable variable = (IncrementingVariable)o;
            return Math.abs(variable.increment - this.increment) <= 1.0F && Math.abs(variable.limit - this.limit) <= 1.0F;
        }
    }

    public int hashCode() {
        return Objects.hash(this.increment, this.limit);
    }

    public boolean isAtEnd() {
        return this.atEnd;
    }

    public void setAtEnd(boolean atEnd) {
        this.atEnd = atEnd;
        if (atEnd && this.atEndTask != null) {
            this.atEndTask.run();
        }

    }
}
