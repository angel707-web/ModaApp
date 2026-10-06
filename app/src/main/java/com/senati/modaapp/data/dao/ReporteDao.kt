package com.senati.modaapp.data.dao

import android.content.Context
import com.senati.modaapp.data.DBHelper
import com.senati.modaapp.data.model.ClienteConPedidos
import com.senati.modaapp.data.model.StockPrenda

class ReporteDao(context: Context) {

    private val dbHelper = DBHelper(context)

    /**
     * HU-12 CA1: Retorna el monto total vendido en el mes actual para pedidos atendidos.
     * Si no hay pedidos en el mes actual pero hay atendidos históricos, suma los atendidos.
     */
    fun totalVendidoAtendidosMes(): Double {
        val db = dbHelper.readableDatabase
        // Primero intentamos filtrar por el año-mes actual (yyyy-MM)
        var total = 0.0
        val sqlMes = """
            SELECT COALESCE(SUM(${DBHelper.COL_PEDIDO_TOTAL}), 0.0)
            FROM ${DBHelper.TABLE_PEDIDO}
            WHERE ${DBHelper.COL_PEDIDO_ESTADO} = 'ATENDIDO'
              AND ${DBHelper.COL_PEDIDO_FECHA} LIKE strftime('%Y-%m', 'now') || '%'
        """.trimIndent()

        var cursor = db.rawQuery(sqlMes, null)
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()

        // Si es 0.0, sumar todos los pedidos atendidos registrados
        if (total == 0.0) {
            val sqlTotalAtendidos = """
                SELECT COALESCE(SUM(${DBHelper.COL_PEDIDO_TOTAL}), 0.0)
                FROM ${DBHelper.TABLE_PEDIDO}
                WHERE ${DBHelper.COL_PEDIDO_ESTADO} = 'ATENDIDO'
            """.trimIndent()
            cursor = db.rawQuery(sqlTotalAtendidos, null)
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0)
            }
            cursor.close()
        }

        return total
    }

    /**
     * HU-12 CA1: Retorna el conteo de pedidos según su estado ('ATENDIDO' o 'PENDIENTE')
     */
    fun contarPedidos(estado: String): Int {
        val db = dbHelper.readableDatabase
        var count = 0
        val sql = "SELECT COUNT(*) FROM ${DBHelper.TABLE_PEDIDO} WHERE ${DBHelper.COL_PEDIDO_ESTADO} = ?"
        val cursor = db.rawQuery(sql, arrayOf(estado))
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    /**
     * HU-12 CA2: Retorna el inventario de cada prenda para el reporte de stock
     */
    fun stockPorPrenda(): List<StockPrenda> {
        val lista = mutableListOf<StockPrenda>()
        val db = dbHelper.readableDatabase
        val sql = """
            SELECT ${DBHelper.COL_ROPA_ID}, ${DBHelper.COL_ROPA_MODELO}, ${DBHelper.COL_ROPA_CANTIDAD}
            FROM ${DBHelper.TABLE_ROPA}
            ORDER BY ${DBHelper.COL_ROPA_CANTIDAD} DESC, ${DBHelper.COL_ROPA_MODELO} ASC
        """.trimIndent()

        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val prenda = StockPrenda(
                    id = cursor.getInt(0),
                    modelo = cursor.getString(1) ?: "",
                    cantidad = cursor.getInt(2)
                )
                lista.add(prenda)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    /**
     * HU-12 CA3: Lista de clientes con el conteo de pedidos (COUNT + GROUP BY)
     * Soporta filtrado por nombre o teléfono para ClientesActivity
     */
    fun clientesConPedidos(filtro: String = ""): List<ClienteConPedidos> {
        val lista = mutableListOf<ClienteConPedidos>()
        val db = dbHelper.readableDatabase

        val filtroLimpio = "%${filtro.trim()}%"
        val sql = """
            SELECT c.${DBHelper.COL_CLIENTE_ID},
                   c.${DBHelper.COL_CLIENTE_NOMBRES},
                   c.${DBHelper.COL_CLIENTE_APELLIDOS},
                   c.${DBHelper.COL_CLIENTE_TELEFONO},
                   COUNT(p.${DBHelper.COL_PEDIDO_ID}) AS num_pedidos
            FROM ${DBHelper.TABLE_CLIENTE} c
            LEFT JOIN ${DBHelper.TABLE_PEDIDO} p ON c.${DBHelper.COL_CLIENTE_ID} = p.${DBHelper.COL_PEDIDO_ID_CLIENTE}
            WHERE c.${DBHelper.COL_CLIENTE_NOMBRES} LIKE ? 
               OR c.${DBHelper.COL_CLIENTE_APELLIDOS} LIKE ? 
               OR c.${DBHelper.COL_CLIENTE_TELEFONO} LIKE ?
            GROUP BY c.${DBHelper.COL_CLIENTE_ID}
            ORDER BY num_pedidos DESC, c.${DBHelper.COL_CLIENTE_NOMBRES} ASC
        """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(filtroLimpio, filtroLimpio, filtroLimpio))
        if (cursor.moveToFirst()) {
            do {
                val item = ClienteConPedidos(
                    id = cursor.getInt(0),
                    nombres = cursor.getString(1) ?: "",
                    apellidos = cursor.getString(2) ?: "",
                    telefono = cursor.getString(3) ?: "",
                    numPedidos = cursor.getInt(4)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}
