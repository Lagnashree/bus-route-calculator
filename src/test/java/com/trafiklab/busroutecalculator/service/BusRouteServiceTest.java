package com.trafiklab.busroutecalculator.service;

import com.trafiklab.busroutecalculator.exception.HttpConnectionException;
import com.trafiklab.busroutecalculator.exception.InvalidApiKeyException;
import com.trafiklab.busroutecalculator.exception.RateLimitExceedException;
import com.trafiklab.busroutecalculator.model.LineWithStops;
import com.trafiklab.busroutecalculator.model.LinesWithMaxStopResponse;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class BusRouteServiceTest {
    @InjectMocks
    private BusRouteService busRouteService;



    @Test
    public void BusRouteService_CountBusLineStops_WithEmptyArray() {
        JSONArray emptyArray = new JSONArray();
        Map<String, Integer> result = busRouteService.countBusLineStops(emptyArray);
        assertTrue(result.isEmpty());
    }

    @Test
    public void BusRouteService_CountBusLineStops_WithSingleLine() {
        JSONArray journeyArr = new JSONArray();
        JSONObject journeyObj = new JSONObject();
        journeyObj.put("LineNumber", "1");
        journeyObj.put("JourneyPatternPointNumber", "10008");
        journeyArr.add(journeyObj);

        Map<String, Integer> result = busRouteService.countBusLineStops(journeyArr);
        assertEquals(1, result.size());
        assertEquals(1, result.get("1"));
    }

    @Test
    public void BusRouteService_CountBusLineStops_WithMultipleLines() {
        JSONArray journeyArr = new JSONArray();
        JSONObject journeyObj1 = new JSONObject();
        journeyObj1.put("LineNumber", "1");
        journeyObj1.put("JourneyPatternPointNumber", "10008");
        journeyArr.add(journeyObj1);

        JSONObject journeyObj2 = new JSONObject();
        journeyObj2.put("LineNumber", "2");
        journeyObj1.put("JourneyPatternPointNumber", "10012");
        journeyArr.add(journeyObj2);

        JSONObject journeyObj3 = new JSONObject();
        journeyObj3.put("LineNumber", "1");
        journeyObj1.put("JourneyPatternPointNumber", "10012");
        journeyArr.add(journeyObj3);

        Map<String, Integer> result = busRouteService.countBusLineStops(journeyArr);
        assertEquals(2, result.size());
        assertEquals(2, result.get("1"));
        assertEquals(1, result.get("2"));
    }
    @Test
    public void BusRouteService_FetchBusLines_ValidResponse() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {

        String mockApiResponse = "{\"StatusCode\":0,\"ResponseData\":{\"Result\":[{\"line\":\"Bus Line 1\"},{\"line\":\"Bus Line 2\"}]}}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());
        // Call the method to be tested
        JSONArray result = spyTemp.fetchBusLines(Mockito.any());
        assertEquals(2, result.size());
        assertEquals("Bus Line 1", ((JSONObject) result.get(0)).get("line"));
        assertEquals("Bus Line 2", ((JSONObject) result.get(1)).get("line"));
    }
    @Test
    public void BusRouteService_FetchBusLines_RateLimitExceeded() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String mockApiResponse = "{\"StatusCode\": 1007, \"Message\": \"Rate limit exceeded\"}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());
        assertThrows(RateLimitExceedException.class, () -> {
            spyTemp.fetchBusLines(Mockito.any());
        });
    }

    @Test
    public void BusRouteService_FetchBusLines_InvalidApiKey() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String mockApiResponse = "{\"StatusCode\": 1002, \"Message\": \"Invalid API key\"}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());
        assertThrows(InvalidApiKeyException.class, () -> {
            spyTemp.fetchBusLines(Mockito.any());
        });
    }
    @Test
    public void BusRouteService_fetchStopInfo() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String mockApiResponse = "{\"StatusCode\":0,\"ResponseData\":{\"Result\":[{\"StopPointNumber\":\"1\",\"StopPointName\":\"Stop A\"},{\"StopPointNumber\":\"2\",\"StopPointName\":\"Stop B\"}]}}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());

        // Call the method to be tested
        Map<String, String> stopPointNumName = spyTemp.fetchStopInfo(Mockito.any());
        assertEquals(2, stopPointNumName.size());
        assertEquals("Stop A", stopPointNumName.get("1"));
        assertEquals("Stop B", stopPointNumName.get("2"));
    }
    @Test
    public void BusRouteService_fetchStopInfo_RateLimitExceeded() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String mockApiResponse = "{\"StatusCode\": 1007, \"Message\": \"Rate limit exceeded\"}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());
        assertThrows(RateLimitExceedException.class, () -> {
            spyTemp.fetchBusLines(Mockito.any());
        });
    }

    @Test
    public void BusRouteService_fetchStopInfo_InvalidApiKey() throws ParseException, HttpConnectionException, RateLimitExceedException, InvalidApiKeyException {
        String mockApiResponse = "{\"StatusCode\": 1002, \"Message\": \"Invalid API key\"}";
        BusRouteService spyTemp = Mockito.spy(busRouteService);
        Mockito.doReturn(mockApiResponse).when(spyTemp).fetchApiResponse(Mockito.any());
        assertThrows(InvalidApiKeyException.class, () -> {
            spyTemp.fetchBusLines(Mockito.any());
        });
    }

    @Test
    public void BusRouteService_CalculateBusRoute() throws ParseException, HttpConnectionException, IOException, RateLimitExceedException, InvalidApiKeyException {
        BusRouteService busRouteService =new BusRouteService();
        BusRouteService spyTemp = Mockito.spy(busRouteService);

        // Mock journeyArray
        JSONParser jsonParser= new JSONParser();
        String file ="src/test/resources/journeyarray.json";
        FileReader reader= new FileReader(file);
        Object obj= jsonParser.parse(reader);
        JSONArray mockfetchBusLinesResponse =(JSONArray)obj;

        //mock fetchStopInfo response
        HashMap<String, String>  mockfetchStopInfoResponse=new HashMap<String, String>();;
        mockfetchStopInfoResponse.put("10001", "Stadshagsplan");
        mockfetchStopInfoResponse.put("10002", "John Bergs plan");
        mockfetchStopInfoResponse.put("10006", "Arbetargatan");
        mockfetchStopInfoResponse.put("10008", "S:t Eriksgatan");
        mockfetchStopInfoResponse.put("100011", "Frihamnsporten");

        //mock countBusLineStop response
        HashMap<String, Integer>  mockcountBusLineStopsResponse=new HashMap<String, Integer>();
        mockcountBusLineStopsResponse.put("1",5);
        mockcountBusLineStopsResponse.put("2",3);
        mockcountBusLineStopsResponse.put("3",4);
        mockcountBusLineStopsResponse.put("4",3);
        mockcountBusLineStopsResponse.put("5",2);
        mockcountBusLineStopsResponse.put("6",2);
        mockcountBusLineStopsResponse.put("7",2);
        mockcountBusLineStopsResponse.put("8",2);
        mockcountBusLineStopsResponse.put("9",2);
        mockcountBusLineStopsResponse.put("10",2);
        mockcountBusLineStopsResponse.put("11",1);
        mockcountBusLineStopsResponse.put("12",1);

    
        Mockito.doReturn(mockfetchStopInfoResponse).when(spyTemp).fetchStopInfo(Mockito.any());
        Mockito.doReturn(mockfetchBusLinesResponse).when(spyTemp).fetchBusLines(Mockito.any());
        Mockito.doReturn(mockcountBusLineStopsResponse).when(spyTemp).countBusLineStops(Mockito.any());

      
        LinesWithMaxStopResponse linesWithMaxStopResponseObj = spyTemp.calculateBusRoute(Mockito.any(),Mockito.any());
        assertNotNull(linesWithMaxStopResponseObj);
        assertEquals("Success", linesWithMaxStopResponseObj.getStatusMessage());
        List<LineWithStops> lineWithStops= linesWithMaxStopResponseObj.getResponseData();
        assertEquals(10, lineWithStops.size());
        assertEquals("1", lineWithStops.get(0).getLineNumber());
        assertEquals("3", lineWithStops.get(1).getLineNumber());
        assertEquals("2", lineWithStops.get(2).getLineNumber());
        List<String> stopNames = lineWithStops.get(0).getStopNames();
        assertEquals(5, stopNames.size());
        assertEquals("Stadshagsplan", stopNames.get(0));
        assertEquals("John Bergs plan", stopNames.get(1));
        assertEquals("Arbetargatan", stopNames.get(2));

    }
}


