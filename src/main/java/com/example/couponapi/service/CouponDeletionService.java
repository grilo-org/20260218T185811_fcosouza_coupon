package com.example.couponapi.service;

import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.CouponNotFoundException;
import com.example.couponapi.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo caso de uso de remoção de cupons.
 *
 * Implementa soft delete: o registro não é apagado do banco,
 * apenas o campo {@code deletedAt} é preenchido com o instante atual.
 *
 * Regras aplicadas:
 *  - Se o cupom não existir     → {@link CouponNotFoundException} (404).
 *  - Se já estiver soft-deletado → {@link BusinessException} (422).
 */
@Service
public class CouponDeletionService {

    private final CouponRepository couponRepository;

    public CouponDeletionService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    /**
     * Remove logicamente um cupom pelo ID.
     *
     * A busca inclui registros já deletados para distinguir entre
     * "nunca existiu" (404) e "já foi deletado" (422).
     *
     * @param id identificador do cupom a ser removido
     */
    @Transactional
    public void delete(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        // Regra de domínio: softDelete() lança BusinessException se já deletado
        coupon.softDelete();
        couponRepository.save(coupon);
    }
}
