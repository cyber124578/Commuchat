package com.example.commuchat.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.commuchat.Activities.Models.Post;
import com.example.commuchat.Activities.ui.home.HomeFragment;
import com.example.commuchat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class Home extends AppCompatActivity {

    private static final int REQUESCODE = 2;
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseAuth mAuth;
    FirebaseUser currentuser;
    Dialog popAddPost;
    ImageView popupUserImage,popupPostImage,popupAddBtn;
    TextView popupTitle,popupDescription;
    ProgressBar popupClickProgress;
    private int PReqCode =2;
    private Uri pickedImgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //initiation
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        //initiation posts
        iniPopup();

        setupPopupImageClick();

        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> popAddPost.show()) ;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_logout).setOnMenuItemClickListener(menuItem -> {
            logout();
            return true;
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
            updateNavHeader();
        // set the home fragment as the default one

        getSupportFragmentManager().beginTransaction().replace(R.id.container,new HomeFragment()).commit();


    }

    private void setupPopupImageClick() {


        popupPostImage.setOnClickListener(view -> {
            // here when image clicked we need to open the gallery
            // before we open the gallery we need to check if our app have the access to user files
            // we did this before in register activity I'm just going to copy the code to save time ...

            checkAndRequestForPermission();


        });



    }


    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(Home.this,"Please accept for required permission",Toast.LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(Home.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            // everything goes well : we have permission to access user gallery
            openGallery();

    }





    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);
    }



    // when user picked an image ...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);

        }


    }



    private void iniPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        Objects.requireNonNull(popAddPost.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;
        //ini popup widgets
        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

//load user pic
        Glide.with(Home.this).load(currentuser.getPhotoUrl()).into(popupUserImage);


        popupAddBtn.setOnClickListener(view -> {
            popupAddBtn.setVisibility(View.INVISIBLE);
            popupClickProgress.setVisibility(View.VISIBLE);


            if (!popupTitle.getText().toString().isEmpty()
            && !popupDescription.getText().toString().isEmpty()
            &&pickedImgUri!=null){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog_images");
                final StorageReference imageFilePath = storageReference.child(Objects.requireNonNull(pickedImgUri.getLastPathSegment()));
                imageFilePath.putFile(pickedImgUri).addOnSuccessListener(taskSnapshot -> imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageDownloadLink = uri.toString();
                    //post object
                    Post post = new Post(popupTitle.getText().toString(),
                            popupDescription.getText().toString(),
                            imageDownloadLink,
                            currentuser.getUid(),
                            currentuser.getPhotoUrl().toString());
                    addPost(post);
                }).addOnFailureListener(e -> {
                    showMessage(e.getMessage());
                    popupClickProgress.setVisibility(View.INVISIBLE);
                    popupAddBtn.setVisibility(View.VISIBLE);
                }));

            }
            else
            {
                showMessage("Please verify all fields and choose an image");
                popupAddBtn.setVisibility(View.VISIBLE);
                popupClickProgress.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void addPost(Post post) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        // get post unique ID and update post key
        String key = myRef.getKey();
        post.setPostKey(key);


        // add post data to firebase database

        myRef.setValue(post).addOnSuccessListener(aVoid -> {
            showMessage("Post Added successfully");
            popupClickProgress.setVisibility(View.INVISIBLE);
            popupAddBtn.setVisibility(View.VISIBLE);
            popAddPost.dismiss();
        });





    }


    private void showMessage(String message) {
        Toast.makeText(Home.this, message,Toast.LENGTH_LONG).show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent loginActivity = new Intent(this,LoginActivity.class);
        startActivity(loginActivity);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void  updateNavHeader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);
        ImageView navUserPhoto = headerView.findViewById(R.id.navphoto);

        navUsername.setText(currentuser.getDisplayName());

        navEmail.setText(currentuser.getEmail());

        Glide.with(this).load(currentuser.getPhotoUrl()).into(navUserPhoto);

    }
}