package com.example.couponapi.entity;

import com.example.couponapi.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários das regras de domínio encapsuladas em {@link Coupon}.
 *
 * Estes testes não dependem de Spring, banco ou mocks —
 * validam apenas o comportamento do objeto de domínio.
 */
@DisplayName("Coupon – regras de domínio")
class CouponTest {

    // =========================================================================
    // sanitizeCode
    // =========================================================================

    @Nested
    @DisplayName("sanitizeCode()")
    class SanitizeCode {

        @Test
        @DisplayName("remove caracteres especiais e mantém alfanuméricos")
        void removesSpecialChars() {
            assertThat(Coupon.sanitizeCode("AB-C1#2")).isEqualTo("ABC12");
            assertThat(Coupon.sanitizeCode("A@B!C1D2")).isEqualTo("ABC1D2");
            assertThat(Coupon.sanitizeCode("SAVE@10")).isEqualTo("SAVE10");
        }

        @Test
        @DisplayName("não altera código já alfanumérico")
        void keepsAlphanumericUnchanged() {
            assertThat(Coupon.sanitizeCode("ABC123")).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("remove todos os caracteres especiais, podendo resultar em string vazia")
        void canResultInEmptyString() {
            assertThat(Coupon.sanitizeCode("---###")).isEmpty();
        }
    }

    // =========================================================================
    // validateCode
    // =========================================================================

    @Nested
    @DisplayName("validateCode()")
    class ValidateCode {

        @Test
        @DisplayName("aceita código com exatamente 6 caracteres")
        void acceptsSixChars() {
            assertThatNoException().isThrownBy(() -> Coupon.validateCode("ABC123"));
        }

        @Test
        @DisplayName("lança BusinessException para código com menos de 6 chars")
        void rejectsTooShort() {
            assertThatThrownBy(() -> Coupon.validateCode("ABC12"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("6 caracteres");
        }

        @Test
        @DisplayName("lança BusinessException para código com mais de 6 chars")
        void rejectsTooLong() {
            assertThatThrownBy(() -> Coupon.validateCode("ABC1234"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("6 caracteres");
        }

        @Test
        @DisplayName("lança BusinessException para código vazio")
        void rejectsEmpty() {
            assertThatThrownBy(() -> Coupon.validateCode(""))
                .isInstanceOf(BusinessException.class);
        }
    }

    // =========================================================================
    // validateDiscountValue
    // =========================================================================

    @Nested
    @DisplayName("validateDiscountValue()")
    class ValidateDiscountValue {

        @Test
        @DisplayName("aceita valor igual ao mínimo (0.5)")
        void acceptsMinimumValue() {
            assertThatNoException()
                .isThrownBy(() -> Coupon.validateDiscountValue(new BigDecimal("0.5")));
        }

        @Test
        @DisplayName("aceita valor acima do mínimo")
        void acceptsAboveMinimum() {
            assertThatNoException()
                .isThrownBy(() -> Coupon.validateDiscountValue(new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("lança BusinessException para valor abaixo do mínimo (0.49)")
        void rejectsBelowMinimum() {
            assertThatThrownBy(() -> Coupon.validateDiscountValue(new BigDecimal("0.49")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("0.5");
        }

        @Test
        @DisplayName("lança BusinessException para valor zero")
        void rejectsZero() {
            assertThatThrownBy(() -> Coupon.validateDiscountValue(BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("lança BusinessException para valor negativo")
        void rejectsNegative() {
            assertThatThrownBy(() -> Coupon.validateDiscountValue(new BigDecimal("-1")))
                .isInstanceOf(BusinessException.class);
        }
    }

    // =========================================================================
    // validateExpirationDate
    // =========================================================================

    @Nested
    @DisplayName("validateExpirationDate()")
    class ValidateExpirationDate {

        private final LocalDate TODAY = LocalDate.of(2026, 2, 18);

        @Test
        @DisplayName("aceita data futura")
        void acceptsFutureDate() {
            assertThatNoException()
                .isThrownBy(() -> Coupon.validateExpirationDate(TODAY.plusDays(1), TODAY));
        }

        @Test
        @DisplayName("aceita a data de hoje")
        void acceptsToday() {
            assertThatNoException()
                .isThrownBy(() -> Coupon.validateExpirationDate(TODAY, TODAY));
        }

        @Test
        @DisplayName("lança BusinessException para data de ontem")
        void rejectsYesterday() {
            assertThatThrownBy(() ->
                    Coupon.validateExpirationDate(TODAY.minusDays(1), TODAY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passado");
        }

        @Test
        @DisplayName("lança BusinessException para data muito no passado")
        void rejectsDistantPast() {
            assertThatThrownBy(() ->
                    Coupon.validateExpirationDate(LocalDate.of(2020, 1, 1), TODAY))
                .isInstanceOf(BusinessException.class);
        }
    }

    // =========================================================================
    // softDelete
    // =========================================================================

    @Nested
    @DisplayName("softDelete()")
    class SoftDelete {

        @Test
        @DisplayName("preenche deletedAt e marca cupom como deletado")
        void marksDeletedAt() {
            Coupon coupon = activeCoupon();
            LocalDateTime before = LocalDateTime.now();

            coupon.softDelete();

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.getDeletedAt()).isAfterOrEqualTo(before);
        }

        @Test
        @DisplayName("lança BusinessException ao tentar deletar cupom já deletado")
        void rejectsDoubleDeletion() {
            Coupon coupon = activeCoupon();
            coupon.softDelete(); // primeira deleção — ok

            assertThatThrownBy(coupon::softDelete) // segunda deleção — deve falhar
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi removido");
        }

        @Test
        @DisplayName("cupom recém-criado não está deletado")
        void newCouponIsNotDeleted() {
            assertThat(activeCoupon().isDeleted()).isFalse();
        }

        // helper
        private Coupon activeCoupon() {
            return Coupon.builder()
                .code("TST001")
                .description("Teste")
                .discountValue(new BigDecimal("10"))
                .expirationDate(LocalDate.now().plusDays(30))
                .build();
        }
    }
}
