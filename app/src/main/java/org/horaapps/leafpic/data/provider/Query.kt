package org.horaapps.leafpic.data.provider

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import java.util.*

class Query private constructor(builder: Builder) {

    private val uri: Uri
    private var projection: Array<String>? = null
    private var selection: String? = null
    private var args: Array<String>? = null
    private var sort: String? = null
    private var ascending: Boolean = false
    private var limit: Int = -1

    init {
        uri = builder.uri
        projection = builder.projection
        selection = builder.selection
        args = builder.args?.toTypedArray()
        sort = builder.sort
        ascending = builder.ascending
        limit = builder.limit
    }

    fun getCursor(cr: ContentResolver): Cursor? {
        return cr.query(uri, projection, selection, args, hack())
    }

    private fun hack(): String? {
        if (sort == null && limit == -1) return null

        val builder = StringBuilder()
        // Sorting by Relative Position
        // ORDER BY 1
        // sort by the first column in the PROJECTION
        // otherwise the LIMIT should not work
        if (sort != null)
            builder.append(sort)
        else
            builder.append(1)

        builder.append(" ")

        if (!ascending)
            builder.append("DESC").append(" ")

        if (limit != -1)
            builder.append("LIMIT").append(" ").append(limit)

        return builder.toString()
    }

    class Builder {
        internal lateinit var uri: Uri
        internal var projection: Array<String>? = null
        internal var selection: String? = null
        internal var args: MutableList<String>? = null
        internal var sort: String? = null
        internal var limit = -1
        internal var ascending = false

        fun uri(value: Uri): Builder {
            uri = value
            return this
        }

        fun projection(value: Array<String>): Builder {
            projection = value
            return this
        }

        fun selection(value: String): Builder {
            selection = value
            return this
        }

        fun args(vararg value: String): Builder {
            if (args == null)
                args = mutableListOf()
            for (v in value)
                args?.add(v)
            return this
        }

        fun sort(value: String): Builder {
            sort = value
            return this
        }

        fun limit(value: Int): Builder {
            limit = value
            return this
        }

        fun ascending(value: Boolean): Builder {
            ascending = value
            return this
        }

        fun build(): Query {
            return Query(this)
        }
    }

    override fun toString(): String {
        return "Query{" +
                "\nuri=" + uri +
                "\nprojection=" + Arrays.toString(projection) +
                "\nselection='" + selection + '\''.toString() +
                "\nargs=" + Arrays.toString(args) +
                "\nsortMode='" + sort + '\''.toString() +
                "\nascending='" + ascending + '\''.toString() +
                "\nlimit='" + limit + '\''.toString() +
                '}'.toString()
    }
}