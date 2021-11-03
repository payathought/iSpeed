package com.example.ispeed.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ispeed.R;
import com.example.ispeed.View.Dialog.TimePickerFragment;
import com.example.ispeed.View.Dialog.TimePickerFragmentEnding;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    TextView tv_selectDate;
    TextView et_getDate,tv_startinghour,tv_endinghour;
    int year, month, day;

    Button btn_okay;
    private static final String TAG = "SettingsActivity";
    DatePickerDialog.OnDateSetListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tv_selectDate = findViewById(R.id.tv_selectDate);
        et_getDate = findViewById(R.id.et_getDate);
        tv_startinghour = findViewById(R.id.tv_startinghour);
        tv_endinghour = findViewById(R.id.tv_endinghour);
        btn_okay = findViewById(R.id.btn_okay);

        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);


        tv_selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(SettingsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,listener,year,month,day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            }
        });


        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                month = month+1;

                String  date = day + "/"+ month + "/" + year;

                tv_selectDate.setText(date);

            }
        };

        et_getDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(SettingsActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date =  day +"/"+month+"/"+year;
                        et_getDate.setText(date);

                    }
                },year,month,day

                );

                datePickerDialog.show();
            }
        });
        btn_okay.setVisibility(View.GONE);
        tv_startinghour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timepicker = new TimePickerFragment();
                timepicker.show(getSupportFragmentManager(), "Time picker");

            }
        });
        tv_endinghour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timepicker = new TimePickerFragmentEnding();
                timepicker.show(getSupportFragmentManager(), "Time picker");



            }
        });


    tv_endinghour.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (et_getDate.getText().equals("Click Here")||
                    tv_endinghour.getText().equals("Click Here")||
                    tv_startinghour.getText().equals("Click Here")
            ){
                btn_okay.setVisibility(View.GONE);
            }else{
                btn_okay.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (et_getDate.getText().equals("Click Here")||
                    tv_endinghour.getText().equals("Click Here")||
                    tv_startinghour.getText().equals("Click Here")
            ){
                btn_okay.setVisibility(View.GONE);
            }else{
                btn_okay.setVisibility(View.VISIBLE);

            }
        }
    });
    tv_startinghour.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (et_getDate.getText().equals("Click Here")||
                    tv_endinghour.getText().equals("Click Here")||
                    tv_startinghour.getText().equals("Click Here")
            ){
                btn_okay.setVisibility(View.GONE);
            }else{
                btn_okay.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (et_getDate.getText().equals("Click Here")||
                    tv_endinghour.getText().equals("Click Here")||
                    tv_startinghour.getText().equals("Click Here")
            ){
                btn_okay.setVisibility(View.GONE);
            }else{
                btn_okay.setVisibility(View.VISIBLE);

            }
        }
    });







        btn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), DashboardMainActivity.class);
                intent.putExtra("date",et_getDate.getText().toString());
                intent.putExtra("start",tv_startinghour.getText().toString());
                intent.putExtra("end",tv_endinghour.getText().toString());

                Log.d(TAG, "onClick: end " + tv_endinghour.getText());
                Log.d(TAG, "onClick: start " + tv_startinghour.getText());
                startActivity(intent);
            }
        });




    }
}