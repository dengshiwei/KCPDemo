package com.explore.kcpdemo

object TestTarget {

    fun simpleFunction() {
        println("Inside simpleFunction")
    }

    fun sumFunction(a: Int, b: Int): Int {
        println("Inside sumFunction")
        return a + b
    }

    fun nestedFunction() {
        println("Inside nestedFunction start")
        helperFunction()
        println("Inside nestedFunction end")
    }

    private fun helperFunction() {
        println("Inside helperFunction")
    }
}