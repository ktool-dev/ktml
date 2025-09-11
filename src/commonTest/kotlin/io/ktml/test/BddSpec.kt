package io.ktml.test

import io.kotest.common.runBlocking
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecRootScope
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestScope
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class BddSpec(body: suspend BddSpec.() -> Unit = {}) : StringSpec(), BddSpecRootScope {
    final override val bddSpecCallState = BddSpecCallState()

    init {
        runBlocking { body() }
    }

    override suspend fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
    }
}

data class BddSpecCallState(
    var givenCalled: Boolean = false,
    var whenCalled: Boolean = false,
    var thenCalled: Boolean = false,
    var andCalled: Boolean = false,
    var expectCalled: Boolean = false,
) {
    fun clear() {
        givenCalled = false
        whenCalled = false
        thenCalled = false
        andCalled = false
        expectCalled = false
    }
}

interface BddSpecRootScope : StringSpecRootScope {
    /**
     * Adds a String Spec test using the default test case config.
     */
    override operator fun String.invoke(test: suspend StringSpecScope.() -> Unit) {
        addTest(TestName(null, this, false), false, null) {
            callTest(this, testCase, test)
        }
    }

    private suspend fun callTest(testScope: TestScope, testCase: TestCase, test: suspend StringSpecScope.() -> Unit) {
        bddSpecCallState.clear()

        StringSpecScope(testScope.coroutineContext, testCase).test()
        if (!thenCalled && !expectCalled) error("You should at least have a When/Then or Expect block in your test")
    }

    operator fun <T : ToValueList> String.invoke(vararg a: T, test: suspend StringSpecScope.(T) -> Unit) {
        val testTemplate = this
        addContainer(TestName(null, testTemplate, false), false, null) {
            a.map { data ->
                val values = data.toValueList().map {
                    when (it) {
                        null -> "null"
                        "" -> "\"\""
                        is KClass<*> -> it.simpleName ?: it.toString()
                        is KFunction<*> -> it.name
                        else -> it.toString()
                    }
                }
                var injectedValues = false
                var name = Regex("""\{(?!\d+})(.*?)}""").replace(testTemplate, "{}")
                values.forEachIndexed { i, v ->
                    if (name.contains("{${i + 1}")) {
                        injectedValues = true
                        name = name.replace("{${i + 1}}", v)
                    } else if (name.contains("{}")) {
                        injectedValues = true
                        name = name.replaceFirst("{}", v)
                    }
                }
                if (!injectedValues) {
                    name += values
                }
                registerTest(TestName(null, name, false), false, null) {
                    callTest(this, testCase) { test(data) }
                }
            }
        }
    }

    fun <A> row(a: A) = One(a)
    fun <A, B> row(a: A, b: B) = Two(a, b)
    fun <A, B, C> row(a: A, b: B, c: C) = Three(a, b, c)
    fun <A, B, C, D> row(a: A, b: B, c: C, d: D) = Four(a, b, c, d)
    fun <A, B, C, D, E> row(a: A, b: B, c: C, d: D, e: E) = Five(a, b, c, d, e)
    fun <A, B, C, D, E, F> row(a: A, b: B, c: C, d: D, e: E, f: F) = Six(a, b, c, d, e, f)
    fun <A, B, C, D, E, F, G> row(a: A, b: B, c: C, d: D, e: E, f: F, g: G) = Seven(a, b, c, d, e, f, g)
    fun <A, B, C, D, E, F, G, H> row(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H) = Eight(a, b, c, d, e, f, g, h)
    fun <A, B, C, D, E, F, G, H, I> row(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I) =
        Nine(a, b, c, d, e, f, g, h, i)

    fun <A, B, C, D, E, F, G, H, I, J> row(a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J) =
        Ten(a, b, c, d, e, f, g, h, i, j)

    interface ToValueList {
        fun toValueList(): List<Any?>
    }

    data class One<A>(val a: A) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a)
    }

    data class Two<A, B>(val a: A, val b: B) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b)
    }

    data class Three<A, B, C>(val a: A, val b: B, val c: C) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c)
    }

    data class Four<A, B, C, D>(val a: A, val b: B, val c: C, val d: D) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d)
    }

    data class Five<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e)
    }

    data class Six<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e, f)
    }

    data class Seven<A, B, C, D, E, F, G>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G) :
        ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e, f, g)
    }

    data class Eight<A, B, C, D, E, F, G, H>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E,
        val f: F,
        val g: G,
        val h: H,
    ) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e, f, g, h)
    }

    data class Nine<A, B, C, D, E, F, G, H, I>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E,
        val f: F,
        val g: G,
        val h: H,
        val i: I,
    ) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e, f, g, h, i)
    }

    data class Ten<A, B, C, D, E, F, G, H, I, J>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E,
        val f: F,
        val g: G,
        val h: H,
        val i: I,
        val j: J,
    ) : ToValueList {
        override fun toValueList(): List<Any?> = listOf(a, b, c, d, e, f, g, h, i)
    }

    val bddSpecCallState: BddSpecCallState
    private val givenCalled: Boolean get() = bddSpecCallState.givenCalled
    private val whenCalled: Boolean get() = bddSpecCallState.whenCalled
    private val thenCalled: Boolean get() = bddSpecCallState.thenCalled
    private val andCalled: Boolean get() = bddSpecCallState.andCalled
    private val expectCalled: Boolean get() = bddSpecCallState.expectCalled

    @Suppress("ktlint:standard:function-naming")
    fun Given(given: String) {
        if (whenCalled || thenCalled || expectCalled) error("Given should only be called before other blocks")
        if (givenCalled) error("You should only have one Given block in a test, if you want a second one use And")
        if (given.isNotBlank()) println("Given $given")
        bddSpecCallState.givenCalled = true
    }

    @Suppress("ktlint:standard:function-naming")
    fun When(`when`: String) {
        if (thenCalled || expectCalled) error("When should only be called before Then")
        if (whenCalled) error("You should only have one When block in a test, if you need another When you should write a another test")
        if (`when`.isNotBlank()) println("When $`when`")
        bddSpecCallState.whenCalled = true
    }

    @Suppress("ktlint:standard:function-naming")
    fun Then(then: String) {
        if (!whenCalled) error("You should have a When block before a Then block, or use an Expect block")
        if (thenCalled) error("You should only have one Then block in a test")
        if (then.isNotBlank()) println("Then $then")
        bddSpecCallState.thenCalled = true
    }

    @Suppress("ktlint:standard:function-naming")
    fun And(and: String) {
        if ((!givenCalled && !whenCalled) || expectCalled || thenCalled) error("And should only be called after a Given or When block")
        if (and.isNotBlank()) println("And $and")
        bddSpecCallState.andCalled = true
    }

    @Suppress("ktlint:standard:function-naming")
    fun Expect(expect: String) {
        if (thenCalled || whenCalled) error("You should either use When/Then or Expect, but not both")
        if (expectCalled) error("You should only have one Expect block in a test")
        if (expect.isNotBlank()) println("Expect $expect")
        bddSpecCallState.expectCalled = true
    }

    val Given: Unit get() = Given("")
    val When: Unit get() = When("")
    val Then: Unit get() = Then("")
    val Expect: Unit get() = Expect("")
    val And: Unit get() = And("")
}
