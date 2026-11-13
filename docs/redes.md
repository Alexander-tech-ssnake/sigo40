# SIGO 4.0 – Documentación de redes
 
Parte del Integrante A (Analista de redes). Cubre el plan de direccionamiento, el enrutamiento entre sedes y el análisis de la captura de tráfico en Wireshark.
 
## 1. Plan de direccionamiento
 
Una subred privada /24 por sede. El servidor de aplicaciones está en San Isidro (`192.168.10.10`).
 
| Sede | Ubicación | Subred | Gateway |
|---|---|---|---|
| Oficina central | San Isidro | 192.168.10.0/24 | 192.168.10.1 |
| Almacén central | Lurín | 192.168.20.0/24 | 192.168.20.1 |
| Obra Residencial Los Pinos | Chorrillos | 192.168.30.0/24 | 192.168.30.1 |
| Obra Condominio Vista Este | Ate | 192.168.40.0/24 | 192.168.40.1 |
| Obra Conjunto Carabayllo | Carabayllo | 192.168.50.0/24 | 192.168.50.1 |
 
Máscara de todas las subredes: `255.255.255.0`.
 
### Enlaces entre routers (punto a punto /30)
 
Cada enlace entre el router central y el de una sede usa una subred /30 (2 equipos útiles), por VPN sobre fibra (Lurín) o 4G (obras).
 
| Enlace | Subred | Router central | Router de la sede |
|---|---|---|---|
| Central – Lurín | 10.0.0.0/30 | 10.0.0.1 | 10.0.0.2 |
| Central – Chorrillos | 10.0.0.4/30 | 10.0.0.5 | 10.0.0.6 |
| Central – Ate | 10.0.0.8/30 | 10.0.0.9 | 10.0.0.10 |
| Central – Carabayllo | 10.0.0.12/30 | 10.0.0.13 | 10.0.0.14 |
 
## 2. Tabla de enrutamiento del router central (San Isidro)
 
| Destino | Máscara | Siguiente salto | Interfaz |
|---|---|---|---|
| 192.168.10.0 | 255.255.255.0 | directa | LAN |
| 192.168.20.0 | 255.255.255.0 | 10.0.0.2 | WAN1 |
| 192.168.30.0 | 255.255.255.0 | 10.0.0.6 | WAN2 |
| 192.168.40.0 | 255.255.255.0 | 10.0.0.10 | WAN3 |
| 192.168.50.0 | 255.255.255.0 | 10.0.0.14 | WAN4 |
 
Rutas estáticas: la red es pequeña (5 sedes) y no cambia seguido.
 
## 3. Captura de tráfico en Wireshark
 
### Entorno de laboratorio
 
Se simuló el enlace *obra → servidor central* con dos equipos reales:
 
| Rol | Equipo | IP | MAC |
|---|---|---|---|
| Servidor central (`ServidorCentral`, TCP 8080) | WSL (Ubuntu) | 172.22.124.50 | 00:15:5d:3e:e0:82 |
| Cliente de obra (`ClienteObra`) | Windows | 172.22.112.1 | 00:15:5d:52:21:c2 |
 
Las IP del laboratorio (172.22.x.x) son de prueba y distintas de las del plan de direccionamiento (192.168.x.x). Ambos equipos están en la misma subred virtual de WSL.
 
- Interfaz capturada: `vEthernet (WSL)` en Windows.
- Filtro de visualización: `tcp.port == 8080`.
- Archivo de captura: `captura.pcapng`.
### Resumen de la conversación
 
| Paquetes | Qué ocurre |
|---|---|
| 37 – 39 | Saludo de tres vías: `SYN`, `SYN, ACK`, `ACK` |
| 40 – 42 | `ENVIAR_PARTE` (115 bytes) y respuesta `OK\|PARTE-0001\|…` (41 bytes) |
| 43 – 44 | `PEDIR_MATERIAL` (117 bytes) y su respuesta `OK` |
| 45 – 46 | Parte inválido y respuesta `ERROR\|parte_invalido` (21 bytes) |
| 47 – 48 | `SALIR` (7 bytes) y `OK\|ADIOS` (9 bytes) |
| 49 – 51 | Cierre de la conexión con `FIN` |
 
### Análisis por capas del paquete 40 (`ENVIAR_PARTE`)
 
**Capa de enlace – Ethernet II**
 
| Campo | Valor |
|---|---|
| MAC de origen | 00:15:5d:52:21:c2 (adaptador vEthernet de Windows) |
| MAC de destino | 00:15:5d:3e:e0:82 (WSL) |
| Tipo | IPv4 (0x0800) |
 
Como ambos equipos están en la misma subred, la MAC de destino es la del servidor. Si estuvieran en subredes distintas, sería la MAC del gateway.
 
**Capa de red – IPv4**
 
| Campo | Valor |
|---|---|
| Versión / longitud de cabecera | 4 / 20 bytes |
| Longitud total | 155 bytes (20 IP + 20 TCP + 115 de datos) |
| Identificación | 0x900c |
| Flags | Don't fragment |
| TTL | 128 |
| Protocolo | 6 (TCP) |
| Checksum de cabecera | 0x0000 (no verificado) |
| IP de origen | 172.22.112.1 |
| IP de destino | 172.22.124.50 |
 
**Capa de transporte – TCP**
 
| Campo | Valor |
|---|---|
| Puerto de origen | 64920 (aleatorio) |
| Puerto de destino | 8080 |
| N.º de secuencia | 1 (relativo; real 348527666) |
| N.º de confirmación (ACK) | 1 (relativo; real 507149391) |
| Longitud de cabecera | 20 bytes |
| Flags | `PSH, ACK` (0x018) |
| Ventana | 255 × factor de escala 256 = 65280 |
| Checksum | 0x44ee (no verificado) |
| Datos (payload) | 115 bytes |
 
**Capa de aplicación**
 
Línea de texto del protocolo propio: `ENVIAR_PARTE|{json}` (115 bytes).
 
### Notas
 
- **Checksum 0x0000 y «unverified»:** la tarjeta de red calcula los checksums al enviar (*checksum offload*). Wireshark captura antes de ese cálculo y por eso no puede verificarlos. No indica un error de red.
- **Números de secuencia relativos:** Wireshark muestra la secuencia contando desde 1 para facilitar la lectura. El valor real está entre corchetes en el panel de TCP.
- **Tamaños:** 169 bytes de trama = 14 (Ethernet) + 20 (IP) + 20 (TCP) + 115 (datos).
## 4. Cómo reproducir la captura
 
1. En WSL, desde la carpeta del proyecto:
```bash
   mkdir -p out && javac -d out $(find src -name "*.java")
   hostname -I        # IP de WSL
   java -cp out pe.sigo40.red.ServidorCentral
```
2. En Windows, abrir Wireshark, elegir la interfaz `vEthernet (WSL)` y aplicar el filtro `tcp.port == 8080`.
3. En Windows, ejecutar el cliente con la IP de WSL:
```powershell
   java -cp out pe.sigo40.red.ClienteObra 172.22.124.50
```
4. Detener la captura y guardarla como `captura.pcapng`.
