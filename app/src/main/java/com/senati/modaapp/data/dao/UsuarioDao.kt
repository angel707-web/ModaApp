package com.senati.modaapp.data.dao

import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.Usuario

class UsuarioDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * Valida credenciales de usuario mediante consulta parametrizada (rawQuery con ?)
     */
    fun validarUsuario(usuario: String, clave: String): Usuario? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT ${DBHelper.COL_USUARIO_ID}, ${DBHelper.COL_USUARIO_USER}, 
                   ${DBHelper.COL_USUARIO_CLAVE}, ${DBHelper.COL_USUARIO_ROL}, 
                   ${DBHelper.COL_USUARIO_TELEFONO}
            FROM ${DBHelper.TABLE_USUARIO}
            WHERE ${DBHelper.COL_USUARIO_USER} = ? AND ${DBHelper.COL_USUARIO_CLAVE} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(usuario, clave))
        var usuarioEncontrado: Usuario? = null

        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ID))
                val user = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_USUARIO_USER))
                val pass = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_USUARIO_CLAVE))
                val rol = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_USUARIO_ROL))
                val telefono = it.getString(it.getColumnIndexOrThrow(DBHelper.COL_USUARIO_TELEFONO))
                usuarioEncontrado = Usuario(id, user, pass, rol, telefono)
            }
        }
        return usuarioEncontrado
    }

    /**
     * Obtiene el teléfono del administrador para envío de WhatsApp de pedidos (HU-10)
     */
    fun obtenerTelefonoAdmin(): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT ${DBHelper.COL_USUARIO_TELEFONO} FROM ${DBHelper.TABLE_USUARIO} WHERE ${DBHelper.COL_USUARIO_ROL} = 'ADMIN' LIMIT 1",
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val tel = it.getString(0)
                if (!tel.isNullOrBlank()) return tel
            }
        }
        return "987654321"
    }
}
