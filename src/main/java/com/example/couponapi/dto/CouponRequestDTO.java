package com.example.couponapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload de criação de um cupom de desconto")
public class CouponRequestDTO {

    @Schema(
        description = "Código do cupom. Caracteres especiais são removidos automaticamente; " +
                      "o resultado deve ter exatamente 6 caracteres alfanuméricos.",
        example = "SAVE@10",
        maxLength = 20
    )
    @NotBlank(message = "O campo 'code' é obrigatório.")
    @Size(max = 20, message = "O campo 'code' deve ter no máximo 20 caracteres.")
    private String code;

    @Schema(description = "Descrição legível do cupom.", example = "10% de desconto na primeira compra")
    @NotBlank(message = "O campo 'description' é obrigatório.")
    private String description;

    @Schema(
        description = "Valor do desconto. Mínimo: 0.5. Sem máximo definido.",
        example = "10.00",
        minimum = "0.5"
    )
    @NotNull(message = "O campo 'discountValue' é obrigatório.")
    @DecimalMin(value = "0.5", inclusive = true,
                message = "O desconto mínimo permitido é 0.5.")
    private BigDecimal discountValue;

    @Schema(
        description = "Data de expiração do cupom. Não pode estar no passado.",
        example = "2026-12-31",
        type = "string",
        format = "date"
    )
    @NotNull(message = "O campo 'expirationDate' é obrigatório.")
    private LocalDate expirationDate;

    @Schema(
        description = "Se true, o cupom é criado já publicado/ativo para uso.",
        example = "true",
        defaultValue = "false"
    )
    private boolean published = false;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
