package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.activity.NewPostActivity;


public class HomeFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://core-cr.firebaseio.com");
    //FirebaseRecyclerAdapter<String, PostViewHolder> adapter;

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
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fab = (FloatingActionButton)rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewPostActivity.class));
            }
        });



        /*RecyclerView recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        adapter = new FirebaseRecyclerAdapter<String, PostViewHolder>(
                String.class,
                R.layout.post_layout,
                PostViewHolder.class,
                rootRef.child()
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder postViewHolder, String s, int i) {

            }
        };*/


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