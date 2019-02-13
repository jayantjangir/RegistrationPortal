package com.intellimind.registrationportal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.autofill.AutofillValue;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int PICK_IMAGE_REQUEST = 75;
    public static final String MALE_STRING = "Male";
    public static final String FEMALE_STRING = "Female";

    int day, month, year, hour, minute;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

    public static final String Profession_Engineer = "Engineer";
    public static final String Profession_Finance = "Finance";
    public static final String Profession_Insurance = "Insurance";
    public static final String Profession_Other = "Other";

    public static final int GALLERY_PICK = 102;

    public static final String[] AVAILABLE_GENDERS = {MALE_STRING, FEMALE_STRING};
    public static final String[] AVAILABLE_PROFESSIONS = {Profession_Engineer,Profession_Finance,
            Profession_Insurance,Profession_Other};

    private ImageButton mpick;
    private TextView mDisplay;
    private CircleImageView mProfilePic;
    private EditText mUserFName,mUserLName, mRetirementAge;
    private Button mSubmitDetails;
    private TextView mGenderTextView, mProfessionTextView;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef, mImageRef;
    private FirebaseStorage mStorage;
    private UploadTask uploadTask;
    private ProgressDialog progressDialog;
    private Uri selectImageUri;
    private String userFirstNameFinal,userLastNameFinal,userRetirementAgeFinal;
    private Calendar calendarFinal;
    private int genderFinal,professionFinal;
    private String downloadUrl;
    private ProgressBar progressBar;
    private String phonenumber;
    private FirebaseAuth mAuth;

    private TextView mCountryCode;
    private EditText mPhoneNumber;
    private String countryCodeFinal;
    private String phoneNumberFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Profile Details");

        firebaseUser = LogHandle.checkLogin(FirebaseAuth.getInstance(), this);

        mGenderTextView = findViewById(R.id.tv_submit_select_gender);
        mProfessionTextView = findViewById(R.id.tv_submit_select_profession);
        mProfilePic = findViewById(R.id.iv_submit_profile_pic);
        mRetirementAge = findViewById(R.id.et_sumit_retirement_age);
        mUserFName = findViewById(R.id.et_sumit_profile_fname);
        mUserLName = findViewById(R.id.et_sumit_profile_lname);

        progressBar = findViewById(R.id.pb_apd_progress_bar);
        progressBar.setVisibility(View.GONE);
        mSubmitDetails = findViewById(R.id.b_submit_profile_od);
        mSubmitDetails.setClickable(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        genderFinal = 0;
        mAuth = FirebaseAuth.getInstance();

        mCountryCode = findViewById(R.id.et_sumit_profile_country_code);
        countryCodeFinal = mCountryCode.getText().toString();
        mPhoneNumber = findViewById(R.id.et_sumit_profile_phone);


        mpick = findViewById(R.id.b_pick_cr);
        mDisplay = findViewById(R.id.tv_date_time_cr);

        Calendar calendar = Calendar.getInstance();
        calendarFinal = calendar;
        java.text.DateFormat dateFormat = java.text.DateFormat
                .getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT);
        String str = dateFormat.format(calendar.getTime());
        mDisplay.setText(str);

        mPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString();
                if (TextUtils.isEmpty(value) == false && value.length() == 10) {
                    phoneNumberFinal = value;
                } else {
                    mPhoneNumber.setError("Enter valid number");
                    mPhoneNumber.requestFocus();
                    phoneNumberFinal = null;
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mpick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, RegisterActivity.this, year, month, day);
                datePickerDialog.show();
            }
        });

        mSubmitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

//                mSubmitDetails.setClickable(false);

                if (!TextUtils.isEmpty(userFirstNameFinal)
                        && (genderFinal == 0 || genderFinal == 1 )
                        && (professionFinal == 0 || professionFinal == 1 || professionFinal == 2 || professionFinal == 3)
                        && (!TextUtils.isEmpty(phoneNumberFinal))
                        && !TextUtils.isEmpty(calendarFinal.toString())
                        && !TextUtils.isEmpty(userRetirementAgeFinal) && userRetirementAgeFinal.length()==2 ){

                    if (firebaseUser == null) {
                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    } else {

                        String imageExt;

                        if(selectImageUri != null){

                            final Bitmap imageBitmap = AppUtils.reduceImageSize(RegisterActivity.this, selectImageUri);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                            final int imageSize = imageBitmap.getByteCount();

                            final byte[] uploadImageAsByteArray = baos.toByteArray();

                            mProfilePic.setImageBitmap(imageBitmap);

                            mStorageRef = mImageRef.child(firebaseUser.getUid() + "_" + UUID.randomUUID().toString() + ".jpeg");

                            UploadTask uploadTask = mStorageRef.putBytes(uploadImageAsByteArray);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if(!task.isSuccessful()){
                                        throw task.getException();
                                    }
                                    return mStorageRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this,"Profile Image Updated", Toast.LENGTH_LONG).show();
                                        Uri uri = task.getResult();

                                        Log.d("-------", "++++++++++\n\t" + uri.toString() + "------------\n\t");

                                        updateDatabase(true, uri, imageSize);
                                    }
                                }
                            });

                        } else {

                            updateDatabase(false, null, -1);

                        }

                        phonenumber = countryCodeFinal + phoneNumberFinal;
                        Intent intent = new Intent(RegisterActivity.this, OTPActivity.class);
                        intent.putExtra("phoneNumberFinal", phoneNumberFinal);
                        intent.putExtra("countryCodeFinal", countryCodeFinal);
                        intent.putExtra(AppUtils.CURRENT_USER_STRING, mAuth.getCurrentUser());
                        intent.putExtra(AppUtils.CURRENT_USER_UID_STRING, mAuth.getCurrentUser().getUid());
                        startActivity(intent);
                        finish();

                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Please provide complete details.", Toast.LENGTH_LONG).show();
                }
            }
        });

        mImageRef = FirebaseStorage.getInstance().getReference().child(AppUtils.PROFILE_IMAGE_FOLDER_STRING);
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1, 1)
//                        .start(RegisterActivity.this);
            }
        });

        mUserFName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString();
                if (TextUtils.isEmpty(value) == false) {
                    userFirstNameFinal = value;
                } else {
                    mUserFName.setError("Enter first name");
                    mUserFName.requestFocus();
                    userFirstNameFinal=null;
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUserLName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString();
                if (TextUtils.isEmpty(value) == false) {
                    userLastNameFinal = value;
                } else {
                    mUserLName.setError("Enter last name");
                    mUserLName.requestFocus();
                    userLastNameFinal=null;
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mRetirementAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = s.toString();
                if (TextUtils.isEmpty(value) == false) {
                    userRetirementAgeFinal = value;
                } else {
                    mRetirementAge.setError("Enter Retirement Age");
                    mRetirementAge.requestFocus();
                    userRetirementAgeFinal=null;
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mGenderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Select Gender")
                        .setSingleChoiceItems(AVAILABLE_GENDERS, genderFinal, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                genderFinal = which;
                                mGenderTextView.setText(AVAILABLE_GENDERS[genderFinal]);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        mProfessionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Select Profession")
                        .setSingleChoiceItems(AVAILABLE_PROFESSIONS, professionFinal, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                professionFinal = which;
                                mProfessionTextView.setText(AVAILABLE_PROFESSIONS[professionFinal]);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    public void updateDatabase(final boolean uploadProfilePic, final Uri photoUri, final int imageSize){

        UserProfileChangeRequest userProfileChangeRequest;

        userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(userFirstNameFinal+" "+userLastNameFinal)
                .build();

        firebaseUser.updateProfile(userProfileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                        if(task.isSuccessful()) {

                            mDatabase = FirebaseDatabase.getInstance().getReference("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            HashMap<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put(AppUtils.USER_FNAME_STRING, userFirstNameFinal);
                            childUpdates.put(AppUtils.USER_LNAME_STRING, userLastNameFinal);
                            childUpdates.put(AppUtils.EMAIL_STRING, firebaseUser.getEmail());
                            childUpdates.put(AppUtils.CURRENT_USER_UID_STRING, mAuth.getCurrentUser().getUid());
                            childUpdates.put(AppUtils.GENDER_STRING, genderFinal);
                            childUpdates.put(AppUtils.PROFESSION_STRING, professionFinal);
                            childUpdates.put(AppUtils.RA_STRING, userRetirementAgeFinal);
                            childUpdates.put(AppUtils.DOB_STRING,calendarFinal);
                            childUpdates.put(AppUtils.EMAIL_STRING_VERIFICATION,mAuth.getCurrentUser().isEmailVerified());
                            childUpdates.put(AppUtils.DOB_DSTRING, calendarFinal.DAY_OF_YEAR);
                            childUpdates.put(AppUtils.DOB_MSTRING, calendarFinal.MONTH);
                            childUpdates.put(AppUtils.DOB_YSTRING,calendarFinal.YEAR);
                            childUpdates.put(AppUtils.COUNTRY_CODE_STRING, countryCodeFinal);
                            childUpdates.put(AppUtils.PHONE_NUMBER_STRING, phoneNumberFinal);
                            childUpdates.put(AppUtils.PHONE_VERIFIED_STRING, false);
                            if(uploadProfilePic == true) {
                                childUpdates.put(AppUtils.PROFILE_PIC_URL_STRING, photoUri.toString());
                                childUpdates.put(AppUtils.PROFILE_PIC_SIZE_STRING, imageSize);
                            }

                            mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        progressBar.setVisibility(View.GONE);

                                        Intent intent = new Intent(RegisterActivity.this, OTPActivity.class);
                                        startActivity(intent);
                                        finish();
                                        mSubmitDetails.setClickable(true);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Error Saving Data!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                selectImageUri = result.getUri();
                mProfilePic.setImageURI(selectImageUri);
            } else  {
                Toast.makeText(RegisterActivity.this, "Error cropping", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.i(this.getClass().getName(), "----------------------------------YEAR --- " + year);
        yearFinal = year;
        monthFinal = month;
        dayFinal = dayOfMonth;

        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(RegisterActivity.this, RegisterActivity.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hourFinal = hourOfDay;
        minuteFinal = minute;
        Calendar calendar = new GregorianCalendar(yearFinal, monthFinal, dayFinal, hourFinal, minuteFinal);
        calendarFinal = calendar;
        java.text.DateFormat dateFormat = java.text.DateFormat
                .getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT);
        String str = dateFormat.format(calendar.getTime());
        mDisplay.setText(str);
    }

    public void close() {
        finish();
    }

    private void uploadImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        if(selectImageUri!=null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading . . .");
            progressDialog.show();

//            selectImageUri = data.getData();
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            selectImageUri = result.getUri();

            final Bitmap imageBitmap = AppUtils.reduceImageSize(RegisterActivity.this, selectImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final int imageSize = imageBitmap.getByteCount();

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mProfilePic.setImageBitmap(imageBitmap);

            StorageReference ref = FirebaseStorage.getInstance().getReference().child("Users/" + mAuth.getCurrentUser().getUid());
            ref.putFile(selectImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Failed"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Cancel Uploading ",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploading "+(int)progress+"%");
                }
            });
        }
    }
}
