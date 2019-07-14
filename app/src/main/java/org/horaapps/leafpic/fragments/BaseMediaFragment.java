package org.horaapps.leafpic.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;

import org.horaapps.leafpic.SharedVM;
import org.horaapps.leafpic.data.Media;
import org.horaapps.liz.ThemeHelper;

/**
 * A Base Fragment for showing Media.
 */
public abstract class BaseMediaFragment extends BaseFragment {

    private static final String ARGS_MEDIA = "args_media";

    protected Media media;
    private MediaTapListener mediaTapListener;

    @NonNull
    protected static <T extends BaseMediaFragment> T newInstance(@NonNull T mediaFragment,
                                                                 @NonNull Media media) {
        return mediaFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MediaTapListener) mediaTapListener = (MediaTapListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedVM sharedVM = ViewModelProviders.of(getActivity()).get(SharedVM.class);
        media = sharedVM.getMedia();
    }

    @Override
    public void refreshTheme(ThemeHelper themeHelper) {
        // Default implementation
    }

    protected void setTapListener(@NonNull View view) {
        view.setOnClickListener(v -> onTapped());
    }

    private void onTapped() {
        mediaTapListener.onViewTapped();
    }

    /**
     * Interface for listeners to react on Media Clicks.
     */
    public interface MediaTapListener {

        /**
         * Called when user taps on the Media view.
         */
        void onViewTapped();
    }
}
