package org.horaapps.leafpic.ui.media;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.ui.common.SharedVM;
import org.horaapps.leafpic.activities.PaletteActivity;
import org.horaapps.leafpic.data.Album;
import org.horaapps.leafpic.data.AlbumRepository;
import org.horaapps.leafpic.data.LoadingState;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.MediaHelper;
import org.horaapps.leafpic.data.MediaType;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.di.Injector;
import org.horaapps.leafpic.ui.base.BaseMediaGridFragment;
import org.horaapps.leafpic.progress.ProgressBottomSheet;
import org.horaapps.leafpic.ui.viewer.ViewerFragment;
import org.horaapps.leafpic.util.Affix;
import org.horaapps.leafpic.util.AlertDialogsHelper;
import org.horaapps.leafpic.util.AnimationUtils;
import org.horaapps.leafpic.util.DeviceUtils;
import org.horaapps.leafpic.util.Measure;
import org.horaapps.leafpic.util.MediaUtils;
import org.horaapps.leafpic.util.Security;
import org.horaapps.leafpic.util.SparseBooleanArrayParcelable;
import org.horaapps.leafpic.util.StringUtils;
import org.horaapps.leafpic.util.preferences.Prefs;
import org.horaapps.leafpic.views.GridSpacingItemDecoration;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedActivity;
import org.horaapps.liz.ui.ThemedIcon;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

/**
 * Created by dnld on 3/13/17.
 */

public class RvMediaFragment extends BaseMediaGridFragment {

    public static final String TAG = "RvMediaFragment";
    private static final String BUNDLE_ALBUM = "album";

    private final String ARG_IS_SELECTING = "IS_SELECTING";
    private final String ARG_SELECTED_COUNT = "SELECTED_COUNT";
    private final String ARG_SELECTED = "SELECTED";
    private final String ARG_LAST_SELECTED_POS = "LAST_SELECTED_POS";

    @BindView(R.id.media) RecyclerView rv;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout refresh;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private MediaViewModel mediaViewModel;

    private MediaAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;

    private SortingMode sortingMode;
    private SortingOrder sortingOrder;
    private MediaType mediaFilter;

    private Album album;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            adapter = new MediaAdapter(getContext(), this, getArguments().getParcelable(ARG_SELECTED));
            adapter.selectedCount = getArguments().getInt(ARG_SELECTED_COUNT);
            adapter.isSelecting = getArguments().getBoolean(ARG_IS_SELECTING);
            adapter.lastSelectedPosition = getArguments().getInt(ARG_LAST_SELECTED_POS);
        } else {
            adapter = new MediaAdapter(getContext(), this, new SparseBooleanArrayParcelable());
        }

        SharedVM sharedVM = ViewModelProviders.of(getActivity()).get(SharedVM.class);
        album = sharedVM.getAlbum();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        viewModelFactory = Injector.Companion.get().viewModelFactory();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!clearSelected())
            updateToolbar();
        setUpColumns();
    }

    private void reload() {
        loadAlbum(album);
    }

    private void loadAlbum(Album album) {
        this.album = album;
        mediaViewModel.refreshMedia(album);
        mediaViewModel.loadMedia(album.getId(), sortingMode(), sortingOrder(), mediaFilter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_SELECTED_COUNT, adapter.selectedCount);
        outState.putInt(ARG_LAST_SELECTED_POS, adapter.lastSelectedPosition);
        outState.putBoolean(ARG_IS_SELECTING, adapter.isSelecting);
        outState.putParcelable(ARG_SELECTED, adapter.selectedItems);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rv_media, container, false);
        ButterKnife.bind(this, v);

        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        rv.setItemAnimator(
                AnimationUtils.getItemAnimator(
                        new LandingAnimator(new OvershootInterpolator(1f))
                ));

        refresh.setOnRefreshListener(this::reload);
        rv.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mediaViewModel = ViewModelProviders.of(this, viewModelFactory).get(MediaViewModel.class);

        mediaViewModel.getMedia().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);

            if (getNothingToShowListener() != null)
                getNothingToShowListener().changedNothingToShow(getCount() == 0);
        });
        mediaViewModel.getMediaLoadingState().observe(this, state -> {
            if (state == LoadingState.Companion.getLOADED())
                refresh.setRefreshing(false);
            if (state.getMsg() != null) {
                refresh.setRefreshing(false);
                Toast.makeText(getContext(), state.getMsg(), Toast.LENGTH_SHORT).show();
            }
        });

        reload();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpColumns();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            ((GridLayoutManager) rv.getLayoutManager()).getSpanCount();
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
            rv.addItemDecoration(spacingDecoration);
        }
    }

    public int columnsCount() {
        return DeviceUtils.isPortrait(getResources())
                ? Prefs.getMediaColumnsPortrait()
                : Prefs.getMediaColumnsLandscape();
    }

    @Override
    public int getTotalCount() {
        return adapter.getItemCount();
    }

    @Override
    public View.OnClickListener getToolbarButtonListener(boolean editMode) {
        if (editMode) return null;
        else return v -> adapter.clearSelected();
    }

    @Override
    public String getToolbarTitle() {
        return editMode() ? null : album.getAlbumName();
    }

    public SortingMode sortingMode() {
        if (sortingMode == null)
            sortingMode = SortingMode.DATE;
        return sortingMode;
    }

    public SortingOrder sortingOrder() {
        if (sortingOrder == null)
            sortingOrder = SortingOrder.DESCENDING;
        return sortingOrder;
    }

    private AlbumRepository db() {
        return Injector.Companion.get().albumRepository();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.grid_media, menu);

        menu.findItem(R.id.select_all).setIcon(ThemeHelper.getToolbarIcon(getContext(), GoogleMaterial.Icon.gmd_select_all));
        menu.findItem(R.id.delete).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_delete)));
        menu.findItem(R.id.sharePhotos).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_share)));
        menu.findItem(R.id.sort_action).setIcon(ThemeHelper.getToolbarIcon(getContext(),(GoogleMaterial.Icon.gmd_sort)));
        menu.findItem(R.id.filter_menu).setIcon(ThemeHelper.getToolbarIcon(getContext(), (GoogleMaterial.Icon.gmd_filter_list)));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        boolean editMode = editMode();
        boolean oneSelected = getSelectedCount() == 1;

        menu.setGroupVisible(R.id.general_album_items, !editMode);
        menu.setGroupVisible(R.id.edit_mode_items, editMode);
        menu.setGroupVisible(R.id.one_selected_items, oneSelected);

        menu.findItem(R.id.select_all).setTitle(
                getSelectedCount() == getCount()
                        ? R.string.clear_selected
                        : R.string.select_all);
        if (editMode) {
            menu.findItem(R.id.filter_menu).setVisible(false);
            menu.findItem(R.id.sort_action).setVisible(false);
        } else {
            menu.findItem(R.id.filter_menu).setVisible(true);
            menu.findItem(R.id.sort_action).setVisible(true);

            menu.findItem(R.id.ascending_sort_order).setChecked(sortingOrder() == SortingOrder.ASCENDING);
            switch (sortingMode()) {
                case NAME:  menu.findItem(R.id.name_sort_mode).setChecked(true); break;
                case SIZE:  menu.findItem(R.id.size_sort_mode).setChecked(true); break;
                case DATE: default:
                    menu.findItem(R.id.date_taken_sort_mode).setChecked(true); break;
                case NUMERIC:  menu.findItem(R.id.numeric_sort_mode).setChecked(true); break;
            }
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.all_media_filter:
                mediaFilter = null;
                mediaViewModel.setMediaFilter(null);
                item.setChecked(true);
                reload();
                return true;

            case R.id.video_media_filter:
                mediaFilter = MediaType.VIDEO;
                mediaViewModel.setMediaFilter(MediaType.VIDEO);
                item.setChecked(true);
                reload();
                return true;

            case R.id.image_media_filter:
                mediaFilter = MediaType.IMAGE;
                mediaViewModel.setMediaFilter(MediaType.IMAGE);
                item.setChecked(true);
                reload();
                return true;

            case R.id.gifs_media_filter:
                mediaFilter = MediaType.GIF;
                mediaViewModel.setMediaFilter(MediaType.GIF);
                item.setChecked(true);
                reload();
                return true;

            case R.id.sharePhotos:
                MediaUtils.shareMedia(getContext(), getSelected());
                return true;

            case R.id.set_as_cover:
                String path = getFirstSelected().getPath();
                mediaViewModel.setCover(album, path);
                adapter.clearSelected();
                return true;

            case R.id.action_palette:
                Intent paletteIntent = new Intent(getActivity(), PaletteActivity.class);
                paletteIntent.setData(getFirstSelected().getUri());
                startActivity(paletteIntent);
                return true;

            case R.id.rename:
                final EditText editTextNewName = new EditText(getActivity());
                editTextNewName.setText(StringUtils.getPhotoNameByPath(getFirstSelected().getPath()));

                AlertDialog renameDialog = AlertDialogsHelper.getInsertTextDialog(((ThemedActivity) getActivity()), editTextNewName, R.string.rename_photo_action);

                renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok_action).toUpperCase(), (dialog, which) -> {
                    if (editTextNewName.length() != 0) {
                        boolean b = MediaHelper.renameMedia(getActivity(), getFirstSelected(), editTextNewName.getText().toString());
                        if (!b) {
                            StringUtils.showToast(getActivity(), getString(R.string.rename_error));
                            //adapter.notifyDataSetChanged();
                        } else
                            adapter.clearSelected(); // Deselect media if rename successful
                    } else
                        StringUtils.showToast(getActivity(), getString(R.string.nothing_changed));
                });
                renameDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase(), (dialog, which) -> dialog.dismiss());
                renameDialog.show();
                return true;

            case R.id.select_all:
                if (adapter.getSelectedCount() == adapter.getItemCount())
                    adapter.clearSelected();
                else adapter.selectAll();
                return true;

            case R.id.name_sort_mode:
                sortingMode = SortingMode.NAME;
                mediaViewModel.setSortOptions(sortingMode, sortingOrder);
                item.setChecked(true);
                return true;

            case R.id.date_taken_sort_mode:
                sortingMode = SortingMode.DATE;
                mediaViewModel.setSortOptions(sortingMode, sortingOrder);
                item.setChecked(true);
                return true;

            case R.id.size_sort_mode:
                sortingMode = SortingMode.SIZE;
                mediaViewModel.setSortOptions(sortingMode, sortingOrder);
                item.setChecked(true);
                return true;

            case R.id.numeric_sort_mode:
                // remove option
                return true;

            case R.id.ascending_sort_order:
                item.setChecked(!item.isChecked());
                sortingOrder = SortingOrder.fromValue(item.isChecked());
                mediaViewModel.setSortOptions(sortingMode, sortingOrder);
                return true;

            case R.id.delete:

                if (Security.isPasswordOnDelete()) {

                    Security.authenticateUser(((ThemedActivity) getActivity()), new Security.AuthCallBack() {
                        @Override
                        public void onAuthenticated() {
                            showDeleteBottomSheet();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getContext(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showDeleteBottomSheet();
                }
                return true;

            //region Affix
            // TODO: 11/21/16 move away from here
            case R.id.affix:

                //region Async MediaAffix
                class affixMedia extends AsyncTask<Affix.Options, Integer, Void> {
                    private AlertDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = AlertDialogsHelper.getProgressDialog((ThemedActivity) getActivity(), getString(R.string.affix), getString(R.string.affix_text));
                        dialog.show();
                    }

                    @Override
                    protected Void doInBackground(Affix.Options... arg0) {
                        ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
                        for (int i = 0; i < adapter.getSelectedCount(); i++) {
                            if(getSelected().get(i).getMediaType() != MediaType.VIDEO) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap bitmap = BitmapFactory.decodeFile(getSelected().get(i).getPath(), bmOptions);
                                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                                bitmapArray.add(bitmap);
                            }
                        }

                        if (bitmapArray.size() > 1)
                            Affix.AffixBitmapList(getActivity(), bitmapArray, arg0[0]);
                        else getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), R.string.affix_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        adapter.clearSelected();
                        dialog.dismiss();
                    }
                }
                //endregion

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), getDialogStyle());
                final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_affix, null);

                dialogLayout.findViewById(R.id.affix_title).setBackgroundColor(getPrimaryColor());
                ((CardView) dialogLayout.findViewById(R.id.affix_card)).setCardBackgroundColor(getCardBackgroundColor());

                //ITEMS
                final SwitchCompat swVertical = dialogLayout.findViewById(R.id.affix_vertical_switch);
                final SwitchCompat swSaveHere = dialogLayout.findViewById(R.id.save_here_switch);

                final LinearLayout llSwVertical = dialogLayout.findViewById(R.id.ll_affix_vertical);
                final LinearLayout llSwSaveHere = dialogLayout.findViewById(R.id.ll_affix_save_here);

                final RadioGroup radioFormatGroup = dialogLayout.findViewById(R.id.radio_format);

                final TextView txtQuality = dialogLayout.findViewById(R.id.affix_quality_title);
                final SeekBar seekQuality = dialogLayout.findViewById(R.id.seek_bar_quality);

                //region Example
                final LinearLayout llExample = dialogLayout.findViewById(R.id.affix_example);
                llExample.setBackgroundColor(getBackgroundColor());
                llExample.setVisibility(Prefs.getToggleValue(getContext().getString(R.string.preference_show_tips), true) ? View.VISIBLE : View.GONE);
                final LinearLayout llExampleH = dialogLayout.findViewById(R.id.affix_example_horizontal);
                //llExampleH.setBackgroundColor(getCardBackgroundColor());
                final LinearLayout llExampleV = dialogLayout.findViewById(R.id.affix_example_vertical);
                //llExampleV.setBackgroundColor(getCardBackgroundColor());


                //endregion

                //region THEME STUFF
                getThemeHelper().setScrollViewColor(dialogLayout.findViewById(R.id.affix_scrollView));

                /** TextViews **/
                int color = getTextColor();
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.compression_settings_title)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.save_here_title)).setTextColor(color);

                //Example Stuff
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt1)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_horizontal_txt2)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt1)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_example_vertical_txt2)).setTextColor(color);


                /** Sub TextViews **/
                color = getThemeHelper().getSubTextColor();
                ((TextView) dialogLayout.findViewById(R.id.save_here_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_vertical_sub)).setTextColor(color);
                ((TextView) dialogLayout.findViewById(R.id.affix_format_sub)).setTextColor(color);
                txtQuality.setTextColor(color);

                /** Icons **/
                color = getIconColor();
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_quality_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_format_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.affix_vertical_icon)).setColor(color);
                ((ThemedIcon) dialogLayout.findViewById(R.id.save_here_icon)).setColor(color);

                //Example bg
                color = getCardBackgroundColor();
                dialogLayout.findViewById(R.id.affix_example_horizontal_txt1).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_horizontal_txt2).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_vertical_txt1).setBackgroundColor(color);
                dialogLayout.findViewById(R.id.affix_example_vertical_txt2).setBackgroundColor(color);

                seekQuality.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));
                seekQuality.getThumb().setColorFilter(new PorterDuffColorFilter(getAccentColor(), PorterDuff.Mode.SRC_IN));

                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_jpeg));
                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_png));
                getThemeHelper().themeRadioButton(dialogLayout.findViewById(R.id.radio_webp));
                getThemeHelper().setSwitchCompactColor( swSaveHere, getAccentColor());
                getThemeHelper().setSwitchCompactColor( swVertical, getAccentColor());
                //#endregion

                seekQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        txtQuality.setText(StringUtils.html(String.format(Locale.getDefault(), "%s <b>%d</b>", getString(R.string.quality), progress)));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                seekQuality.setProgress(50);

                swVertical.setClickable(false);
                llSwVertical.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swVertical.setChecked(!swVertical.isChecked());
                        getThemeHelper().setSwitchCompactColor(swVertical, getAccentColor());
                        llExampleH.setVisibility(swVertical.isChecked() ? View.GONE : View.VISIBLE);
                        llExampleV.setVisibility(swVertical.isChecked() ? View.VISIBLE : View.GONE);
                    }
                });

                swSaveHere.setClickable(false);
                llSwSaveHere.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swSaveHere.setChecked(!swSaveHere.isChecked());
                        getThemeHelper().setSwitchCompactColor(swSaveHere, getAccentColor());
                    }
                });

                builder.setView(dialogLayout);
                builder.setPositiveButton(this.getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Bitmap.CompressFormat compressFormat;
                        switch (radioFormatGroup.getCheckedRadioButtonId()) {
                            case R.id.radio_jpeg:
                            default:
                                compressFormat = Bitmap.CompressFormat.JPEG;
                                break;
                            case R.id.radio_png:
                                compressFormat = Bitmap.CompressFormat.PNG;
                                break;
                            case R.id.radio_webp:
                                compressFormat = Bitmap.CompressFormat.WEBP;
                                break;
                        }

                        Affix.Options options = new Affix.Options(
                                swSaveHere.isChecked() ? getFirstSelected().getPath() : Affix.getDefaultDirectoryPath(),
                                compressFormat,
                                seekQuality.getProgress(),
                                swVertical.isChecked());
                        new affixMedia().execute(options);
                    }
                });
                builder.setNegativeButton(this.getString(R.string.cancel).toUpperCase(), null);
                builder.show();
                return true;
            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteBottomSheet() {
        MediaUtils.deleteMedia(getContext(), getSelected(), getChildFragmentManager(),
                new ProgressBottomSheet.Listener<Media>() {
                    @Override
                    public void onCompleted() {
                        adapter.invalidateSelectedCount();
                    }

                    @Override
                    public void onProgress(Media item) {
                        removeSelectedMedia(item);
                    }
                });
    }

    public int getCount() {
        return adapter.getItemCount();
    }

    public int getSelectedCount() {
        return adapter.getSelectedCount();
    }

    @Override
    public boolean editMode() {
        return adapter.selecting();
    }

    @Override
    public void onItemSelected(int position) {
        Bundle bundle = new Bundle();
        bundle.putLong(ViewerFragment.EXTRA_ARGS_ALBUM_ID, adapter.getMedia(position).getAlbumId());
        bundle.putInt(ViewerFragment.EXTRA_ARGS_POSITION, position);
        bundle.putSerializable(ViewerFragment.EXTRA_ARGS_FILTER, mediaFilter);
        bundle.putInt(ViewerFragment.EXTRA_ARGS_SORT_MODE, sortingMode.getValue());
        bundle.putInt(ViewerFragment.EXTRA_ARGS_SORT_ORDER, sortingOrder.getValue());

        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_rvMediaFragment_to_viewerFragment, bundle);
    }

    @Override
    public void onSelectMode(boolean selectMode) {
        refresh.setEnabled(!selectMode);
        updateToolbar();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {
        getEditModeListener().onItemsSelected(selectionCount, totalCount);
    }

    @Override
    public boolean clearSelected() {
        return adapter.clearSelected();
    }

    @Override
    public void refreshTheme(ThemeHelper t) {
        rv.setBackgroundColor(t.getBackgroundColor());
        adapter.refreshTheme(t);
        refresh.setColorSchemeColors(t.getAccentColor());
        refresh.setProgressBackgroundColorSchemeColor(t.getBackgroundColor());
    }

    public void remove(Media media) {
//        int i = this.media.indexOf(media);
//        this.media.remove(i);
//        notifyItemRemoved(i);
    }

    public void removeSelectedMedia(Media media) {
//        int i = this.media.indexOf(media);
//        this.media.remove(i);
//        notifyItemRemoved(i);
//
////        this.notifySelected(false);
    }

    public ArrayList<Media> getSelected() {
//        ArrayList<Media> arrayList = new ArrayList<>(selectedCount);
//        for (Media m : media)
//            if (m.isSelected())
//                arrayList.add(m);
//        return arrayList;
        return null;
    }

    public Media getFirstSelected() {
//        if (selectedCount > 0) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//                return media.stream().filter(Media::isSelected).findFirst().orElse(null);
//            else
//                for (Media m : media)
//                    if (m.isSelected())
//                        return m;
//        }
        return null;
    }
}
