package ru.hollowhorizon.hc.common.objects.molang

import ru.hollowhorizon.hc.common.utils.molang.Molang
import kotlin.system.measureTimeMillis

fun main() {
    println(measureTimeMillis {
        (0..10000).forEach {
            Molang.compileBoolean("${it} > ${100-it}")
        }
    })
}