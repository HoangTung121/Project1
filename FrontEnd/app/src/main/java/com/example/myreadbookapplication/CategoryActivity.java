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

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.ViewHolder> {

    private List<String> bookTitles;
    private Context context;
    private String categoryName;  // Để set label dynamic (e.g., "Horror")

    public CategoryBookAdapter(List<String> bookTitles, Context context, String categoryName) {
        this.bookTitles = bookTitles;
        this.context = context;
        this.categoryName = categoryName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String title = bookTitles.get(position);
        holder.bookTitle.setText(categoryName);  // Label category (dynamic)
        // Load ảnh: Placeholder, thay bằng dynamic (e.g., Glide nếu URL)
        holder.bookCover.setImageResource(R.drawable.horror_image);  // Hoặc R.drawable.book_cover + position
    }

    @Override
    public int getItemCount() {
        return bookTitles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_category_label);
        }
    }
}