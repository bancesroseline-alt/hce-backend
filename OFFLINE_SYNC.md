# Sincronizacion Offline - Backend HCE

Este backend soporta la recepcion de datos creados o modificados en modo offline desde el frontend Angular.

## Cambios Principales

- `Paciente` ahora incluye `uuidLocal`, `origenRegistro`, `sincronizado` y `fechaSincronizacion`.
- `PacienteRepository` permite buscar pacientes por `uuidLocal`.
- `SincronizacionController` expone `POST /api/sincronizacion/pacientes`.
- `SincronizacionService` registra pacientes offline evitando duplicados por `uuidLocal` o numero de documento.
- `SincronizacionRequestDTO` ahora incluye signos vitales y medicamentos para atenciones offline.
- `SincronizacionCitaService` actualiza una cita existente si llega otra sincronizacion con el mismo `uuidLocal`.

## Endpoints de Sincronizacion

| Endpoint | Funcion |
| --- | --- |
| `POST /api/sincronizacion` | Sincroniza atenciones medicas offline. |
| `POST /api/sincronizacion/citas` | Sincroniza citas creadas offline. |
| `POST /api/sincronizacion/pacientes` | Sincroniza pacientes creados offline. |

## Reglas

- `uuidLocal` evita duplicados cuando el frontend reintenta una sincronizacion.
- Si existe un paciente con el mismo documento, el backend reutiliza el registro existente.
- Si una cita con el mismo `uuidLocal` ya existe, se actualiza con los nuevos datos.
- Las atenciones offline conservan motivo, diagnostico, tratamiento, observaciones, signos vitales y medicamentos.

## Limites

- La resolucion avanzada de conflictos entre usuarios no esta implementada.
- La sincronizacion requiere internet y token valido.
- La prediccion offline se conserva localmente en frontend; no se persiste en backend en esta version.
