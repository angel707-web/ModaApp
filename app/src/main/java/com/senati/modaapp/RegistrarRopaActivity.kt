package com.senati.modaapp

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

    // Selector de fotos moderno PickVisualMedia con fallback a GetContent
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

        cargarSpinners()
        setupListeners()
    }

    private fun cargarSpinners() {
        // Cargar categorías precargadas de la base de datos
        categoriasDisponibles = categoriaDao.listar()
        val adapterCategorias = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoriasDisponibles
        )
        binding.spCategoria.adapter = adapterCategorias

        // Cargar tallas estándar
        val tallas = listOf("XS", "S", "M", "L", "XL")
        val adapterTallas = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tallas
        )
        binding.spTalla.adapter = adapterTallas
        binding.spTalla.setSelection(2) // Selección por defecto: M
    }

    private fun setupListeners() {
        binding.btnBackRegistrar.setOnClickListener {
            finish()
        }

        // Selección de foto desde la galería
        binding.btnSeleccionarFoto.setOnClickListener {
            try {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: Exception) {
                // Fallback para dispositivos sin compatibilidad PickVisualMedia
                getContent.launch("image/*")
            }
        }

        // Limpiar errores en tiempo real
        binding.etModelo.doAfterTextChanged { binding.tilModelo.error = null }
        binding.etMarca.doAfterTextChanged { binding.tilMarca.error = null }
        binding.etColor.doAfterTextChanged { binding.tilColor.error = null }
        binding.etCantidad.doAfterTextChanged { binding.tilCantidad.error = null }
        binding.etPrecio.doAfterTextChanged { binding.tilPrecio.error = null }

        // Botón Guardar
        binding.btnGuardarPrenda.setOnClickListener {
            guardarPrenda()
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

    private fun guardarPrenda() {
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

        if (rutaFotoGuardada.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.error_foto_requerida), Toast.LENGTH_SHORT).show()
            hayError = true
        }

        if (hayError) {
            return
        }

        // Obtener categoría y talla seleccionadas
        val categoriaSeleccionada = binding.spCategoria.selectedItem as? Categoria
        val idCategoria = categoriaSeleccionada?.id ?: 1
        val tallaSeleccionada = binding.spTalla.selectedItem?.toString() ?: "M"

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
