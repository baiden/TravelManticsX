package com.example.travelmanaticsx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InsertActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText mTxtTitle;
    private EditText mTxtPrice;
    private EditText mTxtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");

        mTxtTitle = findViewById(R.id.txtTitle);
        mTxtPrice = findViewById(R.id.txtPrice);
        mTxtDescription = findViewById(R.id.txtDescription);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                savedDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void savedDeal() {
        String title = mTxtTitle.getText().toString();
        String price = mTxtPrice.getText().toString();
        String description = mTxtDescription.getText().toString();
        TravelDeal deal = new TravelDeal(title, price, description, "");

        mDatabaseReference.push().setValue(deal);
    }

    private void clean() {
        mTxtTitle.setText("");
        mTxtPrice.setText("");
        mTxtDescription.setText("");
        mTxtTitle.requestFocus();
    }
}
