package com.electrichead.kotlinject.registration.packagescanning

import com.electrichead.kotlinject.registration.TypeRegistry
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass

class AutoDiscovery(typeRegistry: TypeRegistry) {
    private val registry = typeRegistry

    // Java
    fun fromPackageContaining(
        iface: java.lang.Class<*>,
        bindChoice: (op: BindingOperations) -> IBindingStrategy
    ): AutoDiscovery {
        return fromPackageContaining(iface.kotlin, bindChoice)
    }

    inline fun <reified T : Any> fromPackageContaining(noinline bindChoice: (op: BindingOperations) -> IBindingStrategy): AutoDiscovery {
        return fromPackageContaining(T::class, bindChoice)
    }

    fun fromClasspathWhere(where: (op: Collection<KClass<*>>) -> Boolean, bindChoice: (op: BindingOperations) -> IBindingStrategy): AutoDiscovery {
        val inspector = ClasspathInspector()
        val classes = inspector.allKnownClasses(where)
        bindChoice(BindingOperations()).bind(registry, classes)
        return this
    }

    fun fromPackageContaining(t: KClass<*>, bindChoice: (op: BindingOperations) -> IBindingStrategy): AutoDiscovery {
        val packageName = t.java.`package`.name
        val classes = getClasses(packageName)
        bindChoice(BindingOperations()).bind(registry, classes)
        return this
    }

    private fun getClasses(packageName: String): List<KClass<*>> {
        return ClassGraph().enableClassInfo().whitelistPackages(packageName).scan().use { result ->
            result.allClasses.loadClasses(true).map { it.kotlin }
        }
    }
}


