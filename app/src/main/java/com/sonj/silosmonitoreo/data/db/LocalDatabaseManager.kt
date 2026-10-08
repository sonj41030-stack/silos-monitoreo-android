package com.sonj.silosmonitoreo.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sonj.silosmonitoreo.model.RolUsuario
import com.sonj.silosmonitoreo.model.Usuario
import java.time.LocalDate
import java.util.UUID

class LocalDatabaseManager(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "silos_monitoreo.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USUARIOS = "usuarios"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_FECHA_NAC = "fecha_nacimiento"
        private const val COLUMN_ROL = "rol"
        private const val COLUMN_TOKEN = "token"

        @Volatile
        private var instance: LocalDatabaseManager? = null

        fun getInstance(context: Context): LocalDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabaseManager(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
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
        insertarUsuariosPredeterminados(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    private fun insertarUsuariosPredeterminados(db: SQLiteDatabase) {
        // Operador
        insertarUsuarioDirecto(
            db,
            id = "u1",
            nombre = "Carlos Operador",
            email = "operador@silos.com",
            password = "operador123",
            fechaNac = "1995-05-15",
            rol = RolUsuario.OPERADOR.name
        )
        // Administrador
        insertarUsuarioDirecto(
            db,
            id = "u2",
            nombre = "Ana Administradora",
            email = "admin@silos.com",
            password = "admin123",
            fechaNac = "1988-10-20",
            rol = RolUsuario.ADMINISTRADOR.name
        )
        // Jefatura
        insertarUsuarioDirecto(
            db,
            id = "u3",
            nombre = "Roberto Jefe",
            email = "jefe@silos.com",
            password = "jefe123",
            fechaNac = "1980-02-01",
            rol = RolUsuario.JEFATURA.name
        )
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
            TABLE_USUARIOS,
            null,
            "$COLUMN_EMAIL = ?",
            arrayOf(email.lowercase().trim()),
            null,
            null,
            null
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getString(c.getColumnIndexOrThrow(COLUMN_ID))
                val nombre = c.getString(c.getColumnIndexOrThrow(COLUMN_NOMBRE))
                val mail = c.getString(c.getColumnIndexOrThrow(COLUMN_EMAIL))
                val pwd = c.getString(c.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val fechaNacStr = c.getString(c.getColumnIndexOrThrow(COLUMN_FECHA_NAC))
                val rolStr = c.getString(c.getColumnIndexOrThrow(COLUMN_ROL))
                val token = c.getString(c.getColumnIndexOrThrow(COLUMN_TOKEN))

                val rol = RolUsuario.entries.firstOrNull { it.name == rolStr } ?: RolUsuario.OPERADOR
                val fechaNac = LocalDate.parse(fechaNacStr)

                return Usuario(
                    id = id,
                    nombre = nombre,
                    email = mail,
                    contrasena = pwd,
                    fechaNacimiento = fechaNac,
                    rol = rol,
                    tokenSesion = token
                )
            }
        }
        return null
    }

    fun actualizarTokenUsuario(email: String, token: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TOKEN, token)
        }
        db.update(TABLE_USUARIOS, values, "$COLUMN_EMAIL = ?", arrayOf(email.lowercase().trim()))
    }
}
