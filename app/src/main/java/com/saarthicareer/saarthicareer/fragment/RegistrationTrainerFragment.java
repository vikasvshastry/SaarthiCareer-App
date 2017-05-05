package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.activity.LoginActivity;
import com.saarthicareer.saarthicareer.activity.MainActivity;
import com.saarthicareer.saarthicareer.classes.Admin;
import com.saarthicareer.saarthicareer.classes.Trainee;
import com.saarthicareer.saarthicareer.classes.Trainer;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

public class RegistrationTrainerFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private Firebase rootRef = new Firebase("https://saarthi-career.firebaseio.com/");
    private String uid;

    public RegistrationTrainerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration_trainer, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        final Bundle bundle = this.getArguments();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());

        final EditText editTextCompany = (EditText)rootView.findViewById(R.id.input_company);
        final EditText editTextExperience = (EditText)rootView.findViewById(R.id.input_experience);
        final EditText editTextExpertise = (EditText)rootView.findViewById(R.id.input_expertise);
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
                final String company = editTextCompany.getText().toString().trim();
                final String experience = editTextExperience.getText().toString().trim();
                final String expertice = editTextExpertise.getText().toString();
                final String email = bundle.getString("email");
                final String name = bundle.getString("name");
                final String password = bundle.getString("password");
                final String mobileNo = bundle.getString("mobileno");
                final String type = bundle.getString("type");
                final String linkedInUrl = bundle.getString("linkedin");

                if(company.isEmpty()){
                    Toast.makeText(getActivity(), "Company field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(experience.isEmpty()){
                    Toast.makeText(getActivity(), "Training experience cannot be left empty or is incorrect", Toast.LENGTH_SHORT).show();
                }
                else if(expertice.isEmpty()){
                    Toast.makeText(getActivity(), "Expertice field cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {

                    progressDialog.setMessage("Registering...");
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        uid = firebaseAuth.getCurrentUser().getUid();

                                        if(type.equals("TRAINER")){
                                            Trainer trainer = new Trainer();
                                            trainer.setPassword(password);
                                            trainer.setType(type);
                                            trainer.setUid(uid);
                                            trainer.setPhno(mobileNo);
                                            trainer.setName(name);
                                            trainer.setCompany(company);
                                            trainer.setTrainingExperience(experience);
                                            trainer.setAreasOfExpertise(expertice);
                                            trainer.setEmail(email);
                                            trainer.setLinkedinUrl(linkedInUrl);
                                            rootRef.child("users").child(uid).setValue(type);
                                            rootRef.child("userDetails").child("TRAINER").child(uid).setValue(trainer);
                                        }
                                        else{
                                            Admin trainer = new Admin();
                                            trainer.setPassword(password);
                                            trainer.setType(type);
                                            trainer.setUid(uid);
                                            trainer.setPhno(mobileNo);
                                            trainer.setName(name);
                                            trainer.setCompany(company);
                                            trainer.setTrainingExperience(experience);
                                            trainer.setAreasOfExpertise(expertice);
                                            trainer.setEmail(email);
                                            trainer.setLinkedinUrl(linkedInUrl);
                                            rootRef.child("users").child(uid).setValue(type);
                                            rootRef.child("userDetails").child("ADMIN").child(uid).setValue(trainer);
                                        }

                                        progressDialog.dismiss();

                                        Toast.makeText(getActivity(),"Successfully Registered",Toast.LENGTH_SHORT).show();

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), "Verification email sent to your mail", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getActivity(), MainActivity.class));
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(getActivity(), "Unable to send verification mail", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                        });

                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(),"Registration Failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

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