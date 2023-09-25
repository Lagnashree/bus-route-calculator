package com.trafiklab.busroutecalculator.service;

import java.time.Duration;
import java.util.*;
import com.trafiklab.busroutecalculator.exception.HttpConnectionException;
import com.trafiklab.busroutecalculator.exception.InvalidApiKeyException;
import com.trafiklab.busroutecalculator.exception.RateLimitExceedException;
import com.trafiklab.busroutecalculator.model.LineWithStops;
import com.trafiklab.busroutecalculator.model.LinesWithMaxStopResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.util.retry.Retry;

@Service
public class BusRouteService {
    private static final Logger logger = LogManager.getLogger(BusRouteService.class);
    @Autowired
    private WebClient webClient;

    /**
     * This function return top 10 bus lines with maximum stops alog with name of all stops for the line.
     * @param string, API key from trafiklab
     * @return LinesWithMaxStopResponse object which contains list of LineWithStops object.
     * @throws HttpConnectionException If there is any error while calling the external SL traffic REST API
     * @throws ParseException if there is any error while parsing response data from SL traffic API
     * @throws RateLimitExceedException if the provided API key is exceeded the rate limit for SL traffic API
     * @throws InvalidApiKeyException if the provided API key is invalid for SL traffic API
     */
    public LinesWithMaxStopResponse calculateBusRoute(String apiKey, String uuidAsString)
            throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        // Initialize a list to store the response objects
        List<LineWithStops> responseObjectArray = new ArrayList<>();

        // Fetch stop point information
        HashMap<String, String> stopPointInfo = fetchStopInfo(apiKey);
        logger.info("UUID:{} fetchStopInfo call is completed", uuidAsString);

        // Fetch bus lines
        JSONArray journeyArr = fetchBusLines(apiKey);
        logger.info("UUID:{} fetchBusLines call is completed", uuidAsString);

        // Count the number of stops for each bus line
        HashMap<String, Integer> lineCount = countBusLineStops(journeyArr);
        logger.info("UUID:{} countBusLineStops call is completed", uuidAsString);

        // Sort the bus lines by the number of stops in descending order
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(lineCount.entrySet());
        entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Create a LinkedHashMap to store the sorted bus lines
        LinkedHashMap<String, Integer> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entryList) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }

        // Initialize a map to store the top 10 lines with the most stops
        HashMap<String, List<String>> lineWithStopNum = new HashMap<>();
        int count = 0;
        logger.info("UUID:{} Top 10 lines with max stops", uuidAsString);
        for (Map.Entry<String, Integer> entry : sortedHashMap.entrySet()) {
            if (count < 10) {
                logger.info("Line number: {}", entry.getKey());
                lineWithStopNum.put(entry.getKey(), new ArrayList<>());
                count++;
            } else {
                break;
            }
        }

        // Populate the map with stop information for the top 10 lines
        for (int i = 0; i < journeyArr.size(); i++) {
            JSONObject journeyObj = (JSONObject) journeyArr.get(i);
            String lineNumber = (String) journeyObj.get("LineNumber");
            if (lineWithStopNum.containsKey(lineNumber)) {
                String journeyPatternPointNumber = (String) journeyObj.get("JourneyPatternPointNumber");
                lineWithStopNum.get(lineNumber).add(stopPointInfo.get(journeyPatternPointNumber));
            }
        }

        // Create the response objects for the top 10 lines
        int count1 = 0;
        for (Map.Entry<String, Integer> entry : sortedHashMap.entrySet()) {
            if (count1 < 10) {
                LineWithStops lineWithStopsObj = new LineWithStops();
                lineWithStopsObj.setLineNumber(entry.getKey());
                lineWithStopsObj.setStopNames(lineWithStopNum.get(entry.getKey()));
                responseObjectArray.add(lineWithStopsObj);
                count1++;
            } else {
                break;
            }
        }

        // Create and return the final response object
        LinesWithMaxStopResponse linesWithMaxStopResponseObj = new LinesWithMaxStopResponse();
        linesWithMaxStopResponseObj.setStatusMessage("Success");
        linesWithMaxStopResponseObj.setResponseData(responseObjectArray);
        return linesWithMaxStopResponseObj;
    }



    /**
     * This function returns all available stops in SL BUS Line
     *  @param String Trafiklab's API Key.
     * @return HashMap<String, String> where key is stop number and value is stop name.
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     * @throws ParseException if there is any error while parsing response data from SL API
     * @throws RateLimitExceedException if the provided API key is exceeded the rate limit for SL traffic API
     * @throws InvalidApiKeyException if the provided API key is invalid for SL traffic API
     */
    public HashMap<String, String> fetchStopInfo(String apiKey) throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String url = buildApiUrl(apiKey, "stop&DefaultTransportModeCode=BUS");
        HashMap<String, String> stopPointNumName = new HashMap<>();
        String httpResponse=fetchApiResponse(url);
        JSONParser parse = new JSONParser();
        JSONObject data_obj = (JSONObject) parse.parse(httpResponse);
        Long statusCode = (Long) data_obj.get("StatusCode");
        if (statusCode==0) {
            JSONObject responseData = (JSONObject) data_obj.get("ResponseData");
            JSONArray stopArr = (JSONArray) responseData.get("Result");
            for (int i = 0; i < stopArr.size(); i++) {
                JSONObject stopObj = (JSONObject) stopArr.get(i);
                String stopPointNum = (String) stopObj.get("StopPointNumber");
                String stopPointName = (String) stopObj.get("StopPointName");
                stopPointNumName.put(stopPointNum, stopPointName);
            }
            return stopPointNumName;
        }
        else if(statusCode==1007)
            throw new RateLimitExceedException((String)data_obj.get("Message"));
        else if(statusCode==1002)
            throw new InvalidApiKeyException((String)data_obj.get("Message"));
        else
            throw new HttpConnectionException("Server error while making SL API Call");
    }



    /**
     * This function returns all available buslines in SL in form of JSONArray
     * @param String Trafiklab's API Key.
     * @return JSONArray of JSONObject that contains of LineNumber,DirectionCode,JourneyPatternPointNumber etc .
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     * @throws ParseException if there is any error while parsing response data from SL API
     * @throws RateLimitExceedException if the provided API key is exceeded the rate limit for SL traffic API
     * @throws InvalidApiKeyException if the provided API key is invalid for SL traffic API
     */
    public JSONArray fetchBusLines(String apiKey) throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException{
        String url = buildApiUrl(apiKey, "jour&DefaultTransportModeCode=BUS");
        String httpResponse=fetchApiResponse(url);
        JSONParser parse = new JSONParser();
        JSONObject data_obj = (JSONObject) parse.parse(httpResponse);
        Long statusCode = (Long) data_obj.get("StatusCode");
        if (statusCode==0){
            //Get the required object from the above created object
            JSONObject obj = (JSONObject) data_obj.get("ResponseData");
            JSONArray journeyArr = (JSONArray) obj.get("Result");
            return journeyArr;
        }
        else if(statusCode==1007)
            throw new RateLimitExceedException((String)data_obj.get("Message"));
        else if(statusCode==1002)
            throw new InvalidApiKeyException((String)data_obj.get("Message"));
        else
            throw new HttpConnectionException("Server error while making SL API Call");

    }



    /**
     * This function calculated stops count of all bus lines.
     * @param JSONArray of JSONObject that contains of LineNumber,DirectionCode,JourneyPatternPointNumber etc.
     * @return hashmap where key is bus line number and value is number of stops of the busline .
     */
    public HashMap<String, Integer> countBusLineStops(JSONArray journeyArr){
        HashMap<String, Integer> lineCount = new HashMap<String, Integer>();
        for (int i = 0; i < journeyArr.size(); i++) {
            JSONObject journey_obj = (JSONObject) journeyArr.get(i);
            String lineNumber = (String) journey_obj.get("LineNumber");
            if (lineCount.containsKey(lineNumber))
                lineCount.computeIfPresent(lineNumber, (k, v) -> v + 1);
            else
                lineCount.put(lineNumber, 1);
        }
        return lineCount;
    }



    /**
     * This function is used to make REST API call using webClient
     * @param String Trafiklab's API Key.
     * @param String  model for the SL API call
     * @return String, http url for SL API call
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     */
    public String buildApiUrl(String apiKey, String model) {
        return "https://api.sl.se/api2/LineData.json?key=" + apiKey + "&model=" + model;
    }



    /**
     * This function is used to make REST API call using webClient
     * @param String http url for the API call.
     * @return String, HTTP response.
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     */
    public String fetchApiResponse(String url) throws HttpConnectionException {
        try {
            return webClient.get()
                    .uri(url)
                    .header("Accept-Encoding", "gzip")
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> Mono.error(new HttpConnectionException("Server error while making SL API Call")))
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                            .filter(throwable -> throwable instanceof HttpConnectionException))
                    .block();
        }
        catch (Exception e) {
            logger.error("Exception while making SL API call ",e);
            throw new HttpConnectionException("Server error while making SL API Call");
        }
    }
}

