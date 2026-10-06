

# SECCIÓN DE GESTIÓN DE PROYECTO Y CALIDAD (MÉXICO - COIL)

## 1. Tablero de Actividades y Seguimiento (Kanban)
El proyecto se gestiona mediante el marco de trabajo **Scrum/Kanban** integrado directamente en el repositorio de GitHub.

* **URL del Tablero de Trabajo:** [https://github.com/users/SofiaHerrera27/projects/3/views/1]
* **Criterio de Terminado (Definition of Done - DoD):** Una Historia de Usuario se considera completamente **Done (Hecha)** cuando:
  1. El código cumple con los criterios de aceptación (Dado/Cuando/Entonces).
  2. Ha sido probado por el equipo de Análisis (México) sin presentar errores de ejecución.
  3. El Pull Request (PR) ha sido revisado y fusionado a la rama principal `main`.

### Resumen del Product Backlog y Estimación
| ID | Historia de Usuario | Story Points (SP) | Prioridad | Asignación Principales | Estado |
| :--- | :--- | :---: | :---: | :--- | :--- |
| HU-01 | Bienvenida con Drako | 2 SP | Alta 🔴 | Devs Colombia / UI | Done / In Progress |
| HU-02a| Acertijo Caza de Colores | 3 SP | Alta 🔴 | Devs Colombia / Logic | In Progress |
| HU-02b| Detección de Color (CameraX) | 5 SP | Alta 🔴 | Devs Colombia / Camera | In Progress |
| HU-03 | Álbum de Personajes | 5 SP | Alta 🔴 | Devs Colombia / Data | Ready / Backlog |
| HU-04 | Sistema de ParquePuntos | 5 SP | Alta 🔴 | Devs Colombia / State | Ready / Backlog |
| HU-06 | Trivia de Cuidado Ambiental | 3 SP | Alta 🔴 | Devs Colombia / QA México| Backlog |
| HU-05 | Canje Simulado de Premios | 3 SP | Media 🟡 | Devs Colombia / QA México| Backlog |
| HU-08 | Tutorial Corto de Cámara | 2 SP | Media 🟡 | Devs Colombia / QA México| Backlog |
| HU-07 | FAQ y Soporte con Drako | 3 SP | Baja 🟢 | Devs Colombia / QA México| Backlog |
| **Total**| **Esfuerzo Estimado Total** | **31 SP** | | | |

---

## 2. Marco de Gobernanza y Comunicación Binacional

### Matriz de Asignación de Responsabilidades (Matriz RACI)
* **R (Responsible):** Encargado de realizar la tarea (Equipo de Desarrollo - Colombia).
* **A (Accountable):** Aprueba la tarea y asegura la calidad (Equipo de Métodos/Análisis - México).
* **C (Consulted):** Aporta información técnica o de diseño (Ambos equipos).
* **I (Informed):** Recibe los reportes de estado y avances (Docentes y coordinadores).

| Actividad / Entregable | Equipo México (UCaribe) | Equipo Colombia (JDC) |
| :--- | :---: | :---: |
| Definición del Backlog y Criterios de Aceptación | **A / R** | C |
| Diseño de Wireframes y UX/UI (Figma) | C | **R** |
| Programación y Arquitectura MVVM (Android Studio) | I | **A / R** |
| Gestión del Tablero Kanban y Sprints | **A / R** | I |
| Pruebas de Software (QA) y Aprobación de Releases | **A / R** | C |

---

## 3. Plan de Aseguramiento de Calidad (Quality Assurance - QA)

Para validar cada entrega enviada por el equipo de desarrollo, se aplicará el siguiente Plan de Pruebas basado en los criterios de aceptación:

### Casos de Prueba (Test Cases)

#### CP-01: Validación de Bienvenida y Tiempo de Carga (HU-01)
* **Precondición:** Instalación limpia de la aplicación en el dispositivo/emulador.
* **Pasos de Ejecución:**
  1. Abrir la aplicación `ParqueVivo`.
  2. Observar la pantalla principal y el personaje Drako.
* **Resultado Esperado:** Se muestra el mensaje de bienvenida y el botón para continuar en un tiempo menor a 2 segundos.
* **Estado:** [ ] Aprobado  [ ] Rechazado  [ ] Pendiente de prueba

#### CP-02: Flujo de Acertijo y Selección de Color (HU-02a)
* **Precondición:** Usuario inicia el módulo "Caza de Colores".
* **Pasos de Ejecución:**
  1. Iniciar la actividad desde el menú.
  2. Leer el acertijo mostrado por Drako.
  3. Seleccionar una de las 3 opciones de color disponibles.
* **Resultado Esperado:** La app procesa la opción elegida y habilita el acceso a la cámara.
* **Estado:** [ ] Aprobado  [ ] Rechazado  [ ] Pendiente de prueba

#### CP-03: Detección Continua por Cámara (HU-02b)
* **Precondición:** Permiso de cámara concedido y color seleccionado previamente.
* **Pasos de Ejecución:**
  1. Apuntar la cámara a un objeto del color solicitado durante 2 segundos continuos.
* **Resultado Esperado:** La app detecta el color, desbloquea la criatura en el Álbum y otorga los ParquePuntos correspondientes en la interfaz.
* **Estado:** [ ] Aprobado  [ ] Rechazado  [ ] Pendiente de prueba
