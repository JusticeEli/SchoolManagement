package com.justice.schoolmanagement.presentation.ui.blog

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.firebase.firestore.*
import com.justice.schoolmanagement.R
import com.justice.schoolmanagement.databinding.ItemBlogBinding
import com.justice.schoolmanagement.presentation.ui.blog.model.Blog
import com.justice.schoolmanagement.presentation.ui.chat.util.FirebaseUtil
import com.justice.schoolmanagement.presentation.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class BlogAdapter(private val requestManager: RequestManager) : androidx.recyclerview.widget.ListAdapter<DocumentSnapshot, BlogAdapter.ViewHolder>(DIFF_UTIL) {

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<DocumentSnapshot>() {
            override fun areItemsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: DocumentSnapshot, newItem: DocumentSnapshot): Boolean {
                val old = oldItem.toObject(Blog::class.java)
                val new = newItem.toObject(Blog::class.java)
                return old!!.equals(new)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
        val binding: ItemBlogBinding = ItemBlogBinding.bind(view)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position).toObject(Blog::class.java)!!

        FirebaseUtil.collectionReferenceTeachers().document(model.userId).get().addOnSuccessListener { documentSnapshot ->
            requestManager.load(documentSnapshot.getString("photo")).into(holder.binding.userProfileImageView)
            holder.binding.userNameTxtView.text = documentSnapshot.getString("firstName")
        }
        val date = SimpleDateFormat("dd/MM/yy : HH:mm:ss").format(model.date)
        holder.binding.dateTxtView.text = date
        requestManager.load(model.photo).into(holder.binding.postImageView)
        holder.binding.descriptionTxtView.text = model.description

        FirebaseUtil.collectionReferenceBlogs().document(model.id!!).collection(Constants.COLLECTION_LIKES).document(FirebaseUtil.getUid()).get().addOnSuccessListener { documentSnapshot ->
            holder.binding.apply {

                if (documentSnapshot.exists()) {
                    holder.hasLiked = true
                    requestManager.load(R.drawable.ic_like).into(likeImageView)
                } else {
                    holder.hasLiked = false
                    requestManager.load(R.drawable.ic_unlike).into(likeImageView)

                }
            }
            holder.binding.likeImageView.setOnClickListener {
                if (holder.hasLiked) {
                    holder.hasLiked = false
                    requestManager.load(R.drawable.ic_unlike).into(it as ImageView)
                    FirebaseUtil.collectionReferenceBlogs().document(model.id!!).collection(Constants.COLLECTION_LIKES).document(FirebaseUtil.getUid()).delete()
                } else {
                    val map: MutableMap<String, String> = HashMap()
                    map["data"] = "data"
                    holder.hasLiked = true
                    requestManager.load(R.drawable.ic_like).into(it as ImageView)
                    FirebaseUtil.collectionReferenceBlogs().document(model.id!!).collection(Constants.COLLECTION_LIKES).document(FirebaseUtil.getUid()).set(map)
                }
            }

            ///////////////////////////////////////////
            FirebaseUtil.collectionReferenceBlogs().document(model.id!!).collection(Constants.COLLECTION_LIKES).addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (queryDocumentSnapshots!!.isEmpty) {
                    holder.binding.numberOfLikesTxtView.text = "0 Likes"
                } else {
                    holder.binding.numberOfLikesTxtView.text = queryDocumentSnapshots.size().toString() + " Likes"
                }
            }

        }

    }


    inner class ViewHolder(val binding: ItemBlogBinding) : RecyclerView.ViewHolder(binding.root) {
        var hasLiked = false
    }


}