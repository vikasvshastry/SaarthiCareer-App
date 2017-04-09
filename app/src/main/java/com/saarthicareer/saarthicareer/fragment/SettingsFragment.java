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
import android.widget.EditText;
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


public class SettingsFragment extends Fragment {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    private Firebase tempRef;
    String uid;
    List<String> selectedColleges = new ArrayList<>();
    List<String> items = new ArrayList<>();
    Map<String,String> mapColleges = new ArrayMap<>();
    List<String> listCourses = new ArrayList<>();
    Map<String,String> mapCourses = new ArrayMap<>();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        TextView textViewCreateNewCourse = (TextView)rootView.findViewById(R.id.createNewCourse);
        TextView textViewAddCollege = (TextView)rootView.findViewById(R.id.addCollege);
        TextView textViewAddCourseToCollege = (TextView)rootView.findViewById(R.id.assignCouseToCollege);
        TextView textViewSubscriptions = (TextView)rootView.findViewById(R.id.subscriptions);

        //subscriptions
        final Dialog dialogSubscriptions = new Dialog(getActivity());
        dialogSubscriptions.setContentView(R.layout.dialog_subscriptions);
        final TextView currentSubscription = (TextView)dialogSubscriptions.findViewById(R.id.currentSubscription);
        final Button buttonSubscribe = (Button)dialogSubscriptions.findViewById(R.id.btn_subscribe);
        textViewSubscriptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSubscriptions.show();
                String myCollege;

                //setting currently subscribed course
                try {
                    rootRef.child("subscriptions").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String courseId = dataSnapshot.getValue(String.class);
                            if(courseId!=null)
                            rootRef.child("courses").child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String courseName = dataSnapshot.getValue(String.class);
                                    currentSubscription.setText(courseName);
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
                }catch (Exception e){
                }

                //populating courses into the spinner
                rootRef.child("userDetails").child("STUDENT").child(uid).child("college").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String college = dataSnapshot.getValue(String.class);
                        rootRef.child("college-course").child(college).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                listCourses.clear();
                                for(DataSnapshot child : dataSnapshot.getChildren()){
                                    listCourses.add(child.getValue(String.class));
                                }

                                rootRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot child : dataSnapshot.getChildren()){
                                            if(listCourses.contains(child.getKey())){
                                                mapCourses.put(child.getValue(String.class),child.getKey());
                                                listCourses.remove(child.getKey());
                                                listCourses.add(child.getValue(String.class));
                                            }
                                        }

                                        Spinner spinnerCourse = (Spinner)dialogSubscriptions.findViewById(R.id.spinnerCourse);
                                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,listCourses);
                                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerCourse.setAdapter(adapter2);
                                        adapter2.notifyDataSetChanged();

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

                buttonSubscribe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Spinner spinnerCourse = (Spinner)dialogSubscriptions.findViewById(R.id.spinnerCourse);
                        String selectedCourse =  spinnerCourse.getSelectedItem().toString();

                        if(selectedCourse.isEmpty()){
                            Toast.makeText(getActivity(), "Course not selected.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String newCourseId = mapCourses.get(selectedCourse);
                        rootRef.child("subscriptions").child(uid).setValue(newCourseId);

                    }
                });

            }
        });


        //Assign a Course to a College
        final Dialog dialogAddCourseToCollege = new Dialog(getActivity());
        dialogAddCourseToCollege.setContentView(R.layout.dialog_assign_course_to_college);
        final Button buttonAssign = (Button)dialogAddCourseToCollege.findViewById(R.id.btn_assign);
        textViewAddCourseToCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddCourseToCollege.show();
                items.clear();
                mapColleges.clear();

                rootRef.child("colleges").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            mapColleges.put(child.getValue(String.class),child.getKey());
                            items.add(child.getValue(String.class));
                        }

                        Spinner spinnerCollege = (Spinner)dialogAddCourseToCollege.findViewById(R.id.spinnerCollege);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,items);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCollege.setAdapter(adapter);

                        spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedCollege = parent.getItemAtPosition(position).toString();
                                listCourses.clear();
                                mapCourses.clear();

                                rootRef.child("college-course").child(mapColleges.get(selectedCollege)).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                                            String course = childSnap.getValue(String.class);
                                            listCourses.add(course);
                                        }

                                        rootRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for(DataSnapshot child : dataSnapshot.getChildren()){
                                                    if(listCourses.contains(child.getKey())){
                                                        listCourses.remove(child.getKey());
                                                    }
                                                    else {
                                                        mapCourses.put(child.getValue(String.class),child.getKey());
                                                        listCourses.add(child.getValue(String.class));
                                                    }
                                                }

                                                Spinner spinnerCourse = (Spinner)dialogAddCourseToCollege.findViewById(R.id.spinnerCourse);
                                                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,listCourses);
                                                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                spinnerCourse.setAdapter(adapter2);
                                                adapter2.notifyDataSetChanged();

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

                buttonAssign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Spinner spinnerCollege = (Spinner)dialogAddCourseToCollege.findViewById(R.id.spinnerCollege);
                        Spinner spinnerCourse = (Spinner)dialogAddCourseToCollege.findViewById(R.id.spinnerCourse);
                        String courseId = mapCourses.get(spinnerCourse.getSelectedItem().toString());
                        String collegeId = mapColleges.get(spinnerCollege.getSelectedItem().toString());

                        rootRef.child("college-course").child(collegeId).push().setValue(courseId, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if(firebaseError==null) {
                                    Toast.makeText(getActivity(), "Updation successful", Toast.LENGTH_SHORT).show();
                                    dialogAddCourseToCollege.dismiss();
                                }
                                else {
                                    Toast.makeText(getActivity(), "Failed to update", Toast.LENGTH_SHORT).show();
                                    dialogAddCourseToCollege.dismiss();
                                }
                            }
                        });

                    }
                });
            }
        });


        //Creating a new course
        final Dialog dialogCreateNewCourse = new Dialog(getActivity());
        dialogCreateNewCourse.setContentView(R.layout.dialog_new_course);

        final Button buttonCreateNewCourse = (Button)dialogCreateNewCourse.findViewById(R.id.btn_create);
        final TextView outputColleges = (TextView)dialogCreateNewCourse.findViewById(R.id.output_selected_colleges);
        final EditText inputCourseName = (EditText)dialogCreateNewCourse.findViewById(R.id.input_new_course_name);
        final MultiSpinner multiSpinner = (MultiSpinner) dialogCreateNewCourse.findViewById(R.id.multi_spinner);

        textViewCreateNewCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCreateNewCourse.show();
                items.clear();

                rootRef.child("colleges").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            mapColleges.put(child.getValue(String.class),child.getKey());
                            items.add(child.getValue(String.class));
                        }
                        multiSpinner.setItems(items, "choose", new MultiSpinner.MultiSpinnerListener() {
                            @Override
                            public void onItemsSelected(boolean[] selected) {
                                String s = "";
                                selectedColleges.clear();
                                for(int x=0; x<selected.length; x++ ){
                                    if(selected[x]){
                                        s = s + items.get(x) + "\n";
                                        selectedColleges.add(items.get(x));
                                    }
                                }
                                outputColleges.setText(s);
                                outputColleges.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getActivity(), "Unable to retrieve Colleges List", Toast.LENGTH_SHORT).show();
                    }
                });

                buttonCreateNewCourse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String courseName = inputCourseName.getText().toString().trim();
                        if(courseName.isEmpty()){
                            Toast.makeText(getActivity(), "Please enter a Course Name", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        tempRef = rootRef.child("courses").push();
                        rootRef.child("courses").child(tempRef.getKey()).setValue(courseName);
                        for(String x: selectedColleges){
                            rootRef.child("college-course").child(mapColleges.get(x)).push().setValue(tempRef.getKey());
                        }
                        dialogCreateNewCourse.dismiss();
                    }
                });
            }
        });


        //Adding a new College
        final Dialog dialogAddCollege = new Dialog(getActivity());
        dialogAddCollege.setContentView(R.layout.dialog_add_college);

        final EditText editTextCollegeName = (EditText) dialogAddCollege.findViewById(R.id.input_college_name);
        final Button addCollege = (Button) dialogAddCollege.findViewById(R.id.btn_create);

        textViewAddCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddCollege.show();
                addCollege.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String collegeName = editTextCollegeName.getText().toString().trim();
                        if(collegeName.isEmpty()){
                            Toast.makeText(getActivity(), "College Name cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        rootRef.child("colleges").push().setValue(collegeName, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if(firebaseError == null) {
                                    editTextCollegeName.setText("");
                                    Toast.makeText(getActivity(), "Add successful", Toast.LENGTH_SHORT).show();
                                    dialogAddCollege.dismiss();
                                }
                                else {
                                    Toast.makeText(getActivity(), "Unable to add college", Toast.LENGTH_SHORT).show();
                                    dialogAddCollege.dismiss();
                                }
                            }
                        });
                    }
                });
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
