package com.ferjuarez.androidthingsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ferjuarez.emotions.Emotion;
import com.ferjuarez.emotions.JoyEmotion;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;

    @BindView(R.id.imageViewLastPicture)
    ImageView mImageViewlastPicture;
    @BindView(R.id.textViewTimestamp)
    TextView textViewTimestamp;
    @BindView(R.id.textViewEmotion)
    TextView textViewEmotion;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupDatabaseReference();
    }

    private void setupDatabaseReference(){
        progressBar.setVisibility(View.VISIBLE);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("logs");
        progressBar.setVisibility(View.VISIBLE);

        mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                populateSnapshot(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                populateSnapshot(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void populateSnapshot(DataSnapshot dataSnapshot){
        progressBar.setVisibility(View.VISIBLE);
        PhotoEntry photo = dataSnapshot.getValue(PhotoEntry.class);
        populateImage(photo);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void populateImage(PhotoEntry photo){
        if(photo != null && photo.getImage() != null){
            CharSequence prettyTime = DateUtils.getRelativeDateTimeString(this,
                    photo.getTimestamp(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
            textViewTimestamp.setText("Actualizado " + prettyTime.toString().toLowerCase());
            populateEmotion(new JoyEmotion(photo.getEmotion()));

            if (photo.getImage() != null) {
                byte[] imageBytes = Base64.decode(photo.getImage(), Base64.NO_WRAP | Base64.URL_SAFE);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    mImageViewlastPicture.setImageBitmap(bitmap);
                } else {
                    mImageViewlastPicture.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
                }
            }
        }
    }

    private void populateEmotion(Emotion emotion){
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "emojifont.ttf");
        textViewEmotion.setTypeface(myTypeface);
        if(emotion.getState().getEmojiIcon() != null){
            textViewEmotion.setText(emotion.getState().getEmojiIcon());
        }
    }
}
