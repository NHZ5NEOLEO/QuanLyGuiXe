package com.example.demo.service.impl;

import com.example.demo.dto.PhuongTienDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.PhuongTien;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.PhuongTienRepository;
import com.example.demo.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("phuongTienService")
public class VehicleServiceImpl implements VehicleService {

    private final PhuongTienRepository phuongTienRepository;
    private final LoaiXeRepository loaiXeRepository;

    public VehicleServiceImpl(PhuongTienRepository phuongTienRepository, LoaiXeRepository loaiXeRepository) {
        this.phuongTienRepository = phuongTienRepository;
        this.loaiXeRepository = loaiXeRepository;
    }

    private PhuongTienDto mapToDto(PhuongTien pt) {
        if (pt == null) return null;
        PhuongTienDto dto = new PhuongTienDto();
        dto.setId(pt.getId());
        dto.setMaPhuongTien(pt.getMaPhuongTien());
        dto.setBienSoXe(pt.getBienSoXe());
        if (pt.getLoaiXe() != null) {
            dto.setLoaiXeId(pt.getLoaiXe().getId());
            dto.setTenLoaiXe(pt.getLoaiXe().getTenLoai());
        }
        dto.setChuXe(pt.getChuXe());
        dto.setSoDienThoai(pt.getSoDienThoai());
        dto.setMoTa(pt.getMoTa());
        dto.setNgayTao(pt.getNgayTao());
        return dto;
    }

    @Override
    public List<PhuongTienDto> layTatCaPhuongTien() {
        return phuongTienRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PhuongTienDto layPhuongTienTheoId(Long id) {
        PhuongTien pt = phuongTienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện với ID: " + id));
        return mapToDto(pt);
    }

    @Override
    public Optional<PhuongTienDto> timTheoBienSoXe(String bienSoXe) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) {
            return Optional.empty();
        }
        String cleanPlate = com.example.demo.util.BienSoXeHelper.toCleanKey(bienSoXe);
        List<PhuongTien> list = phuongTienRepository.findAll();
        for (PhuongTien pt : list) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(pt.getBienSoXe(), cleanPlate)) {
                return Optional.of(mapToDto(pt));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<PhuongTienDto> timKiemPhuongTien(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return layTatCaPhuongTien();
        }
        String kw = tuKhoa.trim();
        List<PhuongTien> all = phuongTienRepository.findAll();
        return all.stream()
                .filter(pt -> com.example.demo.util.BienSoXeHelper.matchesSearch(pt.getBienSoXe(), kw)
                        || (pt.getChuXe() != null && pt.getChuXe().toLowerCase().contains(kw.toLowerCase()))
                        || (pt.getSoDienThoai() != null && pt.getSoDienThoai().contains(kw))
                        || (pt.getMaPhuongTien() != null && pt.getMaPhuongTien().toLowerCase().contains(kw.toLowerCase())))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhuongTienDto taoPhuongTien(PhuongTienDto dto) {
        if (dto.getBienSoXe() == null || dto.getBienSoXe().trim().isEmpty()) {
            throw new BadRequestException("Biển số xe không được để trống!");
        }

        LoaiXe loaiXe = null;
        if (dto.getLoaiXeId() != null) {
            loaiXe = loaiXeRepository.findById(dto.getLoaiXeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + dto.getLoaiXeId()));
        } else {
            List<LoaiXe> list = loaiXeRepository.findAll();
            if (!list.isEmpty()) {
                loaiXe = list.get(0);
            }
        }

        String rawPlate = dto.getBienSoXe().trim();
        com.example.demo.util.BienSoXeHelper.ValidationResult vResult = 
                com.example.demo.util.BienSoXeHelper.validate(rawPlate, loaiXe != null ? loaiXe.getTenLoai() : null);
        if (!vResult.valid()) {
            throw new BadRequestException(vResult.message());
        }

        String displayPlate = vResult.displayValue();
        String cleanKey = vResult.cleanKey();

        // Kiểm tra xem phương tiện đã tồn tại trong hệ thống chưa (dựa trên cleanKey)
        List<PhuongTien> existingList = phuongTienRepository.findAll();
        for (PhuongTien existing : existingList) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(existing.getBienSoXe(), cleanKey)) {
                throw new BadRequestException("Phương tiện với biển số [" + displayPlate + "] đã tồn tại trong hệ thống!");
            }
        }

        String maPhuongTien = dto.getMaPhuongTien();
        if (maPhuongTien == null || maPhuongTien.trim().isEmpty()) {
            maPhuongTien = "PT-" + cleanKey;
        }

        PhuongTien pt = new PhuongTien(maPhuongTien, displayPlate, loaiXe, dto.getChuXe(), dto.getSoDienThoai(), dto.getMoTa());
        PhuongTien saved = phuongTienRepository.save(pt);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public PhuongTienDto capNhatPhuongTien(Long id, PhuongTienDto dto) {
        PhuongTien pt = phuongTienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phương tiện ID: " + id));

        LoaiXe loaiXe = pt.getLoaiXe();
        if (dto.getLoaiXeId() != null) {
            loaiXe = loaiXeRepository.findById(dto.getLoaiXeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại xe ID: " + dto.getLoaiXeId()));
            pt.setLoaiXe(loaiXe);
        }

        if (dto.getBienSoXe() != null && !dto.getBienSoXe().trim().isEmpty()) {
            String rawPlate = dto.getBienSoXe().trim();
            com.example.demo.util.BienSoXeHelper.ValidationResult vResult = 
                    com.example.demo.util.BienSoXeHelper.validate(rawPlate, loaiXe != null ? loaiXe.getTenLoai() : null);
            if (!vResult.valid()) {
                throw new BadRequestException(vResult.message());
            }

            String displayPlate = vResult.displayValue();
            String cleanKey = vResult.cleanKey();

            // Kiểm tra trùng lặp với phương tiện khác
            List<PhuongTien> existingList = phuongTienRepository.findAll();
            for (PhuongTien other : existingList) {
                if (!other.getId().equals(id) && com.example.demo.util.BienSoXeHelper.isSamePlate(other.getBienSoXe(), cleanKey)) {
                    throw new BadRequestException("Biển số xe [" + displayPlate + "] đã trùng với phương tiện khác!");
                }
            }
            pt.setBienSoXe(displayPlate);
        }

        if (dto.getChuXe() != null) {
            pt.setChuXe(dto.getChuXe().trim());
        }
        if (dto.getSoDienThoai() != null) {
            pt.setSoDienThoai(dto.getSoDienThoai().trim());
        }
        if (dto.getMoTa() != null) {
            pt.setMoTa(dto.getMoTa().trim());
        }

        PhuongTien updated = phuongTienRepository.save(pt);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void xoaPhuongTien(Long id) {
        if (!phuongTienRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phương tiện ID: " + id);
        }
        phuongTienRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PhuongTien timHoacTaoTheoBienSo(String bienSoXe, Long loaiXeId, String chuXe, String soDienThoai) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) {
            return null;
        }

        LoaiXe lx = null;
        if (loaiXeId != null) {
            lx = loaiXeRepository.findById(loaiXeId).orElse(null);
        }
        if (lx == null) {
            List<LoaiXe> loaiXes = loaiXeRepository.findAll();
            if (!loaiXes.isEmpty()) lx = loaiXes.get(0);
        }

        com.example.demo.util.BienSoXeHelper.ValidationResult vResult = 
                com.example.demo.util.BienSoXeHelper.validate(bienSoXe, lx != null ? lx.getTenLoai() : null);
        String displayPlate = vResult.valid() ? vResult.displayValue() : bienSoXe.trim().toUpperCase();
        String cleanKey = com.example.demo.util.BienSoXeHelper.toCleanKey(bienSoXe);

        List<PhuongTien> list = phuongTienRepository.findAll();
        for (PhuongTien pt : list) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(pt.getBienSoXe(), cleanKey)) {
                return pt;
            }
        }

        PhuongTien newPt = new PhuongTien(displayPlate, lx, chuXe, soDienThoai, "Tự động ghi nhận khi gửi xe");
        return phuongTienRepository.save(newPt);
    }
}
