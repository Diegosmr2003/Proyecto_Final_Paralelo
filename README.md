Sistema de Simulación de Gimnasio - ParaGym----------------------------------

Autores:
Jesús Abel Gutirrez Calvillo
Jose Bernardo Sandoval Martinez
Diego Sebastian Montoya Rodriguez


Descripción
ParaGym es un sistema de simulación concurrente que modela las operaciones de un gimnasio, implementado en Java utilizando programación multi-hilo. El sistema simula el comportamiento de clientes, personal de limpieza, instructores y recepcionistas, todos interactuando de manera concurrente con recursos compartidos como máquinas de ejercicio, duchas, toallas y clases grupales.


Características Principales
Agentes Concurrentes
Clientes: Usuarios del gimnasio con comportamiento autónomo y aleatorio
Personal de Limpieza: Encargados de limpiar duchas y reponer toallas
Instructor: Imparte clases grupales cuando se alcanza el cupo
Recepcionista: Autoriza el acceso de clientes mediante servicio remoto
Recursos Compartidos
Buffers: Toallas, duchas libres y duchas sucias (sincronizados)
Zonas Críticas: Torniquete, máquinas de ejercicio, clase grupal
Sincronización: Implementación de productor-consumidor y monitores
Libre Albedrío de Clientes


Cada cliente decide aleatoriamente:
Usar máquinas (80% probabilidad) - De 1 a 3 veces
Ducharse (70% probabilidad)
Tomar clase grupal (60% probabilidad)


Visualización en Tiempo Real
Vista del Gimnasio: Canvas animado con movimiento de agentes
Tablero de Hilos: Estados de threads (RUNNABLE, BLOCKED, TIMED_WAITING, TERMINATED)
Monitor BPMN: Estados detallados por tipo de agente
Log de Eventos: Registro completo de todas las acciones


Arquitectura del Sistema
Componentes Principales
ParaGym/
├── ParaGym.java              # Ventana principal y configuración
├── SimulationController.java # Controlador de la simulación
├── Agentes/
│   ├── Cliente.java          # Agente cliente con libre albedrío
│   ├── Limpieza.java         # Agente de limpieza
│   ├── Instructor.java       # Agente instructor
│   └── Recepcionista.java    # Agente recepcionista
├── Recursos/
│   ├── Buffer.java           # Buffer sincronizado (productor-consumidor)
│   ├── Torniquete.java       # Zona crítica de entrada
│   ├── Maquina.java          # Zona crítica de máquinas
│   └── ClaseGrupo.java       # Sincronización de clase grupal
├── Visualización/
│   ├── GymCanvas.java        # Canvas de animación del gimnasio
│   ├── ThreadDashboard.java  # Tablero de estados de hilos
│   └── BPMNMonitor.java      # Monitor de estados BPMN
├── Servicios/
│   └── RemoteAuthService.java # Servicio de autorización remota
└── Utilidades/
    ├── Params.java           # Parámetros de configuración
    ├── Log.java              # Sistema de logging
    └── Solicitud.java        # Objeto de solicitud de autorización


--------------------------------Instalación y Ejecución--------------------------------------------
Requisitos
Java JDK: 17 o superior
IDE: IntelliJ IDEA, Eclipse, o VS Code
Imágenes: Carpeta Images/ con los recursos gráficos:
Gym.png (fondo del gimnasio)
Flaco.png (cliente)
Brazo50.png (cliente post-ejercicio)
Limpieza.png (personal de limpieza)
Instructor.png (instructor)
PersonaRecepcion.png (recepcionista izquierda)
PersonaRecepcionDerecha.png (recepcionista derecha)


Compilación
bash
# Compilar todos los archivos Java
javac *.java

# O usando un IDE, simplemente abrir el proyecto y compilar
Ejecución
bash
# Ejecutar el programa principal
java ParaGym


Uso del Sistema
1. Ventana de Configuración
Al iniciar, aparece la ventana principal donde puedes configurar:

Cantidades
Clientes: Número total de clientes (1-1000)
Limpieza: Número de personal de limpieza (1-200)
Toallas máx: Capacidad máxima del buffer de toallas
Toallas iniciales: Cantidad inicial de toallas disponibles
Duchas totales: Número total de duchas disponibles

Tiempos (milisegundos)
Paso torniquete: Tiempo para pasar el torniquete (ms)
Uso máquina: Tiempo de uso de cada máquina (ms)
Ducha (cliente): Tiempo que tarda un cliente en ducharse (ms)
Limpieza ducha: Tiempo para limpiar una ducha (ms)
Pausa entre ciclos de limpieza: Pausa del personal de limpieza (ms)
Inter-arribo clientes: Tiempo entre llegadas de clientes (ms)

Producción
Toallas por lote: Cantidad de toallas que produce limpieza en cada ciclo

2. Iniciar Simulación
Haz clic en "Iniciar simulación" para comenzar. Se abrirán automáticamente 4 ventanas:

    Tablero de hilos y agentes

Muestra:

Estados globales de threads (RUNNABLE, TIMED_WAITING, BLOCKED, TERMINATED)
Estados por tipo de agente (Cliente, Limpieza, Instructor, Recepcionista)
Resumen Global de Buffers:
Nombre del buffer
Agentes que lo usan (desglosado por tipo)
Estado actual (cantidad/capacidad)
Resumen Global de Zonas Críticas:
Nombre de la zona crítica
Agentes que la usan
Tipo de sincronización

    Log de simulación
Registro completo de eventos:

Preferencias de cada cliente
Entrada/salida de zonas críticas
Uso de buffers
Inicio/fin de actividades
Decisiones de los clientes

    Vista del Gimnasio
Visualización gráfica animada:

Cuadrantes:
Recepción (esquina superior derecha)
Máquinas (derecha)
Duchas/Toallas (izquierda)
Clase grupal (abajo izquierda)
Movimiento en tiempo real de los agentes

    Monitor de Estados BPMN
Pestañas separadas para cada tipo de agente:

Cliente: 14 estados (LLEGANDO, EN_RECEPCION, EN_MAQUINA, etc.)
Limpieza: 7 estados (INICIANDO, LIMPIANDO_DUCHA, PRODUCIENDO_TOALLAS, etc.)
Instructor: 5 estados (ESPERANDO_ALUMNOS, IMPARTIENDO_CLASE, etc.)
Recepcionista: 6 estados (ESPERANDO_SOLICITUD, CONSULTANDO_SERVICIO_REMOTO, etc.)

3. Detener Simulación
Haz clic en "Detener" para finalizar la simulación de manera ordenada.


Conceptos de Concurrencia Implementados

1. Patrón Productor-Consumidor
Buffer de Toallas: Limpieza produce, Clientes consumen
Buffer de Duchas: Limpieza y Clientes intercambian duchas limpias/sucias
Sincronización con wait() y notifyAll()
2. Zonas Críticas (Monitores)
Torniquete: Solo un cliente a la vez
Máquinas: Exclusión mutua por máquina
Clase Grupal: Sincronización de grupo con barrera
3. Estados de Thread
RUNNABLE: Ejecutándose o listo para ejecutar
TIMED_WAITING: Esperando con timeout (sleep)
BLOCKED: Esperando por un lock (synchronized)
TERMINATED: Thread finalizado
4. Comunicación Entre Threads
Recepcionista-Cliente: BlockingQueue para solicitudes
Instructor-Clientes: Barrera de sincronización para clase grupal
Limpieza-Clientes: Buffers compartidos
5. Deadlock Prevention
Orden consistente de adquisición de recursos
Timeout en esperas condicionales

Estados BPMN por Agente
Cliente (14 estados)
LLEGANDO
EN_RECEPCION
ACCESO_DENEGADO
TORNIQUETE
EN_GIMNASIO
EN_MAQUINA
MAQUINA_TERMINADA
TOMANDO_TOALLA
TOMANDO_DUCHA
EN_DUCHA
DUCHA_TERMINADA
ESPERANDO_CLASE
EN_CLASE
CLASE_TERMINADA
SALIENDO
Limpieza (7 estados)
INICIANDO
ESPERANDO_DUCHAS_SUCIAS
LIMPIANDO_DUCHA
PRODUCIENDO_DUCHA_LIBRE
PRODUCIENDO_TOALLAS
PAUSANDO
FINALIZADO
Instructor (5 estados)
INICIANDO
ESPERANDO_ALUMNOS
IMPARTIENDO_CLASE
FINALIZANDO_CLASE
FINALIZADO
Recepcionista (6 estados)
INICIANDO
ESPERANDO_SOLICITUD
PROCESANDO_SOLICITUD
CONSULTANDO_SERVICIO_REMOTO
ENVIANDO_RESPUESTA
FINALIZADO



---------Servicio de Autorización Remota
El sistema se conecta a un servicio REST para autorizar clientes:

Endpoint: https://autorizacion-gym-final-d7dcb7c8edgwdreg.westus3-01.azurewebsites.net/api/Autorizacion

Request:

json
{
  "cliente": "Cliente-5"
}
Response:

json
{
  "autorizado": true
}
