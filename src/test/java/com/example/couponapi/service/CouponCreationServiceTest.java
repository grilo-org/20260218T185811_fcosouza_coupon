package com.example.couponapi.service;

import com.example.couponapi.dto.CouponRequestDTO;
import com.example.couponapi.dto.CouponResponseDTO;
import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.BusinessException;
import com.example.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponCreationService")
class CouponCreationServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponValidationService validationService;

    @InjectMocks
    private CouponCreationService creationService;

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private CouponRequestDTO validDto(String rawCode) {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setCode(rawCode);
        dto.setDescription("Desconto de verão");
        dto.setDiscountValue(new BigDecimal("15.00"));
        dto.setExpirationDate(LocalDate.now().plusDays(30));
        dto.setPublished(false);
        return dto;
    }

    private Coupon savedCoupon(String sanitizedCode, CouponRequestDTO dto) {
        Coupon c = Coupon.builder()
            .code(sanitizedCode)
            .description(dto.getDescription())
            .discountValue(dto.getDiscountValue())
            .expirationDate(dto.getExpirationDate())
            .build();
        return c;
    }

    // -------------------------------------------------------------------------
    // happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Caminho feliz")
    class HappyPath {

        @Test
        @DisplayName("cria cupom com código saneado e retorna DTO")
        void createsCouponWithSanitizedCode() {
            CouponRequestDTO dto = validDto("AB-C1#23");  // sanitized → "ABC123"
            given(validationService.sanitizeAndValidateCode("AB-C1#23")).willReturn("ABC123");
            willDoNothing().given(validationService).validateDiscountValue(any());
            willDoNothing().given(validationService).validateExpirationDate(any());

            Coupon persisted = savedCoupon("ABC123", dto);
            given(couponRepository.save(any(Coupon.class))).willReturn(persisted);

            CouponResponseDTO result = creationService.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("ABC123");

            ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
            then(couponRepository).should().save(captor.capture());
            assertThat(captor.getValue().getCode()).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("persiste cupom como published=true quando solicitado")
        void savesPublishedFlag() {
            CouponRequestDTO dto = validDto("PUB001");
            dto.setPublished(true);

            given(validationService.sanitizeAndValidateCode("PUB001")).willReturn("PUB001");
            willDoNothing().given(validationService).validateDiscountValue(any());
            willDoNothing().given(validationService).validateExpirationDate(any());

            Coupon persisted = savedCoupon("PUB001", dto);
            given(couponRepository.save(any(Coupon.class))).willReturn(persisted);

            creationService.create(dto);

            ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
            then(couponRepository).should().save(captor.capture());
            assertThat(captor.getValue().isPublished()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // validation failures delegated to validationService
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Falhas de validação")
    class ValidationFailures {

        @Test
        @DisplayName("código com resultado saneado de tamanho errado → BusinessException")
        void invalidSanitizedCodeLength() {
            CouponRequestDTO dto = validDto("AB---");
            given(validationService.sanitizeAndValidateCode("AB---"))
                .willThrow(new BusinessException("Código deve ter 6 caracteres"));

            assertThatThrownBy(() -> creationService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("6 caracteres");

            then(couponRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("código duplicado → BusinessException")
        void duplicateCode() {
            CouponRequestDTO dto = validDto("DUP001");
            given(validationService.sanitizeAndValidateCode("DUP001"))
                .willThrow(new BusinessException("Já existe um cupom com o código DUP001"));

            assertThatThrownBy(() -> creationService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DUP001");

            then(couponRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("discountValue < 0.5 → BusinessException")
        void lowDiscountValue() {
            CouponRequestDTO dto = validDto("OK0001");
            given(validationService.sanitizeAndValidateCode(anyString())).willReturn("OK0001");
            willThrow(new BusinessException("Desconto mínimo é 0.5"))
                .given(validationService).validateDiscountValue(any());

            assertThatThrownBy(() -> creationService.create(dto))
                .isInstanceOf(BusinessException.class);

            then(couponRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("data de expiração no passado → BusinessException")
        void pastExpirationDate() {
            CouponRequestDTO dto = validDto("OK0002");
            given(validationService.sanitizeAndValidateCode(anyString())).willReturn("OK0002");
            willDoNothing().given(validationService).validateDiscountValue(any());
            willThrow(new BusinessException("Data de expiração no passado"))
                .given(validationService).validateExpirationDate(any());

            assertThatThrownBy(() -> creationService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passado");

            then(couponRepository).should(never()).save(any());
        }
    }
}
