package com.example.commuchat.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.commuchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static  int PReqCode = 1 ;
    static  int REQUESCODE = 1;
    Uri pickedImgUri;
    private EditText  userEmail, userPassword, userPassword2, userName;
    private ProgressBar loadingProgress;
    private Button regBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //view
        userName = findViewById(R.id.regName);
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        loadingProgress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);
        loadingProgress.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(view -> {
            regBtn.setVisibility(View.INVISIBLE);
            loadingProgress.setVisibility(View.VISIBLE);
            final String email = userEmail.getText().toString();
            final String password = userPassword.getText().toString();
            final String password2 = userPassword2.getText().toString();
            final String name = userName.getText().toString();


            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || !password.equals(password2)) {
                //en cas d'erreur: tout les champ doit étre véerifier
                // il faut afficher un alert
                showMessage("Please fill all the fields!");
                regBtn.setVisibility(View.VISIBLE);
                loadingProgress.setVisibility(View.INVISIBLE);
            }
            else
            {
                //tout va bien et les champs sont tout remplis et on peut faire l'operation de l'inscription
                //
                CreateUserAccount(email,name,password);
            }
        });

        ImgUserPhoto = findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT>=22) {
                checkAndRequestForpermission();
            }
            else
            {
                openGallery();
            }
        });
    }

    private void CreateUserAccount(String email, final String name, String password) {
        //creation du compte d'utilisateur avec un email et mot de pass specifique
       mAuth.createUserWithEmailAndPassword(email,password)
               .addOnCompleteListener(this, task -> {
                   if (task.isSuccessful()) {
                       showMessage("Account created");
                       updateUserInfo(name, pickedImgUri,mAuth.getCurrentUser());
                   }
                   else {
                       showMessage("Account creation failed" + Objects.requireNonNull(task.getException()).getMessage());
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                   }
               });
    }

    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("user_photos");
        final StorageReference imageFilepath = mStorage.child(Objects.requireNonNull(pickedImgUri.getLastPathSegment()));
        imageFilepath.putFile(pickedImgUri).addOnSuccessListener(taskSnapshot -> imageFilepath.getDownloadUrl().addOnSuccessListener(uri -> {


            UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .setPhotoUri(uri)
                    .build();
            currentUser.updateProfile(profleUpdate)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showMessage("Registration completed");
                            updateUI();
                        }
                    });

        }));
    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(),Home.class);
        startActivity(homeActivity);
        finish();
    }

    //code pour l'alert en cas d'erreur!
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        //ouvrir: le gallery et attends l'utilisateur pour choisir un photo
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);

    }

    private void checkAndRequestForpermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, "Please accept for required permissions", Toast.LENGTH_SHORT).show();
            }
        else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    PReqCode);
            }
        else
        {
            openGallery();
        }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null){

            // l'utilisateur a choisi une image avec succès
            // nous devons enregistrer sa référence à une variable Uri
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }


}
