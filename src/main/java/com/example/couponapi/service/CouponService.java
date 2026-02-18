package com.example.couponapi.service;

import com.example.couponapi.dto.CouponRequestDTO;
import com.example.couponapi.dto.CouponResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

    private final CouponCreationService  creationService;
    private final CouponQueryService     queryService;
    private final CouponDeletionService  deletionService;

    public CouponService(CouponCreationService creationService,
                         CouponQueryService queryService,
                         CouponDeletionService deletionService) {
        this.creationService = creationService;
        this.queryService    = queryService;
        this.deletionService = deletionService;
    }

    public CouponResponseDTO create(CouponRequestDTO dto) {
        return creationService.create(dto);
    }

    public CouponResponseDTO findById(Long id) {
        return queryService.findById(id);
    }

    public List<CouponResponseDTO> findAll() {
        return queryService.findAll();
    }

    public void delete(Long id) {
        deletionService.delete(id);
    }
}

