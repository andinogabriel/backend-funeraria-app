# OpenAPI

`openapi.yaml` es la fuente de verdad de la documentacion HTTP del servicio.

## Que documenta

- Todos los endpoints expuestos hoy por `web/controller`
- Los DTOs reales de request y response usados por el backend
- Seguridad por `Bearer JWT` con binding por dispositivo
- Idempotencia para `login` y `refresh`
- Errores comunes con `ProblemDetail`

## Como leer la especificacion

- `Authentication`: registro, login, refresh, logout y activacion
- `Users`: perfil, roles, direcciones y telefonos del usuario autenticado
- `Affiliates`, `Deceased`, `Funerals`, `Incomes`, `Items`, `Plans`, `Suppliers`: slices de negocio
- `Catalogs`: datos de referencia para formularios y procesos

## Convenciones importantes

- Los endpoints protegidos requieren:
  - `Authorization: Bearer <token>`
  - `X-Device-Id`
- `POST /api/v1/users/login` y `POST /api/v1/users/refresh` aceptan `Idempotency-Key`
- Los DTOs en `components/schemas` usan el nombre real de las clases Java para evitar referencias ambiguas
- Algunas fechas de response no salen en ISO:
  - `LocalDate` serializado como `dd-MM-yyyy`
  - `LocalDateTime` serializado como `dd-MM-yyyy HH:mm`
- Las fechas de request siguen el formato Jackson por defecto del backend:
  - `date`: `yyyy-MM-dd`
  - `date-time`: `yyyy-MM-dd'T'HH:mm:ss`

## Mantenimiento

- Si cambia un controller o un DTO, actualizar `openapi.yaml` en el mismo PR
- No volver al esquema fragmentado anterior
- Mantener las referencias con nombres consistentes para que Swagger UI no rompa por `$ref` huerfanos
