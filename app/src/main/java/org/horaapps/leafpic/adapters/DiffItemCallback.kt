package org.horaapps.leafpic.adapters

import androidx.recyclerview.widget.DiffUtil
import org.horaapps.leafpic.data.DiffableEntity

class DiffItemCallback<T : DiffableEntity<T>> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.isSameAs(newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.hasSameContentAs(newItem)
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return oldItem.diffWithAndGetChangePayload(newItem)
    }
}