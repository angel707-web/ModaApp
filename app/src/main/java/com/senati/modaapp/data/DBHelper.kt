package com.senati.modaapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "modaapp.db"
        const val DB_VERSION = 1

        // Tabla usuario
        const val TABLE_USUARIO = "usuario"
        const val COL_USUARIO_ID = "id"
        const val COL_USUARIO_USER = "usuario"
        const val COL_USUARIO_CLAVE = "clave"
        const val COL_USUARIO_ROL = "rol"
        const val COL_USUARIO_TELEFONO = "telefono"

        // Tabla categoria
        const val TABLE_CATEGORIA = "categoria"
        const val COL_CATEGORIA_ID = "id"
        const val COL_CATEGORIA_NOMBRE = "nombre"

        // Tabla ropa
        const val TABLE_ROPA = "ropa"
        const val COL_ROPA_ID = "id"
        const val COL_ROPA_MODELO = "modelo"
        const val COL_ROPA_ID_CATEGORIA = "id_categoria"
        const val COL_ROPA_TALLA = "talla"
        const val COL_ROPA_MARCA = "marca"
        const val COL_ROPA_COLOR = "color"
        const val COL_ROPA_PRECIO = "precio"
        const val COL_ROPA_CANTIDAD = "cantidad"
        const val COL_ROPA_FOTO = "foto"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Activar soporte de claves foráneas
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Crear tabla usuario
        val sqlCreateUsuario = """
            CREATE TABLE $TABLE_USUARIO (
                $COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO_USER TEXT UNIQUE NOT NULL,
                $COL_USUARIO_CLAVE TEXT NOT NULL,
                $COL_USUARIO_ROL TEXT NOT NULL,
                $COL_USUARIO_TELEFONO TEXT
            )
        """.trimIndent()
        db.execSQL(sqlCreateUsuario)

        // Insertar usuario administrador por defecto
        val sqlInsertAdmin = """
            INSERT INTO $TABLE_USUARIO ($COL_USUARIO_USER, $COL_USUARIO_CLAVE, $COL_USUARIO_ROL, $COL_USUARIO_TELEFONO)
            VALUES ('admin', '1234', 'ADMIN', '987654321')
        """.trimIndent()
        db.execSQL(sqlInsertAdmin)

        // 2. Crear tabla categoria
        val sqlCreateCategoria = """
            CREATE TABLE $TABLE_CATEGORIA (
                $COL_CATEGORIA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORIA_NOMBRE TEXT UNIQUE NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlCreateCategoria)

        // Insertar las 5 categorías requeridas
        db.execSQL("INSERT INTO $TABLE_CATEGORIA ($COL_CATEGORIA_NOMBRE) VALUES ('Polos')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIA ($COL_CATEGORIA_NOMBRE) VALUES ('Pantalones')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIA ($COL_CATEGORIA_NOMBRE) VALUES ('Vestidos')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIA ($COL_CATEGORIA_NOMBRE) VALUES ('Casacas')")
        db.execSQL("INSERT INTO $TABLE_CATEGORIA ($COL_CATEGORIA_NOMBRE) VALUES ('Zapatillas')")

        // 3. Crear tabla ropa
        val sqlCreateRopa = """
            CREATE TABLE $TABLE_ROPA (
                $COL_ROPA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ROPA_MODELO TEXT NOT NULL,
                $COL_ROPA_ID_CATEGORIA INTEGER NOT NULL,
                $COL_ROPA_TALLA TEXT,
                $COL_ROPA_MARCA TEXT,
                $COL_ROPA_COLOR TEXT,
                $COL_ROPA_PRECIO REAL CHECK($COL_ROPA_PRECIO > 0),
                $COL_ROPA_CANTIDAD INTEGER CHECK($COL_ROPA_CANTIDAD >= 0),
                $COL_ROPA_FOTO TEXT,
                FOREIGN KEY ($COL_ROPA_ID_CATEGORIA) REFERENCES $TABLE_CATEGORIA($COL_CATEGORIA_ID)
            )
        """.trimIndent()
        db.execSQL(sqlCreateRopa)

        // Insertar prendas iniciales de muestra para el catálogo
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo oversize', 1, 'M', 'Urban Style', 'Azul', 39.90, 15, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo básico', 1, 'S', 'Urban Style', 'Negro', 29.90, 20, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo estampado', 1, 'L', 'Boutique', 'Rojo', 45.00, 10, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo cuello V', 1, 'M', 'Boutique', 'Blanco', 32.00, 8, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Jean slim fit', 2, '32', 'Denim Co', 'Gris', 89.90, 12, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Vestido midi', 3, 'S', 'Elegance', 'Morado', 119.00, 6, '')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En Sprint 3 se implementará onUpgrade para versión 2 agregando cliente y pedidos sin perder datos
    }
}
