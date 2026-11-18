# Recordatorios — Actividad 2

Proyecto Kotlin / Jetpack Compose / Material 3 basado en la Actividad 1.

## Cómo abrir
Abrir esta carpeta (la que contiene `settings.gradle.kts`) en Android Studio. Esperar Sync y ejecutar `app` en un emulador.

## Rutas
| Ruta | Argumentos | Uso |
|---|---|---|
| `listado` | Ninguno | Destino inicial; datos, carga, vacío y error |
| `crear` | Ninguno | Formulario validado |
| `detalle/{id}` | `id: Long` | Consulta del recordatorio por identificador |

## Capturas para el informe
1. Listado con recordatorios (consignas 1 y 5).
2. Formulario de creación (consigna 2).
3. Detalle de un registro (consignas 1 y 5).
4. Estado vacío: botón **Vacío** en listado (consigna 3).
5. Estado error con reintento: botón **Error** en listado (consigna 3).
6. Árbol de archivos Android Studio (organización general).

## Arquitectura
`MainActivity` contiene `NavHost` y `NavController`. Las pantallas reciben estado y lambdas. `RecordatoriosViewModel` usa `MutableStateFlow` internamente, publica `StateFlow`, y ejecuta operaciones asíncronas en `viewModelScope`. El repositorio se consume mediante la interfaz `RecordatoriosRepository`; `RepositorioEnMemoria` mantiene los registros simulados. `collectAsStateWithLifecycle` observa el estado de forma consciente del ciclo de vida.

**Nota:** las capturas deben tomarse desde Android Studio y el emulador, no se incluyen imágenes simuladas como evidencias. Compilación pendiente de verificar en el entorno local.
