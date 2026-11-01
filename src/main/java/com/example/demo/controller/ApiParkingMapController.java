package com.example.demo.controller;

import com.example.demo.dto.ParkingOverviewDto;
import com.example.demo.service.ParkingLotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parking-map")
public class ApiParkingMapController {

    private final ParkingLotService parkingLotService;

    public ApiParkingMapController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ParkingOverviewDto> getOverview() {
        ParkingOverviewDto overview = parkingLotService.layTongQuanBaiDoXe();
        return ResponseEntity.ok(overview);
    }
}

