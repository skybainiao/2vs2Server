package com.example._vs2Server.controller;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.service.BindingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bindings")
public class BindingController {

    private final BindingService bindingService;

    public BindingController(BindingService bindingService) {
        this.bindingService = bindingService;
    }

    @PostMapping
    public ResponseEntity<?> createBindings(@RequestBody List<BindingRequest> requests) {
        try {
            bindingService.saveBindings(requests);
            return ResponseEntity.ok().body("绑定数据保存成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("保存失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Binding>> getAllBindings() {
        return ResponseEntity.ok(bindingService.getAllBindings());
    }
}