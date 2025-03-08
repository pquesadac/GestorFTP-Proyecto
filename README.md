# Gestor FTP - Herramienta de Sincronización Segura de Archivos

## Descripción General

Gestor FTP es una aplicación Java que sincroniza automáticamente un directorio local con un servidor FTP remoto. La aplicación monitorea una carpeta local específica para detectar cambios en los archivos (creación, modificación, eliminación) y replica estos cambios en un servidor FTP remoto. Para mayor seguridad, los archivos de texto se cifran automáticamente usando encriptación AES antes de ser subidos al servidor.

## Características

- **Sincronización en tiempo real**: Monitorea una carpeta local y automáticamente sube archivos nuevos o modificados al servidor FTP
- **Seguimiento de eliminación de archivos**: Elimina automáticamente archivos del servidor remoto cuando se eliminan localmente
- **Cifrado AES**: Los archivos de texto plano se cifran automáticamente antes de ser subidos al servidor
- **Procesamiento multihilo**: Utiliza un pool de hilos para manejar eficientemente múltiples operaciones de archivo simultáneamente
- **Herramienta de descifrado**: Incluye una utilidad independiente para descargar y descifrar archivos del servidor FTP

## Requisitos del Sistema

- Java 8 o superior
- Conexión a Internet
- Acceso a un servidor FTP
- Biblioteca Apache Commons Net (para comunicación FTP)

## Estructura del Proyecto

El proyecto está compuesto por varias clases Java que trabajan juntas:

- `Main.java`: Punto de entrada principal para iniciar el servicio de sincronización
- `ClienteFTP.java`: Maneja la conexión y operaciones con el servidor FTP
- `MonitorCarpeta.java`: Monitorea los cambios en la carpeta local y gestiona la sincronización
- `Cifrador.java`: Proporciona funcionalidad de cifrado/descifrado AES
- `Descifrador.java`: Aplicación independiente para descargar y descifrar archivos desde el servidor FTP
- `Subidor.java`: Clasepara subir archivos al servidor FTP

### Proceso de Sincronización

1. Al iniciar, la aplicación registra todos los archivos existentes en la carpeta local
2. Establece un servicio de vigilancia (`WatchService`) para detectar eventos de archivo
3. Cuando se detecta un evento (creación, modificación, eliminación):
   - Se procesa en un hilo separado del pool de hilos
   - Para archivos de texto, se cifra el contenido antes de subir
   - Para otros tipos de archivo, se suben sin cifrar
4. La carpeta remota se mantiene sincronizada con la carpeta local

### Seguridad

El cifrado AES se aplica a archivos con las siguientes extensiones:
- .txt, .html, .xml, .json, .csv
- .log, .md, .java, .py, .js, .css

La implementación utiliza:
- Algoritmo AES con modo ECB y padding PKCS5
- Clave derivada mediante SHA-1
- Codificación Base64 para el contenido cifrado

## Limitaciones

- El renombrado de archivos se maneja como una eliminación seguida de una creación
- No sincroniza estructura de subcarpetas (solo sincroniza archivos)
- Utiliza modo ECB para AES, que no es el más seguro para todos los casos de uso

## Futuras Mejoras

- Soporte para sincronización bidireccional
- Sincronización de estructura de carpetas
- Mejora del algoritmo de cifrado (cambiar a AES-GCM)
- Interfaz gráfica de usuario
- Opción para sincronización programada en lugar de tiempo real
