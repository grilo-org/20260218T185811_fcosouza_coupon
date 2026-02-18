package com.example.couponapi.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: código com menos de 6
 * caracteres válidos após sanitização, data no passado, cupom já deletado).
 * Resulta em HTTP 422 Unprocessable Entity.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
