package com.ucall.widget.lite

object PhoneFormatter {
    fun formatPhoneNumber(number: String): String {
        val clean = number.filter { it.isDigit() }

        return when {
            clean.length >= 12 -> {
                "+" +
                        clean.substring(0, 2) + " " + // код країни
                        clean.substring(2, 5) + " " + // оператор
                        clean.substring(5, 8) + " " + // блок 3
                        clean.substring(8, 10) + " " + // блок 2
                        clean.substring(10, 12) // блок 2
            }

            clean.length == 10 -> {
                clean.substring(0, 3) + " " +
                        clean.substring(3, 6) + " " +
                        clean.substring(6, 8) + " " +
                        clean.substring(8, 10)
            }

            else -> number // fallback
        }
    }
}
