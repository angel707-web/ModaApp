package com.senati.modaapp.data.dao

import android.content.ContentValues
import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.Cliente

class ClienteDao(context: Context) {

    private val dbHelper = DBHelper(context)

    fun buscarPorTelefono(telefono: String): Cliente? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT ${DBHelper.COL_CLIENTE_ID}, ${DBHelper.COL_CLIENTE_TELEFONO}, 
                   ${DBHelper.COL_CLIENTE_NOMBRES}, ${DBHelper.COL_CLIENTE_APELLIDOS}, 
                   ${DBHelper.COL_CLIENTE_FECHA_REGISTRO}
            FROM ${DBHelper.TABLE_CLIENTE}
            WHERE ${DBHelper.COL_CLIENTE_TELEFONO} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(telefono))
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_ID))
                val tel = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_TELEFONO))
                val nombres = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_NOMBRES))
                val apellidos = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_APELLIDOS))
                val fecha = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_CLIENTE_FECHA_REGISTRO))
                return Cliente(id, tel, nombres, apellidos, fecha)
            }
        }
        return null
    }

    fun insertar(cliente: Cliente): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COL_CLIENTE_TELEFONO, cliente.telefono)
            put(DBHelper.COL_CLIENTE_NOMBRES, cliente.nombres)
            put(DBHelper.COL_CLIENTE_APELLIDOS, cliente.apellidos)
            put(DBHelper.COL_CLIENTE_FECHA_REGISTRO, cliente.fechaRegistro)
        }
        return db.insert(DBHelper.TABLE_CLIENTE, null, values)
    }
}
