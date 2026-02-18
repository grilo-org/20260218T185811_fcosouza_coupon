package com.example.couponapi.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler global de exceções.
 *
 * Centraliza o tratamento de erros da aplicação, padronizando o formato
 * de resposta de erro para todos os endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Erros de validação do Bean Validation (@Valid no controller)
    // -------------------------------------------------------------------------

    /**
     * Captura erros de validação de @RequestBody (Bean Validation via @Valid).
     * Retorna HTTP 400 com a lista de campos inválidos e as mensagens.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Coleta todos os erros de campo em um mapa campo → mensagem
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        // em caso de campo duplicado, mantém o primeiro erro
                        (msg1, msg2) -> msg1
                ));

        return buildResponse(HttpStatus.BAD_REQUEST, "Erro de validação.", fieldErrors);
    }

    /**
     * Captura violações de constraint em parâmetros de método (@PathVariable, @RequestParam).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    // -------------------------------------------------------------------------
    // Erros de negócio customizados
    // -------------------------------------------------------------------------

    /**
     * Cupom não encontrado → HTTP 404.
     */
    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CouponNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /**
     * Violação de regra de negócio → HTTP 422.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    /**
     * Qualquer outra exceção não mapeada → HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.",
                null
        );
    }

    // -------------------------------------------------------------------------
    // Auxiliar – monta o corpo padronizado de resposta de erro
    // -------------------------------------------------------------------------

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        // Details é opcional (ex.: mapa de erros de campo)
        if (details != null) {
            body.put("details", details);
        }

        return ResponseEntity.status(status).body(body);
    }
}
