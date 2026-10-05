package com.lkhealth.healthcabinui.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 轻量自研路由：维护一个可回退的 Route 栈，不引入外部导航库。 */
class Navigator(start: Route = Route.Welcome) {

    private val _backStack = MutableStateFlow(listOf(start))
    val backStack: StateFlow<List<Route>> = _backStack.asStateFlow()

    val current: Route get() = _backStack.value.last()

    fun push(route: Route) {
        _backStack.update { it + route }
    }

    fun pop(): Boolean {
        val stack = _backStack.value
        if (stack.size <= 1) return false
        _backStack.value = stack.dropLast(1)
        return true
    }

    /** 用新页面替换当前页面，不留下可回退的历史记录（用于过渡态/结果态页面）。 */
    fun replaceTop(route: Route) {
        _backStack.update { it.dropLast(1) + route }
    }

    /** 清空回退栈，回到某个"枢纽"页面（欢迎页、项目选择页）。 */
    fun resetTo(route: Route) {
        _backStack.value = listOf(route)
    }
}
