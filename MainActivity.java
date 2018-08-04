package com.example.vijay.Fantasy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {



    private FirebaseAuth mAuth;
    private Toolbar mainToolbar;

    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;
    private BottomNavigationView mainbottonNav;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        mAuth = FirebaseAuth.getInstance ();
        firebaseFirestore=FirebaseFirestore.getInstance ();


        mainToolbar=(Toolbar ) findViewById ( R.id.maintoolbar );
        setSupportActionBar ( mainToolbar );
        getSupportActionBar ().setTitle ( "Lutopatang" );

        if (mAuth.getCurrentUser ()!=null ) {


            mainbottonNav = findViewById ( R.id.bottomNavigationView );


            //Fragment from here


            homeFragment = new HomeFragment ();
            notificationFragment = new NotificationFragment ();
            accountFragment = new AccountFragment ();

            replaceFragment ( homeFragment );

            mainbottonNav.setOnNavigationItemSelectedListener ( new BottomNavigationView.OnNavigationItemSelectedListener () {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId ()) {

                        case R.id.bottom_action_home:
                            replaceFragment ( homeFragment );
                            return true;
                        case R.id.bottom_action_notify:
                            replaceFragment ( notificationFragment );
                            return true;
                        case R.id.bottom_action_account:
                            replaceFragment ( accountFragment );
                            return true;
                        default:
                            return false;

                    }
                }
            } );
        }

    }

    @Override
    protected void onStart() {
        super.onStart ();

        FirebaseUser currentuser = FirebaseAuth.getInstance ().getCurrentUser ();
        if(currentuser==null ) {

                sendToLogin ();
        }else{

            current_user_id=mAuth.getCurrentUser ().getUid ();
            firebaseFirestore.collection ( "Users" ).document (current_user_id).get ().addOnCompleteListener ( new OnCompleteListener <DocumentSnapshot> () {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful ()){
                        if(!task.getResult ().exists ()){
                            Intent setUpIntent = new Intent ( MainActivity.this,SetupActivity.class );
                            startActivity ( setUpIntent );
                            finish ();
                        }
                    }else {
                        String errormessage =task.getException ().getMessage ();
                        Toast.makeText ( MainActivity.this, "error:"+errormessage, Toast.LENGTH_SHORT ).show ();
                    }
                }
            } );
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater ().inflate ( R.menu.main_menu,menu );


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId ()){

            case R.id.action_logout_btn:

                logout();
                return  true;

            case R.id.action_settings_btn:
                Intent settingsIntent = new Intent ( MainActivity.this,SetupActivity.class );
                startActivity ( settingsIntent );
                return true;

            case R.id.add_post_btn:
                Intent newPostIntent = new Intent ( MainActivity.this,NewPostActivity.class );
                startActivity ( newPostIntent );
                return true;

                default:
                    return false;

        }

    }

    private void logout() {

        mAuth.signOut ();
        sendToLogin ();
    }


    private void sendToLogin() {

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish ();
    }


    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction=getSupportFragmentManager ().beginTransaction ();
        fragmentTransaction.replace ( R.id.main_container,fragment );
        fragmentTransaction.commit ();

    }

}
