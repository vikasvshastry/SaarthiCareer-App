package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.saarthicareer.saarthicareer.activity.NewPostActivity;
import com.saarthicareer.saarthicareer.classes.Post;
import com.saarthicareer.saarthicareer.classes.Trainee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    FirebaseRecyclerAdapter<String, PostViewHolder> adapter;
    private String uid,collegeId;
    private RecyclerView recyclerView;
    private View rootView;
    LinearLayoutManager mLayoutManager;
    List<String> collegeList = new ArrayList<>();
    Map<String,String> collegeMap = new ArrayMap<>();
    List<String> courseList = new ArrayList<>();
    Map<String,String> courseMap = new ArrayMap<>();
    int currentCourseIndex;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);

        final String uid = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                final String type = snapshot.getValue(String.class);
                rootRef.child("userDetails").child(type).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(type.equals("STUDENT")){
                            final Trainee trainee = dataSnapshot.getValue(Trainee.class);
                            rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        String course = child.getValue(String.class);
                                        adapterFunc(trainee.getCollege(),course);
                                    }
                                }
                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                        }
                        else if(type.equals("ADMIN") || type.equals("TRAINER")){
                            final RelativeLayout relativeLayout = (RelativeLayout)rootView.findViewById(R.id.trainerView);
                            relativeLayout.setVisibility(View.VISIBLE);
                            final RelativeLayout relativeLayout2 = (RelativeLayout)rootView.findViewById(R.id.trainerView2);
                            relativeLayout2.setVisibility(View.VISIBLE);
                            final FloatingActionButton fabColl = (FloatingActionButton)rootView.findViewById(R.id.fabColl);
                            fabColl.setVisibility(View.VISIBLE);
                            final FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
                            fab.setVisibility(View.VISIBLE);
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(getActivity(), NewPostActivity.class));
                                }
                            });

                            final Dialog collegeSelectDialog = new Dialog(getActivity());
                            collegeSelectDialog.setContentView(R.layout.dialog_college_select_trainers);
                            final ListView listView = (ListView)collegeSelectDialog.findViewById(R.id.collegeListView);
                            final ImageView navLeft = (ImageView)rootView.findViewById(R.id.navLeft);
                            final ImageView navRight = (ImageView)rootView.findViewById(R.id.navRight);
                            final TextView heading = (TextView)rootView.findViewById(R.id.heading);
                            final TextView headingCollege = (TextView)rootView.findViewById(R.id.headingCollege);

                            //Initial loading
                            rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    courseMap.clear();
                                    courseList.clear();
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        collegeId = child.getKey();
                                        rootRef.child("colleges").child(child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                headingCollege.setText(dataSnapshot.getValue(String.class));
                                            }
                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                            }
                                        });
                                        for(DataSnapshot ds : child.getChildren()){
                                            rootRef.child("courses").child(ds.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    courseMap.put(dataSnapshot.getValue(String.class),dataSnapshot.getKey());
                                                    courseList.add(dataSnapshot.getValue(String.class));
                                                    currentCourseIndex = 0;
                                                    adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                                    heading.setText(courseList.get(currentCourseIndex));
                                                }
                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                }
                                            });
                                        }
                                        navLeft.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(currentCourseIndex==0){
                                                    currentCourseIndex=courseList.size()-1;
                                                }
                                                else{
                                                    --currentCourseIndex;
                                                }
                                                heading.setText(courseList.get(currentCourseIndex));
                                                adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                            }
                                        });
                                        navRight.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(currentCourseIndex==courseList.size()-1){
                                                    currentCourseIndex=0;
                                                }
                                                else{
                                                    ++currentCourseIndex;
                                                }

                                                heading.setText(courseList.get(currentCourseIndex));
                                                adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                            }
                                        });
                                        break;
                                    }
                                }
                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });

                            //changing college
                            fabColl.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    collegeSelectDialog.show();

                                    rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            collegeMap.clear();
                                            collegeList.clear();
                                            for(DataSnapshot child : dataSnapshot.getChildren()){
                                                rootRef.child("colleges").child(child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot){
                                                        collegeMap.put(dataSnapshot.getValue(String.class),dataSnapshot.getKey());
                                                        collegeList.add(dataSnapshot.getValue(String.class));
                                                        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item,collegeList);
                                                        listView.setAdapter(adapter);
                                                    }
                                                    @Override
                                                    public void onCancelled(FirebaseError firebaseError) {
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                        }
                                    });

                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            collegeSelectDialog.dismiss();
                                            String collegeName = ((TextView) view).getText().toString();
                                            headingCollege.setText(collegeName);
                                            collegeId = collegeMap.get(collegeName);
                                            rootRef.child("subscriptions").child(uid).child(collegeId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    courseMap.clear();
                                                    courseList.clear();
                                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                                        rootRef.child("courses").child(child.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                courseMap.put(dataSnapshot.getValue(String.class),dataSnapshot.getKey());
                                                                courseList.add(dataSnapshot.getValue(String.class));
                                                                currentCourseIndex = 0;
                                                                adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                                                heading.setText(courseList.get(currentCourseIndex));
                                                            }
                                                            @Override
                                                            public void onCancelled(FirebaseError firebaseError) {
                                                            }
                                                        });
                                                    }
                                                    navLeft.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if(currentCourseIndex==0){
                                                                currentCourseIndex=courseList.size()-1;
                                                            }
                                                            else{
                                                                --currentCourseIndex;
                                                            }
                                                            heading.setText(courseList.get(currentCourseIndex));
                                                            adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                                        }
                                                    });
                                                    navRight.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if(currentCourseIndex==courseList.size()-1){
                                                                currentCourseIndex=0;
                                                            }
                                                            else{
                                                                ++currentCourseIndex;
                                                            }

                                                            heading.setText(courseList.get(currentCourseIndex));
                                                            adapterFunc(collegeId,courseMap.get(courseList.get(currentCourseIndex)));
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
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

        return rootView;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        TextView headingText,bodyText,senderText,timeText,dateText,commentcount,postLetterForCircle;
        ImageView commentbutton;
        View mView;

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
            postLetterForCircle = (TextView) v.findViewById(R.id.postLetterForCircle);

        }
    }

    public void adapterFunc(String college, String course){

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
                        String firstLetter = p.getHead().charAt(0)+"";
                        postViewHolder.postLetterForCircle.setText(firstLetter);
                        postViewHolder.commentcount.setText(p.getNoOfComments()+"");

                        //to display the full post
                        postViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("msgId",s);

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

                        postViewHolder.commentbutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString("msgid", s);

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