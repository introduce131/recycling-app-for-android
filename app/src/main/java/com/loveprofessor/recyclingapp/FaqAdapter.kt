package com.loveprofessor.recyclingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loveprofessor.recyclingapp.databinding.FaqRecyclerviewItemBinding

class FaqAdapter(var listData: ArrayList<FaqListData>) : RecyclerView.Adapter<FaqAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqAdapter.Holder {
        val binding = FaqRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: FaqAdapter.Holder, position: Int) {
        holder.qText.text = "Q."
        holder.question.text = listData[position].list_question
        holder.nextButton.setImageResource(R.drawable.ic_button_next)
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
        val nextButton = binding.rvNextButton
    }
}