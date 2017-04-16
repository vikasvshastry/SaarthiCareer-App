package com.saarthicareer.saarthicareer.activity;

import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.classes.Admin;
import com.saarthicareer.saarthicareer.classes.Post;
import com.saarthicareer.saarthicareer.classes.Trainee;
import com.saarthicareer.saarthicareer.classes.Trainer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    String senderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //working with the toolbar for this activity
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Compose");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final TextView textViewFrom = (TextView)findViewById(R.id.fromText);
        final EditText editTextSubject = (EditText)findViewById(R.id.input_heading);
        final EditText editTextBody = (EditText)findViewById(R.id.input_body);
        ImageView buttonSend = (ImageView)findViewById(R.id.sendButton);

        //getting name for textViewFrom
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final String uid = firebaseAuth.getCurrentUser().getUid();
        senderName = firebaseAuth.getCurrentUser().getDisplayName();
        textViewFrom.setText(senderName);

        final List<String> listColleges = new ArrayList<>();
        final Map<String,String> mapColleges = new ArrayMap<>();
        final List<String> listCourses = new ArrayList<>();
        final Map<String,String> mapCourses = new ArrayMap<>();

        //populating the spinners
        rootRef.child("colleges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapColleges.clear();
                listColleges.clear();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    mapColleges.put(child.getValue(String.class),child.getKey());
                    listColleges.add(child.getValue(String.class));
                }

                Spinner spinnerCollege = (Spinner)findViewById(R.id.spinnerCollege);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(NewPostActivity.this,android.R.layout.simple_spinner_item,listColleges);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCollege.setAdapter(adapter);

                spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        listCourses.clear();
                        mapCourses.clear();

                        String collegeKey = mapColleges.get(parent.getItemAtPosition(position).toString());
                        rootRef.child("college-course").child(collegeKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    listCourses.add(snapshot.getValue(String.class));
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

                                        Spinner spinnerCourse = (Spinner)findViewById(R.id.spinnerCourse);
                                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(NewPostActivity.this,android.R.layout.simple_spinner_item,listCourses);
                                        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerCourse.setAdapter(adapter2);

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
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        //on send button click -> sending
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String subject = editTextSubject.getText().toString().trim();
                String body = editTextBody.getText().toString().trim();
                String college,course;

                Spinner spinnerCollege = (Spinner)findViewById(R.id.spinnerCollege);
                Spinner spinnerCourse = (Spinner)findViewById(R.id.spinnerCourse);

                course = spinnerCourse.getSelectedItem().toString();
                college = spinnerCollege.getSelectedItem().toString();
                course = mapCourses.get(course);
                college = mapColleges.get(college);

                if(subject.isEmpty()){
                    Toast.makeText(NewPostActivity.this, "Subject cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(body.isEmpty()){
                    Toast.makeText(NewPostActivity.this, "Body cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Post post = new Post();
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                post.setHead(subject);
                post.setBody(body);
                post.setTime(currentDateTimeString.substring(12, currentDateTimeString.length()));
                post.setDate(currentDateTimeString.substring(0, 11));
                post.setSender(senderName);

                Firebase tempRef = rootRef.child("notifications").child("messagesBank").push();
                tempRef.setValue(post);
                rootRef.child("notifications").child("messages").child(college).child(course).push().setValue(tempRef.getKey());

                editTextBody.setText("");
                editTextSubject.setText("");

            }
        });
    }
}
