package com.pij.horrocks

class SysoutLogger : Logger {

    override fun <T : Any?> print(aClass: Class<T>, message: String, e: Throwable) {
        print(javaClass, message)
        e.printStackTrace()
    }

    override fun <T> print(javaClass: Class<T>, message: String) {
        System.out.println(javaClass.toString() + " " + message)
    }
}