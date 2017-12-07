package com.example.uttam.driver_behaviour;

import java.util.List;

/**
 * Created by Amrit on 12/6/2017.
 */

public class ScoreArrayList {
    private List<Double> score;

    public ScoreArrayList(List<Double> score) {
        this.score = score;
    }

    public Double getAverage() {
        double sum = 0;
        for (Double i : score)
            sum = sum + i;

        int count
                = score.size();
        return sum / count;
    }

}
