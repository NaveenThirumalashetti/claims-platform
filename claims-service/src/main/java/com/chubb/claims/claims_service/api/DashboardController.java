package com.chubb.claims.claims_service.api;

import com.chubb.claims.claims_service.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping("/workload")
    public DashboardService.WorkloadSummary workload() { return service.workload(); }

    @GetMapping("/exposure")
    public List<DashboardService.ExposureLine> exposure() { return service.exposure(); }
}