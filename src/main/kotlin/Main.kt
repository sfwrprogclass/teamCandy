package edu.teamcandy

import edu.teamcandy.services.exposed.init
import edu.teamcandy.services.exposed.startApiAndDatabase

fun main() {
    startApiAndDatabase()
    init()
    Thread.currentThread().join()
}