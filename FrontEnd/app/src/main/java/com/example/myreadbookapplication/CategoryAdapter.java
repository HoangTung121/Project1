package com.example.myreadbookapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<String> categories;
    private Context context;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(List<String> categories, Context context, OnCategoryClickListener listener) {
        this.categories = categories;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryName.setText(category);
        switch (category.toLowerCase()) {
            case "horror":
                holder.category_icon.setImageResource(R.drawable.honor_image);
                break;
            case "romance":
                holder.category_icon.setImageResource(R.drawable.romance_image);
                break;
            case "sci-fi":
                holder.category_icon.setImageResource(R.drawable.scifi_image);
                break;
            case "fantasy":
                holder.category_icon.setImageResource(R.drawable.fantasy_image);
                break;
            case "mystery":
                holder.category_icon.setImageResource(R.drawable.mystery_image);
        }
        // Xử lý sự kiện click vào category
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView category_icon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            category_icon = itemView.findViewById(R.id.category_icon);
        }
    }
}
