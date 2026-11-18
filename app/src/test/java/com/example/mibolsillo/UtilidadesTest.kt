package com.example.mibolsillo

import com.example.mibolsillo.ui.components.escalaRedonda
import com.example.mibolsillo.util.FormatoMoneda
import com.example.mibolsillo.util.Seguridad
import com.example.mibolsillo.util.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas unitarias de las utilidades (se ejecutan en la PC, sin emulador). */
class UtilidadesTest {

    @Test
    fun correo_valido_e_invalido() {
        assertNull(Validaciones.validarCorreo("ana@correo.com"))
        assertNotNull(Validaciones.validarCorreo(""))
        assertNotNull(Validaciones.validarCorreo("ana@correo"))
        assertNotNull(Validaciones.validarCorreo("anacorreo.com"))
    }

    @Test
    fun password_minimo_6_caracteres() {
        assertNotNull(Validaciones.validarPassword("12345"))
        assertNull(Validaciones.validarPassword("123456"))
    }

    @Test
    fun confirmacion_debe_coincidir() {
        assertNull(Validaciones.validarConfirmacion("secreto1", "secreto1"))
        assertNotNull(Validaciones.validarConfirmacion("secreto1", "secreto2"))
    }

    @Test
    fun hash_es_sha256_y_depende_del_correo() {
        val h1 = Seguridad.hashPassword("secreto1", "ana@correo.com")
        val h2 = Seguridad.hashPassword("secreto1", "ANA@correo.com ")
        val h3 = Seguridad.hashPassword("secreto1", "luis@correo.com")
        assertEquals(64, h1.length)   // SHA-256 = 32 bytes = 64 caracteres hex
        assertEquals(h1, h2)          // el correo se normaliza
        assertNotEquals(h1, h3)       // misma contraseña, distinto usuario -> distinto hash
    }

    @Test
    fun formato_moneda_con_separador_de_miles() {
        assertEquals("RD$ 1,250.50", FormatoMoneda.formatear(1250.5))
        assertEquals("RD$ 0.00", FormatoMoneda.formatear(0.0))
        assertEquals("-RD$ 1,000,000.00", FormatoMoneda.formatear(-1_000_000.0))
    }

    @Test
    fun formato_compacto_para_ejes() {
        assertEquals("950", FormatoMoneda.compacto(950.0))
        assertEquals("1.5k", FormatoMoneda.compacto(1500.0))
        assertEquals("10k", FormatoMoneda.compacto(10_000.0))
        assertEquals("2.3M", FormatoMoneda.compacto(2_300_000.0))
    }

    @Test
    fun escala_del_eje_redondeada() {
        assertEquals(5000f, escalaRedonda(3700f), 0.01f)
        assertEquals(2000f, escalaRedonda(1200f), 0.01f)
        assertEquals(10000f, escalaRedonda(6000f), 0.01f)
        assertEquals(1000f, escalaRedonda(0f), 0.01f) // sin datos: escala por defecto
    }

    @Test
    fun parsear_montos() {
        assertEquals(1500.5, Validaciones.parsearMonto("1,500.50")!!, 0.001)
        assertEquals(1500.5, Validaciones.parsearMonto("1500,5")!!, 0.001)
        assertNull(Validaciones.parsearMonto("abc"))
        assertNotNull(Validaciones.validarMonto("0"))
    }
}
