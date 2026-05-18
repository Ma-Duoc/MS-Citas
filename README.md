# MS-Citas 

Microservicio para la gestión de citas médicas en una arquitectura de microservicios. Este servicio es responsable de crear, consultar y gestionar citas médicas, validando la disponibilidad de pacientes, médicos y salas a través de comunicación con otros microservicios.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Configuración](#configuración)
- [API Endpoints](#api-endpoints)
- [Microservicios Dependientes](#microservicios-dependientes)
- [Base de Datos](#base-de-datos)
- [Tolerancia a Fallos](#tolerancia-a-fallos)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Manejo de errores]()


## Características

- **Gestión de Citas Médicas**: Creación y consulta de citas médicas
- **Validación de Disponibilidad**: Verifica disponibilidad de pacientes, médicos y salas
- **Comunicación entre Microservicios**: Integración con ms-pacientes, ms-medicos, ms-salas y ms-notificaciones
- **Circuit Breaker**: Implementación de Resilience4j para tolerancia a fallos
- **Validación de Datos**: Validación de entrada con Jakarta Validation
- **Manejo de Excepciones**: Manejo centralizado de errores
- **Logging**: Logging detallado para debugging y monitoreo
- **Base de Datos H2**: Base de datos en memoria para desarrollo

## Tecnologías

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0**
- **Spring Data JPA**
- **Spring Cloud OpenFeign**
- **Resilience4j** (Circuit Breaker & Retry)
- **H2 Database**
- **Lombok**
- **Jakarta Validation**

## Arquitectura

El microservicio `ms-citas` se comunica con los siguientes microservicios externos:

```
┌─────────────────┐
│   ms-citas      │
│   (Port 8081)   │
└────────┬────────┘
         │
         ├──► ms-pacientes (localhost:8082)
         ├──► ms-medicos (localhost:8083)
         ├──► ms-salas (localhost:8084)
         └──► ms-notificaciones (localhost:8086)
```

### Flujo de Creación de Cita

1. Recibe solicitud de creación de cita
2. Valida disponibilidad del paciente (ms-pacientes)
3. Valida disponibilidad del médico (ms-medicos)
4. Valida disponibilidad de la sala (ms-salas)
5. Verifica que no existan duplicados (médico/sala en misma fecha)
6. Guarda la cita en base de datos
7. Envía notificación (ms-notificaciones)
8. Retorna la cita creada

## Requisitos Previos

- **Java 17** o superior
- **Maven 3.6+**
- **Microservicios dependientes** ejecutándose:
  - ms-pacientes (puerto 8082)
  - ms-medicos (puerto 8083)
  - ms-salas (puerto 8084)
  - ms-notificaciones (puerto 8086)

## ⚙️ Configuración

### Configuración por Defecto

El servicio se configura mediante `application.properties`:

- **Puerto**: 8081
- **Base de Datos**: H2 en memoria (jdbc:h2:mem:citasdb)
- **Consola H2**: http://localhost:8081/h2-console
- **Credenciales H2**: sa / password

### Configuración de Circuit Breaker

- **Sliding Window Size**: 10 llamadas
- **Failure Rate Threshold**: 50%
- **Wait Duration in Open State**: 10 segundos
- **Minimum Number of Calls**: 5
- **Retry Max Attempts**: 3
- **Retry Wait Duration**: 1 segundo

### Configuración de Feign

- **Connect Timeout**: 3000ms
- **Read Timeout**: 3000ms

## API Endpoints

### Crear Cita
```http
POST /api/citas
Content-Type: application/json

{
  "userId": "12345678-9",
  "userNombre": "Juan Pérez",
  "medicoId": "1",
  "medicoNombre": "Dr. García",
  "especialidad": "Cardiología",
  "salaId": 1,
  "salaNombre": "Sala A",
  "fechaHora": "2024-12-15T10:00:00"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "userId": "12345678-9",
  "userNombre": "Juan Pérez",
  "medicoId": "1",
  "medicoNombre": "Dr. García",
  "especialidad": "Cardiología",
  "salaId": 1,
  "salaNombre": "Sala A",
  "fechaHora": "2024-12-15T10:00:00",
  "estado": "PROGRAMADA"
}
```

### Obtener Cita por ID
```http
GET /api/citas/{id}
```

**Response**: `200 OK`

### Obtener Citas por Usuario
```http
GET /api/citas/usuario/{userId}
```

**Response**: `200 OK`

### Obtener Citas por Médico
```http
GET /api/citas/medico/{medicoId}
```

**Response**: `200 OK`

### Health Check
```http
GET /api/citas/health
```

**Response**: `200 OK`
```
UP - ms-citas
```

## Microservicios Dependientes

### ms-pacientes (localhost:8082)
- **Endpoint utilizado**: `GET /api/pacientes/{rut}`
- **Propósito**: Validar existencia del paciente
- **Response**: Datos del paciente (rut, nombre, apellido, email, activo)

### ms-medicos (localhost:8083)
- **Endpoint utilizado**: `GET /api/medicos/{id}`
- **Propósito**: Validar existencia y estado activo del médico
- **Response**: Datos del médico (id, nombre, apellido, especialidad, activo)

### ms-salas (localhost:8084)
- **Endpoint utilizado**: `GET /api/salas/{id}`
- **Propósito**: Validar existencia de la sala
- **Response**: Datos de la sala (id, nombre, tipo, capacidad)

### ms-notificaciones (localhost:8086)
- **Endpoint utilizado**: `POST /api/notificaciones/enviar`
- **Propósito**: Enviar notificación de cita creada
- **Request**: tipo, destino, mensaje

## Base de Datos

### Tabla: citas

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | Long | Primary Key (auto-generado) |
| user_id | String | ID/RUT del paciente |
| user_nombre | String | Nombre del paciente |
| medico_id | String | ID del médico |
| medico_nombre | String | Nombre del médico |
| especialidad | String | Especialidad médica |
| sala_id | Long | ID de la sala |
| sala_nombre | String | Nombre de la sala |
| fecha_hora | LocalDateTime | Fecha y hora de la cita |

### Restricciones Únicas
- `uk_medico_fecha`: (medico_id, fecha_hora)
- `uk_sala_fecha`: (sala_id, fecha_hora)

### Consola H2
Acceder a la consola H2 en: http://localhost:8081/h2-console
- **JDBC URL**: jdbc:h2:mem:citasdb
- **User**: sa
- **Password**: password

## Tolerancia a Fallos

### Circuit Breaker (Resilience4j)

El servicio implementa Circuit Breaker para manejar fallos en microservicios externos:

- **Estado CLOSED**: Operación normal
- **Estado OPEN**: Circuit abierto tras alcanzar umbral de fallos
- **Estado HALF_OPEN**: Intenta recuperar la conexión

### Fallback Methods

- **crearCita**: Fallback que maneja diferentes escenarios:
  - Re-lanza excepciones de negocio (CitaException, DisponibilidadException)
  - Detecta microservicios caídos (MS_PACIENTES_UNAVAILABLE, MS_MEDICOS_UNAVAILABLE, MS_SALAS_UNAVAILABLE)
  - Fallback general para errores desconocidos

### Retry

Configurado para reintentar operaciones fallidas hasta 3 veces con 1 segundo de espera entre intentos.

## Estructura del Proyecto

```
src/main/java/com/microservicios/mscitas/
├── MsCitasApplication.java          # Clase principal
├── client/
│   ├── MedicoClient.java            # Feign Client para ms-medicos
│   ├── PacienteClient.java          # Feign Client para ms-pacientes
│   ├── SalaClient.java              # Feign Client para ms-salas
│   └── NotificationClient.java      # Feign Client para ms-notificaciones
├── controller/
│   └── CitaController.java         # REST API Controller
├── dto/
│   ├── CitaRequest.java             # DTO para crear cita
│   └── CitaResponse.java            # DTO para respuesta de cita
├── exception/
│   ├── CitaException.java           # Excepción de negocio
│   ├── DisponibilidadException.java # Excepción de disponibilidad
│   └── GlobalExceptionHandler.java  # Manejo global de excepciones
├── model/
│   └── Cita.java                    # Entidad JPA
├── repository/
│   └── CitaRepository.java          # Repository JPA
└── service/
    ├── CitaService.java             # Lógica de negocio de citas
    └── DisponibilidadService.java   # Validación de disponibilidad
```
## Manejo de Errores

### Códigos de Error HTTP

- **400 Bad Request**: Errores de validación o de negocio
- **404 Not Found**: Recurso no encontrado
- **503 Service Unavailable**: Microservicios externos no disponibles
- **500 Internal Server Error**: Errores inesperados del servidor

### Tipos de Excepciones

- **CitaException**: Errores de negocio general
- **DisponibilidadException**: Errores de disponibilidad de recursos externos
- **MethodArgumentNotValidException**: Errores de validación de entrada