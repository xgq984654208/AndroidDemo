package com.example.dong.odometer;

import org.json.JSONObject;

import java.io.Serializable;

public class StepRecord implements Serializable {

    private int step;

    private String date;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        date = date;
    }
}
