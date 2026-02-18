package com.example.couponapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração cobrindo todos os endpoints da API de cupons.
 *
 * Usa H2 em memória (perfil de teste) e MockMvc para simular requisições HTTP
 * sem subir um servidor real.
 *
 * A anotação {@code @TestMethodOrder} garante uma ordem previsível nos testes
 * de ciclo de vida (criar → buscar → deletar → deletar novamente).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Coupon API — testes de integração")
class CouponApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/coupons";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================================
    // POST /api/v1/coupons
    // =========================================================================

    @Test
    @Order(1)
    @DisplayName("POST — cria cupom válido → 201 com código saneado")
    void createCoupon_returns201() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "SAV#E10",          // sanitizado → SAVE10
            "description",   "Desconto de verão",
            "discountValue", "10.00",
            "expirationDate", LocalDate.now().plusDays(30).toString(),
            "published",     false
        ));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SAVE10"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("POST — código duplicado → 422")
    void createCoupon_duplicateCode_returns422() throws Exception {
        // garante que o cupom SAVE10 já foi criado no Order(1)
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "SAVE10",
            "description",   "Duplicado",
            "discountValue", "5.00",
            "expirationDate", LocalDate.now().plusDays(10).toString(),
            "published",     false
        ));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Order(3)
    @DisplayName("POST — data de expiração no passado → 422")
    void createCoupon_pastDate_returns422() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "PAST01",
            "description",   "Expirado",
            "discountValue", "5.00",
            "expirationDate", LocalDate.now().minusDays(1).toString(),
            "published",     false
        ));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Order(4)
    @DisplayName("POST — discountValue abaixo do mínimo (0.3) → 400 (Bean Validation)")
    void createCoupon_lowDiscount_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "LOW001",
            "description",   "Desconto baixo",
            "discountValue", "0.3",
            "expirationDate", LocalDate.now().plusDays(10).toString(),
            "published",     false
        ));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("POST — campos obrigatórios ausentes → 400")
    void createCoupon_missingFields_returns400() throws Exception {
        String body = "{}";

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // GET /api/v1/coupons
    // =========================================================================

    @Test
    @Order(6)
    @DisplayName("GET lista — retorna 200 com cupom criado")
    void listCoupons_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())))
            .andExpect(jsonPath("$[*].code", hasItem("SAVE10")));
    }

    // =========================================================================
    // GET /api/v1/coupons/{id}
    // =========================================================================

    @Test
    @Order(7)
    @DisplayName("GET por id — retorna 200 para cupom existente")
    void getCouponById_returns200() throws Exception {
        // primeiro cria um novo cupom para obter o id
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "GET001",
            "description",   "Para busca por id",
            "discountValue", "8.00",
            "expirationDate", LocalDate.now().plusDays(20).toString(),
            "published",     true
        ));

        MvcResult created = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        String responseBody = created.getResponse().getContentAsString();
        Long id = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.code").value("GET001"));
    }

    @Test
    @Order(8)
    @DisplayName("GET por id — 404 para id inexistente")
    void getCouponById_notFound_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999"))
            .andExpect(status().isNotFound());
    }

    // =========================================================================
    // DELETE /api/v1/coupons/{id}
    // =========================================================================

    @Test
    @Order(9)
    @DisplayName("DELETE — retorna 204 ao deletar cupom existente")
    void deleteCoupon_returns204() throws Exception {
        // cria um cupom dedicado para deleção
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "DEL001",
            "description",   "Para deletar",
            "discountValue", "5.00",
            "expirationDate", LocalDate.now().plusDays(15).toString(),
            "published",     false
        ));

        MvcResult created = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        Long id = objectMapper.readTree(
            created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
            .andExpect(status().isNoContent());
    }

    @Test
    @Order(10)
    @DisplayName("DELETE — retorna 422 ao deletar cupom já deletado")
    void deleteCoupon_alreadyDeleted_returns422() throws Exception {
        // cria e deleta um cupom para então tentar deletar novamente
        String body = objectMapper.writeValueAsString(Map.of(
            "code",          "DEL002",
            "description",   "Deleção dupla",
            "discountValue", "5.00",
            "expirationDate", LocalDate.now().plusDays(15).toString(),
            "published",     false
        ));

        MvcResult created = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();

        Long id = objectMapper.readTree(
            created.getResponse().getContentAsString()).get("id").asLong();

        // primeira deleção — deve funcionar
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
            .andExpect(status().isNoContent());

        // segunda deleção — deve retornar 422
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Order(11)
    @DisplayName("DELETE — retorna 404 para id inexistente")
    void deleteCoupon_notFound_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/99999"))
            .andExpect(status().isNotFound());
    }
}
