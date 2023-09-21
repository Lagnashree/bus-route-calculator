package com.trafiklab.busroutecalculator.model;

import java.util.List;

public class LinesWithMaxStopResponse {
    public String getStatusMessage() {
        return StatusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        StatusMessage = statusMessage;
    }
    private String StatusMessage;

    public List<LineWithStops> getResponseData() {
        return ResponseData;
    }

    public void setResponseData(List<LineWithStops> responseData) {
        ResponseData = responseData;
    }

    private List<LineWithStops> ResponseData;
}
