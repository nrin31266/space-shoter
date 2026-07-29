package com.alexei.spaceshooter.data.wave;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing one wave with its sequence of spawn actions.
 * Contains NO gameplay logic — pure data.
 */
public class WaveData {
    /** Unique wave identifier */
    public int waveId;

    /** Ordered list of spawn actions for this wave */
    public List<SpawnAction> actions = new ArrayList<>();

    public WaveData() {
    }

    public WaveData(int waveId, List<SpawnAction> actions) {
        this.waveId = waveId;
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "WaveData{waveId=" + waveId + ", actions=" + actions.size() + "}";
    }
}
