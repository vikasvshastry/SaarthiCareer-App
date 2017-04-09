package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.activity.LoginActivity;
import com.saarthicareer.saarthicareer.activity.MainActivity;
import com.saarthicareer.saarthicareer.classes.Trainee;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistrationStudentFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    private String uid;
    List<String> listColleges = new ArrayList<>();
    Map<String,String> mapColleges = new ArrayMap<>();

    public RegistrationStudentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration_student, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        final Bundle bundle = this.getArguments();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());

        String[] MAJORS = {"ISE","CSE","ECE","EEE","ME","CIV","MCA"};
        ArrayAdapter<String> arrayAdapterMajors = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line, MAJORS);
        final MaterialBetterSpinner dropDownBoxMajors = (MaterialBetterSpinner)rootView.findViewById(R.id.drop_down_major);
        dropDownBoxMajors.setAdapter(arrayAdapterMajors);

        final MaterialBetterSpinner dropDownBoxColleges = (MaterialBetterSpinner)rootView.findViewById(R.id.drop_down_college);
        rootRef.child("colleges").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    mapColleges.put(child.getValue(String.class),child.getKey());
                    listColleges.add(child.getValue(String.class));
                }
                ArrayAdapter<String> arrayAdapterColleges = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line,listColleges);
                dropDownBoxColleges.setAdapter(arrayAdapterColleges);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getActivity(), "Unable to retrieve list of Colleges", Toast.LENGTH_SHORT).show();
            }
        });

        final EditText editTextUsn = (EditText)rootView.findViewById(R.id.input_usn);
        final EditText editTextYearOfPassing = (EditText)rootView.findViewById(R.id.input_yearOfPassing);
        final Button buttonCreate = (Button)rootView.findViewById(R.id.btn_create);
        final TextView textViewLogin = (TextView)rootView.findViewById(R.id.login_link_text);

        //sending back to Login if account already exists.
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        //when create is pressed.
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String usn = editTextUsn.getText().toString().trim();
                final String yearOfPassing = editTextYearOfPassing.getText().toString().trim();
                final String major = dropDownBoxMajors.getText().toString();
                final String college = dropDownBoxColleges.getText().toString();
                final String email = bundle.getString("email");
                final String name = bundle.getString("name");
                final String password = bundle.getString("password");
                final String mobileNo = bundle.getString("mobileno");
                final String type = bundle.getString("type");
                final String linkedInUrl = bundle.getString("linkedin");

                if(usn.isEmpty()){
                    Toast.makeText(getActivity(), "Usn field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(yearOfPassing.length()<4){
                    Toast.makeText(getActivity(), "Year of Passing field cannot be left empty or is incorrect", Toast.LENGTH_SHORT).show();
                }
                else if(major.isEmpty()){
                    Toast.makeText(getActivity(), "Please select your Major", Toast.LENGTH_SHORT).show();
                }
                else if(college.isEmpty()){
                    Toast.makeText(getActivity(), "Please select your College", Toast.LENGTH_SHORT).show();
                }
                else {

                    progressDialog.setMessage("Registering...");
                    progressDialog.show();

                    try {
                        Toast.makeText(getActivity(), ""+email+name+password, Toast.LENGTH_SHORT).show();

                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    uid = firebaseAuth.getCurrentUser().getUid();

                                    Trainee trainee = new Trainee();
                                    trainee.setPassword(password);
                                    trainee.setType(type);
                                    trainee.setUid(uid);
                                    trainee.setPhno(mobileNo);
                                    trainee.setName(name);
                                    trainee.setCollege(mapColleges.get(college));
                                    trainee.setMajor(major);
                                    trainee.setUsn(usn);
                                    trainee.setYearOfGraduation(yearOfPassing);
                                    trainee.setEmail(email);
                                    trainee.setLinkedinUrl(linkedInUrl);

                                    rootRef.child("users").child(uid).setValue(type);
                                    rootRef.child("userDetails").child("STUDENT").child(uid).setValue(trainee);

                                    progressDialog.dismiss();

                                    Toast.makeText(getActivity(), "Successfully Registered", Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getActivity(), "Verification email sent to your mail", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    startActivity(new Intent(getActivity(), MainActivity.class));
                                    getActivity().finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }catch (Exception e){
                        Toast.makeText(getActivity(), ""+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
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