package org.horaapps.leafpic.ui.common;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.ui.base.BaseMediaFragment;

import pl.droidsonroids.gif.GifImageView;

/**
 * Media Fragment for showing an Image (static)
 */
public class GifFragment extends BaseMediaFragment {

    @NonNull
    public static GifFragment newInstance(@NonNull Uri mediaUri, String mimeType) {
        return BaseMediaFragment.newInstance(new GifFragment(), mediaUri, mimeType);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        GifImageView photoView = new GifImageView(getContext());
        photoView.setImageURI(mediaUri);
        setTapListener(photoView);
        return photoView;
    }
}
