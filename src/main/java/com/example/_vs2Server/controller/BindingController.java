package com.example._vs2Server.controller;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.dto.CheckDuplicateRequest;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.service.BindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bindings")
@CrossOrigin(origins = "*")
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

    @PostMapping("/check-duplicates")
    public ResponseEntity<Map<String, Object>> checkDuplicates(@RequestBody CheckDuplicateRequest request) {
        try {
            return ResponseEntity.ok(bindingService.checkDuplicates(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteBindings(
            @RequestParam int source,
            @RequestParam String league,
            @RequestParam String team,
            @RequestParam String teamType // 新增参数，区分主队还是客队，值为 "home" 或 "away"
    ) {
        try {
            if (source < 1 || source > 3) {
                throw new IllegalArgumentException("无效的数据源参数");
            }
            bindingService.deleteTeam(source, league, team, teamType);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("删除失败: " + e.getMessage());
        }
    }
}    