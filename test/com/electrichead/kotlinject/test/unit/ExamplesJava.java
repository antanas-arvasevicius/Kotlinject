package com.electrichead.kotlinject.test.unit;

import com.electrichead.kotlinject.test.unit.javastubs.*;
import com.electrichead.kotlinject.registration.Lifecycle;
import com.electrichead.kotlinject.test.unit.stubs.Bar;
import com.electrichead.kotlinject.test.unit.stubs.Foo;
import com.electrichead.kotlinject.test.unit.stubs.IBar;
import com.electrichead.kotlinject.test.unit.stubs.IFoo;
import org.junit.jupiter.api.*;
import com.electrichead.kotlinject.Container;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
public class ExamplesJava {

    @Test
    public void AutobindingToDefaultInterfaces() {
        var container = new Container();

        var foo = container.resolve(Far.class);
        var foo2 = container.resolve(IFar.class);

        assertNotNull(foo);
        assertNotNull(foo2);
    }

    @Test
    public void ExplicitBindings() {
        var container = new Container();
        container.registrations()
                .bind(Far.class, Far.class)
                .bind(Bas.class, Bas.class);

        var foo = container.resolve(Far.class);
        var foo2 = container.resolve(Bas.class);

        assertNotNull(foo);
        assertNotNull(foo2);
    }

    @Test
    public void BindingsToFactoryFunctions(){
        var container = new Container();

        container.registrations()
                .bind(Far.class, () -> new Far())
                .bind(Bas.class, () -> new Bas());

        var foo = container.resolve(Far.class);
        var foo2 = container.resolve(Bas.class);

        assertNotNull(foo);
        assertNotNull(foo2);
    }

    @Test
    public void ScanForAutoRegistrations() {
        var container = new Container();
        container.registrations().scan()
            .fromPackageContaining(Far.class, x -> x.bindAllInterfaces())
            .fromPackageContaining(Far.class, x -> x.bindClassesToSelf());

        var bar = (Far)container.resolve(Far.class);

        assertNotNull(bar);
    }

    @Test
    public void ContextualBindings() {
        var container = new Container();

        container.registrations()
                .bind(ConditionalBindingParent1.class)
                .bind(ConditionalBindingParent2.class)
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation1.class)
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation2.class,
                    x -> x.whenInjectedInto(ConditionalBindingParent2.class));

        var instance1 = (ConditionalBindingParent1) container.resolve(ConditionalBindingParent1.class);
        var instance2 = (ConditionalBindingParent2) container.resolve(ConditionalBindingParent2.class);

        assertEquals(ConditionalBindingImplementation1.class, instance1.getDep().getClass());
        assertEquals(ConditionalBindingImplementation2.class, instance2.getDep().getClass());
    }

    @Test
    public void ContextualBindingsUsingOnlyWhen() {
        var container = new Container();

        container.registrations()
                .bind(ConditionalBindingParent1.class)
                .bind(ConditionalBindingParent2.class)
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation1.class,
                    x -> x.onlyWhen(y -> y.getRootType().getSimpleName().equals("ConditionalBindingParent1")))
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation2.class,
                    x -> x.onlyWhen(y -> y.getRootType().getSimpleName().equals("ConditionalBindingParent2")));

        var instance1 = (ConditionalBindingParent1) container.resolve(ConditionalBindingParent1.class);
        var instance2 = (ConditionalBindingParent2) container.resolve(ConditionalBindingParent2.class);

        assertEquals(ConditionalBindingImplementation1.class, instance1.getDep().getClass());
        assertEquals(ConditionalBindingImplementation2.class, instance2.getDep().getClass());
    }

    @Test
    public void BindASingleton(){
        var container = new Container();

        container.registrations()
                .bind(Far.class, Far.class, Lifecycle.Singleton);

        var one = container.resolve(Far.class);
        var two = container.resolve(Far.class);

        assertEquals(one, two);
    }

    @Test
    public void RecommendedApproach (){
        var container = new Container();

        // Scan for everything
        container.registrations().scan()
            .fromPackageContaining(IFoo.class, x -> x.bindAllInterfaces())
            .fromPackageContaining(IFoo.class, x -> x.bindClassesToSelf());

        // Register factories for special cases
        container.registrations()
            .bind(Foo.class, () -> createFoo(), Lifecycle.Singleton)
            .bind(IBar.class, () -> new Bar());

        // Override bindings when you need to switch out implementations
        container.registrations()
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation1.class,
                        x -> x.whenInjectedInto(ConditionalBindingParent1.class))
                .bind(IConditionalBindingStub.class, ConditionalBindingImplementation2.class,
                        x -> x.whenInjectedInto(ConditionalBindingParent2.class));

        var bar = container.resolve(Bar.class);

        assertNotNull(bar);
    }

    private Foo createFoo() {
        return new Foo();
    }
}
