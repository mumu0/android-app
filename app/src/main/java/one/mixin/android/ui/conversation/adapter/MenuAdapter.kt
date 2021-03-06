package one.mixin.android.ui.conversation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_chat_menu.view.*
import one.mixin.android.MixinApplication
import one.mixin.android.R
import one.mixin.android.ui.conversation.holder.MenuHolder
import org.jetbrains.anko.dip

class MenuAdapter(private val onMenuClickListener: OnMenuClickListener) : RecyclerView.Adapter<MenuHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MenuHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_menu, parent, false))

    private val dp16 by lazy {
        MixinApplication.appContext.dip(16)
    }

    private val dp24 by lazy {
        MixinApplication.appContext.dip(24)
    }

    var isGroup = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isBot = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isSelfCreatedBot = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: MenuHolder, position: Int) {
        when (position) {
            0 -> holder.itemView.setPadding(dp16, 0, 0, 0)
            itemCount - 1 -> holder.itemView.setPadding(dp24, 0, dp16, 0)
            else -> holder.itemView.setPadding(dp24, 0, 0, 0)
        }
        var index = when {
            isGroup -> position + 2
            isBot && !isSelfCreatedBot -> position + 2
            isBot && isSelfCreatedBot -> position + 1
            else -> position
        }

        if (isSelfCreatedBot && position == 0) {
            index -= 1
        }

        holder.itemView.menu_icon.setBackgroundResource(backgrounds[index])
        holder.itemView.menu_icon.setImageResource(icons[index])
        holder.itemView.menu_title.setText(titles[index])
        holder.itemView.setOnClickListener {
            onMenuClickListener.onMenuClick(ids[index])
        }
    }

    companion object {
        val icons = intArrayOf(R.drawable.ic_selector_transfer, R.drawable.ic_selector_voice,
            R.drawable.ic_selector_camera, R.drawable.ic_selector_gallery,
            R.drawable.ic_selector_document, R.drawable.ic_selector_contact)
        val backgrounds = intArrayOf(R.drawable.bg_selector_contact, R.drawable.bg_selector_voice,
            R.drawable.bg_selector_camera, R.drawable.bg_selector_gallery,
            R.drawable.bg_selector_document, R.drawable.bg_selector_contact)
        val titles = intArrayOf(R.string.transfer, R.string.voice, R.string.camera, R.string.gallery,
            R.string.document, R.string.contact)
        val ids = intArrayOf(R.id.menu_transfer, R.id.menu_voice, R.id.menu_camera, R.id.menu_gallery,
            R.id.menu_document, R.id.menu_contact)
    }

    override fun getItemCount(): Int {
        return if (isGroup) {
            icons.size - 2
        } else if (isBot && !isSelfCreatedBot) {
            icons.size - 2
        } else if (isBot && isSelfCreatedBot) {
            icons.size - 1
        } else {
            icons.size
        }
    }

    interface OnMenuClickListener {
        fun onMenuClick(id: Int)
    }
}
