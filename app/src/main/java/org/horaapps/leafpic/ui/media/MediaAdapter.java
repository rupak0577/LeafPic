package org.horaapps.leafpic.ui.media;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.ui.base.ThemedListAdapter;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.ui.base.interfaces.ActionsListener;
import org.horaapps.leafpic.util.SparseBooleanArrayParcelable;
import org.horaapps.leafpic.views.SquareRelativeLayout;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedViewHolder;
import org.horaapps.liz.ui.ThemedIcon;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter used to display Media Items.
 *
 * TODO: This class needs a major cleanup. Remove code from onBindViewHolder!
 */
public class MediaAdapter extends ThemedListAdapter<Media, MediaAdapter.ViewHolder> {
    public SparseBooleanArrayParcelable selectedItems;

    public int selectedCount = 0;
    public int lastSelectedPosition = -1;

    private Drawable placeholder;
    private final ActionsListener actionsListener;

    public boolean isSelecting = false;

    public MediaAdapter(Context context, ActionsListener actionsListener, SparseBooleanArrayParcelable selectedItems) {
        super(context);
        placeholder = getThemeHelper().getPlaceHolder();
        this.actionsListener = actionsListener;
        this.selectedItems = selectedItems;
    }

    public ArrayList<Media> getMedia() {
        ArrayList<Media> list = new ArrayList<>();
        for (int i=0; i<getItemCount(); ++i)
            list.add(getItem(i));
        return list;
    }

    public Media getMedia(int position) {
        return getItem(position);
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            if (setSelectedState(i, true)) {
                notifyItemChanged(i);
            }
        }
        selectedCount = getItemCount();
        startSelection();
    }

    public boolean clearSelected() {
        boolean changed = true;
        for (int i = 0; i < getItemCount(); i++) {
            boolean b = setSelectedState(i, false);

            if (b)
                notifyItemChanged(i);
            changed &= b;
        }

        selectedCount = 0;
        stopSelection();
        return changed;
    }

    private boolean setSelectedState(int index, boolean selected) {
        if (selectedItems.get(index) == selected)
            return false;
        selectedItems.put(index, selected);
        return true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false));
    }

    private void notifySelected(int position) {
        boolean increase = false;
        if (selectedItems.get(position))
            selectedItems.put(position, false);
        else {
            selectedItems.put(position, true);
            increase = true;
        }
        selectedCount += increase ? 1 : -1;
        actionsListener.onSelectionCountChanged(selectedCount, getItemCount());

        if (selectedCount == 0 && isSelecting) stopSelection();
        else if (selectedCount > 0 && !isSelecting) startSelection();
    }

    private void startSelection() {
        isSelecting = true;
        actionsListener.onSelectMode(true);
    }

    private void stopSelection() {
        isSelecting = false;
        actionsListener.onSelectMode(false);
    }

    public boolean selecting() {
        return isSelecting;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Media f = getMedia(position);
        holder.icon.setVisibility(View.GONE);


        holder.gifIcon.setVisibility(f.isGif() ? View.VISIBLE : View.GONE);

        RequestOptions options = new RequestOptions()
//                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_RGB_565)
                .centerCrop()
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);


        Glide.with(holder.imageView.getContext())
                .load(f.getUri())
                .apply(options)
                .thumbnail(0.5f)
                .into(holder.imageView);

        if (f.isVideo()) {
            holder.icon.setIcon(GoogleMaterial.Icon.gmd_play_circle_filled);
            holder.icon.setVisibility(View.VISIBLE);
            holder.path.setVisibility(View.VISIBLE);
            holder.path.setText(f.getName());
            /*holder.path.setTextColor(ContextCompat.getColor(holder.path.getContext(), R.color.md_dark_primary_text));
            holder.path.setBackgroundColor(
                    ColorPalette.getTransparentColor(
                            ContextCompat.getColor(holder.path.getContext(), R.color.md_black_1000), 100));*/
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
            holder.path.animate().alpha(1).setDuration(250);
        } else {
            holder.icon.setVisibility(View.GONE);
            holder.path.setVisibility(View.GONE);

            holder.icon.animate().alpha(0).setDuration(250);
            holder.path.animate().alpha(0).setDuration(250);
        }

        if (selectedItems.get(position)) {
            holder.icon.setIcon(CommunityMaterial.Icon.cmd_check);
            holder.icon.setVisibility(View.VISIBLE);
            holder.imageView.setColorFilter(0x88000000, PorterDuff.Mode.SRC_ATOP);
            holder.layout.setPadding(15, 15, 15, 15);
            //ANIMS
            holder.icon.animate().alpha(1).setDuration(250);
        } else {
            holder.imageView.clearColorFilter();
            holder.layout.setPadding(0, 0, 0, 0);
        }

        holder.layout.setOnClickListener(v -> {
            if (selecting()) {
                lastSelectedPosition = holder.getAdapterPosition();
                notifySelected(holder.getAdapterPosition());
                notifyItemChanged(holder.getAdapterPosition());
            } else
                actionsListener.onItemSelected(holder.getAdapterPosition());
        });

        holder.layout.setOnLongClickListener(v -> {
            if (!selecting()) {
                // If it is the first long press
                lastSelectedPosition = holder.getAdapterPosition();
                notifySelected(holder.getAdapterPosition());
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                selectAllUpTo(holder.getAdapterPosition());
            }

            return true;
        });
    }

    public void invalidateSelectedCount() {
        int c = 0;
        for (int i = 0; i< getItemCount(); ++i) {
            c += selectedItems.get(i) ? 1 : 0;
        }

        this.selectedCount = c;

        if (this.selectedCount == 0) stopSelection();
        else {
            this.actionsListener.onSelectionCountChanged(selectedCount, getItemCount());
        }
    }

    @Override
    public void refreshTheme(ThemeHelper theme) {
        placeholder = theme.getPlaceHolder();
        //super.refreshTheme(theme);
    }


    /**
     * On longpress, it finds the last or the first selected image before or after the targetIndex
     * and selects them all.
     *
     * @param
     */
    public void selectAllUpTo(int targetIndex) {
        int i = targetIndex;

        if (targetIndex < lastSelectedPosition) {
            while (i != lastSelectedPosition) {
                if (!selectedItems.get(i)) {
                    notifySelected(i);
                    notifyItemChanged(i);
                }
                i++;
            }
            lastSelectedPosition = --i;
        } else {
            while (i != lastSelectedPosition) {
                if (!selectedItems.get(i)) {
                    notifySelected(i);
                    notifyItemChanged(i);
                }
                i--;
            }
            lastSelectedPosition = ++i;
        }
    }

    static class ViewHolder extends ThemedViewHolder {
        @BindView(R.id.photo_preview)
        ImageView imageView;
        @BindView(R.id.photo_path)
        TextView path;
        @BindView(R.id.gif_icon)
        ThemedIcon gifIcon;
        @BindView(R.id.icon)
        ThemedIcon icon;
        @BindView(R.id.media_card_layout)
        SquareRelativeLayout layout;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void refreshTheme(ThemeHelper themeHelper) {
            icon.setColor(Color.WHITE);
        }
    }
}
