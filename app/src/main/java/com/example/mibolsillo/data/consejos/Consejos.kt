package com.example.mibolsillo.data.consejos

import java.time.LocalDate

/** Tema de un consejo (sirve para filtrar y para elegir el icono en la pantalla Consejos). */
enum class TemaConsejo(val etiqueta: String) {
    AHORRO("Ahorro"),
    PRESUPUESTO("Presupuesto"),
    GASTOS("Gastos inteligentes"),
    DEUDAS("Crédito y deudas"),
    FUTURO("Inversión y futuro"),
    SEGURIDAD("Seguridad")
}

data class Consejo(
    val id: Int,
    val tema: TemaConsejo,
    val titulo: String,
    val texto: String
)

/**
 * Contenido educativo de la app. Es contenido fijo (no cambia por usuario),
 * por eso vive en código y no en la base de datos.
 * Se usa en el "Consejo del día" de Inicio y en la pantalla Consejos.
 */
object Consejos {

    val lista: List<Consejo> = listOf(
        Consejo(
            1, TemaConsejo.PRESUPUESTO, "La regla 50/30/20",
            "Divide tus ingresos en tres partes: 50% para necesidades (transporte, comida, estudios), " +
                "30% para gustos (salidas, ropa, streaming) y 20% para ahorro o pagar deudas. " +
                "Si tus ingresos son pequeños, ajusta los porcentajes, pero nunca dejes el ahorro en 0%."
        ),
        Consejo(
            2, TemaConsejo.AHORRO, "Págate a ti primero",
            "Apenas recibas tu mesada o salario, separa lo que vas a ahorrar ANTES de gastar. " +
                "Si esperas a ver qué sobra a fin de mes, casi nunca sobra nada. " +
                "Aunque sea RD$ 200, la constancia importa más que la cantidad."
        ),
        Consejo(
            3, TemaConsejo.AHORRO, "Crea un fondo de emergencia",
            "Es dinero apartado solo para imprevistos: una consulta médica, un celular dañado o un mes sin ingresos. " +
                "La meta recomendada es cubrir de 3 a 6 meses de tus gastos básicos. " +
                "Empieza con una meta pequeña, como un mes de gastos, y guárdalo en una cuenta separada."
        ),
        Consejo(
            4, TemaConsejo.GASTOS, "Cuidado con los gastos hormiga",
            "Un café, un snack o un envío a domicilio parecen poco, pero sumados son mucho. " +
                "RD$ 150 diarios equivalen a unos RD$ 4,500 al mes. " +
                "Registra estos gastos en MiBolsillo durante dos semanas: te sorprenderá el total."
        ),
        Consejo(
            5, TemaConsejo.GASTOS, "La regla de las 48 horas",
            "Antes de una compra que no tenías planeada, espera 48 horas. " +
                "Si después de ese tiempo todavía la necesitas y cabe en tu presupuesto, cómprala. " +
                "La mayoría de las compras impulsivas pierden su atractivo en ese tiempo."
        ),
        Consejo(
            6, TemaConsejo.PRESUPUESTO, "Revisa tus suscripciones",
            "Streaming, música, juegos, almacenamiento en la nube… Haz una lista de todo lo que pagas cada mes " +
                "y cancela lo que no usaste en los últimos 30 días. Compartir planes familiares " +
                "con amigos o familia también reduce el costo."
        ),
        Consejo(
            7, TemaConsejo.DEUDAS, "Usa la tarjeta de crédito a tu favor",
            "Paga siempre el saldo TOTAL antes de la fecha límite. Si solo pagas el mínimo, " +
                "el resto genera intereses muy altos y la deuda crece mes a mes. " +
                "Una buena regla: no compres con tarjeta nada que no podrías pagar hoy en efectivo."
        ),
        Consejo(
            8, TemaConsejo.DEUDAS, "Método bola de nieve",
            "Si tienes varias deudas, paga el mínimo en todas y pon todo el dinero extra en la más pequeña. " +
                "Cuando la termines, usa ese dinero para la siguiente. " +
                "Ver deudas desaparecer te motiva a seguir. (Si prefieres ahorrar más intereses, " +
                "empieza por la de interés más alto: es el método avalancha)."
        ),
        Consejo(
            9, TemaConsejo.AHORRO, "Metas claras y con fecha",
            "\"Quiero ahorrar\" es difícil de cumplir; \"Quiero RD$ 15,000 para una laptop en 6 meses\" no. " +
                "Divide el monto entre los meses (RD$ 2,500/mes) y crea la meta en la sección Metas " +
                "para ver tu progreso."
        ),
        Consejo(
            10, TemaConsejo.GASTOS, "Lleva tu comida",
            "Comer fuera todos los días es uno de los gastos más grandes de un estudiante. " +
                "Preparar el almuerzo en casa aunque sea 3 días a la semana puede ahorrarte miles de pesos al mes. " +
                "Lleva también tu botella de agua."
        ),
        Consejo(
            11, TemaConsejo.GASTOS, "Aprovecha los descuentos de estudiante",
            "Muchas tiendas, software, transporte y servicios de streaming tienen precios especiales " +
                "para estudiantes. Pregunta siempre si existe uno y lleva tu carnet. " +
                "También compara precios en al menos dos lugares antes de comprar."
        ),
        Consejo(
            12, TemaConsejo.AHORRO, "Ahorra el dinero inesperado",
            "Cuando recibas dinero extra (un regalo, un bono, un trabajo freelance), " +
                "ahorra al menos la mitad. Como no contabas con él, no lo vas a extrañar " +
                "y tu meta avanzará mucho más rápido."
        ),
        Consejo(
            13, TemaConsejo.FUTURO, "El poder del interés compuesto",
            "Cuando tu dinero genera intereses y esos intereses también generan intereses, el crecimiento se acelera. " +
                "Por eso empezar joven es una ventaja enorme: ahorrar poco desde los 20 años puede rendir " +
                "más que ahorrar mucho desde los 40."
        ),
        Consejo(
            14, TemaConsejo.FUTURO, "Aprende antes de invertir",
            "No pongas dinero en algo que no entiendes. Antes de invertir, infórmate sobre certificados " +
                "financieros, cuentas de ahorro o fondos regulados, y verifica que la entidad esté " +
                "autorizada por el regulador financiero de tu país."
        ),
        Consejo(
            15, TemaConsejo.SEGURIDAD, "Desconfía de las ganancias fáciles",
            "Si alguien te promete duplicar tu dinero en poco tiempo, o te pide reclutar a otras personas " +
                "para ganar, probablemente sea una estafa o un esquema piramidal. " +
                "Ninguna inversión legítima garantiza ganancias altas sin riesgo."
        ),
        Consejo(
            16, TemaConsejo.SEGURIDAD, "Protege tus datos bancarios",
            "Tu banco nunca te pedirá tu clave, PIN o código de verificación por llamada, SMS o WhatsApp. " +
                "No compartas esos datos con nadie y activa las notificaciones de cada transacción " +
                "para detectar cargos que no reconoces."
        ),
        Consejo(
            17, TemaConsejo.PRESUPUESTO, "Revisa tus finanzas cada semana",
            "Dedica 10 minutos cada domingo a revisar tus movimientos y presupuestos en MiBolsillo. " +
                "Detectar a tiempo que gastaste de más en una categoría te permite corregir " +
                "antes de que termine el mes."
        ),
        Consejo(
            18, TemaConsejo.GASTOS, "Necesidad vs. deseo",
            "Antes de comprar, pregúntate: ¿lo necesito o lo quiero? Las necesidades (comida, transporte, " +
                "materiales de estudio) van primero. Los deseos no son malos, pero deben caber " +
                "en la parte de \"gustos\" de tu presupuesto."
        ),
        Consejo(
            19, TemaConsejo.AHORRO, "Automatiza tu ahorro",
            "Si tu banco lo permite, programa una transferencia automática a tu cuenta de ahorro " +
                "el mismo día que recibes ingresos. Lo que no ves en tu cuenta principal, no lo gastas."
        ),
        Consejo(
            20, TemaConsejo.DEUDAS, "Cuidado con los préstamos rápidos",
            "Los préstamos \"al instante\" por apps o prestamistas informales suelen tener costos " +
                "muy altos escondidos en comisiones y cargos por atraso. Antes de pedir prestado, " +
                "pregunta cuánto pagarás EN TOTAL, no solo la cuota."
        )
    )

    /** Un consejo distinto cada día (mismo consejo durante todo el día). */
    fun delDia(fecha: LocalDate = LocalDate.now()): Consejo = lista[fecha.dayOfYear % lista.size]
}
