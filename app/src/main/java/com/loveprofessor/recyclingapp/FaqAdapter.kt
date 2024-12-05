package com.loveprofessor.recyclingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loveprofessor.recyclingapp.databinding.FaqRecyclerviewItemBinding

class FaqAdapter(
    private var listData: ArrayList<FaqListData>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FaqAdapter.Holder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = FaqRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.qText.text = "Q."
        holder.question.text = listData[position].list_question

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(holder.itemView, position)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun setItems(list: ArrayList<FaqListData>) {
        listData.clear()
        listData.addAll(list)
        Log.d("FaqAdapter", "New data set, list size: ${listData.size}")
        notifyDataSetChanged()  // 데이터가 변경된 후 리사이클러뷰 갱신
    }

    inner class Holder(val binding: FaqRecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val qText = binding.rvQText
        val question = binding.rvQuestion
    }
}
