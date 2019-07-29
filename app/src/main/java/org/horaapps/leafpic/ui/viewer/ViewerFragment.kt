package org.horaapps.leafpic.ui.viewer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import org.horaapps.leafpic.R
import org.horaapps.leafpic.adapters.MediaPagerAdapter
import org.horaapps.leafpic.animations.DepthPageTransformer
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import org.horaapps.leafpic.di.Injector
import org.horaapps.leafpic.ui.media.MediaViewModel
import org.horaapps.leafpic.util.AnimationUtils
import org.horaapps.leafpic.views.HackyViewPager
import javax.inject.Inject


class ViewerFragment : Fragment() {

    @BindView(R.id.photos_pager)
    lateinit var mViewPager: HackyViewPager
    @BindView(R.id.PhotoPager_Layout)
    lateinit var activityBackground: RelativeLayout
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mediaViewModel: MediaViewModel
    private lateinit var unbinder: Unbinder
    private lateinit var adapter: MediaPagerAdapter

    companion object {
        const val EXTRA_ARGS_POSITION = "args_position"
        const val EXTRA_ARGS_ALBUM_ID = "args_album_id"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = Injector.get().viewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = MediaPagerAdapter(fragmentManager, arrayListOf())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_single_media, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewPager.adapter = adapter
        mViewPager.setPageTransformer(true, AnimationUtils.getPageTransformer(DepthPageTransformer()))

        mediaViewModel = ViewModelProviders.of(this, viewModelFactory).get(MediaViewModel::class.java)

        val position = arguments!!.getInt(EXTRA_ARGS_POSITION, 0)
        mediaViewModel.media.observe(viewLifecycleOwner, Observer<List<Media>> { mediaList ->
            if (mediaList != null && mediaList.isNotEmpty()) {
                adapter.swapDataSet(ArrayList(mediaList))
                mViewPager.currentItem = position
            }
        })
        mediaViewModel.mediaLoadingState.observe(this, Observer { state ->
            if (state.msg != null) {
                Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
            }
        })

        val albumId = arguments!!.getLong(EXTRA_ARGS_ALBUM_ID)
        mediaViewModel.loadMedia(albumId, SortingMode.DATE, SortingOrder.DESCENDING)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }
}