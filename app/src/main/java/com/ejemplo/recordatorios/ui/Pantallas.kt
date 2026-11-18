package com.ejemplo.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ejemplo.recordatorios.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListado(
    estado: ListaUiState,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Long) -> Unit,
    onReintentar: () -> Unit,
    onSimularVacio: () -> Unit,
    onSimularError: () -> Unit,
    onRestaurar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis recordatorios") }) },
        floatingActionButton = { FloatingActionButton(onClick = onCrearRecordatorio) { Text("+", style = MaterialTheme.typography.headlineMedium) } }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            Text("Simulación de estados", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onRestaurar) { Text("Datos") }
                TextButton(onClick = onSimularVacio) { Text("Vacío") }
                TextButton(onClick = onSimularError) { Text("Error") }
            }
            when (estado) {
                ListaUiState.Cargando -> MensajeEstado("Cargando recordatorios…", mostrarProgreso = true)
                ListaUiState.Vacio -> MensajeEstado("Todavía no existen recordatorios", textoAccion = "Crear recordatorio", onAccion = onCrearRecordatorio)
                is ListaUiState.Error -> MensajeEstado(estado.mensaje, textoAccion = "Reintentar", onAccion = onReintentar)
                is ListaUiState.Datos -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(estado.items, key = { it.id }) { item -> TarjetaRecordatorio(item, onClick = { onRecordatorioClick(item.id) }) }
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(item: Recordatorio, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(item.titulo, style = MaterialTheme.typography.titleMedium)
            Text("Categoría: ${item.categoria.etiqueta}")
            Text("Prioridad: ${item.prioridad.etiqueta}")
            Text("Estado: ${if (item.estado is Estado.Completado) "Completado" else "Pendiente"}")
        }
    }
}

@Composable
fun MensajeEstado(
    mensaje: String,
    modifier: Modifier = Modifier,
    textoAccion: String? = null,
    onAccion: () -> Unit = {},
    mostrarProgreso: Boolean = false
) {
    Column(modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (mostrarProgreso) CircularProgressIndicator()
        Text(mensaje)
        if (textoAccion != null) Button(onClick = onAccion) { Text(textoAccion) }
    }
}

@Composable
fun CampoTextoControlado(valor: String, onCambio: (String) -> Unit, etiqueta: String, error: String?, modifier: Modifier = Modifier, lineas: Int = 1) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valor, onValueChange = onCambio, label = { Text(etiqueta) },
            modifier = Modifier.fillMaxWidth().then(if (error != null) Modifier.semantics { this.error(error) } else Modifier),
            isError = error != null, minLines = lineas, maxLines = if (lineas > 1) 5 else 1,
            supportingText = { if (error != null) Text(error) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCreacion(
    estado: FormularioUiState,
    errorSimulado: Boolean,
    onTitulo: (String) -> Unit,
    onDescripcion: (String) -> Unit,
    onCategoria: (Categoria) -> Unit,
    onPrioridad: (Prioridad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    onAlternarError: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Crear recordatorio") }) }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTextoControlado(estado.titulo, onTitulo, "Título", estado.errorTitulo)
            CampoTextoControlado(estado.descripcion, onDescripcion, "Descripción", estado.errorDescripcion, lineas = 3)
            Text("${estado.descripcion.length}/200 caracteres")
            Text("Categoría", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Categoria.entries.forEach { categoria -> FilterChip(selected = estado.categoria == categoria, onClick = { onCategoria(categoria) }, label = { Text(categoria.etiqueta) }) }
            }
            estado.errorCategoria?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
            Text("Prioridad", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Prioridad.entries.forEach { prioridad -> FilterChip(selected = estado.prioridad == prioridad, onClick = { onPrioridad(prioridad) }, label = { Text(prioridad.etiqueta) }) }
            }
            estado.errorPrioridad?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error)
            }
            Row { Checkbox(checked = errorSimulado, onCheckedChange = { onAlternarError() }); Text("Simular error de guardado", Modifier.padding(top = 12.dp)) }
            if (estado.errorGeneral != null) Text(estado.errorGeneral, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { error(estado.errorGeneral) })
            if (estado.enviando) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancelar, enabled = !estado.enviando) { Text("Cancelar") }
                Button(onClick = onGuardar, enabled = estado.puedeGuardar) { Text(if (estado.enviando) "Guardando…" else "Guardar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(estado: DetalleUiState, onVolver: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle del recordatorio") }, navigationIcon = { TextButton(onClick = onVolver) { Text("Volver") } }) }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (estado) {
                DetalleUiState.Cargando -> MensajeEstado("Cargando detalle…", mostrarProgreso = true)
                is DetalleUiState.Error -> MensajeEstado(estado.mensaje, textoAccion = "Volver", onAccion = onVolver)
                is DetalleUiState.Datos -> {
                    val item = estado.recordatorio
                    Text(item.titulo, style = MaterialTheme.typography.headlineSmall)
                    Text("Identificador: ${item.id}")
                    Text("Descripción: ${item.descripcion.ifBlank { "Sin descripción" }}")
                    Text("Categoría: ${item.categoria.etiqueta}")
                    Text("Prioridad: ${item.prioridad.etiqueta}")
                    Text("Estado: ${if (item.estado is Estado.Completado) "Completado" else "Pendiente"}")
                    Button(onClick = onVolver) { Text("Volver al listado") }
                }
            }
        }
    }
}
