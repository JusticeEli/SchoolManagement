package com.justice.schoolmanagement.presentation.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemImageMessageBinding
import com.justice.schoolmanagement.databinding.ItemTextMessageBinding
import com.justice.schoolmanagement.presentation.ui.chat.model.ImageMessage
import com.justice.schoolmanagement.presentation.ui.chat.model.Message
import com.justice.schoolmanagement.presentation.ui.chat.model.MessageType
import com.justice.schoolmanagement.presentation.ui.chat.model.TextMessage
import java.text.SimpleDateFormat

class ChatAdapter(private val requestManager: RequestManager) : ListAdapter<DocumentSnapshot, RecyclerView.ViewHolder>(DIFF_UTIL) {

    companion object {

        val TAG = "ChatFragmentAdapter"
        val TEXT_TYPE = 1
        val IMAGE_TYPE = 2

        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(Message::class.java)
                val new = newItem.toObject(Message::class.java)
                return old!!.equals(new)
            }

        }
    }

    inner class ViewHolderText(val binding: ItemTextMessageBinding) : RecyclerView.ViewHolder(binding.root) {}
    inner class ViewHolderImage(val binding: ItemImageMessageBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TEXT_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text_message, parent, false)
            val binding: ItemTextMessageBinding = ItemTextMessageBinding.bind(view)

            return ViewHolderText(binding)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_message, parent, false)
            val binding: ItemImageMessageBinding = ItemImageMessageBinding.bind(view)

            return ViewHolderImage(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val originalMessage = currentList[position].toObject(Message::class.java)!!
        if (getItemViewType(position) == TEXT_TYPE) {

            val message = originalMessage as TextMessage
            (holder as ViewHolderText).binding.textViewMessageText.text = message.text
            setTimeTextForTxtMessage(holder, originalMessage)
            setMessageRootGravityText(holder, originalMessage)
        } else {
            val viewHolder = (holder as ViewHolderImage)
            val message = currentList[position] as ImageMessage
            requestManager.load(message.imagePath).into(viewHolder.binding.imageViewMessageImage)
            setTimeTextForImageMessage(viewHolder, message)
            setMessageRootGravityImage(viewHolder, originalMessage)

        }

    }

    private fun setTimeTextForTxtMessage(viewHolder: ViewHolderText, message: Message) {
        val dateFormat = SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.binding.textViewMessageTime.text = dateFormat.format(message.time)
    }

    private fun setTimeTextForImageMessage(viewHolder: ViewHolderImage, message: Message) {
        val dateFormat = SimpleDateFormat
                .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.binding.textViewMessageTime.text = dateFormat.format(message.time)
    }

    private fun setMessageRootGravityText(viewHolder: ViewHolderText, message: Message) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            viewHolder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_white)
                val lParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.END

                }
                this.layoutParams = lParams
            }
        } else {
            viewHolder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)

                val lParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.START

                }
                this.layoutParams = lParams
            }
        }
    }

    private fun setMessageRootGravityImage(viewHolder: ViewHolderImage, message: Message) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            viewHolder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_white)
                val lParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.END

                }
                this.layoutParams = lParams
            }
        } else {
            viewHolder.binding.messageRoot.apply {
                setBackgroundResource(R.drawable.rect_round_primary_color)
                val lParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.START
                }
                this.layoutParams = lParams
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = currentList[position].toObject(Message::class.java)!!

        if (message.type == MessageType.TEXT) {
            return TEXT_TYPE

        } else {
            return IMAGE_TYPE
        }
    }
}