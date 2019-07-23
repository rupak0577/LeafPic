package org.horaapps.leafpic.adapters

import android.content.Context
import androidx.recyclerview.widget.ListAdapter
import org.horaapps.leafpic.data.DiffableEntity
import org.horaapps.liz.ThemeHelper
import org.horaapps.liz.Themed
import org.horaapps.liz.ThemedViewHolder

abstract class ThemedListAdapter<T : DiffableEntity<T>, VH : ThemedViewHolder>(context: Context)
    : ListAdapter<T, VH>(DiffItemCallback<T>()), Themed {

    private var themeHelper: ThemeHelper? = null

    init {
        themeHelper = ThemeHelper.getInstanceLoaded(context)
    }

    fun getThemeHelper(): ThemeHelper? {
        return themeHelper
    }

    fun setThemeHelper(themeHelper: ThemeHelper) {
        this.themeHelper = themeHelper
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.refreshTheme(getThemeHelper())
    }

    override fun refreshTheme(themeHelper: ThemeHelper) {
        setThemeHelper(themeHelper)
        notifyDataSetChanged()
    }
}