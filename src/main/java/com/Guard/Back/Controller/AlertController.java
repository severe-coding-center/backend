package com.Guard.Back.Controller;

import com.Guard.Back.Dto.AlertLogDto;
import com.Guard.Back.Service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/{protectedUserId}")
    public ResponseEntity<List<AlertLogDto>> getAlertLogs(@PathVariable Long protectedUserId) {
        List<AlertLogDto> logs = alertService.getAlertLogs(protectedUserId);
        return ResponseEntity.ok(logs);
    }
}