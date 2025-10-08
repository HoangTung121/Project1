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
    private List<String> bookTitles; // danh sách tên sách
    private Context context;
    private String categoryName;

    public CategoryBookAdapter(List<String> bookTitles, Context context, String categoryName) {
        this.bookTitles = bookTitles;
        this.context = context;
        this.categoryName = categoryName;
    }


    @NonNull
    @Override
    public CategoryBookAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryBookAdapter.ViewHolder holder, int position) {
        String title = bookTitles.get(position);
        holder.bookTitle.setText(title);

        holder.bookCover.setImageResource(R.drawable.honor_image);
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
