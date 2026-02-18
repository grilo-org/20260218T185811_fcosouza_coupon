package com.example.couponapi.service;

import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.BusinessException;
import com.example.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Serviço de validação — orquestra as regras de negócio que requerem
 * acesso ao repositório (ex.: unicidade do código).
 *
 * As regras puras de domínio (tamanho do código, valor mínimo, data)
 * são delegadas para os métodos estáticos de {@link Coupon}, mantendo
 * o conhecimento encapsulado no objeto de domínio.
 */
@Service
public class CouponValidationService {

    private final CouponRepository couponRepository;

    public CouponValidationService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Sanitiza e valida o código completo:
     *  1. Remove caracteres especiais (regra de domínio).
     *  2. Verifica tamanho exato de 6 chars (regra de domínio).
     *  3. Verifica unicidade no banco (requer repositório).
     *
     * @return código sanitizado e validado
     */
    public String sanitizeAndValidateCode(String rawCode) {
        String sanitized = Coupon.sanitizeCode(rawCode);
        Coupon.validateCode(sanitized);
        validateCodeUniqueness(sanitized);
        return sanitized;
    }

    /**
     * Delega para o domínio a validação do valor mínimo de desconto.
     *
     * @see Coupon#validateDiscountValue(BigDecimal)
     */
    public void validateDiscountValue(BigDecimal discountValue) {
        Coupon.validateDiscountValue(discountValue);
    }

    /**
     * Delega para o domínio a validação da data de expiração,
     * passando a data de hoje como referência (permite mock em testes).
     *
     * @see Coupon#validateExpirationDate(LocalDate, LocalDate)
     */
    public void validateExpirationDate(LocalDate expirationDate) {
        Coupon.validateExpirationDate(expirationDate, LocalDate.now());
    }

    // -------------------------------------------------------------------------
    // Validação que precisa do repositório
    // -------------------------------------------------------------------------

    private void validateCodeUniqueness(String sanitizedCode) {
        if (couponRepository.existsByCode(sanitizedCode)) {
            throw new BusinessException(
                "Já existe um cupom com o código '" + sanitizedCode + "'."
            );
        }
    }
}
