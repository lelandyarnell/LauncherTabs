package org.fossify.home.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.home.R
import org.fossify.home.models.DrawerTab

/**
 * Horizontally scrolling row of tab buttons shown below the app drawer search bar.
 * A null tab id stands for the built-in "All" tab.
 */
class DrawerTabsBar(context: Context, attrs: AttributeSet) : HorizontalScrollView(context, attrs) {

    var onTabSelected: ((tabId: Long?) -> Unit)? = null

    var selectedTabId: Long? = null
        private set

    private var tabs = emptyList<DrawerTab>()
    private val holder = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
    }

    init {
        isHorizontalScrollBarEnabled = false
        isFillViewport = false
        addView(holder, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    fun setTabs(newTabs: List<DrawerTab>) {
        tabs = newTabs
        if (selectedTabId != null && tabs.none { it.id == selectedTabId }) {
            selectTab(null)
        }

        beVisibleIf(tabs.isNotEmpty())
        rebuild()
    }

    fun hasTabs() = tabs.isNotEmpty()

    fun selectTab(tabId: Long?, scroll: Boolean = true) {
        val changed = selectedTabId != tabId
        selectedTabId = tabId
        updateColors()
        if (scroll) {
            scrollToSelected()
        }

        if (changed) {
            onTabSelected?.invoke(tabId)
        }
    }

    /** Moves to the next (direction = 1) or previous (direction = -1) tab, returns true if it switched. */
    fun selectAdjacentTab(direction: Int): Boolean {
        if (tabs.isEmpty()) {
            return false
        }

        val ids = listOf<Long?>(null) + tabs.map { it.id }
        val newIndex = ids.indexOf(selectedTabId) + direction
        if (newIndex !in ids.indices) {
            return false
        }

        selectTab(ids[newIndex])
        return true
    }

    fun updateColors() {
        val primaryColor = context.getProperPrimaryColor()
        val textColor = context.getProperTextColor()
        val radius = resources.getDimension(org.fossify.commons.R.dimen.material_button_corner_radius)
        for (i in 0 until holder.childCount) {
            val tabView = holder.getChildAt(i) as TextView
            val isSelected = tabView.tag == selectedTabId
            val backgroundColor = if (isSelected) primaryColor else textColor.adjustAlpha(UNSELECTED_ALPHA)
            val shape = GradientDrawable().apply {
                cornerRadius = radius
                setColor(backgroundColor)
            }

            tabView.background = RippleDrawable(
                ColorStateList.valueOf(textColor.adjustAlpha(RIPPLE_ALPHA)), shape, null
            )
            tabView.setTextColor(if (isSelected) primaryColor.getContrastColor() else textColor)
        }
    }

    private fun rebuild() {
        holder.removeAllViews()
        if (tabs.isEmpty()) {
            return
        }

        val allTitle = context.getString(R.string.all_tab)
        (listOf<Pair<Long?, String>>(null to allTitle) + tabs.map { it.id to it.name }).forEach { (id, name) ->
            holder.addView(createTabView(id, name))
        }

        updateColors()
        post { scrollToSelected() }
    }

    private fun createTabView(tabId: Long?, name: String): TextView {
        val horizontalPadding = resources.getDimensionPixelSize(org.fossify.commons.R.dimen.big_margin)
        val margin = resources.getDimensionPixelSize(org.fossify.commons.R.dimen.tiny_margin)
        return TextView(context).apply {
            tag = tabId
            text = name
            gravity = Gravity.CENTER
            isSingleLine = true
            minWidth = resources.getDimensionPixelSize(R.dimen.drawer_tab_min_width)
            minimumWidth = minWidth
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(org.fossify.commons.R.dimen.bigger_text_size))
            setPadding(horizontalPadding, 0, horizontalPadding, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                resources.getDimensionPixelSize(R.dimen.drawer_tab_height)
            ).apply {
                setMargins(margin, 0, margin, 0)
            }

            setOnClickListener { selectTab(tabId) }
        }
    }

    private fun scrollToSelected() {
        for (i in 0 until holder.childCount) {
            val child = holder.getChildAt(i)
            if (child.tag == selectedTabId) {
                val target = child.left - (width - child.width) / 2
                smoothScrollTo(target.coerceAtLeast(0), 0)
                return
            }
        }
    }

    companion object {
        private const val UNSELECTED_ALPHA = 0.1f
        private const val RIPPLE_ALPHA = 0.2f
    }
}
