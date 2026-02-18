package com.example.couponapi.entity;

import com.example.couponapi.exception.BusinessException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidade de domínio que representa um cupom de desconto.
 */
@Entity
@Table(name = "coupons")
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MIN_DISCOUNT = new BigDecimal("0.5");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private boolean published = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    public Coupon() {}

    private Coupon(Builder builder) {
        this.code           = builder.code;
        this.description    = builder.description;
        this.discountValue  = builder.discountValue;
        this.expirationDate = builder.expirationDate;
        this.published      = builder.published;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================================================================
    // Regras de domínio
    // =========================================================================

    /** Remove caracteres não alfanuméricos do código bruto. */
    public static String sanitizeCode(String rawCode) {
        return rawCode.replaceAll("[^a-zA-Z0-9]", "");
    }

    /** Valida que o código sanitizado tem exatamente 6 caracteres. */
    public static void validateCode(String sanitizedCode) {
        if (sanitizedCode.length() != CODE_LENGTH) {
            throw new BusinessException(
                "O campo 'code' deve resultar em exatamente " + CODE_LENGTH +
                " caracteres alfanuméricos após a remoção de caracteres especiais. " +
                "Código sanitizado: '" + sanitizedCode + "' (" + sanitizedCode.length() + " chars)."
            );
        }
    }

    /** Valida que o desconto é maior ou igual ao mínimo permitido (0.5). */
    public static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue.compareTo(MIN_DISCOUNT) < 0) {
            throw new BusinessException("O desconto mínimo permitido é " + MIN_DISCOUNT + ".");
        }
    }

    /**
     * Valida que a data de expiração não está no passado.
     * @param today data de referência injetável para facilitar testes
     */
    public static void validateExpirationDate(LocalDate expirationDate, LocalDate today) {
        if (expirationDate.isBefore(today)) {
            throw new BusinessException("A data de expiração não pode estar no passado.");
        }
    }

    /**
     * Soft delete: preenche deletedAt com o instante atual.
     * Lança BusinessException se o cupom já tiver sido removido.
     */
    public void softDelete() {
        if (isDeleted()) {
            throw new BusinessException("O cupom com id " + id + " já foi removido anteriormente.");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getCode()                    { return code; }
    public void setCode(String code)           { this.code = code; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public BigDecimal getDiscountValue()       { return discountValue; }
    public void setDiscountValue(BigDecimal v) { this.discountValue = v; }

    public LocalDate getExpirationDate()       { return expirationDate; }
    public void setExpirationDate(LocalDate d) { this.expirationDate = d; }

    public boolean isPublished()               { return published; }
    public void setPublished(boolean p)        { this.published = p; }

    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public LocalDateTime getDeletedAt()        { return deletedAt; }
    public void setDeletedAt(LocalDateTime t)  { this.deletedAt = t; }

    // =========================================================================
    // Builder
    // =========================================================================

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String code;
        private String description;
        private BigDecimal discountValue;
        private LocalDate expirationDate;
        private boolean published = false;

        public Builder code(String code)               { this.code = code; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder discountValue(BigDecimal v)     { this.discountValue = v; return this; }
        public Builder expirationDate(LocalDate d)     { this.expirationDate = d; return this; }
        public Builder published(boolean published)    { this.published = published; return this; }

        public Coupon build() { return new Coupon(this); }
    }
}
