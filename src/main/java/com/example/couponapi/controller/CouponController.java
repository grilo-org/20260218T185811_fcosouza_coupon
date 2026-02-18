package com.example.couponapi.controller;

import com.example.couponapi.dto.CouponRequestDTO;
import com.example.couponapi.dto.CouponResponseDTO;
import com.example.couponapi.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para o recurso "cupom".
 * Base path: /api/v1/coupons
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = "Gerenciamento de cupons de desconto")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/coupons
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Criar cupom",
        description = "Cadastra um novo cupom de desconto. " +
                      "Caracteres especiais no campo `code` são removidos automaticamente, " +
                      "devendo restar exatamente **6 caracteres alfanuméricos**."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Cupom criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CouponResponseDTO.class),
                examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "code": "SAVE10",
                      "description": "10% de desconto na primeira compra",
                      "discountValue": 10.0,
                      "expirationDate": "2026-12-31",
                      "published": true,
                      "createdAt": "2026-02-18T10:30:00"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos (Bean Validation falhou)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-02-18T10:30:00",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Erro de validação.",
                      "details": {
                        "code": "O campo 'code' é obrigatório."
                      }
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Violação de regra de negócio (código inválido, data no passado, código duplicado)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-02-18T10:30:00",
                      "status": 422,
                      "error": "Unprocessable Entity",
                      "message": "O campo 'code' deve resultar em exatamente 6 caracteres alfanuméricos."
                    }
                    """)
            )
        )
    })
    @PostMapping
    public ResponseEntity<CouponResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados do novo cupom",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CouponRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Exemplo com caractere especial no code",
                        value = """
                            {
                              "code": "SAVE@10",
                              "description": "10% de desconto na primeira compra",
                              "discountValue": 10.00,
                              "expirationDate": "2026-12-31",
                              "published": true
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody CouponRequestDTO dto) {
        CouponResponseDTO response = couponService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/coupons
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Listar cupons",
        description = "Retorna todos os cupons **ativos** (não soft-deletados), " +
                      "ordenados do mais recente para o mais antigo."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista retornada com sucesso (pode ser vazia)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = CouponResponseDTO.class))
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<CouponResponseDTO>> findAll() {
        return ResponseEntity.ok(couponService.findAll());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/coupons/{id}
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Buscar cupom por ID",
        description = "Retorna um cupom ativo pelo seu identificador. " +
                      "Cupons soft-deletados são tratados como inexistentes."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Cupom encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CouponResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cupom não encontrado ou já removido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-02-18T10:30:00",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Cupom com id 99 não encontrado ou já foi removido."
                    }
                    """)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> findById(
            @Parameter(description = "ID do cupom", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(couponService.findById(id));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/coupons/{id}
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Remover cupom (soft delete)",
        description = "Remove logicamente o cupom definindo `deletedAt`. " +
                      "O dado é preservado no banco. " +
                      "Não é possível deletar um cupom que já foi removido."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Cupom removido com sucesso"),
        @ApiResponse(
            responseCode = "404",
            description = "Cupom não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-02-18T10:30:00",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Cupom com id 99 não encontrado ou já foi removido."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Cupom já havia sido removido anteriormente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2026-02-18T10:30:00",
                      "status": 422,
                      "error": "Unprocessable Entity",
                      "message": "O cupom com id 1 já foi removido anteriormente."
                    }
                    """)
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do cupom a ser removido", example = "1", required = true)
            @PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
