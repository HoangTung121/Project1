package com.example.myreadbookapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;  // Thêm import này
import com.example.myreadbookapplication.model.Book;

import java.util.List;

public class NewBookAdapter extends RecyclerView.Adapter<NewBookAdapter.ViewHolder> {
    private List<Book> newBooks;  // Book đã import ngầm qua package
    private Context context;

    public NewBookAdapter(List<Book> newBooks, Context context) {
        this.newBooks = newBooks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = newBooks.get(position);
        holder.bookTitle.setText(book.getTitle());
        // Load ảnh với Glide (nếu imageUrl là URL; nếu local, dùng setImageResource)
        Glide.with(context).load(book.getImageUrl()).into(holder.bookCover);
    }

    @Override
    public int getItemCount() {
        return newBooks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
        }
    }
}