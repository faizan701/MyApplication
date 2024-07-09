package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LandingPage extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter<FireBaseModel, CompanyViewHolder> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();RecyclerView recyclerView = findViewById(R.id.companyRecyclerView);
        Toolbar myToolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setHasFixedSize(true);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(LandingPage.this, createrow.class);
            startActivity(intent);
        });

        if (firebaseUser != null) {
            Query query = firestore.collection("Notes")
                    .document(firebaseUser.getUid())
                    .collection("MyNotes")
                    .orderBy("Title", Query.Direction.ASCENDING);

            FirestoreRecyclerOptions<FireBaseModel> allusernotes = new FirestoreRecyclerOptions.Builder<FireBaseModel>()
                    .setQuery(query, FireBaseModel.class)
                    .build();

            adapter = new FirestoreRecyclerAdapter<FireBaseModel, CompanyViewHolder>(allusernotes) {
                @Override
                protected void onBindViewHolder(@NonNull CompanyViewHolder holder, int position, @NonNull FireBaseModel model) {
                    holder.TitleTextView.setText(model.getTitle());
                    holder.ContentTextView.setText(model.getContent());

                    holder.itemView.setOnClickListener(view -> {
                        int pos = holder.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            showActionDialog(pos);
                        }
                    });
                }

                @NonNull
                @Override
                public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
                    return new CompanyViewHolder(view);
                }
            };

            recyclerView.setAdapter(adapter);

            query.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.w("LandingPage", "Error fetching documents.", e);
                    Toast.makeText(LandingPage.this, "Error fetching notes.", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(LandingPage.this, "Please log in to view notes.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LandingPage.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            performLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showActionDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        handleEditAction(position);
                    } else if (which == 1) {
                        handleDeleteAction(position);
                    }
                })
                .show();
    }

    private void handleEditAction(int position) {
        DocumentSnapshot documentSnapshot = adapter.getSnapshots().getSnapshot(position);
        String documentId = documentSnapshot.getId();

        Intent intent = new Intent(LandingPage.this, createrow.class);
        intent.putExtra("documentId", documentId);
        startActivityForResult(intent, 1);
    }

    private void handleDeleteAction(int position) {
        DocumentSnapshot documentSnapshot = adapter.getSnapshots().getSnapshot(position);
        String documentId = documentSnapshot.getId();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firestore.collection("Notes")
                    .document(firebaseUser.getUid())
                    .collection("MyNotes")
                    .document(documentId)
                    .delete().addOnSuccessListener(aVoid -> Toast.makeText(LandingPage.this, "Note deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Log.w("LandingPage", "Error deleting document", e);
                        Toast.makeText(LandingPage.this, "Error deleting note", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void performLogout() {
        mAuth.signOut();
        Intent intent = new Intent(LandingPage.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    static class CompanyViewHolder extends RecyclerView.ViewHolder {
        TextView TitleTextView;
        TextView ContentTextView;

        CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            TitleTextView = itemView.findViewById(R.id.NameTextView);
            ContentTextView = itemView.findViewById(R.id.noteContent);

            // Ensure you are using MaterialCardView here
            MaterialCardView cardView = itemView.findViewById(R.id.cardView);
            if (cardView != null) {
                // Your existing color randomization logic
                List<Integer> colors = Arrays.asList(
                        R.color.color1,
                        R.color.color2,
                        R.color.color3,
                        R.color.color4,
                        R.color.color5,
                        R.color.color6,
                        R.color.color7,
                        R.color.color8,
                        R.color.color9,
                        R.color.color10
                );
                int randomColor = colors.get(new Random().nextInt(colors.size()));
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), randomColor));
            }
        }
    }

}