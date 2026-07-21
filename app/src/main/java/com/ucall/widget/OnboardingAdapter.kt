package com.ucall.widget.lite

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(
    private val images: List<Int>
) : RecyclerView.Adapter<OnboardingAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_image, parent, false) as ImageView
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.iv.setImageResource(images[position])
    }

    override fun getItemCount() = images.size

    class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)
}
