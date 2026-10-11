package org.fossify.home.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.extensions.getProperBackgroundColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.home.databinding.ItemTabAppPickerBinding
import org.fossify.home.models.AppLauncher

class TabAppsPickerAdapter(
    val activity: Activity,
    val selectedIdentifiers: MutableSet<String>,
) : RecyclerView.Adapter<TabAppsPickerAdapter.ViewHolder>() {

    private var apps = emptyList<AppLauncher>()
    private val textColor = activity.getProperTextColor()
    private val primaryColor = activity.getProperPrimaryColor()
    private val backgroundColor = activity.getProperBackgroundColor()

    @Suppress("NotifyDataSetChanged")
    fun setApps(newApps: List<AppLauncher>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun getItemCount() = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTabAppPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    inner class ViewHolder(private val binding: ItemTabAppPickerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppLauncher) {
            val identifier = app.getLauncherIdentifier()
            binding.tabAppLabel.text = app.title
            binding.tabAppLabel.setTextColor(textColor)
            binding.tabAppIcon.setImageDrawable(app.drawable)
            binding.tabAppCheckbox.setColors(textColor, primaryColor, backgroundColor)
            binding.tabAppCheckbox.isChecked = selectedIdentifiers.contains(identifier)
            binding.root.setOnClickListener {
                if (!selectedIdentifiers.add(identifier)) {
                    selectedIdentifiers.remove(identifier)
                }
                binding.tabAppCheckbox.isChecked = selectedIdentifiers.contains(identifier)
            }
        }
    }
}
