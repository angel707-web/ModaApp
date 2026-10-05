package com.senati.modaapp

import android.database.sqlite.SQLiteConstraintException
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.senati.modaapp.data.dao.CategoriaDao
import com.senati.modaapp.data.dao.RopaDao
import com.senati.modaapp.data.model.Categoria
import com.senati.modaapp.data.model.Ropa
import com.senati.modaapp.databinding.ActivityRegistrarRopaBinding
import java.io.File

class RegistrarRopaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarRopaBinding
    private lateinit var categoriaDao: CategoriaDao
    private lateinit var ropaDao: RopaDao

    private var rutaFotoGuardada: String? = null
    private var categoriasDisponibles: List<Categoria> = emptyList()
    private var idRopaEdicion: Int = -1

    companion object {
        const val EXTRA_ID_ROPA = "extra_id_ropa"
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            guardarYMostrarFoto(uri)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            guardarYMostrarFoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarRopaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoriaDao = CategoriaDao(this)
        ropaDao = RopaDao(this)

        idRopaEdicion = intent.getIntExtra(EXTRA_ID_ROPA, -1)

        cargarSpinners()
        configurarModoEdicion()
        setupListeners()
    }

    private fun cargarSpinners() {
        categoriasDisponibles = categoriaDao.listar()
        val adapterCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoriasDisponibles
        )
        binding.spCategoria.adapter = adapterCategorias

        val tallas = listOf("XS", "S", "M", "L", "XL")
        val adapterTallas = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tallas
        )
        binding.spTalla.adapter = adapterTallas
        binding.spTalla.setSelection(2) // M por defecto
    }

    private fun configurarModoEdicion() {
        if (idRopaEdicion > 0) {
            // HU-07 CA1: Modo edición
            val ropaExistente = ropaDao.obtener(idRopaEdicion)
            if (ropaExistente != null) {
                binding.toolbarRegistrarRopa.title = getString(R.string.title_editar_ropa)
                binding.btnGuardarPrenda.text = getString(R.string.btn_actualizar_prenda)
                binding.btnEliminarPrenda.visibility = View.VISIBLE

                binding.etModelo.setText(ropaExistente.modelo)
                binding.etMarca.setText(ropaExistente.marca)
                binding.etColor.setText(ropaExistente.color)
                binding.etCantidad.setText(ropaExistente.cantidad.toString())
                binding.etPrecio.setText(ropaExistente.precio.toString())

                // Seleccionar categoría
                val indexCat = categoriasDisponibles.indexOfFirst { it.id == ropaExistente.idCategoria }
                if (indexCat >= 0) {
                    binding.spCategoria.setSelection(indexCat)
                }

                // Seleccionar talla
                val tallas = listOf("XS", "S", "M", "L", "XL")
                val indexTalla = tallas.indexOf(ropaExistente.talla)
                if (indexTalla >= 0) {
                    binding.spTalla.setSelection(indexTalla)
                }

                // Cargar foto si existe
                if (!ropaExistente.foto.isNullOrEmpty()) {
                    rutaFotoGuardada = ropaExistente.foto
                    val file = File(ropaExistente.foto)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            binding.ivPreviewFoto.setImageBitmap(bitmap)
                            binding.btnSeleccionarFoto.text = getString(R.string.btn_cambiar_foto)
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBackRegistrar.setOnClickListener {
            finish()
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            try {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: Exception) {
                getContent.launch("image/*")
            }
        }

        binding.etModelo.doAfterTextChanged { binding.tilModelo.error = null }
        binding.etMarca.doAfterTextChanged { binding.tilMarca.error = null }
        binding.etColor.doAfterTextChanged { binding.tilColor.error = null }
        binding.etCantidad.doAfterTextChanged { binding.tilCantidad.error = null }
        binding.etPrecio.doAfterTextChanged { binding.tilPrecio.error = null }

        // Guardar o Actualizar
        binding.btnGuardarPrenda.setOnClickListener {
            guardarOActualizarPrenda()
        }

        // Eliminar (HU-07 CA2)
        binding.btnEliminarPrenda.setOnClickListener {
            confirmarEliminacion()
        }
    }

    private fun guardarYMostrarFoto(uri: Uri) {
        try {
            val fileName = "ropa_${System.currentTimeMillis()}.jpg"
            val fileDest = File(filesDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                fileDest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            rutaFotoGuardada = fileDest.absolutePath
            val bitmap = BitmapFactory.decodeFile(rutaFotoGuardada)
            if (bitmap != null) {
                binding.ivPreviewFoto.setImageBitmap(bitmap)
                binding.btnSeleccionarFoto.text = getString(R.string.btn_cambiar_foto)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al copiar la foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarOActualizarPrenda() {
        val modelo = binding.etModelo.text?.toString()?.trim().orEmpty()
        val marca = binding.etMarca.text?.toString()?.trim().orEmpty()
        val color = binding.etColor.text?.toString()?.trim().orEmpty()
        val cantidadStr = binding.etCantidad.text?.toString()?.trim().orEmpty()
        val precioStr = binding.etPrecio.text?.toString()?.trim().orEmpty()

        var hayError = false

        if (modelo.isEmpty()) {
            binding.tilModelo.error = getString(R.string.error_modelo_requerido)
            hayError = true
        }

        if (marca.isEmpty()) {
            binding.tilMarca.error = getString(R.string.error_marca_requerida)
            hayError = true
        }

        if (color.isEmpty()) {
            binding.tilColor.error = getString(R.string.error_color_requerido)
            hayError = true
        }

        val cantidad = cantidadStr.toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            binding.tilCantidad.error = getString(R.string.error_cantidad_invalida)
            hayError = true
        }

        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio <= 0.0) {
            binding.tilPrecio.error = getString(R.string.error_precio_invalido)
            hayError = true
        }

        if (hayError) {
            return
        }

        val categoriaSeleccionada = binding.spCategoria.selectedItem as? Categoria
        val idCategoria = categoriaSeleccionada?.id ?: 1
        val tallaSeleccionada = binding.spTalla.selectedItem?.toString() ?: "M"

        if (idRopaEdicion > 0) {
            // Actualizar prenda existente (HU-07 CA1)
            val prendaActualizada = Ropa(
                id = idRopaEdicion,
                modelo = modelo,
                idCategoria = idCategoria,
                talla = tallaSeleccionada,
                marca = marca,
                color = color,
                precio = precio!!,
                cantidad = cantidad!!,
                foto = rutaFotoGuardada
            )
            val filas = ropaDao.actualizar(prendaActualizada)
            if (filas > 0) {
                Toast.makeText(this, getString(R.string.toast_prenda_actualizada), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar prenda", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Nueva prenda
            if (rutaFotoGuardada.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.error_foto_requerida), Toast.LENGTH_SHORT).show()
                return
            }

            val nuevaRopa = Ropa(
                modelo = modelo,
                idCategoria = idCategoria,
                talla = tallaSeleccionada,
                marca = marca,
                color = color,
                precio = precio!!,
                cantidad = cantidad!!,
                foto = rutaFotoGuardada
            )

            val idInsertado = ropaDao.insertar(nuevaRopa)
            if (idInsertado > 0) {
                Toast.makeText(this, getString(R.string.toast_prenda_guardada), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirmar_eliminar_titulo))
            .setMessage(getString(R.string.dialog_confirmar_eliminar_mensaje))
            .setPositiveButton(getString(R.string.dialog_btn_eliminar)) { _, _ ->
                eliminarPrenda()
            }
            .setNegativeButton(getString(R.string.dialog_btn_cancelar), null)
            .show()
    }

    private fun eliminarPrenda() {
        try {
            val eliminada = ropaDao.eliminar(idRopaEdicion)
            if (eliminada) {
                Toast.makeText(this, getString(R.string.toast_prenda_eliminada), Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: SQLiteConstraintException) {
            // HU-07 CA2: Validación de clave foránea con pedidos
            AlertDialog.Builder(this)
                .setTitle("No se puede eliminar")
                .setMessage(getString(R.string.error_no_se_puede_eliminar_pedidos))
                .setPositiveButton("Entendido", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
