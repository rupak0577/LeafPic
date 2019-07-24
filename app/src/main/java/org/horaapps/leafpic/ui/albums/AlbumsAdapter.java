package org.horaapps.leafpic.ui.albums;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.horaapps.leafpic.CardViewStyle;
import org.horaapps.leafpic.R;
import org.horaapps.leafpic.ui.base.ThemedListAdapter;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.ui.base.interfaces.ActionsListener;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.liz.ColorPalette;
import org.horaapps.liz.Theme;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedViewHolder;
import org.horaapps.liz.ui.ThemedIcon;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends ThemedListAdapter<Album, AlbumsAdapter.ViewHolder> {

    private Drawable placeholder;
    private CardViewStyle cardViewStyle;

    private ActionsListener actionsListener;

    public AlbumsAdapter(Context context, ActionsListener actionsListener) {
        super(context);
        placeholder = getThemeHelper().getPlaceHolder();
        cardViewStyle = Prefs.getCardStyle();
        this.actionsListener = actionsListener;
    }

    public Album get(int pos) {
        return getItem(pos);
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        placeholder = theme.getPlaceHolder();

        cardViewStyle = Prefs.getCardStyle();
        super.refreshTheme(theme);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (cardViewStyle) {
            default:
            case MATERIAL: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_material, parent, false); break;
            case FLAT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_flat, parent, false); break;
            case COMPACT: v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album_compact, parent, false); break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        // TODO Calvin: Major Refactor - No business logic here.
        AlbumsFragment listener = (AlbumsFragment) actionsListener;
        boolean isSelected = listener.selected.get(position);
        Album a = getItem(position);
        holder.refreshTheme(getThemeHelper(), cardViewStyle, isSelected);

        String coverPath = a.getAlbumInfo().getCoverPath();

        RequestOptions options = new RequestOptions()
//                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .placeholder(placeholder)
                .error(org.horaapps.leafpic.R.drawable.ic_error)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(holder.picture.getContext())
                .load(coverPath)
                .apply(options)
                .into(holder.picture);


        int accentColor = getThemeHelper().getAccentColor();

        if (accentColor == getThemeHelper().getPrimaryColor())
            accentColor = ColorPalette.getDarkerColor(accentColor);

        int textColor = getThemeHelper().getColor(getThemeHelper().getBaseTheme().equals(Theme.LIGHT) ? R.color.md_album_color_2 : R.color.md_album_color);

        if (isSelected)
            textColor = getThemeHelper().getColor(R.color.md_album_color);

        holder.mediaLabel.setTextColor(textColor);

        holder.llCount.setVisibility(Prefs.showMediaCount() ? View.VISIBLE : View.GONE);
        holder.name.setText(StringUtils.htmlFormat(a.getAlbumName(), textColor, false, true));
        holder.nMedia.setText(StringUtils.htmlFormat(String.valueOf(a.getFileCount()), accentColor, true, false));
        holder.path.setVisibility(Prefs.showAlbumPath() ? View.VISIBLE : View.GONE);
        holder.path.setText(a.getPath());

        //START Animation MAKES BUG ON FAST TAP ON CARD
        //Animation anim;
        //anim = AnimationUtils.loadAnimation(holder.albumCard.getContext(), R.anim.slide_fade_card);
        //holder.albumCard.startAnimation(anim);
        //ANIMS
        //holder.card.animate().alpha(1).setDuration(250);

        holder.card.setOnClickListener(v -> {
            if (listener.selecting()) {
                listener.notifySelected(position);
                notifyItemChanged(position);
            } else
                actionsListener.onItemSelected(position);
        });

        holder.card.setOnLongClickListener(v -> {
            listener.notifySelected(position);
            notifyItemChanged(position);
            return true;
        });
    }

    static class ViewHolder extends ThemedViewHolder {

        @BindView(R.id.album_card) CardView card;
        @BindView(R.id.album_preview) ImageView picture;
        @BindView(R.id.selected_icon)
        ThemedIcon selectedIcon;
        @BindView(R.id.ll_album_info) View footer;
        @BindView(R.id.ll_media_count) View llCount;
        @BindView(R.id.album_name) TextView name;
        @BindView(R.id.album_media_count) TextView nMedia;
        @BindView(R.id.album_media_label) TextView mediaLabel;
        @BindView(R.id.album_path) TextView path;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void refreshTheme(ThemeHelper theme, CardViewStyle cvs, boolean selected) {

            if (selected) {
                footer.setBackgroundColor(theme.getPrimaryColor());
                picture.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                selectedIcon.setVisibility(View.VISIBLE);
                selectedIcon.setColor(theme.getPrimaryColor());
            } else {
                picture.clearColorFilter();
                selectedIcon.setVisibility(View.GONE);
                switch (cvs) {
                    default: case MATERIAL:
                        footer.setBackgroundColor(theme.getCardBackgroundColor());
                        break;
                    case FLAT: case COMPACT:
                        footer.setBackgroundColor(ColorPalette.getTransparentColor(theme.getBackgroundColor(), 150));
                        break;
                }
            }

            path.setTextColor(theme.getSubTextColor());
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {

        }
    }
}