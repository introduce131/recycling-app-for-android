package com.loveprofessor.recyclingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loveprofessor.recyclingapp.databinding.RecyclerviewItemBinding

class CustomAdapter(val listData: ArrayList<ListData>): RecyclerView.Adapter<CustomAdapter.Holder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.Holder {
        val binding = RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: CustomAdapter.Holder, position: Int) {
        // Glide 사용하여 URL로 이미지 삽입하기
        // holder.binding.root.context는 이 뷰가 속한 액티비티, 프래그먼트의 context를 가져온다는 뜻
        Glide.with(holder.binding.root.context)
            .load(listData[position].list_img)
            .into(holder.image)

        holder.category.text = listData[position].list_category
        holder.uploadDt.text = listData[position].list_uploadDt.toString()
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: RecyclerviewItemBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.rvImage
        val category = binding.rvCategory
        val uploadDt = binding.rvUploadDt
    }
}