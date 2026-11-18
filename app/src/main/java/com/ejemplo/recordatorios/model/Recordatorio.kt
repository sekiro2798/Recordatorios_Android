package com.ejemplo.recordatorios.model

enum class Categoria(val etiqueta: String) { ESTUDIO("Estudio"), TRABAJO("Trabajo"), PERSONAL("Personal") }
enum class Prioridad(val etiqueta: String) { ALTA("Alta"), MEDIA("Media"), BAJA("Baja") }
sealed interface Estado {
    data object Pendiente : Estado
    data object Completado : Estado
}
data class Recordatorio(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: Estado = Estado.Pendiente
)
