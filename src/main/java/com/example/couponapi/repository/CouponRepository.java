package com.example.couponapi.repository;

import com.example.couponapi.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Coupon}.
 *
 * Os métodos customizados excluem automaticamente registros soft-deletados
 * das consultas de leitura padrão.
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Retorna um cupom pelo ID somente se ele NÃO tiver sido soft-deletado.
     * Equivalente a: WHERE id = ? AND deleted_at IS NULL
     */
    @Query("SELECT c FROM Coupon c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Coupon> findActiveById(Long id);

    /**
     * Lista todos os cupons que NÃO foram soft-deletados.
     * Equivalente a: WHERE deleted_at IS NULL
     */
    @Query("SELECT c FROM Coupon c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Coupon> findAllActive();

    /**
     * Verifica se já existe um cupom (ativo ou deletado) com o código informado.
     * Usado para garantir unicidade do campo {@code code}.
     */
    boolean existsByCode(String code);
}
