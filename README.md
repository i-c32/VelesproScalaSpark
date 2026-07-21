# VelESPro - Hartree-Fock Scala App

![Scala](https://img.shields.io/badge/Scala-2.13.18-red)
![Java](https://img.shields.io/badge/Java-21-blue)
![Apache Spark](https://img.shields.io/badge/Spark-4.1.2-orange)
![Maven](https://img.shields.io/badge/Maven-3.9.16-C71A22)
![Podman](https://img.shields.io/badge/Podman-Ready-892CA0)

Aplicación para el cálculo de **Hartree-Fock** implementada en **Scala** y **Apache Spark**. Este repositorio está diseñado con una arquitectura de contenedores de múltiples etapas (multi-stage) optimizada tanto para una experiencia de desarrollo fluida (con hot-reloading y Neovim integrado) como para un despliegue de producción ligero y seguro.

---

## 🛠 Stack Tecnológico

*   **Lenguaje:** Scala 2.13.18 / Java 21 (Eclipse Temurin)
*   **Procesamiento Distribuido:** Apache Spark 4.1.2 (Core, SQL, MLlib)
*   **Gestor de Dependencias:** Maven 3.9.16
*   **Testing:** ScalaTest, Scoverage, Mockito
*   **Análisis de Código:** Integración con SonarCloud
*   **Contenedorización:** Podman (Fedora para desarrollo, Ubuntu Jammy JRE para producción)

---

## 🏗 Arquitectura de Contenedores

El proyecto utiliza `compose.yaml` para orquestar dos entornos distintos:

### 1. Entorno de Desarrollo (`dev`)
Un contenedor basado en **Fedora** que incluye todas las herramientas necesarias sin ensuciar la máquina host:
*   Preinstalado con **Neovim**, Git, NodeJS, Java, Maven, Scala y Spark.
*   **Persistencia (Volúmenes):** Sincroniza el código fuente (`src/`), las configuraciones de Neovim (`.config/nvim`), el historial de bash y la caché de dependencias de Maven (`.data/.m2`) directamente con el host.
*   **Hot-reload:** Los cambios en tu IDE/editor local o dentro del contenedor se reflejan de inmediato gracias a los *bind mounts*.

### 2. Entorno de Producción (`prod`)
Una imagen final minimalista optimizada para despliegue:
*   Basada en `eclipse-temurin:21-jre-jammy` (Solo el Runtime de Java).
*   Incluye los binarios de Spark y el archivo `.jar` compilado de la aplicación (Fat JAR / Uber JAR).
*   Se elimina cualquier herramienta de construcción para reducir drásticamente el tamaño y los vectores de ataque.

---

## 🚀 Empezando (Getting Started)

### Prerrequisitos
*   [Podman](https://podman.io/) y `podman-compose` (o Docker y docker-compose equivalentes).

### Levantar el entorno de Desarrollo

1. Construye e inicia el contenedor de desarrollo en segundo plano:
   ```bash
   podman-compose up --build -d dev
   ```

2. Accede a la terminal interactiva del contenedor:
   ```bash
   podman exec -it velespro_scala_dev /bin/bash
   ```
   *(Verás el prompt personalizado de `Velespro-Scala-dev`)*

3. Dentro del contenedor, compila el proyecto y descarga las dependencias (se guardarán en el volumen persistente `.data/.m2`):
   ```bash
   mvn -B clean compile
   ```

### Construir la imagen de Producción

El propio proceso de construcción (STAGE 4: `builder`) se encarga de compilar y empaquetar el código (`mvn clean package`) de forma aislada antes de inyectar el JAR en la imagen final.

```bash
podman-compose build prod
podman-compose up -d prod
```

---

## 🔧 Comandos Útiles de Maven

Ejecutar desde dentro del contenedor `dev` (usando `-B` para modo Batch y evitar logs excesivos):

*   **Compilar el proyecto:** `mvn -B compile`
*   **Ejecutar pruebas unitarias (ScalaTest):** `mvn -B test`
*   **Generar reporte de cobertura (Scoverage):** `mvn -B scoverage:report`
*   **Empaquetar la aplicación (Fat JAR):** `mvn -B clean package -DskipTests`

---

## 👨‍💻 Autor

*   **Iván González Veloso** (ic-32) - *Architect & Developer* - 

