package com.trafiklab.busroutecalculator.controller;

import com.trafiklab.busroutecalculator.model.LineWithStops;
import com.trafiklab.busroutecalculator.model.LinesWithMaxStopResponse;
import com.trafiklab.busroutecalculator.service.BusRouteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class BusRouteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    BusRouteService busRouteService;
    @InjectMocks
    private BusRouteController busRouteController;

    @Before
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        this.mockMvc= MockMvcBuilders.standaloneSetup(busRouteController).build();
    }
    @Test
    public void busRouteController_listApi_success() throws Exception{
        LinesWithMaxStopResponse linesWithMaxStopResponse=new LinesWithMaxStopResponse();
        List<String> stopNames1= Arrays.asList("stop1", "stop2", "stop3");
        LineWithStops lws1=new LineWithStops("113",stopNames1);
        List<String> stopNames2= Arrays.asList("stop4", "stop5", "stop6");
        LineWithStops lws2=new LineWithStops("114",stopNames2);
        List<String> stopNames3= Arrays.asList("stop7", "stop8", "stop9");
        LineWithStops lws3=new LineWithStops("115",stopNames3);
        List<LineWithStops> lineList =Arrays.asList(lws1,lws2,lws3);
        linesWithMaxStopResponse.setStatusMessage("success");
        linesWithMaxStopResponse.setResponseData(lineList);
        Mockito.when(busRouteService.calculateBusRoute(Mockito.any(),Mockito.any())).thenReturn(linesWithMaxStopResponse);
        mockMvc.perform(MockMvcRequestBuilders.
                        get("/trafiklab/v1/busline")
                        .param("apiKey", "apiKey"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(header().exists("unique-req-id"))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(jsonPath("$.statusMessage").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseData",hasSize(3)))
                .andExpect(jsonPath("$.responseData[0].lineNumber").value("113"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseData[0].stopNames",hasSize(3)))
                .andExpect(jsonPath("$.responseData[0].stopNames[0]").value("stop1"));

    }

    @Test
    public void busRouteController_listApi_invalidinput() throws Exception{
        String apiKey = null;
        mockMvc.perform(get("/trafiklab/v1/busline")
                        .param("apiKey", apiKey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }
}

