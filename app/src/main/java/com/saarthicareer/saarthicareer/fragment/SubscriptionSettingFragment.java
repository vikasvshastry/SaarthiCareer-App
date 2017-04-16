package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.other.MultiSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SubscriptionSettingFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    String uid,type,courseNames,selectedCollege;
    List<String> courses = new ArrayList<>();
    List<String> colleges = new ArrayList<>();
    List<String> selectedCourses = new ArrayList<>();
    Map<String,String> courseMap = new ArrayMap<>();
    Map<String,String> collegeMap = new ArrayMap<>();
    private View rootView;

    public SubscriptionSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_subscription_setting, container, false);

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        final TextView currentSubscription = (TextView)rootView.findViewById(R.id.currentSubscription);
        final Button buttonSubscribe = (Button)rootView.findViewById(R.id.btn_subscribe);

        //setting currently subscribed course
        rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                type = dataSnapshot.getValue(String.class);

                if(type.equals("STUDENT")) {
                    rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            courseNames = "";
                            for(DataSnapshot child : dataSnapshot.getChildren()) {
                                String courseId = child.getValue(String.class);
                                rootRef.child("courses").child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        courseNames += dataSnapshot.getValue(String.class) + "\n";
                                    }
                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
                            }
                            currentSubscription.setText(courseNames);
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });
                } else {
                    rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            courseNames = " ";
                            for(DataSnapshot child : dataSnapshot.getChildren()) {
                                for(DataSnapshot childsChild : child.getChildren()){
                                    setTextToView(child.getKey(),childsChild.getValue(String.class));
                                }
                            }
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

        //checking type of user
        rootRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                type = snapshot.getValue(String.class);

                if(type.equals("STUDENT"))
                    rootRef.child("userDetails").child("STUDENT").child(uid).child("college").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String college = dataSnapshot.getValue(String.class);
                            rootRef.child("college-course").child(college).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    courses.clear();
                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                        courses.add(child.getValue(String.class));
                                    }

                                    rootRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot child : dataSnapshot.getChildren()){
                                                if(courses.contains(child.getKey())){
                                                    courseMap.put(child.getValue(String.class),child.getKey());
                                                    courses.remove(child.getKey());
                                                    courses.add(child.getValue(String.class));
                                                }
                                            }

                                            RelativeLayout relativeLayoutStudent = (RelativeLayout)rootView.findViewById(R.id.forTrainee);
                                            relativeLayoutStudent.setVisibility(View.VISIBLE);

                                            Spinner spinnerCourse = (Spinner)rootView.findViewById(R.id.spinnerCourse);
                                            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,courses);
                                            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            spinnerCourse.setAdapter(adapter2);
                                            adapter2.notifyDataSetChanged();

                                            spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                    selectedCourses.clear();
                                                    selectedCourses.add(courseMap.get(parent.getSelectedItem().toString()));
                                                }
                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {
                                                }
                                            });

                                        }
                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                            Toast.makeText(getActivity(), "Unable to populate colleges", Toast.LENGTH_SHORT).show();
                                        }
                                    });

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

                if(type.equals("TRAINER")||type.equals("ADMIN")){
                    RelativeLayout relativeLayoutTrainer = (RelativeLayout)rootView.findViewById(R.id.forTrainer);
                    relativeLayoutTrainer.setVisibility(View.VISIBLE);

                    rootRef.child("colleges").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot child : dataSnapshot.getChildren()){
                                collegeMap.put(child.getValue(String.class),child.getKey());
                                colleges.add(child.getValue(String.class));
                            }

                            Spinner spinnerCollege = (Spinner)rootView.findViewById(R.id.spinnerColl);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,colleges);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCollege.setAdapter(adapter);

                            spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedCollege = parent.getSelectedItem().toString();
                                    courses.clear();
                                    courseMap.clear();
                                    rootRef.child("college-course").child(collegeMap.get(selectedCollege)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                                                String course = childSnap.getValue(String.class);
                                                courses.add(course);
                                            }

                                            rootRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for(DataSnapshot child : dataSnapshot.getChildren()){
                                                        if(courses.contains(child.getKey())) {
                                                            courses.remove(child.getKey());
                                                            courseMap.put(child.getValue(String.class), child.getKey());
                                                            courses.add(child.getValue(String.class));
                                                        }
                                                    }

                                                    final MultiSpinner multiSpinner = (MultiSpinner) rootView.findViewById(R.id.multi_spinner);
                                                    final TextView selectedText = (TextView)rootView.findViewById(R.id.output_selected);
                                                    multiSpinner.setItems(courses, "choose", new MultiSpinner.MultiSpinnerListener() {
                                                        @Override
                                                        public void onItemsSelected(boolean[] selected) {
                                                            String s = "";
                                                            selectedCourses.clear();
                                                            for(int x=0; x<selected.length; x++ ){
                                                                if(selected[x]){
                                                                    s = s + courses.get(x) + "\n";
                                                                    selectedCourses.add(courseMap.get(courses.get(x)));
                                                                }
                                                            }
                                                            selectedText.setText(s);
                                                        }
                                                    });

                                                }
                                                @Override
                                                public void onCancelled(FirebaseError firebaseError) {
                                                    Toast.makeText(getActivity(), "Unable to populate colleges", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });

                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(getActivity(), "Unable to populate colleges", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });


        buttonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedCourses.size()==0){
                    Toast.makeText(getActivity(), "Course not selected.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(type.equals("STUDENT")) {
                    rootRef.child("subscriptions").child(uid).setValue(selectedCourses, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if(firebaseError == null) {
                                Toast.makeText(getActivity(), "Subscription successful", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), "Failed to subscribe", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if(type.equals("TRAINER") || type.equals("ADMIN")) {
                    rootRef.child("subscriptions").child(uid).child(collegeMap.get(selectedCollege)).setValue("");
                    rootRef.child("subscriptions").child(uid).child(collegeMap.get(selectedCollege)).setValue(selectedCourses, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if(firebaseError == null) {
                                Toast.makeText(getActivity(), "Subscription successful", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getActivity(), "Failed to subscribe" + firebaseError.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        return rootView;
    }

    void setTextToView(String collegeId, final String courseId) {
        final TextView currentSubscription = (TextView)rootView.findViewById(R.id.currentSubscription);
        rootRef.child("colleges").child(collegeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String college = dataSnapshot.getValue(String.class);
                rootRef.child("courses").child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String courses = dataSnapshot.getValue(String.class);
                        courseNames += courses + " " + "(" + college + ")" + "\n";
                        currentSubscription.setText(courseNames);
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