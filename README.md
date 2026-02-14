# CineVerse: Plataforma Integral de Gestión Cinematográfica

![CineVerse Banner](https://img.shields.io/badge/Stack-Full--Stack-blue)
![Architecture](https://img.shields.io/badge/Architecture-Clean--Architecture-green)
![Deployment](https://img.shields.io/badge/Deployment-AWS--Docker--Nginx-orange)

**CineVerse** es un ecosistema digital diseñado para modernizar la experiencia cinematográfica. La plataforma permite la gestión de carteleras, reserva de asientos en tiempo real mediante WebSockets y acceso a tickets en modo offline a través de una aplicación móvil nativa. Todo el sistema está desplegado en la nube (AWS) utilizando una arquitectura de contenedores con Docker y un proxy inverso optimizado con Nginx[cite: 148, 150].

## 🚀 Características Principales

* **App Móvil Nativa:** Desarrollada en Kotlin con Jetpack Compose y arquitectura MVVM.
* **Gestión de Reservas:** Sistema que evita la sobreventa de butacas mediante transacciones atómicas de JPA en el backend.
* **Soporte en Tiempo Real:** Chat bidireccional mediante el protocolo STOMP sobre WebSockets para atención al cliente inmediata.
* **Modo Offline:** Sincronización de tickets locales con Room (SQLite) para visualización de QRs sin conexión a internet.
* **Infraestructura en la Nube:** Despliegue en instancias de AWS EC2 para acceso global.

---

## 🛠️ Stack Tecnológico

### Backend

* **Lenguaje:** Java 17[cite: 117].
* **Framework:** Spring Boot 3.x (Clean Architecture).
* **Seguridad:** Spring Security + JWT (JSON Web Tokens) para arquitectura stateless.
* **Base de Datos:** MySQL 8.0 (Persistencia centralizada).
* **Integración:** Cliente de red interno para consumo de SWAPI (Star Wars API).
* **Chat en tiempo real:** Uso de Websockets para el chat de soporte en tiempo real.
* **IDE de desarrollo utilizado:** IntelliJ IDEA

### Frontend (Web Admin)

* **Framework:** React.
* **Estilos:** Tailwind CSS (Diseño responsive).
* **IDE de desarrollo utilizado:** Visual Studio Code

### Mobile

* **Lenguaje:** Kotlin.
* **UI:** Jetpack Compose (Interfaz declarativa).
* **Red:** Retrofit 2 + OkHttp + Coil (con interceptores de User-Agent).
* **Local DB:** Room Database.
* **IDE de desarrollo:** Android Studio

---

## 🔧 Compilación de todos los elementos y preparación del despliegue

### 1. Compilación del Backend (Spring Boot)

Requiere JDK 17 y Maven instalado.

```bash
# Acceder a la carpeta del backend
cd backend
# Compilar y generar el archivo JAR omitiendo los tests
./mvnw clean package -DskipTests
# El archivo resultante estará en `/target/api-0.0.1-SNAPSHOT.jar`
```

### 2. Compilación del Frontend (Web)

Requiere Node.js y npm.

```bash
# Acceder a la carpeta del frontend
cd frontend
# Instalar npm, tailwind y websocket
npm install
npm install -D tailwindcss postcss autoprefixer
npm install stompjs sockjs-client
# Compilar y generar el frontend completo
npm run build
```

La carpeta dist/ contendrá los archivos estáticos listos para ser servidos por Nginx.

### 3. App Móvil (Android Studio)

Abrir la carpeta mobile/ con Android Studio Ladybug o superior.
Sincronizar Gradle.
Configurar la BASE_URL con la IP de tu servidor AWS.
Generar el APK: Build > Build Bundle(s) / APK(s) > Build APK(s).

### 4. Dockerización del Sistema y ficheros de configuración

#### 4.1. Ficheros Dockerfile

Como vamos a desplegar el sistema en Docker, generaremos dos ficheros Dockerfile (uno para el Backend y otro para el Frontend) que usaremos más tarde

Dockerfile para el Backend (Dockerfile.backend)

```bash
FROM eclipse-temurin:17-jdk-alpine
COPY cineverse-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Dockerfile para el Frontend (Dockerfile.frontend)

```bash
FROM nginx:alpine
# Copiamos la carpeta dist y el config personalizado
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 4.2. Configuración Servicio Web

Como durante el despliegue levantaremos un servidor nginx en el contenedor, también necesitaremos el fichero de configuración de este servcios
Fichero de configuración de nginx (nginx.conf)

```bash
server {
    listen 80;
    server_name localhost;

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 1. BACKEND API
    location /api/ {
        proxy_pass http://cineverse-backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 2. CHAT / WEBSOCKETS 
    location ^~ /ws {
        proxy_pass http://cineverse-backend:8080; 
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_buffering off;
    }

    # 3. FRONTEND 
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
```

#### 4.3. Fichero de orquestación para Docker:

Para que docker sepa que contenedores crear, también prepararemos el fichero docker-compose.yml

```bash
version: '3.8'

services:
  db:
    image: mysql:8.0
    container_name: cineverse-db
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: admin
      MYSQL_DATABASE: cineverse
    ports:
      - "3306:3306"
    networks:
      - cineverse-network
    healthcheck:
      test: ["CMD", "mysqladmin" ,"ping", "-h", "localhost", "-uroot", "-padmin"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile.backend
    container_name: cineverse-backend
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/cineverse
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=admin
      - JAVA_OPTS=-Xmx512m -Xms256m
    networks:
      - cineverse-network
    # Límite de memoria para evitar cuelgues en t3.micro
    deploy:
      resources:
        limits:
          memory: 768M

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    container_name: cineverse-frontend
    restart: always
    ports:
      - "80:80"
    volumes:
      - ./dist:/usr/share/nginx/html:ro
      - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
    networks:
      - cineverse-network
    depends_on:
      - backend
    deploy:
      resources:
        limits:
          memory: 256M

networks:
  cineverse-network:
    driver: bridge
```

### 5. Preparación de carpeta para el despliegue

Prepararemos una carpeta con todos los ficheros generados (compillación y ficheros de configuración) lista para subir al servidor de producción. La carpeta Despliegue_Cineverse de este repositorio alberga los ficheros resultantes de la compilación junto a los ficheros de configuración, lista para usarse cono fuente del despliegue que se detalla a continuación.

## 🚢 Despliegue en Producción (AWS + Docker + Nginx)

El despliegue se basa en la orquestación de contenedores para garantizar que el entorno de producción sea idéntico al de desarrollo.

### 1: Preparación del Servidor AWS

Lanzar una instancia Ubuntu Server en AWS EC2.

Configurar el Security Group permitiendo los puertos 80 (HTTP), 8080 (API) y 22 (SSH).

Realizar una conexión por SSH a la instancia EC2 y instalar Docker y Docker Compose:

```bash
sudo apt install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

Cierra la conexión SSH y vuelve a entrar para aplicar permisos

### 2: Subida a producción de la carpeta de despliegue

Subir la carpeta de despliegue con el comando scp. A modo de ejemolo (indica tu fichero de claves y la IP pública de la instancia EC2):

```bash
scp -i "tu-llave.pem" -r ./DESPLIEGUE_CINEVERSE ubuntu@DIRECCION-IP-AWS:/home/ubuntu/
```

Realizamos una conexión SSH a la instancia Ec2, entramos en la carpeta creada y lanzamos Docker:

```bash
cd DESPLIEGUE_CINEVERSE
sudo docker-compose up --build -d
```

### 3: Fuerza el inicio de la base de datos

Accede a IP pública de la instancia EC2 con un navegador para que se inicie el backend por primera vez y genere la base de datos MySQL

![](images/login.png)

### 4: Crea un usuario de administración para poder enpezar a usar la aplicación (crear usuarios, sesiones, ...)

Realizamos una conexión SSH a la instancia Ec2 y nos conectamos a MySQL para crear un usuario con permisos de Asministración:

```bash
# Nos conectamos al servicio MySQL en docker
sudo docker exec -it cineverse-db mysql -u root -p admin cineverse
# Lanzamos la SQL para insertar un usuario con permisos de administrador, la contraseña debe cifrase con BCrypt
INSERT INTO users (username, password, email, role) 
VALUES ('admin', '$2a$10$HB.i844KLiZ.CUnktHSN8uhRSn//ECz7WxHMLIiqILJnoKOc3GHuG', 'admin@cineverse.com', 'ADMIN');
```

### 5: Primer inicio de sesión con el usuario creado

Nos conectamos con el navegador a IP Pública de la instancia EC2 y ya podremos hacer login para acceder al frontend (usuario: el email de la cuenta).

![](images/login_Admin.png)

## 🚢 Funcionalidades de la aplicación web (Frontend + Backend)

A continuación se describen las funcionalidades de la aplicación web, partiendo de la existencia de 3 tipos de perfiles o roles: Administrador, Empleado y Cliente. Veamos las funcionalidades separadas por cada perfil:

### 1: Perfil Administrador

#### 1.1: Menús/Pestañas de la página principal

**Pestaña Películas**

En esta pestaña el administrador puede administrar las películas e importar películas con SWAPI
![](images/adminPeliculas.png)

**Pestaña Usuarios**

En esta pestaña el administrador puede administrar los usuarios del sistema
![](images/adminUsuarios.png)

**Pestaña Salas**

En esta pestaña el administrador puede administrar las salas del cine
![](images/adminSalas.png)

**Pestaña Sesiones**

En esta pestaña el administrador puede administrar/programar las sesiones del cine
![](images/adminSesiones.png)

#### 2: Funcionalidad de Crear/Modificar/Eliminar películas

**Ejemplo de creación de película:**

* Paso 1: Pulsamos el botón
  ![](images/adminNuevaPelicula1.png)
* Paso 2: Completamos los datos de la película (para la imagen indicamos una URL válida)
  ![](images/adminNuevaPelicula2.png)
* Paso 3: Pulsamos Aceptar y observamos que ta se ha creado la película
  ![](images/adminNuevaPelicula3.png)

**Ejemplo de eliminación de película:**

Si pulsamos en el botón rojo Borrar se mostrará un mensaje emergente solciitando confirmación:

![](images/adminBorrarPelicula.png)

#### 3: Funcionalidad de Importación de películas con Swapi

* Paso 1: Pulsamos el botón
  ![](images/adminImportarPeliculas1.png)
* Paso 2: Se realiza la importaciín y se confirma con un mensaje emergente
  ![](images/adminImportarPeliculas2.png)

#### 4: Funcionalidad de Crear/Modificar/Eliminar usuarios

**Ejemplo de creación de usuario:**

* Paso 1: Pulsamos el botón:
  ![](images/adminNuevoUsuario1.png)
* Paso 2: Rellenamos los datos de usuario teniendo en cuenta que podemos elegir el perfil/rol (Admin/Empleado/Cliente):
  ![](images/adminNuevoUsuario2.png)
* Paso 3: Observamos que el usuario se ha creado (en este caso con rol empleado) y ya podríamos hacer login con el:
  ![](images/adminNuevoUsuario3.png)

#### 5: Funcionalidad de Crear/Modificar/Eliminar salas

**Ejemplo de creación de sala:**

* Paso 1: Pulsamos el botón:
  ![](images/adminNuevaSala1.png)
* Paso 2: Rellenamos los datos de la sala, indicando su tamaño por filas y columnas:
  ![](images/adminNuevaSala2.png)
* Paso 3: Observamos que la sala se ha creado y ya podríamos usarla para programar sesiones:
  ![](images/adminNuevaSala3.png)

**Ejemplo de eliminación de sala:**

Si pulsamos en el botón rojo Borrar de sala que acabamos de crear se mostrará un mensaje emergente solciitando confirmación:

![](images/adminBorrarSala1.png)

A continuación podemos comprobar que la sala se ha eliminado:

![](images/adminBorrarSala2.png)

#### 6: Crear/Modificar/Eliminar sesiones

**Ejemplo de creación de sesión:**

* Paso 1: Pulsamos el botón:
  ![](images/adminNuevaSesion1.png)
* Paso 2: Rellenamos los datos de la sesión, indicando la película, la sala, la fecha y la hora, y el precio de la entrada:
  ![](images/adminNuevaSesion2.png)
* Paso 3: Observamos que la sesión se ha creado y ya podríamos vender entradas de la misma:
  ![](images/adminNuevaSala3.png)

**Ejemplo de conflicto durante la creación de sesión:**

Si intentamos crear otra sesión que ocupe una sala ya ocupada, aunque sea a mitad de la proyección de la película:
![](images/adminNuevaSesionConflicto1.png)

Se mostrará un mensaje indicando que la sala ya está ocupada:
![](images/adminNuevaSesionConflicto2.png)

### 2: Perfil Empleado

#### 1: Menús/Pestañas de la página principal

**Taquílla**

![](images/empleadoTaquilla1.png)

Y si entramos al detalle de una sesión podemos ver el tamaño de la sala y las butacas ocupadas, además de vender entradas:

![](images/empleadoTaquilla2.png)

**Validación de entradas**

![](images/empleadoValidar1.png)

Y si entramos en una sesión podemos ver las entradas vendidas y validarlas para controlar el acceso a la sala:

![](images/empleadoValidar2.png)

**Chat de soporte**

![](images/empleadoChat.png)

#### 2: Venta de entradas

**Ejemplo de venta de entradas:**

* Paso 1: Pulsamos en la sesión de la que queremos vender entradas:
  ![](images/empleadoVentaEntradas1.png)
* Paso 2: Seleccionamos los asientos y pulsamos el botón Cobrar y Entregar:
  ![](images/empleadoVentaEntradas2.png)
* Paso 3: Observamos que ahora los asientos están ocupados y las entradas listas para validar:
  ![](images/empleadoVentaEntradas3.png)

  ![](images/empleadoVentaEntradas4.png)
* Paso 4: Imprime el ticket con QR que le validarán a la entrada de la sesión:

  ![](images/empleadoTicket.png)

#### 3: Validación de acceso a la sesión

**Ejemplo de la validación (control de acceso) de las entradas seleccionadas anteriormente**

El empleado valida las entradas que le muestra el usuario pulsando en el botón validar de cada una de ellas:

![](images/empleadoVentaEntradas4.png)

![](images/empleadoValidarEntradas1.png)

#### 4: Chat Soporte

El empleado accede a la pestaña de Chat esperando mensajes:

![](images/empleadoChat.png)

Varios clientes contactan con el soporte enviando un mensaje:

![](images/empleadoChat1.png)

![](images/empleadoChat2.png)

![](images/empleadoChat3.png)

El empleado puede responder a cada cliente de forma individual:

![](images/empleadoChat4.png)

![](images/empleadoChat5.png)

Y los clientes pueden recibir sus mensajes:

![](images/empleadoChat6.png)

![](images/empleadoChat7.png)

### 3: Perfil Cliente

#### 1: Menús/Pestañas de la página principal

**Cartelera y compra**

Puede visualizar las películas en cartelera y las entradas compradas:

![](images/clienteCartelera1.png)

Pulsando sobre el botón descargar de cada entrada puede descargarla con un código QR incluido:

![](images/clienteCartelera2.png)

**Chat Soporte**

![](images/clienteChat.png)

#### 2: Compra de entradas

**Ejemplo de compra de entradas:**

* Paso 1: Pulsamos en la sesión de la que queremos comprar entradas:
  ![](images/clienteCompra1.png)
* Paso 2: Seleccionamos los asientos y pulsamos el botón Finalizar compra:
  ![](images/clienteCompra2.png)
* Paso 3: Observamos que ahora que ya disponemos de las entradas y podemos decargarlas:
  ![](images/clienteCompra3.png)

  Si otro cliente quiere comprar entradas para la misma sesión podrá comprobar que hay butacas ocupadas

  ![](images/clienteCompra4.png)

#### 3: Chat Soporte

Ver ejemplo del chat desde el perfil del empleado.

## 🚢 Funcionalidades de la aplicación movil

### 1: Identificación

Cuando la aplicación movil inicia comprueba si puede acceder al servicio de backend, si no es posible se muestra un mensaje emergente que solicita la IP del servicio:

![](images/movilInicio.png)

A continuación solicita la identificación de usuario:

![](images/moviLogin1.png)

### 2: Registro de usuario

Si no tenemos cuenta, podemos registranos en la aplicación pulsando sobre la leyenda **"¿No tienes cuenta? Crea una"** de la pantalla de login:

![](images/movilRegistro.png)

Una vez completado el registro nos redirecciona a la pantalla de login con las credenciales el usuario registrado:

![](images/movilRegistro2.png)

### 3: Compra de entradas

Después de identificarnos en la aplicación podremos seleccionar una sesión y comprar entradas:

![](images/movilPeliculas.png)

Pulsamos sobre una de las sesiones y podremos seleccionar las butacas libres:

![](images/movilCompra1.png)

Seleccionamos las entradas y las compramos:

![](images/movilCompra2.png)

Pulsamos el botón reservar y directamente nos mostrará las entradas compradas con el QR listo para validar:

![](images/movilCompra3.png)

### 4: Entradas compradas

Pulsando sobre el icono de la derecha el usuario puede ver sus entradas:

![](images/movilmenuEntradas.png)

![](images/movilCompra3.png)

### 5: Chat Soporte

Pulsando sobre el icono del sobre el usuario puede ver sus entradas:

![](images/movilmenuChat.png)

![](images/movilChat1.png)

El usuario puede enviar un mensaje a soporte:

![](images/movilChat2.png)

![](images/movilChat3.png)

El empleado conectado puede verlo y responderlo:

![](images/movilChat4.png)

![](images/movilChat5.png)

### 5: Chat Soporte

Pulsando sobre el icono de la parte izquierda el usuario puede ver sus entradas:

![](images/movilLogout.png)
