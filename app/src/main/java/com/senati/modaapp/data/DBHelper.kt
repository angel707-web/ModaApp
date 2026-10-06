package com.senati.modaapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "modaapp.db"
        const val DB_VERSION = 2

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

        // Tabla cliente (Sprint 3)
        const val TABLE_CLIENTE = "cliente"
        const val COL_CLIENTE_ID = "id"
        const val COL_CLIENTE_TELEFONO = "telefono"
        const val COL_CLIENTE_NOMBRES = "nombres"
        const val COL_CLIENTE_APELLIDOS = "apellidos"
        const val COL_CLIENTE_FECHA_REGISTRO = "fecha_registro"

        // Tabla pedido (Sprint 3)
        const val TABLE_PEDIDO = "pedido"
        const val COL_PEDIDO_ID = "id"
        const val COL_PEDIDO_ID_CLIENTE = "id_cliente"
        const val COL_PEDIDO_FECHA = "fecha"
        const val COL_PEDIDO_TOTAL = "total"
        const val COL_PEDIDO_ESTADO = "estado"
        const val COL_PEDIDO_FECHA_ATENCION = "fecha_atencion"

        // Tabla detalle_pedido (Sprint 3)
        const val TABLE_DETALLE_PEDIDO = "detalle_pedido"
        const val COL_DETALLE_ID = "id"
        const val COL_DETALLE_ID_PEDIDO = "id_pedido"
        const val COL_DETALLE_ID_ROPA = "id_ropa"
        const val COL_DETALLE_CANTIDAD = "cantidad"
        const val COL_DETALLE_PRECIO_UNIT = "precio_unit"
        const val COL_DETALLE_SUBTOTAL = "subtotal"
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

        // Insertar prendas iniciales de muestra
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo oversize', 1, 'M', 'Urban Style', 'Azul', 39.90, 15, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo básico', 1, 'S', 'Urban Style', 'Negro', 29.90, 20, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo estampado', 1, 'L', 'Boutique', 'Rojo', 45.00, 10, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Polo cuello V', 1, 'M', 'Boutique', 'Blanco', 32.00, 8, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Jean slim fit', 2, '32', 'Denim Co', 'Gris', 89.90, 12, '')")
        db.execSQL("INSERT INTO $TABLE_ROPA ($COL_ROPA_MODELO, $COL_ROPA_ID_CATEGORIA, $COL_ROPA_TALLA, $COL_ROPA_MARCA, $COL_ROPA_COLOR, $COL_ROPA_PRECIO, $COL_ROPA_CANTIDAD, $COL_ROPA_FOTO) VALUES ('Vestido midi', 3, 'S', 'Elegance', 'Morado', 119.00, 6, '')")

        // 4. Crear tablas de Sprint 3
        crearTablasSprint3(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            crearTablasSprint3(db)
        }
    }

    private fun crearTablasSprint3(db: SQLiteDatabase) {
        // Tabla cliente
        val sqlCreateCliente = """
            CREATE TABLE IF NOT EXISTS $TABLE_CLIENTE (
                $COL_CLIENTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CLIENTE_TELEFONO TEXT UNIQUE NOT NULL,
                $COL_CLIENTE_NOMBRES TEXT NOT NULL,
                $COL_CLIENTE_APELLIDOS TEXT NOT NULL,
                $COL_CLIENTE_FECHA_REGISTRO TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(sqlCreateCliente)

        // Insertar clientes de prueba de los prototipos
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_CLIENTE ($COL_CLIENTE_ID, $COL_CLIENTE_TELEFONO, $COL_CLIENTE_NOMBRES, $COL_CLIENTE_APELLIDOS, $COL_CLIENTE_FECHA_REGISTRO)
            VALUES (1, '987654321', 'Lucía', 'Torres Medina', '2026-10-01')
        """.trimIndent())
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_CLIENTE ($COL_CLIENTE_ID, $COL_CLIENTE_TELEFONO, $COL_CLIENTE_NOMBRES, $COL_CLIENTE_APELLIDOS, $COL_CLIENTE_FECHA_REGISTRO)
            VALUES (2, '912334556', 'Jorge', 'Ruiz Paredes', '2026-10-02')
        """.trimIndent())
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_CLIENTE ($COL_CLIENTE_ID, $COL_CLIENTE_TELEFONO, $COL_CLIENTE_NOMBRES, $COL_CLIENTE_APELLIDOS, $COL_CLIENTE_FECHA_REGISTRO)
            VALUES (3, '956778120', 'Carla', 'Soto Ramos', '2026-10-03')
        """.trimIndent())
        db.execSQL("""
            INSERT OR IGNORE INTO $TABLE_CLIENTE ($COL_CLIENTE_ID, $COL_CLIENTE_TELEFONO, $COL_CLIENTE_NOMBRES, $COL_CLIENTE_APELLIDOS, $COL_CLIENTE_FECHA_REGISTRO)
            VALUES (4, '934210987', 'Pedro', 'Alva Chávez', '2026-10-04')
        """.trimIndent())

        // Tabla pedido
        val sqlCreatePedido = """
            CREATE TABLE IF NOT EXISTS $TABLE_PEDIDO (
                $COL_PEDIDO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PEDIDO_ID_CLIENTE INTEGER NOT NULL,
                $COL_PEDIDO_FECHA TEXT NOT NULL,
                $COL_PEDIDO_TOTAL REAL NOT NULL,
                $COL_PEDIDO_ESTADO TEXT NOT NULL DEFAULT 'PENDIENTE',
                $COL_PEDIDO_FECHA_ATENCION TEXT,
                FOREIGN KEY ($COL_PEDIDO_ID_CLIENTE) REFERENCES $TABLE_CLIENTE($COL_CLIENTE_ID)
            )
        """.trimIndent()
        db.execSQL(sqlCreatePedido)

        // Tabla detalle_pedido
        val sqlCreateDetallePedido = """
            CREATE TABLE IF NOT EXISTS $TABLE_DETALLE_PEDIDO (
                $COL_DETALLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DETALLE_ID_PEDIDO INTEGER NOT NULL,
                $COL_DETALLE_ID_ROPA INTEGER NOT NULL,
                $COL_DETALLE_CANTIDAD INTEGER NOT NULL CHECK($COL_DETALLE_CANTIDAD > 0),
                $COL_DETALLE_PRECIO_UNIT REAL NOT NULL,
                $COL_DETALLE_SUBTOTAL REAL NOT NULL,
                FOREIGN KEY ($COL_DETALLE_ID_PEDIDO) REFERENCES $TABLE_PEDIDO($COL_PEDIDO_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_DETALLE_ID_ROPA) REFERENCES $TABLE_ROPA($COL_ROPA_ID)
            )
        """.trimIndent()
        db.execSQL(sqlCreateDetallePedido)

        // Insertar pedidos demo si la tabla está vacía
        val cursorCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PEDIDO", null)
        val count = if (cursorCount.moveToFirst()) cursorCount.getInt(0) else 0
        cursorCount.close()

        if (count == 0) {
            // Pedidos atendidos previos para reportes (P2-09: 37 atendidos o suma representativa)
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO, $COL_PEDIDO_FECHA_ATENCION) VALUES (50, 1, '2026-10-01 10:00:00', 1250.00, 'ATENDIDO', '2026-10-01 11:00:00')")
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO, $COL_PEDIDO_FECHA_ATENCION) VALUES (51, 2, '2026-10-02 12:30:00', 890.50, 'ATENDIDO', '2026-10-02 14:00:00')")
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO, $COL_PEDIDO_FECHA_ATENCION) VALUES (52, 1, '2026-10-02 15:45:00', 1420.00, 'ATENDIDO', '2026-10-02 17:10:00')")
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO, $COL_PEDIDO_FECHA_ATENCION) VALUES (53, 3, '2026-10-03 09:15:00', 655.00, 'ATENDIDO', '2026-10-03 10:30:00')")

            // Pedidos pendientes (Prototipo P2-08)
            // #55 Carla Soto (2 prendas · S/ 74.80)
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO) VALUES (55, 3, '2026-10-03 18:02:00', 74.80, 'PENDIENTE')")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (55, 1, 1, 39.90, 39.90)")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (55, 2, 1, 34.90, 34.90)")

            // #57 Jorge Ruiz (1 prenda · S/ 89.90)
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO) VALUES (57, 2, '2026-10-04 09:40:00', 89.90, 'PENDIENTE')")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (57, 5, 1, 89.90, 89.90)")

            // #58 Lucía Torres (3 prendas · S/ 288.70)
            db.execSQL("INSERT INTO $TABLE_PEDIDO ($COL_PEDIDO_ID, $COL_PEDIDO_ID_CLIENTE, $COL_PEDIDO_FECHA, $COL_PEDIDO_TOTAL, $COL_PEDIDO_ESTADO) VALUES (58, 1, '2026-10-04 10:15:00', 288.70, 'PENDIENTE')")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (58, 1, 1, 39.90, 39.90)")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (58, 5, 1, 89.90, 89.90)")
            db.execSQL("INSERT INTO $TABLE_DETALLE_PEDIDO ($COL_DETALLE_ID_PEDIDO, $COL_DETALLE_ID_ROPA, $COL_DETALLE_CANTIDAD, $COL_DETALLE_PRECIO_UNIT, $COL_DETALLE_SUBTOTAL) VALUES (58, 6, 1, 119.00, 119.00)")
        }
    }
}
