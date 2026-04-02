package com.ques_4_ass;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    public interface OnImageClickListener {
        void onImageClick(ImageItem item);
    }

    private final List<ImageItem> images;
    private final OnImageClickListener clickListener;

    public ImageAdapter(List<ImageItem> images, OnImageClickListener clickListener) {
        this.images = images;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem imageItem = images.get(position);
        holder.ivThumb.setImageURI(imageItem.uri);
        holder.itemView.setOnClickListener(v -> clickListener.onImageClick(imageItem));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
        }
    }
}
