package com.Guard.Back.Controller;

import com.Guard.Back.Dto.*;
import com.Guard.Back.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
    @GetMapping("/guardians")
    public ResponseEntity<List<GuardianListDto>> getGuardians() {
        return ResponseEntity.ok(adminService.getAllGuardians());
    }
    @GetMapping("/sos-logs")
    public ResponseEntity<List<SosLogDto>> getSosLogs() {
        return ResponseEntity.ok(adminService.getAllSosLogs());
    }
}