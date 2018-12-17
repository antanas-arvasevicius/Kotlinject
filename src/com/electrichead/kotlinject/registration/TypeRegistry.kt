package com.electrichead.kotlinject.registration

import com.electrichead.kotlinject.activation.MissingBindingException
import com.electrichead.kotlinject.registration.conditionalbinding.AlwaysMatches
import com.electrichead.kotlinject.registration.conditionalbinding.BindingConditions
import com.electrichead.kotlinject.registration.conditionalbinding.IBindingCondition
import com.electrichead.kotlinject.registration.packagescanning.AutoDiscovery
import com.electrichead.kotlinject.resolution.AutoDiscoveryResolver
import kotlin.reflect.KClass

class TypeRegistry {

    var autoDiscovery: Boolean = true
    val scan: AutoDiscovery = AutoDiscovery(this)

    private var _autoDiscovery = AutoDiscoveryResolver()
    private var _bindings = mutableMapOf<KClass<*>, MutableList<Binding>>()

    inline fun <reified T1 : Any, reified T2 : Any> bind(
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        noinline condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        return bind(T1::class, T2::class, lifecycle, condition)
    }

    inline fun <reified T1 : Any> bind(
        noinline function: () -> Any,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        noinline condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        return bind(T1::class, function, lifecycle, condition)
    }

    inline fun <reified T1 : Any> bindSelf(
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        noinline condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        return bind(T1::class, T1::class, lifecycle, condition)
    }

    fun bindSelf(
        self: KClass<*>,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        return bind(self, self, lifecycle, condition)
    }

    fun bind(
        iface: KClass<*>,
        impl: KClass<*>? = null,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        var target = impl

        if (target == null && !iface.isAbstract) {
            target = iface
        }

        if (!_bindings.containsKey(iface)) {
            _bindings[iface] = mutableListOf()
        }

        val binding = Binding(iface, target!!, lifecycle, condition(BindingConditions()))
        _bindings[iface]!!.add(binding)
        return this
    }

    fun bind(
        type: KClass<*>,
        function: () -> Any,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { AlwaysMatches() }
    ): TypeRegistry {

        if (!_bindings.containsKey(type)) {
            _bindings[type] = mutableListOf()
        }

        val binding = Binding(type, function, lifecycle, condition(BindingConditions()))
        _bindings[type]!!.add(binding)
        return this
    }

    fun retrieveBindingFor(requestedType: KClass<*>): List<Binding> {
        if (_bindings.containsKey(requestedType)) {
            return _bindings[requestedType]!!
        }

        if (autoDiscovery) {
            val type = _autoDiscovery.selectTypeFor(requestedType)
            return listOf(Binding(requestedType, type, Lifecycle.PerRequest))
        }

        throw MissingBindingException("No bindings found for: " + requestedType.qualifiedName)
    }

    // For Java
    fun bind(
        iface: java.lang.Class<*>,
        impl: java.lang.Class<*>? = null
    ): TypeRegistry {
        return bind(iface, impl, Lifecycle.PerRequest, condition = { AlwaysMatches() })
    }

    fun bind(
        iface: java.lang.Class<*>,
        impl: java.lang.Class<*>? = null,
        lifecycle: Lifecycle = Lifecycle.PerRequest
    ): TypeRegistry {
        return bind(iface, impl, lifecycle, condition = { AlwaysMatches() })
    }

    fun bind(
        iface: java.lang.Class<*>,
        impl: java.lang.Class<*>? = null,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        val ifaceK = iface.kotlin
        var implK = impl?.kotlin

        if (impl == null && !iface.isInterface) {
            implK = ifaceK
        }

        return bind(ifaceK, implK, lifecycle, condition)
    }

    fun bind(
        type: java.lang.Class<*>,
        function: () -> Any
    ): TypeRegistry {
        return bind(type.kotlin, function, Lifecycle.PerRequest, { AlwaysMatches() })
    }

    fun bind(
        type: java.lang.Class<*>,
        function: () -> Any,
        lifecycle: Lifecycle = Lifecycle.PerRequest
    ): TypeRegistry {
        return bind(type.kotlin, function, lifecycle, { AlwaysMatches() })
    }

    fun bind(
        type: java.lang.Class<*>,
        function: () -> Any,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { AlwaysMatches() }
    ): TypeRegistry {
        return bind(type.kotlin, function, lifecycle, condition)
    }

    fun bindSelf(
        self: java.lang.Class<*>
    ): TypeRegistry {
        return bindSelf(self, Lifecycle.PerRequest, condition = { c -> c.alwaysMatches() })
    }

    fun bindSelf(
        self: java.lang.Class<*>,
        lifecycle: Lifecycle = Lifecycle.PerRequest
    ): TypeRegistry {
        return bindSelf(self, lifecycle, condition = { c -> c.alwaysMatches() })
    }

    fun bindSelf(
        self: java.lang.Class<*>,
        lifecycle: Lifecycle = Lifecycle.PerRequest,
        condition: ((op: BindingConditions) -> IBindingCondition) = { c -> c.alwaysMatches() }
    ): TypeRegistry {
        return bind(self, self, lifecycle, condition)
    }
    // End Java friendly overloads

}