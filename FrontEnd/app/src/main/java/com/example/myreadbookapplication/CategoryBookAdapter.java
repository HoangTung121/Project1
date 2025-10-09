package com.example.myreadbookapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.model.Book;

import java.util.List;

public class CategoryBookAdapter extends RecyclerView.Adapter<CategoryBookAdapter.ViewHolder> {
    private List<Book> books;
    private Context context;
    private String categoryName;

    public CategoryBookAdapter(List<Book> books, Context context, String categoryName) {
        this.books = books;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bookTitle.setText(book.getTitle() + " - " + book.getAuthor());  // Thêm author
        // Optional: Thêm rating nếu có TextView ratingTv
        // holder.ratingTv.setText(String.format("★ %.1f (%d)", book.getAvgRating(), book.getNumberOfReviews()));

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.default_book_cover);
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
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
