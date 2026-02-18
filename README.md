# Coupon API

API REST para gerenciamento de **cupons de desconto**, desenvolvida com **Spring Boot 3**, **Spring Data JPA**, **Bean Validation** e **Lombok**.

---

## ▶ Como executar

### Pré-requisitos

- Java 17+
- Maven 3.8+

```bash
# Na raiz do projeto (onde está o pom.xml)
mvn spring-boot:run
```

A aplicação sobe em **http://localhost:8080**
Console H2: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:coupondb`)

---

## 📦 Estrutura do projeto

```
src/main/java/com/example/couponapi/
├── CouponApiApplication.java          # Ponto de entrada Spring Boot
├── controller/
│   └── CouponController.java          # Endpoints REST
├── service/
│   └── CouponService.java             # Regras de negócio
├── repository/
│   └── CouponRepository.java          # Acesso a dados (Spring Data JPA)
├── entity/
│   └── Coupon.java                    # Entidade JPA (tabela: coupons)
├── dto/
│   ├── CouponRequestDTO.java          # Payload de entrada (criação)
│   └── CouponResponseDTO.java         # Payload de saída (respostas)
└── exception/
    ├── BusinessException.java         # Exceção de regra de negócio (422)
    ├── CouponNotFoundException.java   # Cupom não encontrado (404)
    └── GlobalExceptionHandler.java    # Handler global de exceções
```

---

## 🔗 Endpoints

| Método   | Endpoint               | Descrição                    | Status de sucesso |
| -------- | ---------------------- | ---------------------------- | ----------------- |
| `POST`   | `/api/v1/coupons`      | Cria um novo cupom           | `201 Created`     |
| `GET`    | `/api/v1/coupons`      | Lista todos os cupons ativos | `200 OK`          |
| `GET`    | `/api/v1/coupons/{id}` | Busca cupom por ID           | `200 OK`          |
| `DELETE` | `/api/v1/coupons/{id}` | Soft delete de um cupom      | `204 No Content`  |

---

## 📋 Exemplos de payload

### POST /api/v1/coupons – Criar cupom

**Request:**

```json
{
  "code": "SAVE@10",
  "description": "10% de desconto na primeira compra",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31",
  "published": true
}
```

> O código `"SAVE@10"` tem `@` removido → `"SAVE10"` (6 chars válidos ✔)

**Response (201 Created):**

```json
{
  "id": 1,
  "code": "SAVE10",
  "description": "10% de desconto na primeira compra",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31",
  "published": true,
  "createdAt": "2026-02-18T10:30:00"
}
```

---

### GET /api/v1/coupons – Listar cupons

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "code": "SAVE10",
    "description": "10% de desconto na primeira compra",
    "discountValue": 10.0,
    "expirationDate": "2026-12-31",
    "published": true,
    "createdAt": "2026-02-18T10:30:00"
  }
]
```

---

### DELETE /api/v1/coupons/1 – Soft delete

**Response (204 No Content):** corpo vazio.

Tentar deletar novamente retorna:

```json
{
  "timestamp": "2026-02-18T10:35:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "O cupom com id 1 já foi removido anteriormente."
}
```

---

## ✅ Regras de negócio

| Regra                                    | Comportamento                                 |
| ---------------------------------------- | --------------------------------------------- |
| `code` com caracteres especiais          | Removidos automaticamente antes de salvar     |
| `code` deve ter 6 chars após sanitização | `422` se diferente de 6                       |
| `code` duplicado                         | `422`                                         |
| `discountValue` < 0.5                    | `400` (Bean Validation)                       |
| `expirationDate` no passado              | `422`                                         |
| Delete de cupom inexistente              | `404`                                         |
| Delete de cupom já deletado              | `422`                                         |
| Delete é lógico (soft delete)            | Campo `deletedAt` preenchido, dado preservado |
