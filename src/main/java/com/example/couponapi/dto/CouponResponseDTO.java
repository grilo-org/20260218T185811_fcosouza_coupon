package com.example.couponapi.dto;

import com.example.couponapi.entity.Coupon;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Representação de um cupom de desconto retornado pela API")
public class CouponResponseDTO {

    @Schema(description = "Identificador único do cupom.", example = "1")
    private Long id;

    @Schema(description = "Código do cupom — sempre 6 caracteres alfanuméricos.", example = "SAVE10")
    private String code;

    @Schema(description = "Descrição do cupom.", example = "10% de desconto na primeira compra")
    private String description;

    @Schema(description = "Valor do desconto.", example = "10.0")
    private BigDecimal discountValue;

    @Schema(description = "Data de expiração do cupom.", example = "2026-12-31", type = "string", format = "date")
    private LocalDate expirationDate;

    @Schema(description = "Indica se o cupom está publicado.", example = "true")
    private boolean published;

    @Schema(description = "Timestamp de criação do registro.", example = "2026-02-18T10:30:00")
    private LocalDateTime createdAt;

    private CouponResponseDTO() {}

    public static CouponResponseDTO fromEntity(Coupon coupon) {
        CouponResponseDTO dto = new CouponResponseDTO();
        dto.id             = coupon.getId();
        dto.code           = coupon.getCode();
        dto.description    = coupon.getDescription();
        dto.discountValue  = coupon.getDiscountValue();
        dto.expirationDate = coupon.getExpirationDate();
        dto.published      = coupon.isPublished();
        dto.createdAt      = coupon.getCreatedAt();
        return dto;
    }

    // -------------------------------------------------------------------------
    // Getters (necessários para serialização JSON pelo Jackson)
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public boolean isPublished() { return published; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
