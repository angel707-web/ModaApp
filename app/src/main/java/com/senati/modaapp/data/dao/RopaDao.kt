package com.senati.modaapp.data.dao

import android.content.ContentValues
import android.content.Context
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
     * Lista todas las prendas para el inventario del administrador
     */
    fun listar(): List<Ropa> {
        val lista = mutableListOf<Ropa>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT r.${DBHelper.COL_ROPA_ID}, r.${DBHelper.COL_ROPA_MODELO}, 
                   r.${DBHelper.COL_ROPA_ID_CATEGORIA}, c.${DBHelper.COL_CATEGORIA_NOMBRE} AS categoria_nombre,
                   r.${DBHelper.COL_ROPA_TALLA}, r.${DBHelper.COL_ROPA_MARCA}, 
                   r.${DBHelper.COL_ROPA_COLOR}, r.${DBHelper.COL_ROPA_PRECIO}, 
                   r.${DBHelper.COL_ROPA_CANTIDAD}, r.${DBHelper.COL_ROPA_FOTO}
            FROM ${DBHelper.TABLE_ROPA} r
            INNER JOIN ${DBHelper.TABLE_CATEGORIA} c ON r.${DBHelper.COL_ROPA_ID_CATEGORIA} = c.${DBHelper.COL_CATEGORIA_ID}
            ORDER BY r.${DBHelper.COL_ROPA_ID} DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
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
