package com.example.vijay.Fantasy;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.vijay.Fantasy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private  List<User>  user_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth firebaseAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;





    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate ( R.layout.fragment_home, container, false );
        blog_list = new ArrayList <> ();
        user_list = new ArrayList <> (  );

        blog_list_view = view.findViewById ( R.id.blog_list_view );

        firebaseFirestore = FirebaseFirestore.getInstance ();
        blogRecyclerAdapter = new BlogRecyclerAdapter ( blog_list ,user_list);
        firebaseAuth = FirebaseAuth.getInstance ();

        blog_list_view.setLayoutManager ( new LinearLayoutManager ( getActivity () ) );
        blog_list_view.setAdapter ( blogRecyclerAdapter );

        if ( firebaseAuth.getCurrentUser () != null ) {

            firebaseFirestore = FirebaseFirestore.getInstance ();

            blog_list_view.setOnScrollListener ( new RecyclerView.OnScrollListener () {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled ( recyclerView, dx, dy );
                    boolean reachedBottom = ! recyclerView.canScrollVertically ( -1 );
                    if(reachedBottom){
                        loadmorepost ();
                    }

                }
            } );


            Query blogQuery = firebaseFirestore.collection ( "Posts" ).orderBy ( "timestamp",Query.Direction.DESCENDING );

            blogQuery.addSnapshotListener ( getActivity (),new EventListener <QuerySnapshot> () {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if(isFirstPageFirstLoad){

                        lastVisible = documentSnapshots.getDocuments ().get ( documentSnapshots.size ()-1 );
                        blog_list.clear ();
                        user_list.clear ();

                    }

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges ()) {
                        if ( doc.getType () == DocumentChange.Type.ADDED ) {

                            String blogPostId = doc.getDocument ().getId ();
                            final BlogPost blogPost = doc.getDocument ().toObject ( BlogPost.class ).withId ( blogPostId );
                            String blogUserId = doc.getDocument ().getString ( "user_id" );
                            firebaseFirestore.collection ( "Users" ).document ( blogUserId ).get ().addOnCompleteListener ( new OnCompleteListener <DocumentSnapshot> () {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                   if(task.isSuccessful ()){
                                       User user =task.getResult ().toObject ( User.class );

                                       if(isFirstPageFirstLoad) {

                                           user_list.add ( user );
                                           blog_list.add ( blogPost );
                                       }else {

                                           user_list.add ( user );
                                           blog_list.add ( 0,blogPost );
                                       }

                                       blogRecyclerAdapter.notifyDataSetChanged ();


                                   }


                                }
                            } );



                        }
                    }

                    isFirstPageFirstLoad=false;
                }
            } );
        }



        return view;


    }

    public void loadmorepost(){


        firebaseFirestore = FirebaseFirestore.getInstance ();
        Query nextQuery = firebaseFirestore.collection ( "Posts" )
                .orderBy ( "timestamp",Query.Direction.DESCENDING )
                .startAfter ( lastVisible )
                .limit ( 3 );

        nextQuery.addSnapshotListener (getActivity (), new EventListener <QuerySnapshot> () {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if ( !documentSnapshots.isEmpty () ){

                    lastVisible = documentSnapshots.getDocuments ().get ( documentSnapshots.size () - 1 );

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges ()) {
                        if ( doc.getType () == DocumentChange.Type.ADDED ) {

                            String blogPostId = doc.getDocument ().getId ();

                            final BlogPost blogPost = doc.getDocument ().toObject ( BlogPost.class ).withId ( blogPostId );
                            String blogUserId = doc.getDocument ().getString ( "user_id" );
                            firebaseFirestore.collection ( "Users" ).document ( blogUserId ).get ().addOnCompleteListener ( new OnCompleteListener <DocumentSnapshot> () {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful ()){
                                        User user =task.getResult ().toObject ( User.class );
                                        user_list.add ( user );
                                            blog_list.add ( blogPost );

                                        blogRecyclerAdapter.notifyDataSetChanged ();
                                    }
                                }
                            } );

                        }
                    }
                }
            }
        } );
    }

}

