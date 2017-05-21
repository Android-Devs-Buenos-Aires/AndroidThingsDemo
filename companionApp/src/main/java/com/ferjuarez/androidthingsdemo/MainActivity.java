package com.ferjuarez.androidthingsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseRef;
    private ValueEventListener mPhotoListener;

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
        mPhotoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PhotoEntry photo = snapshot.getValue(PhotoEntry.class);
                    populateImage(photo);
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabaseRef.addValueEventListener(mPhotoListener);
    }

    private void populateImage(PhotoEntry photo){
        if(photo != null && photo.getImage() != null){
            CharSequence prettyTime = DateUtils.getRelativeDateTimeString(this,
                    photo.getTimestamp(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
            textViewTimestamp.setText("Actualizado " + prettyTime.toString().toLowerCase());

            populateEmotion(new JoyEmotion(photo.getEmotion()));

            if (photo.getImage() != null) {
                // Decode image data encoded by the Cloud Vision library
                byte[] imageBytes = Base64.decode(photo.getImage(), Base64.NO_WRAP | Base64.URL_SAFE);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    mImageViewlastPicture.setImageBitmap(bitmap);
                } else {
                    Drawable placeholder =
                            ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
                    mImageViewlastPicture.setImageDrawable(placeholder);
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
