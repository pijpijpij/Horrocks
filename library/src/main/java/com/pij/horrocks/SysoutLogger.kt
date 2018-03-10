/*
 * Copyright 2018, Chiswick Forest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.pij.horrocks

class SysoutLogger : Logger {
    override fun print(javaClass: Class<*>, messageTemplate: String, vararg args: Any?) {
        print(javaClass, messageTemplate.format(*args))
    }

    override fun print(javaClass: Class<*>, e: Throwable, messageTemplate: String, vararg args: Any?) {
        print(javaClass, messageTemplate.format(*args), e)
    }

    override fun print(javaClass: Class<*>, message: String, e: Throwable) {
        print(javaClass, message)
        e.printStackTrace()
    }

    override fun print(javaClass: Class<*>, message: String) {
        println(javaClass.toString() + " " + message)
    }
}