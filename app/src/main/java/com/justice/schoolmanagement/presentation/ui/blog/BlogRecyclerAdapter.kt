package com.justice.schoolmanagement.presentation.ui.blog

/*

class BlogRecyclerAdapter(private val blogFragment: BlogFragment, options: FirestoreRecyclerOptions<Blog?>) : FirestoreRecyclerAdapter<Blog, BlogRecyclerAdapter.ViewHolder>(options) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Blog) {
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.TEACHERS).document(model.userId).get().addOnSuccessListener { documentSnapshot ->
            Glide.with(blogFragment.requireContext()).load(documentSnapshot.getString("photo")).into(holder.binding.userProfileImageView)
            holder.binding.userNameTxtView.text = documentSnapshot.getString("firstName")
            Toast.makeText(blogFragment.requireContext(), "Success loading user Data", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e -> Toast.makeText(blogFragment.requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show() }
        val date = SimpleDateFormat("dd/MM/yy : HH:mm:ss").format(model.date)
        holder.binding.dateTxtView.text = date
        Glide.with(blogFragment.requireContext()).load(model.photo).into(    holder.binding.postImageView)
        holder.binding.descriptionTxtView.text = model.description

        ///////////////////////////////////////////
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.BLOGS).document(model.id).collection("likes").document(FirebaseAuth.getInstance().uid!!).get().addOnSuccessListener { documentSnapshot ->
            holder.binding.apply {


                if (documentSnapshot.exists()) {
                    holder.hasLiked = true
                    likeImageView.setImageDrawable(ContextCompat.getDrawable(blogFragment.requireContext(), R.drawable.ic_like))
                } else {
                    holder.hasLiked = false
                    likeImageView.setImageDrawable(ContextCompat.getDrawable(blogFragment.requireContext(), R.drawable.ic_unlike))
                }
            }
            holder.binding.likeImageView.setOnClickListener {
                if (holder.hasLiked) {
                    holder.hasLiked = false
                    holder.binding.likeImageView.setImageDrawable(ContextCompat.getDrawable(blogFragment.requireContext(), R.drawable.ic_unlike))
                    FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.BLOGS).document(model.id).collection("likes").document(FirebaseAuth.getInstance().uid!!).delete()
                } else {
                    val map: MutableMap<String, Any> = HashMap()
                    map["data"] = "data"
                    holder.hasLiked = true
                    holder.binding.likeImageView.setImageDrawable(ContextCompat.getDrawable(blogFragment.requireContext(), R.drawable.ic_like))
                    FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.BLOGS).document(model.id).collection("likes").document(FirebaseAuth.getInstance().uid!!).set(map)
                }
            }

            ///////////////////////////////////////////
            FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ROOT + Constants.DOCUMENT_CODE + Constants.BLOGS).document(model.id).collection("likes").addSnapshotListener(blogFragment.requireActivity(), EventListener { queryDocumentSnapshots, e ->
                if (e != null) {
                    Toast.makeText(blogFragment.requireContext(), "Error: " + e.message, Toast.LENGTH_SHORT).show()
                    return@EventListener
                }
                if (queryDocumentSnapshots!!.isEmpty) {
                    holder.binding.numberOfLikesTxtView.text = "0 Likes"
                } else {
                    holder.binding.numberOfLikesTxtView.text = queryDocumentSnapshots.size().toString() + " Likes"
                }
            })

        }


    }

    fun getBlogReferenceByPosition(position: Int): DocumentReference {
        return snapshots.getSnapshot(position).reference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
        val binding: ItemBlogBinding = ItemBlogBinding.bind(view)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: ItemBlogBinding) : RecyclerView.ViewHolder(binding.root) {

        var hasLiked = false


    }
}*/
