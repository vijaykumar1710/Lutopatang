package com.example.vijay.Fantasy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Date;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blog_list;
    private List<User> user_list;

    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private TextView blog_username;
    private CircleImageView blog_userimage;


    public BlogRecyclerAdapter (List<BlogPost> blog_list, List <User> user_list){

        this.blog_list=blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from ( viewGroup.getContext () ).inflate ( R.layout.blog_list_item ,viewGroup,false);

        context=viewGroup.getContext ();
        firebaseFirestore=FirebaseFirestore.getInstance ();
        firebaseAuth =FirebaseAuth.getInstance ();
        return new ViewHolder ( view );
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable ( false );

        final String blogPostId =  blog_list.get ( i ).BlogPostId;

        final String currentUserId = firebaseAuth.getCurrentUser ().getUid ();
        String desc_data =blog_list.get ( i ).getDesc ();
        viewHolder.setDescText ( desc_data );

        String image_url =blog_list.get ( i ).getImage_url ();
        viewHolder.setBlogImage ( image_url );

        String user_id = blog_list.get ( i ).getUser_id ();

        // user data will be retrived here


                    String username = user_list.get ( i ).getName ();
                    String userimage = user_list.get ( i ).getImage ();

                    viewHolder.setUserData ( username,userimage );




        // user details

        long millisecond= blog_list.get ( i ).getTimestamp ().getTime ();
        String dateString = android.text.format.DateFormat.format ( "dd/mm/yyyy", new Date ( millisecond ) ).toString ();
        viewHolder.setTime ( dateString );

        //Likes count

        firebaseFirestore.collection ( "Posts/" + blogPostId +"/Likes").addSnapshotListener ( new EventListener <QuerySnapshot> () {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty () ) {

                    int count =documentSnapshots.size ();
                    viewHolder.updateLikesCount (count);
                }    else{
                    viewHolder.updateLikesCount ( 0 );
                }
            }
        } );

        //get likes

        firebaseFirestore.collection ( "Posts/" + blogPostId +"/Likes").document ( currentUserId ).addSnapshotListener ( new EventListener <DocumentSnapshot> () {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists ()){
                    viewHolder.blogLikeBtn.setImageDrawable ( context.getDrawable ( R.mipmap.action_like_accent ) );
                }else{
                    viewHolder.blogLikeBtn.setImageDrawable ( context.getDrawable ( R.mipmap.action_like_gray ) );
                }
            }
        } );

        //Likes feature

        viewHolder.blogLikeBtn.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection ( "Posts/" + blogPostId +"/Likes").document ( currentUserId ).get ().addOnCompleteListener ( new OnCompleteListener <DocumentSnapshot> () {
                    @Override
                    public void onComplete(@NonNull Task <DocumentSnapshot> task) {
                            if(!task.getResult ().exists ()){
                                Map<String ,Object> likesMap = new HashMap <> ( );
                                likesMap.put ( "timestamp", FieldValue.serverTimestamp () );
                                firebaseFirestore.collection ( "Posts/" + blogPostId +"/Likes").document ( currentUserId ).set ( likesMap );
                            }
                            else{
                                firebaseFirestore.collection ( "Posts/" + blogPostId +"/Likes").document ( currentUserId ).delete ();
                            }
                    }
                } );



            }
        } );

        // comments section

        viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);

            }
        });





    }

    @Override
    public int getItemCount() {
        return blog_list.size ();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView descView;
        private View mView;
        private ImageView blogImageView;
        private TextView blogDate;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;

        private ImageView blogCommentBtn;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );
            mView=itemView;

            blogLikeBtn=mView.findViewById ( R.id.likebtn );
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);


        }

        public void setDescText(String descText){
            descView=mView.findViewById ( R.id.blog_desc );
            descView.setText ( descText );
        }

        public void  setBlogImage (String downloadUri){

            blogImageView=mView.findViewById ( R.id.blog_image );
            Glide.with (context).load ( downloadUri ).into ( blogImageView );

        }

        public void setTime(String date){
            blogDate=mView.findViewById ( R.id.blog_date );
            blogDate.setText ( date );
        }

        public  void setUserData(String name,String image){
            blog_username = mView.findViewById ( R.id.blog_user_name);
            blog_userimage =mView.findViewById ( R.id.blog_user_image );
            blog_username.setText ( name );

            RequestOptions placeholderOption = new RequestOptions ();
            placeholderOption.placeholder ( R.mipmap.profile_placeholder );


            Glide.with (context).applyDefaultRequestOptions ( placeholderOption ).load(image).into(blog_userimage);
        }

        public void updateLikesCount(int count){
            blogLikeCount = mView.findViewById ( R.id.blog_like_count );
            blogLikeCount.setText ( count+ "Likes" );
        }
    }
}
