package com.example.myreadbookapplication.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myreadbookapplication.R;

import java.util.ArrayList;
import java.util.List;

public class PaginationManager {
    private static final String TAG = "PaginationManager";
    
    // UI Components
    private View paginationView;
    private TextView tvPageInfo;
    private TextView tvTotalItems;
    private Button btnFirstPage;
    private Button btnPrevPage;
    private Button btnNextPage;
    private Button btnLastPage;
    private Button btnToggleQuickJump;
    private LinearLayout containerPageNumbers;
    private LinearLayout quickJumpContainer;
    
    // Pagination State
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalItems = 0;
    private int itemsPerPage = 10;
    private int maxVisiblePages = 5;
    
    // Callbacks
    private OnPageChangeListener pageChangeListener;
    private OnPageJumpListener pageJumpListener;
    
    // Page number buttons
    private List<Button> pageButtons = new ArrayList<>();
    
    public interface OnPageChangeListener {
        void onPageChanged(int page);
    }
    
    public interface OnPageJumpListener {
        void onPageJump(int page);
    }
    
    public PaginationManager(Context context, ViewGroup parent) {
        initViews(context, parent);
        setupClickListeners();
    }
    
    private void initViews(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        paginationView = inflater.inflate(R.layout.pagination_footer, parent, false);
        
        // Initialize UI components
        tvPageInfo = paginationView.findViewById(R.id.tv_page_info);
        tvTotalItems = paginationView.findViewById(R.id.tv_total_items);
        btnFirstPage = paginationView.findViewById(R.id.btn_first_page);
        btnPrevPage = paginationView.findViewById(R.id.btn_prev_page);
        btnNextPage = paginationView.findViewById(R.id.btn_next_page);
        btnLastPage = paginationView.findViewById(R.id.btn_last_page);
        containerPageNumbers = paginationView.findViewById(R.id.container_page_numbers);

        // Add to parent
        parent.addView(paginationView);
    }
    
    private void setupClickListeners() {
        btnFirstPage.setOnClickListener(v -> goToFirstPage());
        btnPrevPage.setOnClickListener(v -> goToPreviousPage());
        btnNextPage.setOnClickListener(v -> goToNextPage());
        btnLastPage.setOnClickListener(v -> goToLastPage());
    }
    
    public void setPaginationData(int currentPage, int totalPages, int totalItems, int itemsPerPage) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.itemsPerPage = itemsPerPage;
        
        updateUI();
    }
    
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.pageChangeListener = listener;
    }
    
    public void setOnPageJumpListener(OnPageJumpListener listener) {
        this.pageJumpListener = listener;
    }
    
    private void updateUI() {
        updatePageInfo();
        updateNavigationButtons();
        updatePageNumbers();
        updateTotalItems();
    }
    
    private void updatePageInfo() {
        tvPageInfo.setText(String.format("Trang %d / %d", currentPage, totalPages));
    }
    
    private void updateTotalItems() {
        int startItem = (currentPage - 1) * itemsPerPage + 1;
        int endItem = Math.min(currentPage * itemsPerPage, totalItems);
        tvTotalItems.setText(String.format("%d-%d / %d mục", startItem, endItem, totalItems));
    }
    
    private void updateNavigationButtons() {
        btnFirstPage.setEnabled(currentPage > 1);
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnLastPage.setEnabled(currentPage < totalPages);
    }
    
    private void updatePageNumbers() {
        // Clear existing page buttons
        containerPageNumbers.removeAllViews();
        pageButtons.clear();
        
        if (totalPages <= 1) {
            return;
        }
        
        // Calculate visible page range
        int startPage = Math.max(1, currentPage - maxVisiblePages / 2);
        int endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
        
        // Adjust start page if we're near the end
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }
        
        // Add first page and ellipsis if needed
        if (startPage > 1) {
            addPageButton(1);
            if (startPage > 2) {
                addEllipsisButton();
            }
        }
        
        // Add visible page numbers
        for (int i = startPage; i <= endPage; i++) {
            addPageButton(i);
        }
        
        // Add ellipsis and last page if needed
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                addEllipsisButton();
            }
            addPageButton(totalPages);
        }
    }
    
    private void addPageButton(int pageNumber) {
        Button button = new Button(paginationView.getContext());
        button.setText(String.valueOf(pageNumber));
        button.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Apply style
        button.setBackgroundResource(R.drawable.pagination_button_background);
        button.setTextSize(10);
        button.setPadding(8, 4, 8, 4);
        button.setMinWidth(32);
        button.setMinHeight(32);
        
        // Set state
        if (pageNumber == currentPage) {
            button.setSelected(true);
            button.setTextColor(paginationView.getContext().getColor(R.color.surface_color));
        } else {
            button.setSelected(false);
            button.setTextColor(paginationView.getContext().getColor(R.color.primary_text));
        }
        
        // Set click listener
        button.setOnClickListener(v -> goToPage(pageNumber));
        
        // Add margin
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.setMargins(4, 0, 4, 0);
        button.setLayoutParams(params);
        
        containerPageNumbers.addView(button);
        pageButtons.add(button);
    }
    
    private void addEllipsisButton() {
        Button ellipsisButton = new Button(paginationView.getContext());
        ellipsisButton.setText("...");
        ellipsisButton.setEnabled(false);
        ellipsisButton.setBackgroundColor(paginationView.getContext().getColor(R.color.surface_color));
        ellipsisButton.setTextColor(paginationView.getContext().getColor(R.color.secondary_text));
        ellipsisButton.setMinWidth(40);
        ellipsisButton.setMinHeight(40);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(4, 0, 4, 0);
        ellipsisButton.setLayoutParams(params);
        
        containerPageNumbers.addView(ellipsisButton);
    }
    
    private void goToFirstPage() {
        if (currentPage > 1) {
            goToPage(1);
        }
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            goToPage(currentPage - 1);
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages) {
            goToPage(currentPage + 1);
        }
    }
    
    private void goToLastPage() {
        if (currentPage < totalPages) {
            goToPage(totalPages);
        }
    }
    
    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        
        // Animate page change
        animatePageChange(() -> {
            currentPage = page;
            updateUI();
            
            if (pageChangeListener != null) {
                pageChangeListener.onPageChanged(page);
            }
        });
    }
    
    private void jumpToPage(int page) {
        if (page < 1 || page > totalPages) {
            Log.w(TAG, "Invalid page number for jump: " + page);
            return;
        }
        
        animatePageChange(() -> {
            currentPage = page;
            updateUI();
            
            if (pageJumpListener != null) {
                pageJumpListener.onPageJump(page);
            }
        });
        
        // Hide quick jump and reset toggle button
        quickJumpContainer.setVisibility(View.GONE);
        btnToggleQuickJump.setText("...");
    }
    
    private void animatePageChange(Runnable onComplete) {
        // Fade out animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(paginationView, "alpha", 1f, 0.5f);
        fadeOut.setDuration(150);
        
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onComplete.run();
                
                // Fade in animation
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(paginationView, "alpha", 0.5f, 1f);
                fadeIn.setDuration(150);
                fadeIn.start();
            }
        });
        
        fadeOut.start();
    }

    
    
    public void setVisible(boolean visible) {
        paginationView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    public View getView() {
        return paginationView;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public int getItemsPerPage() {
        return itemsPerPage;
    }
}
