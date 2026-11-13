# SIGO 4.0 - Sistema Integrado de Gestion de Obras

Prototipo del trabajo grupal (Principios de los Sistemas de Software Distribuido, IS6085SP).
Java 21 + Gradle, sin librerias externas.

## Estructura
- `pe.sigo40.red`         Servidor TCP (puerto 8080), cliente de obra, sensor UDP y receptor (puerto 9090)
- `pe.sigo40.algoritmos`  Algoritmo del Maton (Bully) para elegir coordinador cuando cae San Isidro
- `pe.sigo40.blockchain`  Cadena de bloques SHA-256 para las actas de calidad
- `pe.sigo40.comun`       Utilidad JSON minima

## Como ejecutar (en terminales separadas)
    gradle servidor          # terminal 1
    gradle clienteObra       # terminal 2  (envia parte diario y pedido)
    gradle receptor          # terminal 3
    gradle sensor            # terminal 4  (envia temperaturas por UDP)
    gradle bully
    gradle actas

Para probar entre dos equipos: `gradle clienteObra --args="192.168.10.10"` (IP del servidor).
