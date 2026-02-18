package com.example.couponapi.service;

import com.example.couponapi.entity.Coupon;
import com.example.couponapi.exception.BusinessException;
import com.example.couponapi.exception.CouponNotFoundException;
import com.example.couponapi.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponDeletionService")
class CouponDeletionServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponDeletionService deletionService;

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Coupon activeCoupon(Long id) {
        Coupon c = Coupon.builder()
            .code("DEL001")
            .description("Para deletar")
            .discountValue(new BigDecimal("10"))
            .expirationDate(LocalDate.now().plusDays(30))
            .build();
        // usa reflection para forçar o id sem JPA
        try {
            var field = Coupon.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(c, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    private Coupon alreadyDeletedCoupon(Long id) {
        Coupon c = activeCoupon(id);
        c.softDelete();
        return c;
    }

    // -------------------------------------------------------------------------
    // caminho feliz
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("soft-deleta cupom ativo — persiste deletedAt preenchido")
    void softDeletesActiveCoupon() {
        Coupon coupon = activeCoupon(1L);
        given(couponRepository.findById(1L)).willReturn(Optional.of(coupon));
        given(couponRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        deletionService.delete(1L);

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        then(couponRepository).should().save(captor.capture());
        assertThat(captor.getValue().isDeleted()).isTrue();
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // cupom não encontrado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lança CouponNotFoundException para ID inexistente")
    void throwsNotFoundForMissingId() {
        given(couponRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> deletionService.delete(99L))
            .isInstanceOf(CouponNotFoundException.class);

        then(couponRepository).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deletar duas vezes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lança BusinessException ao tentar deletar cupom já deletado")
    void throwsBusinessExceptionForDoubleDeletion() {
        Coupon coupon = alreadyDeletedCoupon(2L);
        given(couponRepository.findById(2L)).willReturn(Optional.of(coupon));

        assertThatThrownBy(() -> deletionService.delete(2L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("já foi removido");

        then(couponRepository).should(never()).save(any());
    }
}
