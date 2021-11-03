package com.example.ispeed.View.Dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.ispeed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class ForgotPasswordDialog extends AppCompatDialogFragment {

    EditText et_email;
    Button btn_submit, btn_cancel;
    private static final String TAG = "ForgotPasswordDialog";
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_forgotpassword_dialog,null);
        et_email = view.findViewById(R.id.et_email);
        btn_submit = view.findViewById(R.id.btn_submit);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        FirebaseAuth mFbAuth = FirebaseAuth.getInstance();
        builder.setView(view);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_email.getText().toString().trim().isEmpty())
                {
                   et_email.setError("This Field is required.");
                }else {
                    mFbAuth.sendPasswordResetEmail(et_email.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @SuppressLint("CheckResult")
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toasty.info(getActivity(),
                                            "Please check your email address.", Toast.LENGTH_LONG)
                                            .show();
                                    dismiss();
                                }
                            });
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              dismiss();
            }
        });

        return builder.create();

    }
}
