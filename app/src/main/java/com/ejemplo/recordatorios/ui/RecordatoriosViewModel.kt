package com.ejemplo.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ejemplo.recordatorios.data.RecordatoriosRepository
import com.ejemplo.recordatorios.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ListaUiState {
    data object Cargando : ListaUiState
    data object Vacio : ListaUiState
    data class Datos(val items: List<Recordatorio>) : ListaUiState
    data class Error(val mensaje: String) : ListaUiState
}

data class FormularioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val enviando: Boolean = false,
    val guardado: Boolean = false,
    val errorGeneral: String? = null
) {
    val errorTitulo: String? get() = if (titulo.trim().length < 4) "Mínimo 4 caracteres" else null
    val errorDescripcion: String? get() = if (descripcion.length > 200) "Máximo 200 caracteres" else null
    val errorCategoria: String? get() = if (categoria == null || categoria !in Categoria.entries) "Selecciona una categoría válida" else null
    val errorPrioridad: String? get() = if (prioridad == null) "Selecciona una prioridad" else null
    val esValido: Boolean get() = errorTitulo == null && errorDescripcion == null && errorCategoria == null && errorPrioridad == null
    val puedeGuardar: Boolean get() = esValido && !enviando && !guardado
}

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Datos(val recordatorio: Recordatorio) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}

class RecordatoriosViewModel(private val repositorio: RecordatoriosRepository) : ViewModel() {
    private val _lista = MutableStateFlow<ListaUiState>(ListaUiState.Cargando)
    val lista: StateFlow<ListaUiState> = _lista.asStateFlow()
    private val _formulario = MutableStateFlow(FormularioUiState())
    val formulario: StateFlow<FormularioUiState> = _formulario.asStateFlow()
    private val _detalle = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val detalle: StateFlow<DetalleUiState> = _detalle.asStateFlow()
    private var simularVacio = false
    private var simularError = false
    private var simularErrorGuardado = false

    init { cargar() }

    fun cargar() {
        _lista.value = ListaUiState.Cargando
        viewModelScope.launch {
            try {
                val items = repositorio.obtenerTodos()
                _lista.value = when {
                    simularError -> ListaUiState.Error("No se pudieron cargar los recordatorios")
                    simularVacio || items.isEmpty() -> ListaUiState.Vacio
                    else -> ListaUiState.Datos(items)
                }
            } catch (e: Exception) { _lista.value = ListaUiState.Error(e.message ?: "Error de carga") }
        }
    }
    fun mostrarVacio() { simularVacio = true; simularError = false; cargar() }
    fun mostrarError() { simularError = true; simularVacio = false; cargar() }
    fun restaurar() { simularError = false; simularVacio = false; cargar() }
    fun reintentar() { simularError = false; cargar() }
    fun nuevoFormulario() { _formulario.value = FormularioUiState() }
    fun cambiarTitulo(valor: String) { _formulario.update { it.copy(titulo = valor, errorGeneral = null) } }
    fun cambiarDescripcion(valor: String) { _formulario.update { it.copy(descripcion = valor, errorGeneral = null) } }
    fun cambiarCategoria(valor: Categoria) { _formulario.update { it.copy(categoria = valor, errorGeneral = null) } }
    fun cambiarPrioridad(valor: Prioridad) { _formulario.update { it.copy(prioridad = valor, errorGeneral = null) } }
    fun alternarErrorGuardado() { simularErrorGuardado = !simularErrorGuardado }
    fun guardar() {
        val actual = _formulario.value
        if (!actual.puedeGuardar) return
        _formulario.update { it.copy(enviando = true, errorGeneral = null) }
        viewModelScope.launch {
            try {
                if (simularErrorGuardado) {
                    kotlinx.coroutines.delay(1100)
                    error("Error simulado al guardar. Inténtalo de nuevo.")
                }
                repositorio.guardar(actual.titulo.trim(), actual.descripcion, requireNotNull(actual.categoria), requireNotNull(actual.prioridad))
                _formulario.update { it.copy(enviando = false, guardado = true) }
                simularVacio = false
                simularError = false
                cargar()
            } catch (e: Exception) { _formulario.update { it.copy(enviando = false, errorGeneral = e.message ?: "No se pudo guardar") } }
        }
    }
    fun cargarDetalle(id: Long) {
        _detalle.value = DetalleUiState.Cargando
        viewModelScope.launch {
            try {
                val item = repositorio.obtenerPorId(id)
                _detalle.value = if (item == null) DetalleUiState.Error("Recordatorio no encontrado") else DetalleUiState.Datos(item)
            } catch (e: Exception) { _detalle.value = DetalleUiState.Error(e.message ?: "No se pudo cargar el detalle") }
        }
    }
}

class RecordatoriosViewModelFactory(private val repositorio: RecordatoriosRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RecordatoriosViewModel(repositorio) as T
}
