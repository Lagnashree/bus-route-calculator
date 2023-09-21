/*package com.trafiklab.busroutecalculator.controller;

import com.trafiklab.busroutecalculator.exception.HttpConnectionException;
import com.trafiklab.busroutecalculator.model.LinesWithMaxStopResponse;
import com.trafiklab.busroutecalculator.service.BusRouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.UUID;


@RestController
@Validated
@RequestMapping("/api/v1/busline")
public class BusRouteController {
    @Autowired
    private BusRouteService busRouteService;
    private static final Logger logger = LogManager.getLogger(BusRouteController.class);
    @GetMapping
    public ResponseEntity<LinesWithMaxStopResponse> busRoute() throws ParseException, HttpConnectionException {
        try {
            HttpHeaders headers = new HttpHeaders();
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();
            logger.info("UUID:{} API GET Operation is called with", uuidAsString);
            headers.add("unique-req-id", uuidAsString);
            String apiKey="4c4721d7fc044eae9fff36c824e0062f";
            return new ResponseEntity<>(busRouteService.calculateBusRoute(apiKey,uuidAsString ),
                    headers,
                    HttpStatus.OK);
        }
        catch( Exception e)
        {
            System.out.println("in exception in controller:  "+e);
            throw e;
        }
    }
}*/
package com.trafiklab.busroutecalculator.controller;

import com.trafiklab.busroutecalculator.exception.HttpConnectionException;
import com.trafiklab.busroutecalculator.model.LinesWithMaxStopResponse;
import com.trafiklab.busroutecalculator.service.BusRouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.UUID;


@RestController
@Validated
@RequestMapping("/trafiklab/v1/busline")
public class BusRouteController {
    @Autowired
    private BusRouteService busRouteService;
    private static final Logger logger = LogManager.getLogger(BusRouteController.class);
    @GetMapping
    public ResponseEntity<LinesWithMaxStopResponse> busRoute(@RequestParam String apiKey) throws ParseException, HttpConnectionException {
        try {
            HttpHeaders headers = new HttpHeaders();
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();
            logger.info("UUID:{} API GET Operation is called with", uuidAsString);
            headers.add("unique-req-id", uuidAsString);
            return new ResponseEntity<>(busRouteService.calculateBusRoute(apiKey,uuidAsString),
                    headers,
                    HttpStatus.OK);
        }
        catch( Exception e)
        {
            System.out.println("in exception in controller:  "+e);
            throw e;
        }
    }
}
