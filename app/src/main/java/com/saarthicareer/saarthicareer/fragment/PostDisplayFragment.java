package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.classes.Post;

public class PostDisplayFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    String msgId;
    String uid;
    int color;

    public PostDisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_display, container, false);

        final TextView senderText = (TextView)rootView.findViewById(R.id.senderText);
        final TextView dateText = (TextView)rootView.findViewById(R.id.dateText);
        final TextView timeText = (TextView)rootView.findViewById(R.id.timeText);
        final TextView headingText = (TextView)rootView.findViewById(R.id.headingText);
        final TextView bodyText = (TextView)rootView.findViewById(R.id.bodyText);
        final TextView commentCountText = (TextView)rootView.findViewById(R.id.commentsNumber);
        final FloatingActionButton commentbutton = (FloatingActionButton) rootView.findViewById(R.id.CommentButton);
        final ImageView postImage = (ImageView)rootView.findViewById(R.id.postImage);
        final TextView postLetter = (TextView)rootView.findViewById(R.id.postLetterForCircle);

        Bundle bundle = this.getArguments();
        if(bundle != null){
            msgId = bundle.getString("msgId");
            color = bundle.getInt("color");
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        //fetching the post or notification
        rootRef.child("notifications").child("messagesBank").child(msgId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Post selectedPost = dataSnapshot.getValue(Post.class);
                senderText.setText(selectedPost.getSender());
                dateText.setText(selectedPost.getDate());
                timeText.setText(selectedPost.getTime());
                headingText.setText(selectedPost.getHead());
                bodyText.setText(selectedPost.getBody());
                postImage.setColorFilter(color);
                postLetter.setText(selectedPost.getHead().charAt(0) + "");

                commentbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("msgid", msgId);

                                Fragment fragment = new CommentsFragment();
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

        return rootView;
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