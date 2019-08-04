package com.example.travelmanaticsx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 42; //the answer to everything
    private EditText mTxtTitle;
    private EditText mTxtPrice;
    private EditText mTxtDescription;
    private ImageView imageView;
    TravelDeal deal;
    private StorageReference photoStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        // Creates an instance of the database
        mFirebaseDatabase = FirebaseUtill.mFirebaseDatabase;

        // Creates a reference of the database and assigns the targeted path
        mDatabaseReference = FirebaseUtill.mDatabaseReference;

        mTxtTitle = findViewById(R.id.txtTitle);
        mTxtDescription = findViewById(R.id.txtDescription);
        mTxtPrice = findViewById(R.id.txtPrice);
        imageView = (ImageView) findViewById(R.id.image);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        mTxtTitle.setText(deal.getTitle());
        mTxtDescription.setText(deal.getDescription());
        mTxtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtill.isAdmin) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        } else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            photoStorageReference = FirebaseUtill.mStorageRef.child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            final UploadTask uploadTask = photoStorageReference.putFile(imageUri);

            uploadTask.continueWithTask(task -> {
                //There is an error.
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Otherwise continue with the task to get the download URL
                return photoStorageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String downloadUrl = Objects.requireNonNull(task.getResult()).toString();
                    String pictureName = uploadTask.getSnapshot().getStorage().getPath();

                    deal.setImageUrl(downloadUrl);
                    deal.setImageName(pictureName);
                    Log.d("Uri: ", downloadUrl);
                    Log.d("Name: ", pictureName);

                    showImage(downloadUrl);
                } else {
                    Toast.makeText(DealActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                savedDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted!", Toast.LENGTH_LONG).show();
                backToList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savedDeal() {
        deal.setTitle(mTxtTitle.getText().toString());
        deal.setDescription(mTxtDescription.getText().toString());
        deal.setPrice(mTxtPrice.getText().toString());
        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if (deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if (deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtill.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(DealActivity.this, "Delete image successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DealActivity.this, "Delete image failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        }

        private void backToList () {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }

        private void clean () {
            mTxtTitle.setText("");
            mTxtPrice.setText("");
            mTxtDescription.setText("");
            mTxtTitle.requestFocus();
        }

        private void enableEditTexts ( boolean isEnabled){
            mTxtTitle.setEnabled(isEnabled);
            mTxtDescription.setEnabled(isEnabled);
            mTxtPrice.setEnabled(isEnabled);
        }

        private void showImage (String url){
            if (url != null && !url.isEmpty()) {
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.get()
                        .load(url)
                        .resize(width, width * 2 / 3)
                        .centerCrop()
                        .into(imageView);
            }
        }
}
