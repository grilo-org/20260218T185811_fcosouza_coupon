package com.example.couponapi.exception;

/**
 * Lançada quando um cupom solicitado não é encontrado ou já foi soft-deletado.
 * Resulta em HTTP 404 Not Found.
 */
public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(Long id) {
        super("Cupom com id " + id + " não encontrado ou já foi removido.");
    }
}
