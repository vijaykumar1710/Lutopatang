package com.example.vijay.Fantasy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field;
    private EditText reg_password_field;
    private EditText reg_confirm_password_field;
    private Button reg_btn;
    private Button reg_login_btn;
    private ProgressBar reg_progress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_register );
        mAuth=FirebaseAuth.getInstance ();

        reg_email_field=(EditText)findViewById ( R.id.reg_email );
        reg_password_field=(EditText)findViewById ( R.id.reg_pass);
        reg_confirm_password_field=(EditText)findViewById ( R.id.reg_password );
        reg_btn=(Button)findViewById ( R.id.reg_btn );
        reg_login_btn=(Button)findViewById ( R.id.reg_login_btn );
        reg_progress=(ProgressBar)findViewById ( R.id.login_progress );

        reg_login_btn.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                finish ();
            }
        } );

        reg_btn.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                String email= reg_email_field.getText ().toString ();
                String pass=reg_password_field.getText ().toString ();
                String confirm_pass=reg_confirm_password_field.getText ().toString ();

                if(!TextUtils.isEmpty ( email )&& !TextUtils.isEmpty ( pass )&& !TextUtils.isEmpty ( confirm_pass )){

                    if(pass.equals ( confirm_pass )){

                        reg_progress.setVisibility ( View.VISIBLE );

                        mAuth.createUserWithEmailAndPassword ( email,pass ).addOnCompleteListener ( new OnCompleteListener <AuthResult> () {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                        if(task.isSuccessful ()){
                                            Intent setup_intent= new Intent ( RegisterActivity.this,SetupActivity.class );
                                            startActivity ( setup_intent );
                                            finish ();

                                        }else{
                                            String errormessage =task.getException ().getMessage ();
                                            Toast.makeText ( RegisterActivity.this, "error:"+errormessage, Toast.LENGTH_SHORT ).show ();
                                        }


                                    reg_progress.setVisibility ( View.INVISIBLE );
                            }
                        } );


                    }else{

                        Toast.makeText ( RegisterActivity.this, "password and confirm password field dont match", Toast.LENGTH_SHORT ).show ();
                    }

                }
            }
        } );

    }





    @Override
    protected void onStart() {
        super.onStart ();

        FirebaseUser currentuser = mAuth.getCurrentUser ();
        if(currentuser!=null) {

            sendToMain ();
        }





    }





    private void sendToMain() {
        Intent mainIntent= new Intent ( RegisterActivity.this,MainActivity.class );
        startActivity ( mainIntent );
        finish ();
    }
}
