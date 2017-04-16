package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.classes.Admin;
import com.saarthicareer.saarthicareer.classes.Comment;
import com.saarthicareer.saarthicareer.classes.Trainee;
import com.saarthicareer.saarthicareer.classes.Trainer;

import java.text.DateFormat;
import java.util.Date;


public class CommentsFragment extends Fragment {

    String msgid;
    String uid;
    String name;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflating layout
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);

        //getting name
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final String type = snapshot.getValue(String.class);
                rootRef.child("userDetails").child(type).child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        name = dataSnapshot.getValue(String.class);
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        Bundle bundle = this.getArguments();
        if(bundle != null){
            msgid = bundle.getString("msgid");
        }

        ImageView sendButton = (ImageView)rootView.findViewById(R.id.sendComment);
        final EditText editTextCmt = (EditText)rootView.findViewById(R.id.commentText);

        rootRef.child("notifications").child("comments").child(msgid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                rootRef.child("notifications").child("messagesBank").child(msgid).child("noOfComments").setValue(""+count);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerViewForComments);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        //adapter for recyclerView to populate it with comments
        final FirebaseRecyclerAdapter<Comment, CommentViewHolder> adapter = new FirebaseRecyclerAdapter<Comment,CommentViewHolder>(
                Comment.class,
                R.layout.chat_me_layout,
                CommentViewHolder.class,
                rootRef.child("notifications").child("comments").child(msgid)
        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder commentViewHolder, Comment s,int i) {
                    commentViewHolder.senderName.setText(s.getSender());
                    commentViewHolder.commentBody.setText(s.getCommentBody());
                    commentViewHolder.sentTime.setText(s.getTime());
            }

            @Override
            public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case 1:
                        View userType1 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_me_layout, parent, false);
                        return new CommentViewHolder(userType1);
                    case 2:
                        View userType2 = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_receive_layout, parent, false);
                        return new CommentViewHolder(userType2);
                }
                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public int getItemViewType(int position) {
                Comment c = getItem(position);
                if(TextUtils.equals(c.getSenderId(),uid)){
                    return 1;
                }
                else
                    return 2;
            }
        };

        //scroll down when new comment posted
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                    recyclerView.scrollToPosition(positionStart);
            }
        });

        recyclerView.setAdapter(adapter);

        editTextCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 200);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmtText = editTextCmt.getText().toString().trim();
                if(TextUtils.isEmpty(cmtText))
                {
                    Toast.makeText(getActivity(), "Field is Empty.", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        Firebase tempRefForKey = rootRef.child("notifications").child("comments").child(msgid).push();
                        Comment newComment = new Comment();
                        newComment.setCmtId(tempRefForKey.getKey());
                        newComment.setCommentBody(cmtText);
                        newComment.setDate(currentDateTimeString.substring(0, 11));
                        newComment.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));
                        newComment.setSender(name);
                        newComment.setSenderId(uid);
                        tempRefForKey.setValue(newComment, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError == null) {
                                    editTextCmt.setText("");
                                } else {
                                    Toast.makeText(getActivity(), "Unable to send", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }catch(Exception e){
                        Toast.makeText(getActivity(),"Unknown error. Report to dev", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return rootView;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{

        TextView senderName,commentBody,sentTime;
        View mView;
        ImageView triangle;
        RelativeLayout chatBox;

        public CommentViewHolder(View v){
            super(v);
            this.mView = v;
            senderName = (TextView)v.findViewById(R.id.senderName);
            commentBody = (TextView)v.findViewById(R.id.commentBody);
            sentTime = (TextView)v.findViewById(R.id.sentTime);
            triangle = (ImageView)v.findViewById(R.id.imageView);
            chatBox = (RelativeLayout)v.findViewById(R.id.chatbox);
        }
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