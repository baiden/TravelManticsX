package com.example.travelmanaticsx;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtill {
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;
    private static FirebaseUtill firebaseUtill;
    public static ArrayList<TravelDeal> mDeals;

    private FirebaseUtill() { }

    public static void openFbReference(String ref) {
        if (firebaseUtill == null) {
            firebaseUtill = new FirebaseUtill();
            mFirebaseDatabase = FirebaseDatabase.getInstance();

        }
        mDeals = new ArrayList<TravelDeal>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }
}
