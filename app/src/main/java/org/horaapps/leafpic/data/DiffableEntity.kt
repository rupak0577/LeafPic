package org.horaapps.leafpic.data

interface DiffableEntity<T> {
    fun isSameAs(newItem: T) : Boolean
    fun hasSameContentAs(newItem: T) : Boolean
    fun diffWithAndGetChangePayload(newItem: T) : Any?
}