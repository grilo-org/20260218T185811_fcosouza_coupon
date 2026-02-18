package com.example.couponapi.service;

import com.example.couponapi.dto.CouponResponseDTO;
import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.CouponNotFoundException;
import com.example.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelos casos de uso de consulta de cupons.
 *
 * Responsabilidades:
 *  - Buscar um cupom ativo por ID.
 *  - Listar todos os cupons ativos.
 *
 * Cupons soft-deletados são tratados como inexistentes nestas consultas.
 */
@Service
public class CouponQueryService {

    private final CouponRepository couponRepository;

    public CouponQueryService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Retorna um cupom ativo pelo ID.
     *
     * @param id identificador do cupom
     * @return DTO de resposta
     * @throws CouponNotFoundException se não encontrado ou já soft-deletado
     */
    @Transactional(readOnly = true)
    public CouponResponseDTO findById(Long id) {
        Coupon coupon = couponRepository.findActiveById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
        return CouponResponseDTO.fromEntity(coupon);
    }

    /**
     * Lista todos os cupons ativos, ordenados do mais recente para o mais antigo.
     *
     * @return lista de DTOs de resposta (pode ser vazia)
     */
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> findAll() {
        return couponRepository.findAllActive()
                .stream()
                .map(CouponResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
