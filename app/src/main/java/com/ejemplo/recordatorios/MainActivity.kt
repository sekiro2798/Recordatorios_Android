package com.ejemplo.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejemplo.recordatorios.data.RepositorioEnMemoria
import com.ejemplo.recordatorios.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AplicacionRecordatorios() } }
    }
}

@Composable
fun AplicacionRecordatorios() {
    val repositorio = remember { RepositorioEnMemoria() }
    val factory = remember(repositorio) { RecordatoriosViewModelFactory(repositorio) }
    val vm: RecordatoriosViewModel = viewModel(factory = factory)
    val navController = rememberNavController()
    val lista by vm.lista.collectAsStateWithLifecycle()
    val formulario by vm.formulario.collectAsStateWithLifecycle()
    val detalle by vm.detalle.collectAsStateWithLifecycle()
    var errorGuardado by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = "listado") {
        composable("listado") {
            PantallaListado(
                estado = lista,
                onCrearRecordatorio = { vm.nuevoFormulario(); navController.navigate("crear") },
                onRecordatorioClick = { id -> navController.navigate("detalle/$id") },
                onReintentar = vm::reintentar,
                onSimularVacio = vm::mostrarVacio,
                onSimularError = vm::mostrarError,
                onRestaurar = vm::restaurar
            )
        }
        composable("crear") {
            LaunchedEffect(formulario.guardado) { if (formulario.guardado) navController.popBackStack() }
            PantallaCreacion(
                estado = formulario,
                errorSimulado = errorGuardado,
                onTitulo = vm::cambiarTitulo,
                onDescripcion = vm::cambiarDescripcion,
                onCategoria = vm::cambiarCategoria,
                onPrioridad = vm::cambiarPrioridad,
                onGuardar = vm::guardar,
                onCancelar = { navController.popBackStack() },
                onAlternarError = { errorGuardado = !errorGuardado; vm.alternarErrorGuardado() }
            )
        }
        composable("detalle/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = requireNotNull(entry.arguments).getLong("id")
            LaunchedEffect(id) { vm.cargarDetalle(id) }
            PantallaDetalle(estado = detalle, onVolver = { navController.popBackStack() })
        }
    }
}
