package com.saarthicareer.saarthicareer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.saarthicareer.saarthicareer.R;
import com.saarthicareer.saarthicareer.activity.LoginActivity;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;


public class RegistrationFirstFragment extends Fragment {

    public RegistrationFirstFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registration_first, container, false);

        String[] DROPDOWNLIST = {"STUDENT","TRAINER","ADMIN"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_dropdown_item_1line, DROPDOWNLIST);
        final MaterialBetterSpinner dropDownBox = (MaterialBetterSpinner)rootView.findViewById(R.id.drop_down);
        dropDownBox.setAdapter(arrayAdapter);

        final Button buttonNext = (Button)rootView.findViewById(R.id.btn_next);
        final EditText editTextName = (EditText)rootView.findViewById(R.id.input_name);
        final EditText editTextMobileNo = (EditText)rootView.findViewById(R.id.input_mobile_no);
        final EditText editTextEmail = (EditText)rootView.findViewById(R.id.input_email);
        final EditText editTextPassword = (EditText)rootView.findViewById(R.id.input_password);
        final EditText editTextLinkedInUrl = (EditText)rootView.findViewById(R.id.input_linkedin_url);
        final TextView textLoginLink = (TextView)rootView.findViewById(R.id.login_link_text);

        //sending back to Login if account already exists.
        textLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        //when NEXT is pressed.
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String mobileNo = editTextMobileNo.getText().toString().trim();
                String type = dropDownBox.getText().toString().trim();
                String linkedInUrl = "-";
                if(!editTextLinkedInUrl.getText().toString().trim().isEmpty()){
                    linkedInUrl = dropDownBox.getText().toString().trim();
                }

                if(email.isEmpty()){
                    Toast.makeText(getActivity(), "Email field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(name.isEmpty()){
                    Toast.makeText(getActivity(), "Name field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(password.length()<6){
                    Toast.makeText(getActivity(), "Password field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(mobileNo.length()<10){
                    Toast.makeText(getActivity(), "Mobile No. field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else if(type.isEmpty()){
                    Toast.makeText(getActivity(), "Type field cannot be left empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Bundle bundle = new Bundle();
                    bundle.putString("email",email);
                    bundle.putString("name",name);
                    bundle.putString("password",password);
                    bundle.putString("mobileno",mobileNo);
                    bundle.putString("type",type);
                    bundle.putString("linkedin",linkedInUrl);

                    Fragment fragment;

                    if(type.equals("STUDENT")){
                        fragment = new RegistrationStudentFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                    else if(type.equals("TRAINER")||type.equals("ADMIN")){
                        fragment = new RegistrationTrainerFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container_body, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
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