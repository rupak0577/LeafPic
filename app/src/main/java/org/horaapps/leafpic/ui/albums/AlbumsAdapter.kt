package org.horaapps.leafpic.ui.albums

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.horaapps.leafpic.CardViewStyle
import org.horaapps.leafpic.R
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.ui.base.ThemedListAdapter
import org.horaapps.leafpic.ui.base.interfaces.ActionsListener
import org.horaapps.leafpic.util.SparseBooleanArrayParcelable
import org.horaapps.leafpic.util.StringUtils
import org.horaapps.leafpic.util.preferences.Prefs
import org.horaapps.liz.ColorPalette
import org.horaapps.liz.Theme
import org.horaapps.liz.ThemeHelper
import org.horaapps.liz.ThemedViewHolder
import org.horaapps.liz.ui.ThemedIcon

class AlbumsAdapter(context: Context, private val actionsListener: ActionsListener,
                    var selectedItems: SparseBooleanArrayParcelable, var selectedCount: Int,
                    var isSelecting: Boolean)
    : ThemedListAdapter<Album, AlbumsAdapter.ViewHolder>(context) {

    private var placeholder: Drawable
    private var cardViewStyle: CardViewStyle

    init {
        placeholder = getThemeHelper()!!.placeHolder
        cardViewStyle = Prefs.getCardStyle()
    }

    fun getAlbum(pos: Int): Album {
        return getItem(pos)
    }

    override fun refreshTheme(themeHelper: ThemeHelper) {
        placeholder = themeHelper.placeHolder

        cardViewStyle = Prefs.getCardStyle()
        super.refreshTheme(themeHelper)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = when (cardViewStyle) {
            CardViewStyle.MATERIAL -> LayoutInflater.from(parent.context).inflate(R.layout.card_album_material, parent, false)
            CardViewStyle.FLAT -> LayoutInflater.from(parent.context).inflate(R.layout.card_album_flat, parent, false)
            CardViewStyle.COMPACT -> LayoutInflater.from(parent.context).inflate(R.layout.card_album_compact, parent, false)
        }
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        // TODO Calvin: Major Refactor - No business logic here.
        val (_, path, albumName, _, fileCount, albumInfo) = getItem(position)
        holder.refreshTheme(getThemeHelper(), cardViewStyle, selectedItems.get(position))

        val coverPath = albumInfo.coverPath

        val options = RequestOptions()
                //                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(placeholder)
                .error(R.drawable.ic_error)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

        Glide.with(holder.picture.context)
                .load(coverPath)
                .apply(options)
                .into(holder.picture)


        var accentColor = getThemeHelper()!!.accentColor

        if (accentColor == getThemeHelper()!!.primaryColor)
            accentColor = ColorPalette.getDarkerColor(accentColor)

        var textColor = getThemeHelper()!!.getColor(if (getThemeHelper()!!.baseTheme == Theme.LIGHT) R.color.md_album_color_2 else R.color.md_album_color)

        if (selectedItems.get(position))
            textColor = getThemeHelper()!!.getColor(R.color.md_album_color)

        holder.mediaLabel.setTextColor(textColor)

        holder.llCount.visibility = if (Prefs.showMediaCount()) View.VISIBLE else View.GONE
        holder.name.text = StringUtils.htmlFormat(albumName, textColor, false, true)
        holder.nMedia.text = StringUtils.htmlFormat(fileCount.toString(), accentColor, true, false)
        holder.path.visibility = if (Prefs.showAlbumPath()) View.VISIBLE else View.GONE
        holder.path.text = path

        //START Animation MAKES BUG ON FAST TAP ON CARD
        //Animation anim;
        //anim = AnimationUtils.loadAnimation(holder.albumCard.getContext(), R.anim.slide_fade_card);
        //holder.albumCard.startAnimation(anim);
        //ANIMS
        //holder.card.animate().alpha(1).setDuration(250);

        holder.card.setOnClickListener {
            if (selecting()) {
                notifySelected(position)
                notifyItemChanged(position)
            } else
                actionsListener.onItemSelected(position)
        }

        holder.card.setOnLongClickListener {
            notifySelected(position)
            notifyItemChanged(position)
            true
        }
    }

    fun invalidateSelectedCount() {
        var c = 0
        for (i in 0 until itemCount) {
            c += if (selectedItems.get(i)) 1 else 0
        }

        this.selectedCount = c

        if (this.selectedCount == 0)
            stopSelection()
        else {
            actionsListener.onSelectionCountChanged(selectedCount, itemCount)
        }
    }

    fun selecting(): Boolean {
        return isSelecting
    }

    private fun startSelection() {
        isSelecting = true
        actionsListener.onSelectMode(true)
    }

    private fun stopSelection() {
        isSelecting = false
        actionsListener.onSelectMode(false)
    }

    fun forceSelectedCount(count: Int) {
        selectedCount = count
    }

    fun clearSelected(): Boolean {
        var changed = true
        for (i in 0 until itemCount) {
            val b = setSelected(i, false)
            if (b)
                notifyItemChanged(i)
            changed = changed and b
        }

        selectedCount = 0
        stopSelection()
        return changed
    }

    private fun setSelected(index: Int, selected: Boolean): Boolean {
        if (selectedItems.get(index) == selected)
            return false
        selectedItems.put(index, selected)
        return true
    }

    fun selectAll() {
        for (i in 0 until itemCount) {
            if (setSelected(i, true))
                notifyItemChanged(i)
        }
        selectedCount = itemCount
        startSelection()
    }

    fun notifySelected(position: Int) {
        var increase = false
        if (selectedItems.get(position))
            selectedItems.put(position, false)
        else {
            selectedItems.put(position, true)
            increase = true
        }
        selectedCount += if (increase) 1 else -1
        actionsListener.onSelectionCountChanged(selectedCount, itemCount)

        if (selectedCount == 0 && isSelecting)
            stopSelection()
        else if (selectedCount > 0 && !isSelecting) startSelection()
    }

    class ViewHolder(itemView: View) : ThemedViewHolder(itemView) {

        @BindView(R.id.album_card)
        lateinit var card: CardView
        @BindView(R.id.album_preview)
        lateinit var picture: ImageView
        @BindView(R.id.selected_icon)
        lateinit var selectedIcon: ThemedIcon
        @BindView(R.id.ll_album_info)
        lateinit var footer: View
        @BindView(R.id.ll_media_count)
        lateinit var llCount: View
        @BindView(R.id.album_name)
        lateinit var name: TextView
        @BindView(R.id.album_media_count)
        lateinit var nMedia: TextView
        @BindView(R.id.album_media_label)
        lateinit var mediaLabel: TextView
        @BindView(R.id.album_path)
        lateinit var path: TextView

        init {
            ButterKnife.bind(this, itemView)
        }

        fun refreshTheme(theme: ThemeHelper?, cvs: CardViewStyle?, selected: Boolean) {
            if (selected) {
                footer.setBackgroundColor(theme!!.primaryColor)
                picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP)
                selectedIcon.visibility = View.VISIBLE
                selectedIcon.setColor(theme!!.primaryColor)
            } else {
                picture.clearColorFilter()
                selectedIcon.visibility = View.GONE
                when (cvs) {
                    CardViewStyle.MATERIAL -> footer.setBackgroundColor(theme!!.cardBackgroundColor)
                    CardViewStyle.FLAT, CardViewStyle.COMPACT -> footer.setBackgroundColor(ColorPalette.getTransparentColor(theme!!.backgroundColor, 150))
                    else -> footer.setBackgroundColor(theme!!.cardBackgroundColor)
                }
            }

            path.setTextColor(theme!!.subTextColor)
        }

        override fun refreshTheme(themeHelper: ThemeHelper) {

        }
    }
}