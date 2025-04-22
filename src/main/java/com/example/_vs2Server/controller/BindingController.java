package com.example._vs2Server.controller;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.service.BindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bindings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 根据需要调整CORS策略
public class BindingController {

    private final BindingService bindingService;  // 确保这个字段是final的

    @PostMapping
    public ResponseEntity<?> createBinding(@RequestBody List<BindingRequest> requests) {
        try {
            requests.forEach(bindingService::saveBinding);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "保存失败: " + e.getMessage()));
        }
    }
}