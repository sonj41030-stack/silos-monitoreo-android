package com.sonj.silosmonitoreo.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sonj.silosmonitoreo.model.Alerta
import com.sonj.silosmonitoreo.model.EstadoSilo
import com.sonj.silosmonitoreo.model.Movimiento
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.TipoMovimiento
import com.sonj.silosmonitoreo.model.Usuario
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class LocalDatabaseManager(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "silos_monitoreo.db"
        private const val DATABASE_VERSION = 3   // Incrementado a versión 3 por la tabla movimientos

        // ── Tabla: usuarios ──────────────────────────────────────────────
        private const val TABLE_USUARIOS = "usuarios"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_FECHA_NAC = "fecha_nacimiento"
        private const val COLUMN_ROL = "rol"
        private const val COLUMN_TOKEN = "token"

        // ── Tabla: movimientos ────────────────────────────────────────────
        private const val TABLE_MOVIMIENTOS = "movimientos"
        private const val MOV_ID = "id"
        private const val MOV_SILO_ID = "silo_id"
        private const val MOV_SILO_NOMBRE = "silo_nombre"
        private const val MOV_GRANJA_NOMBRE = "granja_nombre"
        private const val MOV_TIPO = "tipo"
        private const val MOV_CANTIDAD = "cantidad_toneladas"
        private const val MOV_FECHA_HORA = "fecha_hora"
        private const val MOV_RESPONSABLE = "responsable"
        private const val MOV_NOTAS = "notas"

        @Volatile
        private var instance: LocalDatabaseManager? = null

        fun getInstance(context: Context): LocalDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabaseManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // ── Creación de tablas ───────────────────────────────────────────────
    override fun onCreate(db: SQLiteDatabase) {
        // Tabla usuarios
        val createUsuariosTable = """
            CREATE TABLE $TABLE_USUARIOS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_FECHA_NAC TEXT NOT NULL,
                $COLUMN_ROL TEXT NOT NULL,
                $COLUMN_TOKEN TEXT
            )
        """.trimIndent()
        db.execSQL(createUsuariosTable)

        // Tabla movimientos (persistencia de entradas y salidas de grano)
        val createMovimientosTable = """
            CREATE TABLE $TABLE_MOVIMIENTOS (
                $MOV_ID TEXT PRIMARY KEY,
                $MOV_SILO_ID TEXT NOT NULL,
                $MOV_SILO_NOMBRE TEXT NOT NULL,
                $MOV_GRANJA_NOMBRE TEXT NOT NULL,
                $MOV_TIPO TEXT NOT NULL,
                $MOV_CANTIDAD INTEGER NOT NULL,
                $MOV_FECHA_HORA TEXT NOT NULL,
                $MOV_RESPONSABLE TEXT NOT NULL,
                $MOV_NOTAS TEXT
            )
        """.trimIndent()
        db.execSQL(createMovimientosTable)

        insertarUsuariosPredeterminados(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Agrega la tabla movimientos si se viene de una versión anterior sin ella
        if (oldVersion < 3) {
            val createMovimientosTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_MOVIMIENTOS (
                    $MOV_ID TEXT PRIMARY KEY,
                    $MOV_SILO_ID TEXT NOT NULL,
                    $MOV_SILO_NOMBRE TEXT NOT NULL,
                    $MOV_GRANJA_NOMBRE TEXT NOT NULL,
                    $MOV_TIPO TEXT NOT NULL,
                    $MOV_CANTIDAD INTEGER NOT NULL,
                    $MOV_FECHA_HORA TEXT NOT NULL,
                    $MOV_RESPONSABLE TEXT NOT NULL,
                    $MOV_NOTAS TEXT
                )
            """.trimIndent()
            db.execSQL(createMovimientosTable)
        }
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
            onCreate(db)
        }
    }

    // ── Usuarios predeterminados ─────────────────────────────────────────
    private fun insertarUsuariosPredeterminados(db: SQLiteDatabase) {
        insertarUsuarioDirecto(db, "u1", "Carlos Operador", "operador@silos.com", "operador123", "1995-05-15", RolUsuario.OPERADOR.name)
        insertarUsuarioDirecto(db, "u2", "Ana Administradora", "admin@silos.com", "admin123", "1988-10-20", RolUsuario.ADMINISTRADOR.name)
        insertarUsuarioDirecto(db, "u3", "Roberto Jefe", "jefe@silos.com", "jefe123", "1980-02-01", RolUsuario.JEFATURA.name)
    }

    private fun insertarUsuarioDirecto(
        db: SQLiteDatabase,
        id: String,
        nombre: String,
        email: String,
        password: String,
        fechaNac: String,
        rol: String
    ) {
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_NOMBRE, nombre)
            put(COLUMN_EMAIL, email.lowercase())
            put(COLUMN_PASSWORD, password)
            put(COLUMN_FECHA_NAC, fechaNac)
            put(COLUMN_ROL, rol)
            put(COLUMN_TOKEN, "token_simulado_${UUID.randomUUID()}")
        }
        db.insert(TABLE_USUARIOS, null, values)
    }

    // ── CRUD Usuarios ────────────────────────────────────────────────────
    fun guardarUsuario(usuario: Usuario): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, usuario.id)
            put(COLUMN_NOMBRE, usuario.nombre)
            put(COLUMN_EMAIL, usuario.email.lowercase())
            put(COLUMN_PASSWORD, usuario.contrasena)
            put(COLUMN_FECHA_NAC, usuario.fechaNacimiento.toString())
            put(COLUMN_ROL, usuario.rol.name)
            put(COLUMN_TOKEN, usuario.tokenSesion)
        }
        val result = db.insertWithOnConflict(TABLE_USUARIOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    fun buscarUsuarioPorEmail(email: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIOS, null,
            "$COLUMN_EMAIL = ?", arrayOf(email.lowercase().trim()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val rol = RolUsuario.entries.firstOrNull {
                    it.name == c.getString(c.getColumnIndexOrThrow(COLUMN_ROL))
                } ?: RolUsuario.OPERADOR
                return Usuario(
                    id = c.getString(c.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = c.getString(c.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    email = c.getString(c.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    contrasena = c.getString(c.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    fechaNacimiento = LocalDate.parse(c.getString(c.getColumnIndexOrThrow(COLUMN_FECHA_NAC))),
                    rol = rol,
                    tokenSesion = c.getString(c.getColumnIndexOrThrow(COLUMN_TOKEN))
                )
            }
        }
        return null
    }

    fun actualizarTokenUsuario(email: String, token: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put(COLUMN_TOKEN, token) }
        db.update(TABLE_USUARIOS, values, "$COLUMN_EMAIL = ?", arrayOf(email.lowercase().trim()))
    }

    // ── CRUD Movimientos ─────────────────────────────────────────────────

    /** Guarda un movimiento de grano (entrada o salida) en la base de datos. */
    fun guardarMovimiento(movimiento: Movimiento): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MOV_ID, movimiento.id)
            put(MOV_SILO_ID, movimiento.siloId)
            put(MOV_SILO_NOMBRE, movimiento.siloNombre)
            put(MOV_GRANJA_NOMBRE, movimiento.granjaNombre)
            put(MOV_TIPO, movimiento.tipo.name)
            put(MOV_CANTIDAD, movimiento.cantidadToneladas)
            put(MOV_FECHA_HORA, movimiento.fechaHora.toString())
            put(MOV_RESPONSABLE, movimiento.responsable)
            put(MOV_NOTAS, movimiento.notas)
        }
        val result = db.insertWithOnConflict(TABLE_MOVIMIENTOS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return result != -1L
    }

    /** Recupera todos los movimientos almacenados, ordenados del más reciente al más antiguo. */
    fun obtenerTodosLosMovimientos(): List<Movimiento> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MOVIMIENTOS, null, null, null, null, null,
            "$MOV_FECHA_HORA DESC"
        )
        val lista = mutableListOf<Movimiento>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val tipo = TipoMovimiento.entries.firstOrNull {
                    it.name == c.getString(c.getColumnIndexOrThrow(MOV_TIPO))
                } ?: TipoMovimiento.ENTRADA
                lista.add(
                    Movimiento(
                        id = c.getString(c.getColumnIndexOrThrow(MOV_ID)),
                        siloId = c.getString(c.getColumnIndexOrThrow(MOV_SILO_ID)),
                        siloNombre = c.getString(c.getColumnIndexOrThrow(MOV_SILO_NOMBRE)),
                        granjaNombre = c.getString(c.getColumnIndexOrThrow(MOV_GRANJA_NOMBRE)),
                        tipo = tipo,
                        cantidadToneladas = c.getInt(c.getColumnIndexOrThrow(MOV_CANTIDAD)),
                        fechaHora = LocalDateTime.parse(c.getString(c.getColumnIndexOrThrow(MOV_FECHA_HORA))),
                        responsable = c.getString(c.getColumnIndexOrThrow(MOV_RESPONSABLE)),
                        notas = c.getString(c.getColumnIndexOrThrow(MOV_NOTAS)) ?: ""
                    )
                )
            }
        }
        return lista
    }

    /** Recupera los movimientos filtrados por silo. */
    fun obtenerMovimientosPorSilo(siloId: String): List<Movimiento> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_MOVIMIENTOS, null,
            "$MOV_SILO_ID = ?", arrayOf(siloId),
            null, null, "$MOV_FECHA_HORA DESC"
        )
        val lista = mutableListOf<Movimiento>()
        cursor.use { c ->
            while (c.moveToNext()) {
                val tipo = TipoMovimiento.entries.firstOrNull {
                    it.name == c.getString(c.getColumnIndexOrThrow(MOV_TIPO))
                } ?: TipoMovimiento.ENTRADA
                lista.add(
                    Movimiento(
                        id = c.getString(c.getColumnIndexOrThrow(MOV_ID)),
                        siloId = c.getString(c.getColumnIndexOrThrow(MOV_SILO_ID)),
                        siloNombre = c.getString(c.getColumnIndexOrThrow(MOV_SILO_NOMBRE)),
                        granjaNombre = c.getString(c.getColumnIndexOrThrow(MOV_GRANJA_NOMBRE)),
                        tipo = tipo,
                        cantidadToneladas = c.getInt(c.getColumnIndexOrThrow(MOV_CANTIDAD)),
                        fechaHora = LocalDateTime.parse(c.getString(c.getColumnIndexOrThrow(MOV_FECHA_HORA))),
                        responsable = c.getString(c.getColumnIndexOrThrow(MOV_RESPONSABLE)),
                        notas = c.getString(c.getColumnIndexOrThrow(MOV_NOTAS)) ?: ""
                    )
                )
            }
        }
        return lista
    }

    /** Elimina todos los movimientos (útil para testing). */
    fun eliminarTodosLosMovimientos() {
        writableDatabase.delete(TABLE_MOVIMIENTOS, null, null)
    }
}

