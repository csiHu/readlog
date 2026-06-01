package com.example.readlog.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;

import com.example.readlog.LibraryAdapter;
import com.example.readlog.MainActivity;
import com.example.readlog.databinding.FragmentPlannedBinding;
import com.example.readlog.model.LibraryDatabase;

public class PlannedFragment extends Fragment {
    private FragmentPlannedBinding binding;
    private LibraryAdapter adapter;
    private LibraryDatabase db;

    public PlannedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlannedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            db = ((MainActivity) getActivity()).getDatabase();
        }

        adapter = new LibraryAdapter(new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showEditDialog(item);
            }
        });

        adapter.setOnPlusOneClickListener(item -> {
            item.setCurrentProgress(1);
            item.setStatus("READING");

            if (item.getMaxProgress() > 0 && item.getCurrentProgress() >= item.getMaxProgress()) {
                item.setStatus("COMPLETED");
            }

            if (db != null) {
                new Thread(() -> db.libraryDAO().updateLibraryItem(item)).start();
            }
        });

        if (db != null) {
            db.libraryDAO().getItemsByStatus("PLANNED").observe(getViewLifecycleOwner(), books -> {
                if (books != null) {
                    adapter.updateData(books);

                    if (getActivity() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        int count = books.size();

                        mainActivity.binding.totalCountTextView.setText(
                                String.format(java.util.Locale.getDefault(), "All (%d)", count)
                        );
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}