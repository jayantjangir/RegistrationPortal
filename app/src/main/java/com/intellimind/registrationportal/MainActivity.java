package com.intellimind.registrationportal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String PROFILER_STRING = "Profiler";

    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private ImageView mProfilePic;
    private TextView mUserName;
    private TextView mEmail;
    private TextView mContact;
    private TextView mProfession;
    private TextView mRA;
    private TextView mDOB;
    private TextView mGender;
    private Button mlogout;

    private LinearLayout mEditValue;

    private ImageButton mBEditEmail;

    private ImageButton mBEditPhoneContact;
    private EditText mEditTextNewValue;
    private ImageButton mChooseNewValue;
    private ImageButton mCancelNewValue;
    private String newValue;

    private String downloadUrl;

    private ImageButton mImageChangePic;

    private Boolean editable;

    private Uri selectImageUri;

    private ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentUser = LogHandle.checkLogin(FirebaseAuth.getInstance(), this);
        LogHandle.checkDetailsAdded(mCurrentUser, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUser = LogHandle.checkLogin(FirebaseAuth.getInstance(), this);
        LogHandle.checkDetailsAdded(mCurrentUser, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_main);
        editable = false;
        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = LogHandle.checkLogin(FirebaseAuth.getInstance(), this);
        LogHandle.checkDetailsAdded(mCurrentUser, this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setElevation(0);

        mCurrentUser = LogHandle.checkLogin(FirebaseAuth.getInstance(), this);
        LogHandle.checkDetailsAdded(mCurrentUser, this);

        Log.d("--------------PROFILE", mCurrentUser.getUid());

        mProfilePic = findViewById(R.id.im_apv_profile_pic);
        mlogout = findViewById(R.id.logout_button);
        mUserName = findViewById(R.id.tv_apv_u_name);
        mEmail = findViewById(R.id.tv_apv_email);
        mContact = findViewById(R.id.tv_apv_contacts);
        mDOB = findViewById(R.id.tv_apv_date_of_birth);
        mRA = findViewById(R.id.et_sumit_retirement_age);
        mProfession = findViewById(R.id.tv_submit_select_profession);
        mGender = findViewById(R.id.tv_apv_gender);
        mEditValue = findViewById(R.id.ll_apv_edit_layout);
        mBEditEmail = findViewById(R.id.ib_email_edit);
        mBEditPhoneContact = findViewById(R.id.ib_contact_edit);
        mEditTextNewValue = findViewById(R.id.et_apv_new_value);
        mChooseNewValue = findViewById(R.id.ib_apv_edit_true);
        mCancelNewValue = findViewById(R.id.ib_apv_edit_false);
        mImageChangePic = findViewById(R.id.ib_change_profile_image);

        mEditValue.setVisibility(View.GONE);
        mBEditEmail.setVisibility(View.GONE);
        mImageChangePic.setVisibility(View.GONE);
        mBEditPhoneContact.setVisibility(View.GONE);

        mlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHandle.logout(mAuth,MainActivity.this);
            }
        });

        mImageChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(MainActivity.this);
            }
        });

        if(getIntent().hasExtra(PROFILER_STRING)){
            mCurrentUser = getIntent().getParcelableExtra(PROFILER_STRING);
        }
//        else  {
//            Toast.makeText(this, "No Riders Found!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }

        loadLocalDetails();

        if(!mCurrentUser.getUid().equals(mCurrentUser.getUid())){
            (findViewById(R.id.ll_apv_email)).setVisibility(View.GONE);
            (findViewById(R.id.ll_apv_contact)).setVisibility(View.GONE);
            loadUserDetails();
        } else {
            mImageChangePic.setVisibility(View.VISIBLE);
            Map<String, Object> map = LogHandle.mapCache;
            if(map != null){
                Log.d("sdfsdfoksfd---", map.toString());
                loadNetDetails(map);
            } else {
                loadUserDetails();
            }
        }
    }

    public void updateDatabase(Uri photoUri, int imageSize){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> map = new HashMap<>();
        map.put(AppUtils.PROFILE_PIC_URL_STRING, photoUri.toString());
        map.put(AppUtils.PROFILE_PIC_SIZE_STRING, imageSize);
        db.child("Users/" + mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    LogHandle.flushCache();
                    Toast.makeText(MainActivity.this, "Profile Picture Uploaded!", Toast.LENGTH_LONG).show();
                } else  {
                    Toast.makeText(MainActivity.this, "Error Uploading Picture", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadUserDetails(){
        if(AppUtils.isNetworkAvailable(this) == false){
            Toast.makeText(this, "Network Not Available", Toast.LENGTH_SHORT).show();
            mContact.setText("-");
            return;
        } else {
            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            db.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            Log.d("-0-0-", "fetching for -> " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(map == null){
                                mUserName.setText("-");
                                mContact.setText("-");
                                mGender.setText("-");
                                mDOB.setText("-");
//                                mRA.setText("-");
//                                mProfession.setText("-");
                                return;
                            } else {
                                Log.d("909090909", "-0-0-0-0-0-0-0-0-0-9230-4-" + map.toString());

                                loadNetDetails(map);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadLocalDetails(){
        mUserName.setText(mCurrentUser.getDisplayName());
        mEmail.setText(mCurrentUser.getEmail());
    }
    private void loadNetDetails(Map<String, Object> map){

        String cv = "";
        String cp = "";
        String cc = "+91";
        String gender = "-";
        String profession = "-";
        String dob = "";
        String ufn = "",uln = "";

        if(map.get("phoneNumber") != null){
            cv = map.get("phoneNumber").toString();
        }
        if(map.get("userFirstName") != null){
            ufn = map.get("userFirstName").toString();
        }
        if(map.get("userLastName") != null){
            uln = map.get("userLastName").toString();
        }
        if(map.get("retirementAge") != null){
            cp = map.get("retirementAge").toString();
        }

        if(map.get("DOB") != null){
            dob = map.get("DOB").toString();
        }

        if(map.get("countryCode") != null){
            cc = map.get("countryCode").toString();
        }

        if(map.get("gender") != null){
            gender = RegisterActivity.AVAILABLE_GENDERS[Integer.parseInt(map.get("gender").toString())];
        }

        if(map.get("profession") != null){
            profession = RegisterActivity.AVAILABLE_PROFESSIONS[Integer.parseInt(map.get("profession").toString())];
        }

        if(map.get(AppUtils.PROFILE_PIC_URL_STRING) != null && map.get(AppUtils.PROFILE_PIC_SIZE_STRING) != null){
            Log.d("----\n\n", "\n\n\n" + "\t----Loading data----\n\n\n");
            AppUtils.loadFromNetImage(MainActivity.this,
                    map.get(AppUtils.PROFILE_PIC_URL_STRING).toString(),
                    mProfilePic,
                    Integer.parseInt(map.get(AppUtils.PROFILE_PIC_SIZE_STRING).toString()));
        }

        mContact.setText(cc + " " + cv);
        mGender.setText(gender);
        mDOB.setText(dob);
        mUserName.setText(ufn + " " + uln);
//        mProfession.setText(profession);
//        mRA.setText(cp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RegisterActivity.GALLERY_PICK && resultCode == RESULT_OK) {
            selectImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){

                selectImageUri = result.getUri();

                final Bitmap imageBitmap = AppUtils.reduceImageSize(MainActivity.this, selectImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final int imageSize = imageBitmap.getByteCount();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                final byte[] uploadImageAsByteArray = baos.toByteArray();

                mProfilePic.setImageBitmap(imageBitmap);


                if(selectImageUri != null){

                    final StorageReference mImageRef = FirebaseStorage.getInstance().getReference().child(AppUtils.PROFILE_IMAGE_FOLDER_STRING);

                    final StorageReference mStorageRef = mImageRef.child(mCurrentUser.getUid() + "_" + UUID.randomUUID().toString() + ".jpeg");

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
                                Toast.makeText(MainActivity.this,"Profile Image Updated", Toast.LENGTH_LONG).show();
                                Uri uri = task.getResult();

                                Log.d("-------", "++++++++++\n\t" + uri.toString() + "------------\n\t");

                                updateDatabase(uri, imageSize);
                            }
                        }
                    });

                }

            } else  {
                Toast.makeText(MainActivity.this, "Error cropping", Toast.LENGTH_SHORT).show();
            }
        }

    }
    public void close(){
        finish();
    }
}
