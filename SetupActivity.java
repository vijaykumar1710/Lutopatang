package com.example.vijay.Fantasy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolbar;
    private CircleImageView profile;
    private Uri mainImageUri=null;
    private EditText setupName;
    private Button setupButton;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar setupProgress;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private  boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_setup );

        firebaseAuth=FirebaseAuth.getInstance ();
        storageReference= FirebaseStorage.getInstance ().getReference ();
        firebaseFirestore=FirebaseFirestore.getInstance ();
        user_id= firebaseAuth.getCurrentUser ().getUid ();

        setupProgress=(ProgressBar )findViewById ( R.id.setup_progressbar );

        setupToolbar=(Toolbar ) findViewById ( R.id.setupbar );
        setSupportActionBar ( setupToolbar );
        getSupportActionBar ().setTitle ( "Account Setup" );
        setupName=(EditText )findViewById ( R.id.setup_name );
        setupButton=(Button ) findViewById ( R.id.save_settings );
        profile=findViewById ( R.id.imagesetup );

        setupProgress.setVisibility ( View.VISIBLE );
        setupButton.setEnabled ( false );


        firebaseFirestore.collection ( "Users" ).document (user_id).get ().addOnCompleteListener ( new OnCompleteListener <DocumentSnapshot> () {
            @Override
            public void onComplete(@NonNull Task <DocumentSnapshot> task) {
                if(task.isSuccessful ()){
                    if(task.getResult ().exists ()){
                        String name =task.getResult ().getString ( "name" );
                        String image =task.getResult ().getString ( "image" );

                        mainImageUri= Uri.parse ( image );

                        setupName.setText ( name );
                        RequestOptions placeholderRequest = new RequestOptions ();
                        placeholderRequest.placeholder ( R.mipmap.default_profile );

                        Glide.with ( SetupActivity.this ).setDefaultRequestOptions ( placeholderRequest ).load ( image ).into(profile);

                    }

                }else{
                    String error = task.getException ().getMessage ();
                    Toast.makeText ( SetupActivity.this,"(FIRESTORE Retrieve Error) :"+ error,Toast.LENGTH_SHORT ).show ();
                }

                setupProgress.setVisibility ( View.INVISIBLE );
                setupButton.setEnabled ( true );
            }
        } );

        setupButton.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                final String user_name = setupName.getText ().toString ();

                if ( !TextUtils.isEmpty ( user_name ) && mainImageUri != null ) {

                    setupProgress.setVisibility ( View.VISIBLE );

                    if(isChanged) {

                        user_id = firebaseAuth.getCurrentUser ().getUid ();


                        StorageReference image_path = storageReference.child ( "profile_image" ).child ( user_id + ".jpg" );
                        image_path.putFile ( mainImageUri ).addOnCompleteListener ( new OnCompleteListener <UploadTask.TaskSnapshot> () {
                            @Override
                            public void onComplete(@NonNull Task <UploadTask.TaskSnapshot> task) {

                                if ( task.isSuccessful () ) {

                                 storeFirestore(task,user_name);

                                } else {

                                    String error = task.getException ().toString ();
                                    Toast.makeText ( SetupActivity.this, "Image Error: " + error, Toast.LENGTH_SHORT ).show ();
                                    setupProgress.setVisibility ( View.INVISIBLE );
                                }

                            }
                        } );
                    }else{
                        storeFirestore ( null,user_name );
                    }
                }
            }
        } );



        profile=(CircleImageView)findViewById ( R.id.imagesetup );
        profile.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission ( SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE )!=PackageManager.PERMISSION_GRANTED){
                        Toast.makeText ( SetupActivity.this,"permission denied",Toast.LENGTH_LONG ).show ();
                        ActivityCompat.requestPermissions ( SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1 );

                    }else {

                        Toast.makeText ( SetupActivity.this,"permission granted",Toast.LENGTH_LONG ).show ();
                        BringImagePicker ();
                    }
                }else{

                    BringImagePicker();
                }
            }
        } );

    }

    private void storeFirestore(@NonNull Task <UploadTask.TaskSnapshot> task, String user_name) {
        Uri download_uri ;
        if(task!=null){
            download_uri = task.getResult ().getDownloadUrl ();
        }else{
            download_uri = mainImageUri;
        }

        Map <String, String> userMap = new HashMap <> ();
        userMap.put ( "name", user_name );
        userMap.put ( "image", download_uri.toString () );

        firebaseFirestore.collection ( "Users" ).document ( user_id ).set ( userMap ).addOnCompleteListener ( new OnCompleteListener <Void> () {
            @Override
            public void onComplete(@NonNull Task <Void> task) {
                if ( task.isSuccessful () ) {

                    Toast.makeText ( SetupActivity.this, "The User Settings are Updated", Toast.LENGTH_SHORT ).show ();
                    Intent mainIntent = new Intent ( SetupActivity.this, MainActivity.class );
                    startActivity ( mainIntent );
                    finish ();

                } else {
                    String error = task.getException ().getMessage ();
                    Toast.makeText ( SetupActivity.this, "(FIRESTORE Error) :" + error, Toast.LENGTH_SHORT ).show ();
                }

                setupProgress.setVisibility ( View.INVISIBLE );
            }
        } );
    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines( CropImageView.Guidelines.ON)
                .setAspectRatio ( 1,1 )
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult ( requestCode, resultCode, data );
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri  = result.getUri();
                profile.setImageURI ( mainImageUri );
                isChanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
