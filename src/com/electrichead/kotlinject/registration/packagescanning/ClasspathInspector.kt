package com.electrichead.kotlinject.registration.packagescanning

import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass

class ClasspathInspector {
    fun allKnownClasses(where: (op: Collection<KClass<*>>) -> Boolean): List<KClass<*>> {
        return ClassGraph().enableClassInfo().scan().use { result ->
            result.allClasses.loadClasses(true).map { it.kotlin }
        }
    }
}