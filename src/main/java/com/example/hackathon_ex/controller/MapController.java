package com.example.hackathon_ex.controller;

import com.example.hackathon_ex.dto.LocationRequest;
import com.example.hackathon_ex.dto.MiddlePointResponse;
import com.example.hackathon_ex.service.MeetPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor


public class MapController {
    private final MeetPointService meetPointService;
    @PostMapping("/middle")
    public ResponseEntity<MiddlePointResponse> getMiddlePoint(@RequestBody List<LocationRequest> locations) {
        return ResponseEntity.ok(meetPointService.calculateMiddlePoint(locations));
    }
}
