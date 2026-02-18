package com.example.couponapi.service;

import com.example.couponapi.dto.CouponRequestDTO;
import com.example.couponapi.dto.CouponResponseDTO;
import com.example.couponapi.entity.Coupon;
import com.example.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo caso de uso de criação de cupons.
 *
 * Orquestra a validação (delegada ao {@link CouponValidationService})
 * e a persistência da nova entidade.
 */
@Service
public class CouponCreationService {

    private final CouponRepository couponRepository;
    private final CouponValidationService validationService;

    public CouponCreationService(CouponRepository couponRepository,
                                 CouponValidationService validationService) {
        this.couponRepository  = couponRepository;
        this.validationService = validationService;
    }

    /**
     * Cria e persiste um novo cupom aplicando todas as regras de negócio.
     *
     * Fluxo:
     *  1. Sanitiza o código.
     *  2. Valida tamanho do código sanitizado.
     *  3. Valida unicidade do código.
     *  4. Valida valor mínimo de desconto.
     *  5. Valida data de expiração.
     *  6. Persiste e retorna o DTO de resposta.
     *
     * @param dto dados de entrada validados pelo Bean Validation
     * @return DTO com os dados do cupom criado
     */
    @Transactional
    public CouponResponseDTO create(CouponRequestDTO dto) {
        String sanitizedCode = validationService.sanitizeAndValidateCode(dto.getCode());

        validationService.validateDiscountValue(dto.getDiscountValue());
        validationService.validateExpirationDate(dto.getExpirationDate());

        Coupon coupon = Coupon.builder()
                .code(sanitizedCode)
                .description(dto.getDescription())
                .discountValue(dto.getDiscountValue())
                .expirationDate(dto.getExpirationDate())
                .published(dto.isPublished())
                .build();

        return CouponResponseDTO.fromEntity(couponRepository.save(coupon));
    }
}
