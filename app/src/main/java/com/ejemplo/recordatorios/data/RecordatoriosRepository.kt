package com.ejemplo.recordatorios.data

import com.ejemplo.recordatorios.model.*
import kotlinx.coroutines.delay

interface RecordatoriosRepository {
    suspend fun obtenerTodos(): List<Recordatorio>
    suspend fun obtenerPorId(id: Long): Recordatorio?
    suspend fun guardar(titulo: String, descripcion: String, categoria: Categoria, prioridad: Prioridad): Recordatorio
}

class RepositorioEnMemoria : RecordatoriosRepository {
    private val registros = mutableListOf(
        Recordatorio(1, "Preparar examen", "Repasar Kotlin y Compose", Categoria.ESTUDIO, Prioridad.ALTA),
        Recordatorio(2, "Enviar informe", "Entregar actividad de Mobile", Categoria.TRABAJO, Prioridad.ALTA),
        Recordatorio(3, "Comprar alimentos", "Leche, pan y frutas", Categoria.PERSONAL, Prioridad.MEDIA, Estado.Completado),
        Recordatorio(4, "Revisar apuntes", "Releer material de clases", Categoria.ESTUDIO, Prioridad.BAJA)
    )
    override suspend fun obtenerTodos(): List<Recordatorio> { delay(850); return registros.toList() }
    override suspend fun obtenerPorId(id: Long): Recordatorio? { delay(300); return registros.find { it.id == id } }
    override suspend fun guardar(titulo: String, descripcion: String, categoria: Categoria, prioridad: Prioridad): Recordatorio {
        delay(1100)
        val nuevo = Recordatorio((registros.maxOfOrNull { it.id } ?: 0) + 1, titulo, descripcion, categoria, prioridad)
        registros.add(nuevo)
        return nuevo
    }
}
