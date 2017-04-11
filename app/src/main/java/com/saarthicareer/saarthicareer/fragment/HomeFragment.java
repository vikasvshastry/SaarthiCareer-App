package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.activity.NewPostActivity;
import com.saarthicareer.saarthicareer.classes.Post;
import com.saarthicareer.saarthicareer.classes.Trainee;


public class HomeFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    FirebaseRecyclerAdapter<String, PostViewHolder> adapter;
    private String uid,college,course;
    private RecyclerView recyclerView;
    private View rootView;
    LinearLayoutManager mLayoutManager;
    ColorGenerator generator = ColorGenerator.MATERIAL;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewPostActivity.class));
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getKey().equals("college")){
                        college = child.getValue(String.class);
                    }
                    else {
                        course = child.getValue(String.class);
                    }
                }
                TextView errorMsg = (TextView)rootView.findViewById(R.id.subscriptionErrorMessage);
                if(college!=null && course!=null) {
                    errorMsg.setVisibility(View.GONE);
                    adapterFunc();
                }else{
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        return rootView;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        TextView headingText,bodyText,senderText,timeText,dateText,commentcount,postLetterForCircle;
        ImageView commentbutton;
        View mView;
        ImageView postImage;

        public PostViewHolder(View v){
            super(v);
            this.mView = v;
            headingText = (TextView) v.findViewById(R.id.headingText);
            bodyText = (TextView) v.findViewById(R.id.bodyText);
            senderText = (TextView) v.findViewById(R.id.senderText);
            timeText = (TextView) v.findViewById(R.id.timeText);
            dateText = (TextView) v.findViewById(R.id.dateText);
            commentcount = (TextView) v.findViewById(R.id.commentsNumber);
            commentbutton = (ImageView) v.findViewById(R.id.CommentButton);
            postImage = (ImageView) v.findViewById(R.id.postImage);
            postLetterForCircle = (TextView) v.findViewById(R.id.postLetterForCircle);

        }
    }

    public void adapterFunc(){

        adapter = new FirebaseRecyclerAdapter<String, PostViewHolder>(
                String.class,
                R.layout.post_layout,
                PostViewHolder.class,
                rootRef.child("notifications").child("messages").child(college).child(course)
        ) {
            @Override
            protected void populateViewHolder(final PostViewHolder postViewHolder, final String s, final int i) {

                rootRef.child("notifications").child("messagesBank").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post p = dataSnapshot.getValue(Post.class);
                        postViewHolder.headingText.setText(p.getHead());
                        postViewHolder.bodyText.setText(p.getBody());
                        postViewHolder.senderText.setText(p.getSender());
                        postViewHolder.timeText.setText(p.getTime());
                        postViewHolder.dateText.setText(p.getDate());
                        final int color = generator.getColor(p.getSender());
                        String firstLetter = p.getHead().charAt(0)+"";
                        postViewHolder.postImage.setColorFilter(color);
                        postViewHolder.postLetterForCircle.setText(firstLetter);

                        //to display the full post
                        postViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("msgId",s);
                                bundle.putInt("color",color);

                                Toast.makeText(getActivity(), ""+s, Toast.LENGTH_SHORT).show();

                                Fragment fragment = new PostDisplayFragment();
                                fragment.setArguments(bundle);
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.add(R.id.container_body, fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        });

                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}