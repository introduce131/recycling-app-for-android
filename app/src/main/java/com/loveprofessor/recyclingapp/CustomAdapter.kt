package com.loveprofessor.recyclingapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loveprofessor.recyclingapp.databinding.RecyclerviewItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        holder.category.text = getCategoryName(listData[position].list_category)
        holder.uploadDt.text = getFormatDate(listData[position].list_uploadDt)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(val binding: RecyclerviewItemBinding): RecyclerView.ViewHolder(binding.root) {
        val image = binding.rvImage
        val category = binding.rvCategory
        val uploadDt = binding.rvUploadDt
    }

    // category 이름을 얻어오는 메서드 getCategoryName
    private fun getCategoryName(category:String):String {
        return when(category) {
            "aluminum-can" -> "캔류"
            "aluminum-foil" -> "쿠킹호일류"
            "glass-bottle" -> "유리병류"
            "glass-jar" -> "유리용기류"
            "paper-carbon" -> "종이팩류"
            "paper-cardboard" -> "종이상자류"
            "paper-common" -> "종이류"
            "plastic-bags" -> "비닐류"
            "plastic-bottle" -> "플라스틱 병류"
            "plastic-container" -> "플라스틱 용기류"
            "plastic-disposable" -> "일회룡 식기류"
            else -> "Not Found"
        }
    }

    // dateFormating을 처리하는 메서드 getFormatDate
    private fun getFormatDate(uploadDt:String):String {
        val inputFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // 문자열 uploadDt를 Date로 형 변환하고 date변수에 집어넣음
        val date:Date = inputFormat.parse(uploadDt)

        return outputFormat.format(date)
    }
}