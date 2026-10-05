package com.senati.modaapp.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.Ropa

class RopaDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * Inserta una nueva prenda en la base de datos
     */
    fun insertar(ropa: Ropa): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_ROPA_MODELO, ropa.modelo)
            put(DBHelper.COL_ROPA_ID_CATEGORIA, ropa.idCategoria)
            put(DBHelper.COL_ROPA_TALLA, ropa.talla)
            put(DBHelper.COL_ROPA_MARCA, ropa.marca)
            put(DBHelper.COL_ROPA_COLOR, ropa.color)
            put(DBHelper.COL_ROPA_PRECIO, ropa.precio)
            put(DBHelper.COL_ROPA_CANTIDAD, ropa.cantidad)
            put(DBHelper.COL_ROPA_FOTO, ropa.foto ?: "")
        }
        return db.insert(DBHelper.TABLE_ROPA, null, values)
    }

    /**
     * Actualiza los datos de una prenda existente (HU-07 CA1)
     */
    fun actualizar(ropa: Ropa): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_ROPA_MODELO, ropa.modelo)
            put(DBHelper.COL_ROPA_ID_CATEGORIA, ropa.idCategoria)
            put(DBHelper.COL_ROPA_TALLA, ropa.talla)
            put(DBHelper.COL_ROPA_MARCA, ropa.marca)
            put(DBHelper.COL_ROPA_COLOR, ropa.color)
            put(DBHelper.COL_ROPA_PRECIO, ropa.precio)
            put(DBHelper.COL_ROPA_CANTIDAD, ropa.cantidad)
            put(DBHelper.COL_ROPA_FOTO, ropa.foto ?: "")
        }
        return db.update(DBHelper.TABLE_ROPA, values, "${DBHelper.COL_ROPA_ID} = ?", arrayOf(ropa.id.toString()))
    }

    /**
     * Obtiene una prenda por su ID (HU-07 CA1)
     */
    fun obtener(id: Int): Ropa? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT r.${DBHelper.COL_ROPA_ID}, r.${DBHelper.COL_ROPA_MODELO}, 
                   r.${DBHelper.COL_ROPA_ID_CATEGORIA}, c.${DBHelper.COL_CATEGORIA_NOMBRE} AS categoria_nombre,
                   r.${DBHelper.COL_ROPA_TALLA}, r.${DBHelper.COL_ROPA_MARCA}, 
                   r.${DBHelper.COL_ROPA_COLOR}, r.${DBHelper.COL_ROPA_PRECIO}, 
                   r.${DBHelper.COL_ROPA_CANTIDAD}, r.${DBHelper.COL_ROPA_FOTO}
            FROM ${DBHelper.TABLE_ROPA} r
            INNER JOIN ${DBHelper.TABLE_CATEGORIA} c ON r.${DBHelper.COL_ROPA_ID_CATEGORIA} = c.${DBHelper.COL_CATEGORIA_ID}
            WHERE r.${DBHelper.COL_ROPA_ID} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                return extraerRopa(it)
            }
        }
        return null
    }

    /**
     * Elimina una prenda de la base de datos (HU-07 CA2).
     * Si la prenda está asociada a un pedido existente, lanza SQLiteConstraintException.
     */
    @Throws(SQLiteConstraintException::class)
    fun eliminar(id: Int): Boolean {
        val db = dbHelper.writableDatabase

        // Verificar si la prenda tiene pedidos asociados en detalle_pedido
        val checkCursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DBHelper.TABLE_DETALLE_PEDIDO} WHERE ${DBHelper.COL_DETALLE_ID_ROPA} = ?",
            arrayOf(id.toString())
        )
        val tienePedidos = checkCursor.use {
            if (it.moveToFirst()) it.getInt(0) > 0 else false
        }

        if (tienePedidos) {
            throw SQLiteConstraintException("No se puede eliminar: tiene pedidos")
        }

        val filasAfectadas = db.delete(DBHelper.TABLE_ROPA, "${DBHelper.COL_ROPA_ID} = ?", arrayOf(id.toString()))
        return filasAfectadas > 0
    }

    /**
     * Lista todas las prendas para el inventario del administrador, con filtro opcional por búsqueda (HU-07 CA3)
     */
    fun listar(filtro: String? = null): List<Ropa> {
        val lista = mutableListOf<Ropa>()
        val db = dbHelper.readableDatabase

        val sqlBuilder = StringBuilder("""
            SELECT r.${DBHelper.COL_ROPA_ID}, r.${DBHelper.COL_ROPA_MODELO}, 
                   r.${DBHelper.COL_ROPA_ID_CATEGORIA}, c.${DBHelper.COL_CATEGORIA_NOMBRE} AS categoria_nombre,
                   r.${DBHelper.COL_ROPA_TALLA}, r.${DBHelper.COL_ROPA_MARCA}, 
                   r.${DBHelper.COL_ROPA_COLOR}, r.${DBHelper.COL_ROPA_PRECIO}, 
                   r.${DBHelper.COL_ROPA_CANTIDAD}, r.${DBHelper.COL_ROPA_FOTO}
            FROM ${DBHelper.TABLE_ROPA} r
            INNER JOIN ${DBHelper.TABLE_CATEGORIA} c ON r.${DBHelper.COL_ROPA_ID_CATEGORIA} = c.${DBHelper.COL_CATEGORIA_ID}
        """.trimIndent())

        val args = mutableListOf<String>()

        if (!filtro.isNullOrBlank()) {
            val termino = "%${filtro.trim()}%"
            sqlBuilder.append(" WHERE r.${DBHelper.COL_ROPA_MODELO} LIKE ? OR r.${DBHelper.COL_ROPA_MARCA} LIKE ? OR r.${DBHelper.COL_ROPA_COLOR} LIKE ?")
            args.add(termino)
            args.add(termino)
            args.add(termino)
        }

        sqlBuilder.append(" ORDER BY r.${DBHelper.COL_ROPA_ID} DESC")

        val cursor = db.rawQuery(sqlBuilder.toString(), if (args.isEmpty()) null else args.toTypedArray())
        cursor.use {
            while (it.moveToNext()) {
                lista.add(extraerRopa(it))
            }
        }
        return lista
    }

    /**
     * Lista prendas disponibles para el catálogo del cliente (cantidad > 0)
     * Opcionalmente filtradas por categoría
     */
    fun listarDisponibles(idCategoria: Int? = null): List<Ropa> {
        val lista = mutableListOf<Ropa>()
        val db = dbHelper.readableDatabase

        val sqlBuilder = StringBuilder("""
            SELECT r.${DBHelper.COL_ROPA_ID}, r.${DBHelper.COL_ROPA_MODELO}, 
                   r.${DBHelper.COL_ROPA_ID_CATEGORIA}, c.${DBHelper.COL_CATEGORIA_NOMBRE} AS categoria_nombre,
                   r.${DBHelper.COL_ROPA_TALLA}, r.${DBHelper.COL_ROPA_MARCA}, 
                   r.${DBHelper.COL_ROPA_COLOR}, r.${DBHelper.COL_ROPA_PRECIO}, 
                   r.${DBHelper.COL_ROPA_CANTIDAD}, r.${DBHelper.COL_ROPA_FOTO}
            FROM ${DBHelper.TABLE_ROPA} r
            INNER JOIN ${DBHelper.TABLE_CATEGORIA} c ON r.${DBHelper.COL_ROPA_ID_CATEGORIA} = c.${DBHelper.COL_CATEGORIA_ID}
            WHERE r.${DBHelper.COL_ROPA_CANTIDAD} > 0
        """.trimIndent())

        val args = mutableListOf<String>()

        if (idCategoria != null && idCategoria > 0) {
            sqlBuilder.append(" AND r.${DBHelper.COL_ROPA_ID_CATEGORIA} = ?")
            args.add(idCategoria.toString())
        }

        sqlBuilder.append(" ORDER BY r.${DBHelper.COL_ROPA_ID} DESC")

        val cursor = db.rawQuery(sqlBuilder.toString(), if (args.isEmpty()) null else args.toTypedArray())
        cursor.use {
            while (it.moveToNext()) {
                lista.add(extraerRopa(it))
            }
        }
        return lista
    }

    private fun extraerRopa(cursor: android.database.Cursor): Ropa {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_ID))
        val modelo = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_MODELO))
        val idCategoria = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_ID_CATEGORIA))
        val categoriaNombre = cursor.getString(cursor.getColumnIndexOrThrow("categoria_nombre"))
        val talla = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_TALLA))
        val marca = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_MARCA))
        val color = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_COLOR))
        val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_PRECIO))
        val cantidad = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_CANTIDAD))
        val foto = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ROPA_FOTO))

        return Ropa(id, modelo, idCategoria, categoriaNombre, talla, marca, color, precio, cantidad, foto)
    }
}
