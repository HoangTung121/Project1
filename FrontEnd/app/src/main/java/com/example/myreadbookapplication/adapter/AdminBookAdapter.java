package com.example.myreadbookapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myreadbookapplication.R;
import com.example.myreadbookapplication.model.Book;

import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;
    private OnBookActionListener listener;

    public interface OnBookActionListener {
        void onEditClick(Book book);
        void onDeleteClick(Book book);
    }

    public AdminBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public void setOnBookActionListener(OnBookActionListener listener) {
        this.listener = listener;
    }

    public void updateBookList(List<Book> newBookList) {
        this.bookList = newBookList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_admin, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return bookList != null ? bookList.size() : 0;
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBookCover;
        private TextView tvBookTitle;
        private TextView tvBookCategory;
        private TextView tvBookFeatured;
        private ImageView ivEdit;
        private ImageView ivDelete;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.iv_book_cover);
            tvBookTitle = itemView.findViewById(R.id.tv_book_title);
            tvBookCategory = itemView.findViewById(R.id.tv_book_category);
            tvBookFeatured = itemView.findViewById(R.id.tv_book_featured);
            ivEdit = itemView.findViewById(R.id.iv_edit);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(Book book) {
            // Load book cover image
            if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
                Glide.with(context)
                        .load(book.getCoverUrl())
                        .placeholder(R.drawable.default_book_cover)
                        .error(R.drawable.default_book_cover)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.default_book_cover);
            }

            // Set book title
            tvBookTitle.setText(book.getTitle() != null ? book.getTitle() : "Unknown");

            // Set category
            String categoryText = "Category: " + (book.getCategoryName() != null ? book.getCategoryName() : "N/A");
            tvBookCategory.setText(categoryText);

            // Set featured status (assuming it's stored somewhere, for now just show "No")
            tvBookFeatured.setText("Featured: No");

            // Set up click listeners
            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(book);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(book);
                }
            });
        }
    }
}

