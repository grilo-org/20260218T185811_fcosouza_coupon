package com.example.couponapi.service;

import com.example.couponapi.dto.CouponResponseDTO;
import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.CouponNotFoundException;
import com.example.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponQueryService")
class CouponQueryServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponQueryService queryService;

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Coupon couponWithId(Long id, String code) {
        Coupon c = Coupon.builder()
            .code(code)
            .description("Desconto")
            .discountValue(new BigDecimal("20"))
            .expirationDate(LocalDate.now().plusDays(60))
            .build();
        try {
            var field = Coupon.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findById — retorna DTO quando cupom existe")
    void returnsDto_whenFound() {
        Coupon coupon = couponWithId(1L, "QRY001");
        given(couponRepository.findActiveById(1L)).willReturn(Optional.of(coupon));

        CouponResponseDTO result = queryService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("QRY001");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById — lança CouponNotFoundException quando não encontrado")
    void throwsNotFound_whenMissing() {
        given(couponRepository.findActiveById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.findById(42L))
            .isInstanceOf(CouponNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAll — mapeia lista de cupons corretamente")
    void returnsMappedList() {
        given(couponRepository.findAllActive()).willReturn(List.of(
            couponWithId(1L, "QRY001"),
            couponWithId(2L, "QRY002")
        ));

        List<CouponResponseDTO> result = queryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CouponResponseDTO::getCode)
            .containsExactly("QRY001", "QRY002");
    }

    @Test
    @DisplayName("findAll — retorna lista vazia quando não há cupons")
    void returnsEmptyList_whenNoCoupons() {
        given(couponRepository.findAllActive()).willReturn(List.of());

        assertThat(queryService.findAll()).isEmpty();
    }
}
