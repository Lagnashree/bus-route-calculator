package com.trafiklab.busroutecalculator.model;

import java.util.List;

public class LineWithStops {
    private String lineNumber;
    private List<String> stopNames;
    public LineWithStops() {
    }

    public LineWithStops(String lineNumber, List<String> stopNames) {
        this.lineNumber = lineNumber;
        this.stopNames = stopNames;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public void setStopNames(List<String> stopNames) {
        this.stopNames = stopNames;
    }

}
