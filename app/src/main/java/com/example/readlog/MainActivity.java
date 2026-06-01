package com.example.readlog;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.readlog.databinding.ActivityMainBinding;
import com.example.readlog.databinding.DialogAddItemBinding;
import com.example.readlog.databinding.DialogEditItemBinding;
import com.example.readlog.fragments.CompletedFragment;
import com.example.readlog.fragments.PlannedFragment;
import com.example.readlog.fragments.ReadingFragment;
import com.example.readlog.model.LibraryDatabase;
import com.example.readlog.model.LibraryItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    LibraryDatabase libraryDatabase;
    public ActivityMainBinding binding;
    private String currentTabStatus = "READING";
    private final String[] statusOptions = {"READING", "PLANNED", "COMPLETED"};
    private String[] statusDisplayNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        statusDisplayNames = new String[]{
                getString(R.string.status_reading),
                getString(R.string.status_planned),
                getString(R.string.status_completed)
        };

        libraryDatabase = Room.databaseBuilder(this, LibraryDatabase.class, "library_db")
                .fallbackToDestructiveMigration(true)
                .build();

        if (savedInstanceState == null) {
            setCurrentFragment(new ReadingFragment());
            binding.titleTextView.setText(R.string.status_reading);
            currentTabStatus = "READING";
        }

        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_reading) {
                setCurrentFragment(new ReadingFragment());
                binding.titleTextView.setText(R.string.status_reading);
                currentTabStatus = "READING";
            } else if (itemId == R.id.nav_planned) {
                setCurrentFragment(new PlannedFragment());
                binding.titleTextView.setText(R.string.status_planned);
                currentTabStatus = "PLANNED";
            } else if (itemId == R.id.nav_completed) {
                setCurrentFragment(new CompletedFragment());
                binding.titleTextView.setText(R.string.status_completed);
                currentTabStatus = "COMPLETED";
            } else {
                return false;
            }
            return true;
        });
    }

    public LibraryDatabase getDatabase() {
        return libraryDatabase;
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void addItem(View view) {
        DialogAddItemBinding dialogBinding = DialogAddItemBinding.inflate(getLayoutInflater());
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statusDisplayNames);
        dialogBinding.spinnerStatus.setAdapter(adapter);

        int defaultPosition = getStatusPosition(currentTabStatus);
        dialogBinding.spinnerStatus.setText(statusDisplayNames[defaultPosition], false);

        dialogBinding.btnSave.setOnClickListener(v -> {
            String title = Objects.requireNonNull(dialogBinding.editTitle.getText()).toString().trim();
            String author = Objects.requireNonNull(dialogBinding.editAuthor.getText()).toString().trim();
            String currentStr = Objects.requireNonNull(dialogBinding.editCurrentProgress.getText()).toString().trim();
            String maxStr = Objects.requireNonNull(dialogBinding.editMaxProgress.getText()).toString().trim();

            String selectedText = dialogBinding.spinnerStatus.getText().toString();
            int selectedIndex = 0;
            for (int i = 0; i < statusDisplayNames.length; i++) {
                if (statusDisplayNames[i].equals(selectedText)) {
                    selectedIndex = i;
                    break;
                }
            }
            String selectedStatus = statusOptions[selectedIndex];

            if (title.isEmpty()) return;

            int currentProgress = currentStr.isEmpty() ? 0 : Integer.parseInt(currentStr);
            int maxProgress = maxStr.isEmpty() ? 0 : Integer.parseInt(maxStr);

            LibraryItem newItem = new LibraryItem();
            newItem.setTitle(title);
            newItem.setAuthor(author);
            newItem.setCurrentProgress(currentProgress);
            newItem.setMaxProgress(maxProgress);
            newItem.setStatus(selectedStatus);

            checkAndProcessAutoMove(newItem);
            saveOrUpdateInBg(newItem, false);

            bottomSheetDialog.dismiss();
        });

        dialogBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    public void showEditDialog(LibraryItem item) {
        DialogEditItemBinding dialogBinding = DialogEditItemBinding.inflate(getLayoutInflater());
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(dialogBinding.getRoot());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statusDisplayNames);
        dialogBinding.spinnerStatus.setAdapter(adapter);

        dialogBinding.editTitle.setText(item.getTitle());
        dialogBinding.editAuthor.setText(item.getAuthor());
        dialogBinding.editCurrentProgress.setText(String.valueOf(item.getCurrentProgress()));
        dialogBinding.editMaxProgress.setText(item.getMaxProgress() <= 0 ? "" : String.valueOf(item.getMaxProgress()));

        int currentPosition = getStatusPosition(item.getStatus());
        dialogBinding.spinnerStatus.setText(statusDisplayNames[currentPosition], false);

        dialogBinding.btnSave.setOnClickListener(v -> {
            String title = Objects.requireNonNull(dialogBinding.editTitle.getText()).toString().trim();
            String author = Objects.requireNonNull(dialogBinding.editAuthor.getText()).toString().trim();
            String currentStr = Objects.requireNonNull(dialogBinding.editCurrentProgress.getText()).toString().trim();
            String maxStr = Objects.requireNonNull(dialogBinding.editMaxProgress.getText()).toString().trim();

            String selectedText = dialogBinding.spinnerStatus.getText().toString();
            int selectedIndex = 0;
            for (int i = 0; i < statusDisplayNames.length; i++) {
                if (statusDisplayNames[i].equals(selectedText)) {
                    selectedIndex = i;
                    break;
                }
            }
            String selectedStatus = statusOptions[selectedIndex];

            if (title.isEmpty()) return;

            int currentProgress = currentStr.isEmpty() ? 0 : Integer.parseInt(currentStr);
            int maxProgress = maxStr.isEmpty() ? 0 : Integer.parseInt(maxStr);

            item.setTitle(title);
            item.setAuthor(author);
            item.setCurrentProgress(currentProgress);
            item.setMaxProgress(maxProgress);
            item.setStatus(selectedStatus);

            checkAndProcessAutoMove(item);
            saveOrUpdateInBg(item, true);

            bottomSheetDialog.dismiss();
        });

        dialogBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        dialogBinding.btnDelete.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            new Thread(() -> libraryDatabase.libraryDAO().deleteLibraryItem(item)).start();
        });

        bottomSheetDialog.show();
    }

    private int getStatusPosition(String status) {
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(status)) return i;
        }
        return 0;
    }

    private void checkAndProcessAutoMove(LibraryItem item) {
        if (item.getMaxProgress() > 0 && item.getCurrentProgress() >= item.getMaxProgress()) {
            item.setStatus("COMPLETED");
        }
    }

    private void saveOrUpdateInBg(LibraryItem item, boolean isUpdate) {
        new Thread(() -> {
            if (isUpdate) {
                libraryDatabase.libraryDAO().updateLibraryItem(item);
            } else {
                libraryDatabase.libraryDAO().insertLibraryItem(item);
            }
        }).start();
    }
}