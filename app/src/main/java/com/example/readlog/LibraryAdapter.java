package com.example.readlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.readlog.databinding.LibraryItemBinding;
import com.example.readlog.model.LibraryItem;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<LibraryItem> data;
    private OnItemLongClickListener longClickListener;
    private OnPlusOneClickListener plusOneClickListener;

    public interface OnPlusOneClickListener {
        void onPlusOneClick(LibraryItem item);
    }

    public void setOnPlusOneClickListener(OnPlusOneClickListener listener) {
        this.plusOneClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(LibraryItem item);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public LibraryAdapter(List<LibraryItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryAdapter.ViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public LibraryItem getDataItemAt(int position){
        return data.get(position);
    }

    public void updateData(List<LibraryItem> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LibraryItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LibraryItemBinding.bind(itemView);

            itemView.setOnLongClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(data.get(position));
                    return true;
                }
                return false;
            });

            binding.plusOneButton.setOnClickListener(view -> {
                int position = getAbsoluteAdapterPosition();
                if (plusOneClickListener != null && position != RecyclerView.NO_POSITION) {
                    plusOneClickListener.onPlusOneClick(data.get(position));
                }
            });
        }

        void setData(LibraryItem item) {
            binding.textBookTitle.setText(item.getTitle());
            binding.textBookAuthor.setText(item.getAuthor());

            String maxProgressText = item.getMaxProgress() <= 0 ? "?" : String.valueOf(item.getMaxProgress());
            String progress = item.getCurrentProgress() + " / " + maxProgressText;
            binding.textProgress.setText(progress);

            int progressPercent = 0;
            int current = item.getCurrentProgress();
            int max = item.getMaxProgress();

            if ("COMPLETED".equals(item.getStatus())) {
                progressPercent = 100;
                binding.plusOneButton.setVisibility(View.GONE);
            } else if ("PLANNED".equals(item.getStatus())) {
                binding.plusOneButton.setVisibility(View.VISIBLE);
            } else {
                binding.plusOneButton.setVisibility(View.VISIBLE);
                if (max > 0) {
                    progressPercent = (int) (((double) current / max) * 100);
                } else {
                    progressPercent = 50;
                }
            }

            binding.cardProgressBar.setProgress(progressPercent);
        }
    }
}