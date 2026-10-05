package com.senati.modaapp.data.dao

import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.Categoria

class CategoriaDao(context: Context) {

    private val dbHelper = DBHelper(context)

    fun listar(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DBHelper.COL_CATEGORIA_ID}, ${DBHelper.COL_CATEGORIA_NOMBRE} FROM ${DBHelper.TABLE_CATEGORIA} ORDER BY ${DBHelper.COL_CATEGORIA_ID} ASC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DBHelper.COL_CATEGORIA_ID))
                val nombre = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CATEGORIA_NOMBRE))
                lista.add(Categoria(id, nombre))
            }
        }
        return lista
    }

    fun obtenerPorId(id: Int): Categoria? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DBHelper.COL_CATEGORIA_ID}, ${DBHelper.COL_CATEGORIA_NOMBRE} FROM ${DBHelper.TABLE_CATEGORIA} WHERE ${DBHelper.COL_CATEGORIA_ID} = ?",
            arrayOf(id.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                val catId = it.getInt(it.getColumnIndexOrThrow(DBHelper.COL_CATEGORIA_ID))
                val nombre = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CATEGORIA_NOMBRE))
                return Categoria(catId, nombre)
            }
        }
        return null
    }
}
