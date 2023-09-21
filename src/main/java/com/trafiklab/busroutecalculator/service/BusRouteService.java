package com.trafiklab.busroutecalculator.service;

import java.time.Duration;
import java.util.*;
import com.trafiklab.busroutecalculator.exception.HttpConnectionException;
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
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     * @throws ParseException if there is any error while parsing response data from SL API
     */
    public LinesWithMaxStopResponse calculateBusRoute(String apiKey, String uuidAsString) throws ParseException, HttpConnectionException {
        List<LineWithStops> responseObjectArray = new ArrayList<LineWithStops>();
        HashMap<String, String> stopPontInfo= fetchStopInfo(apiKey);
        logger.info("UUID:{} fetchStopInfo call is completed",uuidAsString );
        JSONArray journeyArr = fetchBusLines(apiKey);
        logger.info("UUID:{} fetchBusLines call is completed",uuidAsString );
        HashMap<String, Integer> lineCount =countBusLineStops(journeyArr);
        logger.info("UUID:{} countBusLineStops call is completed",uuidAsString );
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(lineCount.entrySet());
        Comparator<Map.Entry<String, Integer>> valueComparator = (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue());
        Collections.sort(entryList, valueComparator);
        LinkedHashMap<String, Integer> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entryList) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }

        HashMap<String, List<String>> lineWithStopNum = new HashMap<>();
        int count = 0;
        logger.info("UUID:{} Top 10 lines with max stops",uuidAsString );
        for (Map.Entry<String, Integer> entry : sortedHashMap.entrySet()) {
            if (count < 10) {
                logger.info("Line number: {}",entry.getKey());
                lineWithStopNum.put(entry.getKey(), new ArrayList<>());
                count++;
            } else {
                break;
            }
        }
        for (int i = 0; i < journeyArr.size(); i++) {
            JSONObject journey_obj = (JSONObject) journeyArr.get(i);
            String lineNumber = (String) journey_obj.get("LineNumber");
            if (lineWithStopNum.containsKey(lineNumber)) {
                String JourneyPatternPointNumber = (String) journey_obj.get("JourneyPatternPointNumber");
                lineWithStopNum.get(lineNumber).add((String) stopPontInfo.get(JourneyPatternPointNumber));
            }
        }
        int count21 = 0;
        for (Map.Entry<String, Integer> entry : sortedHashMap.entrySet()) {
            if (count21 < 10) {
                LineWithStops LineWithStopsObj = new LineWithStops();
                LineWithStopsObj.setLineNumber(entry.getKey());
                LineWithStopsObj.setStopNames(lineWithStopNum.get(entry.getKey()));
                responseObjectArray.add(LineWithStopsObj);
                count21++;
            } else {
                break;
            }
        }

        LinesWithMaxStopResponse linesWithMaxStopResponseObj =new LinesWithMaxStopResponse();
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
     */
    public HashMap<String, String> fetchStopInfo(String apiKey) throws ParseException, HttpConnectionException {
        String url = buildApiUrl(apiKey, "stop&DefaultTransportModeCode=BUS");
        HashMap<String, String> stopPointNumName = new HashMap<>();
        String httpResponse=fetchApiResponse(url);
        JSONParser parse2 = new JSONParser();
        JSONObject data_obj2 = (JSONObject) parse2.parse(httpResponse);
        JSONObject responseData = (JSONObject) data_obj2.get("ResponseData");
        JSONArray stopArr = (JSONArray) responseData.get("Result");
        for (int i = 0; i < stopArr.size(); i++) {
            JSONObject stopObj = (JSONObject) stopArr.get(i);
            String stopPointNum = (String) stopObj.get("StopPointNumber");
            String stoppointName = (String) stopObj.get("StopPointName");
            stopPointNumName.put(stopPointNum, stoppointName);
        }
        return stopPointNumName;
    }



    /**
     * This function returns all available buslines in SL in form of JSONArray
     * @param String Trafiklab's API Key.
     * @return JSONArray of JSONObject that contains of LineNumber,DirectionCode,JourneyPatternPointNumber etc .
     * @throws HttpConnectionException If there is any error while calling the external SL REST API
     * @throws ParseException if there is any error while parsing response data from SL API
     */
    public JSONArray fetchBusLines(String apiKey) throws ParseException, HttpConnectionException {
        String url = buildApiUrl(apiKey, "jour&DefaultTransportModeCode=BUS");
        String httpResponse=fetchApiResponse(url);
        JSONParser parse = new JSONParser();
        JSONObject data_obj = (JSONObject) parse.parse(httpResponse);
        //Get the required object from the above created object
        JSONObject obj = (JSONObject) data_obj.get("ResponseData");
        JSONArray journeyArr = (JSONArray) obj.get("Result");
        return journeyArr;
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
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> Mono.error(new HttpConnectionException("Client error while making SL API Call")))
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

