package com.example.demo.controller;

import com.example.demo.dto.VehicleDto;
import com.example.demo.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleRestController {

    private final VehicleService vehicleService;

    public VehicleRestController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Lấy danh sách tất cả phương tiện
     */
    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    /**
     * Tìm kiếm phương tiện theo biển số, chủ xe, số điện thoại
     */
    @GetMapping("/search")
    public ResponseEntity<List<VehicleDto>> searchVehicles(@RequestParam("query") String query) {
        return ResponseEntity.ok(vehicleService.searchVehicles(query));
    }

    /**
     * Xem thông tin chi tiết một phương tiện theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDto> getVehicleById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    /**
     * Thêm phương tiện mới
     */
    @PostMapping
    public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody VehicleDto dto) {
        VehicleDto created = vehicleService.createVehicle(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Chỉnh sửa thông tin phương tiện
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleDto> updateVehicle(@PathVariable("id") Long id, @RequestBody VehicleDto dto) {
        VehicleDto updated = vehicleService.updateVehicle(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa phương tiện
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable("id") Long id) {
        vehicleService.deleteVehicle(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đã xóa phương tiện thành công!");
        return ResponseEntity.ok(resp);
    }
}
