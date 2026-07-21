# ══════════════════════════════════════════════════════════════════════════════
# STAGE 1 — base
# Sistema base + Java 17 + variables de entorno
# ══════════════════════════════════════════════════════════════════════════════
FROM fedora:latest AS base

ARG USERNAME=developer
ARG UID=1000
ARG GID=1000

# Versiones objetivo (extraídas de tu pom.xml)
ENV MAVEN_VERSION=3.9.16
ENV SCALA_VERSION=2.13.18
ENV SPARK_VERSION=4.1.3
ENV HADOOP_VERSION=3

# Instalamos utilidades básicas y Java 17
RUN dnf upgrade -y && \
    dnf install -y \
        wget \
        tar \
        gzip \
        findutils \
        procps-ng \
        zlib && \
    dnf clean all && \
    rm -rf /var/cache/dnf*

# Usuario no-root con UID/GID del host
RUN groupadd --gid "$GID" "$USERNAME" && \
    useradd  --uid "$UID" --gid "$GID" -m -s /bin/bash "$USERNAME"

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 2 — dev-tools
# Descarga e instalación manual de Java, Maven, Scala y Spark desde URLs
# ══════════════════════════════════════════════════════════════════════════════
FROM base AS dev-tools

WORKDIR /opt

# 1. Instalar Java
RUN wget -q "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.11%2B10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_10.tar.gz" && \
    tar -xzf "OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_10.tar.gz" && \
    ln -s "jdk-21.0.11+10" java && \
    rm "OpenJDK21U-jdk_x64_linux_hotspot_21.0.11_10.tar.gz"

# 2. Instalar Maven
RUN wget -q "https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    tar -xzf "apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
    ln -s "apache-maven-${MAVEN_VERSION}" maven && \
    rm "apache-maven-${MAVEN_VERSION}-bin.tar.gz"

# 3. Instalar Scala (binarios directos)
RUN wget -q "https://github.com/scala/scala/releases/download/v${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar -xzf "scala-${SCALA_VERSION}.tgz" && \
    ln -s "scala-${SCALA_VERSION}" scala && \
    rm "scala-${SCALA_VERSION}.tgz"

# 4. Instalar Spark (pre-construido para Hadoop)
RUN wget -q "https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" && \
    tar -xzf "spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" && \
    ln -s "spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}" spark && \
    rm "spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz"

# Ajustar permisos para el usuario developer
RUN chown -R root:root /opt/jdk-21.0.11+10 /opt/apache-maven-${MAVEN_VERSION} /opt/scala-${SCALA_VERSION} /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} && \
    chmod -R 755 /opt/jdk-21.0.11+10 /opt/apache-maven-${MAVEN_VERSION} /opt/scala-${SCALA_VERSION} /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}

# Variables de entorno para las herramientas
# Variables de entorno para TODAS las herramientas
ENV JAVA_HOME=/opt/java
ENV MAVEN_HOME=/opt/maven
ENV SCALA_HOME=/opt/scala
ENV SPARK_HOME=/opt/spark
ENV PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$SCALA_HOME/bin:$SPARK_HOME/bin:$PATH"

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 3 — development
# Entorno final para desarrollar (Neovim, NodeJS, etc.)
# ══════════════════════════════════════════════════════════════════════════════
FROM dev-tools AS development

# Herramientas exclusivas del entorno de edición
RUN dnf install -y \
        neovim \
        man-db \
        scdoc \
        git \
        nodejs \
        npm && \
    dnf clean all && \
    rm -rf /var/cache/dnf*

ENV EDITOR=nvim

# Compilar el .scd para Neovim
COPY --chown=root:root .config/man/nvim_cheat.1.scd /tmp/nvim_cheat.scd
RUN mkdir -p /usr/local/share/man/man1 && \
    scdoc < /tmp/nvim_cheat.scd > /usr/local/share/man/man1/nvim_cheat.1 && \
    rm /tmp/nvim_cheat.scd && \
    mandb --quiet

WORKDIR /home/$USERNAME/workspace
USER $USERNAME

# Añadir excepción de seguridad de Git para el volumen montado
RUN git config --global --add safe.directory /home/developer/workspace

CMD ["/bin/bash"]

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 4 — builder
# Exclusivo para compilar el código antes de mandarlo a la imagen de producción
# ══════════════════════════════════════════════════════════════════════════════
FROM dev-tools AS builder
WORKDIR /home/$USERNAME/workspace
COPY --chown=$USERNAME:$USERNAME pom.xml ./
RUN mvn -B dependency:go-offline || true

COPY --chown=$USERNAME:$USERNAME src/ ./src/
RUN mvn -B clean package -DskipTests

# ══════════════════════════════════════════════════════════════════════════════
# STAGE 5 — production
# Imagen final ligera: solo JRE + Spark (si es necesario para correr la app) + JAR
# ══════════════════════════════════════════════════════════════════════════════
FROM docker.io/library/eclipse-temurin:21-jre-jammy AS production

ARG USERNAME=developer
ARG UID=1000
ARG GID=1000

ENV SPARK_VERSION=4.1.3
ENV HADOOP_VERSION=3
ENV SPARK_HOME=/opt/spark
ENV PATH="$SPARK_HOME/bin:$PATH"

RUN groupadd --gid "$GID" "$USERNAME" && \
    useradd  --uid "$UID" --gid "$GID" -m -s /bin/bash "$USERNAME" && \
    apt-get update && \
    apt-get install -y --no-install-recommends wget tar procps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Descargar Spark en prod (necesario si vas a usar spark-submit)
RUN wget -q "https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz" -O /tmp/spark.tgz && \
    tar -xzf /tmp/spark.tgz -C /opt && \
    ln -s /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark && \
    rm /tmp/spark.tgz

WORKDIR /home/$USERNAME/workspace
USER $USERNAME

COPY --from=builder --chown=$USERNAME:$USERNAME /home/$USERNAME/workspace/target/Scala_maven_velespro-1.0-SNAPSHOT.jar ./app.jar

# Si usas spark-submit:
# ENTRYPOINT ["spark-submit", "--class", "VelESPro.App", "app.jar"]
# Si es una app standalone (el jar incluye todo):
ENTRYPOINT ["java", "-jar", "app.jar"]
