package org.horaapps.leafpic.ui.timeline

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_timeline.*
import org.horaapps.leafpic.ui.media.MediaViewModel
import org.horaapps.leafpic.R
import org.horaapps.leafpic.ui.common.SharedVM
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.LoadingState
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.filter.FilterMode
import org.horaapps.leafpic.data.filter.MediaFilter
import org.horaapps.leafpic.data.sort.MediaComparators
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import org.horaapps.leafpic.di.Injector
import org.horaapps.leafpic.ui.base.BaseMediaGridFragment
import org.horaapps.leafpic.ui.base.interfaces.MediaClickListener
import org.horaapps.leafpic.ui.base.interfaces.ActionsListener
import org.horaapps.leafpic.progress.ProgressBottomSheet
import org.horaapps.leafpic.util.DeviceUtils
import org.horaapps.leafpic.util.Security
import org.horaapps.leafpic.util.deleteMedia
import org.horaapps.leafpic.util.preferences.Defaults
import org.horaapps.leafpic.util.shareMedia
import org.horaapps.liz.ThemeHelper
import org.horaapps.liz.ThemedActivity
import java.util.*
import javax.inject.Inject

/**
 * Fragment which shows the Timeline.
 */
class TimelineFragment : BaseMediaGridFragment(), ActionsListener {

    companion object {

        const val TAG = "TimelineFragment"

        private const val ARGS_ALBUM = "args_album"

        private const val KEY_ALBUM = "key_album"
        private const val KEY_GROUPING_MODE = "key_grouping_mode"
        private const val KEY_FILTER_MODE = "key_filter_mode"

        fun newInstance(album: Album) = TimelineFragment()
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mediaViewModel: MediaViewModel

    private lateinit var timelineAdapter: TimelineAdapter
    private lateinit var timelineListener: MediaClickListener
    private lateinit var gridLayoutManager: androidx.recyclerview.widget.GridLayoutManager

    private lateinit var contentAlbum: Album

    private lateinit var groupingMode: GroupingMode
    private lateinit var filterMode: FilterMode

    private val timelineGridSize: Int
        get() = if (DeviceUtils.isPortrait(resources)) Defaults.TIMELINE_ITEMS_PORTRAIT
        else Defaults.TIMELINE_ITEMS_LANDSCAPE

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModelFactory = Injector.get().viewModelFactory()
        if (context is MediaClickListener) timelineListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        savedInstanceState?.let {
            groupingMode = it.get(KEY_GROUPING_MODE) as GroupingMode
            filterMode = it.get(KEY_FILTER_MODE) as FilterMode
            return
        }

        val sharedVM = ViewModelProviders.of(activity!!).get(SharedVM::class.java)
        contentAlbum = sharedVM.album ?: return

        /* Set defaults */
        groupingMode = GroupingMode.DAY
        filterMode = FilterMode.ALL
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_timeline, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaViewModel = ViewModelProviders.of(this, viewModelFactory).get(MediaViewModel::class.java)
        mediaViewModel.media.observe(viewLifecycleOwner, Observer { list ->
            val mediaList = ArrayList<Media>()
            list.forEach { media ->
                if (MediaFilter.getFilter(filterMode).accept(media))
                    mediaList.add(media)
            }
            setAdapterMedia(mediaList)
        })

        mediaViewModel.mediaLoadingState.observe(viewLifecycleOwner, Observer { state ->
            if (state == LoadingState.LOADED)
                timeline_swipe_refresh_layout!!.isRefreshing = false
            else if (state.msg != null) {
                timeline_swipe_refresh_layout!!.isRefreshing = false
                Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
            }
        })

        timeline_swipe_refresh_layout.setOnRefreshListener { this.loadAlbum() }
        setupRecyclerView()
        loadAlbum()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_timeline, menu)

        menu.findItem(getMenuForGroupingMode(groupingMode)).isChecked = true
        menu.findItem(getMenuForFilterMode(filterMode)).isChecked = true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val isEditing = editMode()
        with(menu) {
            setGroupVisible(R.id.timeline_view_items, !isEditing)
            setGroupVisible(R.id.timeline_edit_items, isEditing)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        getGroupingMode(item.itemId)?.let {
            // Handling Grouping Mode selections
            groupingMode = it
            item.isChecked = true
            timelineAdapter.setGroupingMode(it)
            return true
        }

        getFilterMode(item.itemId)?.let {
            // Handling Filter Mode selections
            filterMode = it
            item.isChecked = true
            loadAlbum()
            return true
        }

        return when (item.itemId) {

            R.id.timeline_menu_delete -> {
                if (Security.isPasswordOnDelete()) {
                    Security.authenticateUser(activity as ThemedActivity?, object : Security.AuthCallBack {
                        override fun onAuthenticated() {
                            deleteMedia()
                        }

                        override fun onError() {
                            Toast.makeText(context, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else deleteMedia()

                true
            }

            R.id.timeline_share -> {
                shareMedia(context!!, timelineAdapter.selectedMedia)
                true
            }

            R.id.timeline_menu_select_all -> {
                if (timelineAdapter.selectedCount == timelineAdapter.mediaCount) exitContextMenu()
                else timelineAdapter.selectAll()
                true
            }

            else -> false

        }
    }

    private fun deleteMedia() {
        deleteMedia(context!!, timelineAdapter.selectedMedia, childFragmentManager, object : ProgressBottomSheet.Listener<Media> {

            override fun onCompleted() {
                exitContextMenu()
                loadAlbum()
            }

            override fun onProgress(item: Media?) {
               timelineAdapter.removeItem(item)
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putSerializable(KEY_GROUPING_MODE, groupingMode)
            putSerializable(KEY_FILTER_MODE, filterMode)
        }
        super.onSaveInstanceState(outState)
    }

    private fun getGroupingMode(@IdRes menuId: Int) = when (menuId) {
        R.id.timeline_grouping_day -> GroupingMode.DAY
        R.id.timeline_grouping_week -> GroupingMode.WEEK
        R.id.timeline_grouping_month -> GroupingMode.MONTH
        R.id.timeline_grouping_year -> GroupingMode.YEAR
        else -> null
    }

    @IdRes
    private fun getMenuForGroupingMode(groupingMode: GroupingMode) = when (groupingMode) {
        GroupingMode.DAY -> R.id.timeline_grouping_day
        GroupingMode.WEEK -> R.id.timeline_grouping_week
        GroupingMode.MONTH -> R.id.timeline_grouping_month
        GroupingMode.YEAR -> R.id.timeline_grouping_year
    }

    private fun getFilterMode(@IdRes menuId: Int) = when (menuId) {
        R.id.all_media_filter -> FilterMode.ALL
        R.id.video_media_filter -> FilterMode.VIDEO
        R.id.image_media_filter -> FilterMode.IMAGES
        R.id.gifs_media_filter -> FilterMode.GIF
        else -> null
    }

    @IdRes
    private fun getMenuForFilterMode(filterMode: FilterMode) = when (filterMode) {
        FilterMode.ALL -> R.id.all_media_filter
        FilterMode.IMAGES -> R.id.image_media_filter
        FilterMode.GIF -> R.id.gifs_media_filter
        FilterMode.VIDEO -> R.id.video_media_filter
        FilterMode.NO_VIDEO -> R.id.all_media_filter
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val gridSize = timelineGridSize
        with(gridSize) {
            timelineAdapter.setTimelineGridSize(this)
            gridLayoutManager.spanCount = this
        }
    }

    private fun setupRecyclerView() {
        val decorator = TimelineAdapter.TimelineItemDecorator(context!!, R.dimen.timeline_decorator_spacing)
        gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context, timelineGridSize)
        timeline_items.layoutManager = gridLayoutManager
        timeline_items.addItemDecoration(decorator)

        timelineAdapter = TimelineAdapter(context!!, this, timelineGridSize)
        timelineAdapter.setGridLayoutManager(gridLayoutManager)
        timelineAdapter.setGroupingMode(groupingMode)
        timeline_items.adapter = timelineAdapter
    }

    private fun loadAlbum() {
        mediaViewModel.refreshMedia(contentAlbum)
        mediaViewModel.loadMedia(contentAlbum.id, SortingMode.DATE, SortingOrder.DESCENDING, null)
    }

    private fun setAdapterMedia(mediaList: ArrayList<Media>) {
        Collections.sort(mediaList, MediaComparators.getComparator(SortingMode.DATE, SortingOrder.DESCENDING))
        timelineAdapter.media = mediaList
    }

    override fun editMode() = timelineAdapter.isSelecting

    override fun clearSelected() = timelineAdapter.clearSelected()

    override fun refreshTheme(t: ThemeHelper) {
        with(t) {
            timeline_items.setBackgroundColor(this.backgroundColor)
            timeline_swipe_refresh_layout.setColorSchemeColors(this.accentColor)
            timeline_swipe_refresh_layout.setProgressBackgroundColorSchemeColor(this.backgroundColor)
            timelineAdapter.refreshTheme(this)
        }
    }

    override fun onItemSelected(position: Int) = timelineListener.onMediaClick(contentAlbum.id, timelineAdapter.media.get(position).getFile(), position)

    override fun onSelectMode(selectMode: Boolean) = updateToolbar()

    override fun onSelectionCountChanged(selectionCount: Int, totalCount: Int) = editModeListener.onItemsSelected(selectionCount, totalCount)

    override fun getSelectedCount() = timelineAdapter.selectedCount

    override fun getTotalCount() = timelineAdapter.mediaCount

    override fun getToolbarButtonListener(editMode: Boolean) = when (editMode) {
        true -> View.OnClickListener { exitContextMenu() }
        false -> null
    }

    override fun getToolbarTitle() = when (editMode()) {
        true -> null
        false -> getString(R.string.timeline_toolbar_title)
    }
}
