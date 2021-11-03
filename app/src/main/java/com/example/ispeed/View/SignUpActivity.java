package com.example.ispeed.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ispeed.FunctionMethod.FunctionMethod;
import com.example.ispeed.Model.SignUpModel;
import com.example.ispeed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import es.dmoral.toasty.Toasty;

public class SignUpActivity extends AppCompatActivity {
    Button btn_Register;
    EditText et_FirstName, et_LastName,et_EmailAddress,et_userName,et_Password,et_confirmPassword,et_institution;
    TextView tv_matcher;
    ProgressDialog progressDialog;
    FirebaseAuth mFirebaseAuth;
    SignUpModel signupModel;
    FirebaseFirestore db;
    FunctionMethod functionMethod = new FunctionMethod();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupModel = new SignUpModel();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Signing Up");
        progressDialog.setMessage("It will take a moment");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        btn_Register = findViewById(R.id.btn_Register);
        et_FirstName  = findViewById(R.id.et_FirstName);
        et_LastName  = findViewById(R.id.et_LastName);
        et_EmailAddress  = findViewById(R.id.et_EmailAddress);
        et_userName = findViewById(R.id.et_emailAddress);
        et_Password = findViewById(R.id.et_Password);
        et_confirmPassword = findViewById(R.id.et_confirmPassword);
        tv_matcher = findViewById(R.id.tv_matcher);
        et_institution = findViewById(R.id.et_institution);

        et_confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String pw1 = et_Password.getText().toString();
                String pw2 = et_confirmPassword.getText().toString();
                if(pw1.equals(pw2))
                {
                    tv_matcher.setVisibility(View.GONE);

                }else
                {
                    tv_matcher.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                String pw1 = et_Password.getText().toString();
                String pw2 = et_confirmPassword.getText().toString();
                if(pw1.equals(pw2))
                {
                    tv_matcher.setVisibility(View.GONE);

                }else
                {
                    tv_matcher.setVisibility(View.VISIBLE);
                }

            }
        });
        et_Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String pw1 = et_Password.getText().toString();
                String pw2 = et_confirmPassword.getText().toString();
                if(pw1.equals(pw2))
                {
                    tv_matcher.setVisibility(View.GONE);

                }else
                {
                    tv_matcher.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

                String pw1 = et_Password.getText().toString();
                String pw2 = et_confirmPassword.getText().toString();
                if(pw1.equals(pw2))
                {
                    tv_matcher.setVisibility(View.GONE);

                }else
                {
                    tv_matcher.setVisibility(View.VISIBLE);
                }

            }
        });
        btn_Register.setOnClickListener(v -> {

            if (functionMethod.haveNetworkConnected(SignUpActivity.this))
            {
                String pw1 = et_Password.getText().toString().trim();
                String pw2 = et_confirmPassword.getText().toString().trim();

                if (et_FirstName.getText().toString().trim().isEmpty())
                {
                    setErrors(et_FirstName);
                }else if (et_LastName.getText().toString().trim().isEmpty())
                {
                    setErrors(et_LastName);
                }else if (et_userName.getText().toString().trim().isEmpty())
                {
                    setErrors(et_userName);
                }else if (et_EmailAddress.getText().toString().trim().isEmpty())
                {
                    setErrors(et_EmailAddress);
                }else if (et_institution.getText().toString().trim().isEmpty())
                {
                    setErrors(et_institution);
                }else
                {

                    if (!pw1.equals(pw2)) {
                        et_confirmPassword.setError("Password doesn't match");
                    } else if (pw2.isEmpty()) {
                        et_confirmPassword.setError("Required Field");
                    } else {
                        progressDialog.show();
                        fireBaseAuthSignUp(et_EmailAddress.getText().toString(), et_Password.getText().toString());


                    }
                }
            }else{
                Toasty.error(SignUpActivity.this,
                        "Please check your internet connection.",Toast.LENGTH_LONG)
                        .show();
            }




        });

    }

    public void fireBaseAuthSignUp(String email, String password)
    {
        Log.d("fireBaseAuthSignUp", "fireBaseAuthSignUp: On function fireBaseAuthSignUp");
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with tcreateUserWithEmailhe signed-in user's information
                            Log.d("mFirebaseAuth", "createUserWithEmail:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            Log.d("authResult", "onSuccess: " + task);

                            String user_id = user.getUid();
                            signupModel.setUser_id(user_id);
                            Log.d("UserId", "onSuccess: user_id " + user_id);
                            savedDatatoFS();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("mFirebaseAuth", "createUserWithEmail:failure", task.getException());
                            progressDialog.dismiss();
                            Toasty.error(SignUpActivity.this,
                                    "Failed.", Toast.LENGTH_LONG)
                                    .show();


                        }
                    }
                });


    }

    public void savedDatatoFS(){

        if(signupModel.getUser_id() != null) {
            Log.e("If", "savedDatatoFS: Success");
            signupModel.setFirstname(et_FirstName.getText().toString());
            signupModel.setLastname(et_LastName.getText().toString());
            signupModel.setUsername(et_userName.getText().toString());
            signupModel.setInstitution(et_institution.getText().toString());
            signupModel.setPassword(et_Password.getText().toString());
            signupModel.setCreated(null);


            db.collection(getString(R.string.COLLECTION_USER_INFORMATION))
                    .add(signupModel)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "onComplete: " + task.getResult().getId());
                                progressDialog.dismiss();
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    Toasty.success(SignUpActivity.this,
                                                            "Check your email for Email Verification.", Toast.LENGTH_LONG)
                                                            .show();
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                    finish();
                                                }else{
                                                    Toasty.warning(SignUpActivity.this,
                                                            "Failed to send verification email.",Toast.LENGTH_LONG)
                                                            .show();
                                                }

                                            }
                                        });



                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Exception", "onFailure: " + e);
                }
            });
        }else{
            Log.e("Else", "savedDatatoFS: Failed");
            progressDialog.dismiss();
        }

    }
    private void setErrors(EditText txt)
    {
        txt.setError("Required Field");
        txt.requestFocus();
    }
}