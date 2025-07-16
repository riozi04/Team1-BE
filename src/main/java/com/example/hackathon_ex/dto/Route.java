package com.example.hackathon_ex.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class Route {
    private Summary summary;
    private List<Section> sections;
}
