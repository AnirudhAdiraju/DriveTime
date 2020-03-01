package com.example.workplacedamagemanager;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Dashboard  extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {
    DatePickerDialog picker;

    TextView totalhrs, nighthrs,comments,hrsperweek;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getSupportActionBar().hide();
        nighthrs = (TextView) findViewById(R.id.nighthrs);
        comments = (TextView) findViewById(R.id.comments);
        hrsperweek = (TextView) findViewById(R.id.hrsperweek);
        totalhrs = (TextView) findViewById(R.id.totalhrs);
        Spinner spinner;
        totalhrs.setText(""+Math.round((Double.parseDouble(Login.total))*100.0)/100.0);
        nighthrs.setText(""+Math.round((Double.parseDouble(Login.totalNight))*100.0)/100.0);
        comments.setText(Login.comments);
        final EditText eText = (EditText) findViewById(R.id.lisc);
        eText.setInputType(InputType.TYPE_NULL);
        eText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(Dashboard.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String month = "" + (monthOfYear + 1);
                                String day = "" + dayOfMonth;
                                if (monthOfYear + 1 < 10) {

                                    month = "0" + (monthOfYear + 1);
                                }
                                if (dayOfMonth < 10) {

                                    day = "0" + dayOfMonth;
                                }
                                eText.setText(month + "/" + day + "/" + year);
                                String[] date = eText.getText().toString().split("/");
                                LocalDate dateOfBirth = LocalDate.of(Integer.parseInt(date[2]), Integer.parseInt(date[0]), Integer.parseInt(date[1]));
                                LocalDate currentDate = LocalDate.now();
                                long diffInDays = Math.abs(ChronoUnit.DAYS.between(dateOfBirth, currentDate));
                                Log.d("oof",""+diffInDays);
                                hrsperweek.setText(""+Math.round(100*(50.0-(Double.parseDouble(totalhrs.getText().toString())))/(diffInDays/7.0))/100.0);

                            }
                        }, year, month, day);
                picker.show();
                }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        if (position != 0) {
            String item = parent.getItemAtPosition(position).toString();

            // Showing selected spinner item
            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void refresh(View v){

        final ProgressDialog loading = ProgressDialog.show(this,"Refreshing...","Please wait");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbwPpZvWH3q8Mo_ML5832K9m6zn_E9X-JIhA0WjtZADQ4dp1HYk/exec",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        EditText eText = (EditText) findViewById(R.id.lisc);
                        loading.dismiss();
                        String[] x = response.split(",");
                        totalhrs.setText(""+Math.round((Double.parseDouble(x[0]))*100)/100.0);
                        nighthrs.setText(""+Math.round((Double.parseDouble(x[2]))*100)/100.0);
                        comments.setText(x[6]);
                        Login.total = x[0];
                        Login.totalNight = x[2];
                        Login.comments = x[6];
                        if(eText.getText().toString().length()==10) {
                            String[] date = eText.getText().toString().split("/");
                            LocalDate dateOfBirth = LocalDate.of(Integer.parseInt(date[2]), Integer.parseInt(date[0]), Integer.parseInt(date[1]));
                            LocalDate currentDate = LocalDate.now();
                            long diffInDays = Math.abs(ChronoUnit.DAYS.between(dateOfBirth, currentDate));
                            Log.d("oof", "" + diffInDays);
                            hrsperweek.setText("" + Math.round(100 * (50.0 - (Double.parseDouble(totalhrs.getText().toString()))) / (diffInDays / 7.0)) / 100.0);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                //here we pass params
                params.put("action","refresh");
                params.put("username", RealMainActivity.username);

                return params;
            }
        };

        int socketTimeOut = 5000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);

    }
}
