package org.fossify.home.adapters

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.beInvisible
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.getBottomNavigationBackgroundColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.home.R
import org.fossify.home.databinding.ItemManageTabBinding
import org.fossify.home.databinding.ItemManageTabHintBinding
import org.fossify.home.models.DrawerTab
import java.util.Collections

/**
 * Rows: the hint header, the pinned "All" card, then one card per custom tab.
 * Custom tab cards can be reordered with a long press drag.
 */
class ManageTabsAdapter(
    val activity: Activity,
    val listener: Listener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Listener {
        fun onTabClicked(tab: DrawerTab)
        fun onTabDeleteClicked(tab: DrawerTab)
        fun onTabAddAppsClicked(tab: DrawerTab)
        fun onTabsReordered(tabs: List<DrawerTab>)
    }

    private val tabs = ArrayList<DrawerTab>()
    private var appCounts = emptyMap<Long, Int>()
    private var allAppsCount = 0
    private var hiddenAppsCount = 0

    val itemTouchHelper = ItemTouchHelper(DragCallback())

    @Suppress("NotifyDataSetChanged")
    fun setData(newTabs: List<DrawerTab>, counts: Map<Long, Int>, allCount: Int, hiddenCount: Int) {
        tabs.clear()
        tabs.addAll(newTabs)
        appCounts = counts
        allAppsCount = allCount
        hiddenAppsCount = hiddenCount
        notifyDataSetChanged()
    }

    override fun getItemCount() = tabs.size + FIRST_TAB_POSITION

    override fun getItemViewType(position: Int) = if (position == HINT_POSITION) TYPE_HINT else TYPE_TAB

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HINT) {
            val binding = ItemManageTabHintBinding.inflate(inflater, parent, false)
            binding.manageTabsHint.setTextColor(activity.getProperTextColor())
            object : RecyclerView.ViewHolder(binding.root) {}
        } else {
            TabViewHolder(ItemManageTabBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TabViewHolder) {
            holder.bind(if (position == ALL_POSITION) null else tabs[position - FIRST_TAB_POSITION])
        }
    }

    inner class TabViewHolder(private val binding: ItemManageTabBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tab: DrawerTab?) {
            val textColor = activity.getProperTextColor()
            binding.root.background = GradientDrawable().apply {
                cornerRadius = activity.resources.getDimension(org.fossify.commons.R.dimen.material_button_corner_radius)
                setColor(activity.getBottomNavigationBackgroundColor())
            }

            binding.manageTabName.setTextColor(textColor)
            binding.manageTabCount.setTextColor(textColor)
            binding.manageTabDelete.applyColorFilter(textColor)
            binding.manageTabAdd.applyColorFilter(textColor)

            if (tab == null) {
                binding.manageTabName.text = activity.getString(R.string.all_tab)
                binding.manageTabCount.text = if (hiddenAppsCount > 0) {
                    "${getAppCountText(allAppsCount)} · ${activity.getString(R.string.hidden_apps_count, hiddenAppsCount)}"
                } else {
                    getAppCountText(allAppsCount)
                }
                binding.manageTabDelete.beInvisible()
                binding.manageTabAdd.beInvisible()
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
                return
            }

            binding.manageTabName.text = tab.name
            binding.manageTabCount.text = getAppCountText(appCounts[tab.id] ?: 0)
            binding.manageTabDelete.beVisible()
            binding.manageTabAdd.beVisible()
            binding.manageTabDelete.setOnClickListener { listener.onTabDeleteClicked(tab) }
            binding.manageTabAdd.setOnClickListener { listener.onTabAddAppsClicked(tab) }
            binding.root.setOnClickListener { listener.onTabClicked(tab) }
            binding.root.setOnLongClickListener {
                itemTouchHelper.startDrag(this)
                true
            }
        }
    }

    private fun getAppCountText(count: Int) =
        activity.resources.getQuantityString(R.plurals.tab_app_count, count, count)

    private inner class DragCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        private var moved = false

        override fun isLongPressDragEnabled() = false

        override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (viewHolder.bindingAdapterPosition >= FIRST_TAB_POSITION) {
                super.getDragDirs(recyclerView, viewHolder)
            } else {
                0
            }
        }

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = target.bindingAdapterPosition >= FIRST_TAB_POSITION

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            if (from < FIRST_TAB_POSITION || to < FIRST_TAB_POSITION) {
                return false
            }

            Collections.swap(tabs, from - FIRST_TAB_POSITION, to - FIRST_TAB_POSITION)
            notifyItemMoved(from, to)
            moved = true
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = DRAG_ALPHA
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = 1f
            if (moved) {
                moved = false
                tabs.forEachIndexed { index, tab -> tab.position = index }
                listener.onTabsReordered(tabs.toList())
            }
        }
    }

    companion object {
        private const val TYPE_HINT = 0
        private const val TYPE_TAB = 1
        private const val HINT_POSITION = 0
        private const val ALL_POSITION = 1
        private const val FIRST_TAB_POSITION = 2
        private const val DRAG_ALPHA = 0.8f
    }
}
