package com.example.hackathon_ex.service;

import com.example.hackathon_ex.dto.LocationRequest;
import com.example.hackathon_ex.dto.MiddlePointResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetPointService {
    public MiddlePointResponse calculateMiddlePoint(List<LocationRequest> locations) {
        validateInput(locations);
        double sumX=0;
        double sumY=0;

        for(LocationRequest loc : locations){
            sumX += loc.getX();
            sumY += loc.getY();
        }
        double midX=sumX/locations.size();
        double midY=sumY/locations.size();
        return new MiddlePointResponse(midX,midY);
    }
    private void validateInput(List<LocationRequest> locations) {
        if(locations==null || locations.size()<2){
            throw new IllegalArgumentException("최소 두 명 이상의 출발 장소를 입력해주세요");
        }
    }
}
