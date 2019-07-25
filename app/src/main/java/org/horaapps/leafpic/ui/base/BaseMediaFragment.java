package org.horaapps.leafpic.ui.base;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;

import org.horaapps.leafpic.ui.common.SharedVM;
import org.horaapps.leafpic.data.Media;
import org.horaapps.liz.ThemeHelper;

/**
 * A Base Fragment for showing Media.
 */
public abstract class BaseMediaFragment extends BaseFragment {

    private static final String ARGS_MEDIA_URI = "args_media_uri";
    private static final String ARGS_MEDIA_TYPE = "args_media_type";

    protected Uri mediaUri;
    protected String mimeType;
    private MediaTapListener mediaTapListener;

    @NonNull
    protected static <T extends BaseMediaFragment> T newInstance(@NonNull T mediaFragment,
                                                                 @NonNull Uri mediaUri,
                                                                 String mimeType) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_MEDIA_URI, mediaUri);
        args.putString(ARGS_MEDIA_TYPE, mimeType);
        mediaFragment.setArguments(args);
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
        Bundle args = getArguments();
        if (args == null) throw new RuntimeException("Must pass arguments to Media Fragments!");
        mediaUri = getArguments().getParcelable(ARGS_MEDIA_URI);
        mimeType = getArguments().getString(ARGS_MEDIA_TYPE);
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
