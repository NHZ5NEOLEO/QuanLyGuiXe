package com.example.demo.controller;

import com.example.demo.dto.TicketResponseDto;
import com.example.demo.model.VeXe;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.ParkingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestController {

    private final ParkingService parkingService;
    private final VeXeRepository veXeRepository;

    public TicketRestController(ParkingService parkingService, VeXeRepository veXeRepository) {
        this.parkingService = parkingService;
        this.veXeRepository = veXeRepository;
    }

    /**
     * Tra cứu danh sách vé xe kèm bộ lọc
     */
    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getTickets(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "vehicleTypeId", required = false) Long vehicleTypeId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime start = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
        LocalDateTime end = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;

        List<VeXe> tickets = veXeRepository.searchHistory(keyword, status, vehicleTypeId, start, end);
        List<TicketResponseDto> dtos = tickets.stream()
                .map(parkingService::mapToTicketResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Lấy thông tin chi tiết vé theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> getTicketById(@PathVariable("id") Integer id) {
        VeXe veXe = parkingService.getTicketById(id);
        return ResponseEntity.ok(parkingService.mapToTicketResponseDto(veXe));
    }

    /**
     * Tra cứu vé xe theo Mã vé / Mã QR
     */
    @GetMapping("/code/{ticketCode}")
    public ResponseEntity<TicketResponseDto> getTicketByCode(@PathVariable("ticketCode") String ticketCode) {
        VeXe veXe = parkingService.getTicketByCode(ticketCode);
        return ResponseEntity.ok(parkingService.mapToTicketResponseDto(veXe));
    }
}
