package com.mintu.launchdatainformation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created By Mintu Giri on 9/26/2020.
 */
public class InitializeSdk{

    public static final String TAG = "InitializeSdk";

    public static void init(Context context)
    {
        final String PREFS_NAME = "FirstRun";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            Log.d(TAG,"NORMAL RUN");

        } else if (savedVersionCode == DOESNT_EXIST) {

            //This is a new install (or the user cleared the shared preferences)
            Log.d(TAG,"FIRST RUN");
            SharedPreferences prefSetUserId = context.getSharedPreferences("UserId",
                    MODE_PRIVATE);
            SharedPreferences.Editor editorUserId = prefSetUserId.edit();
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
            String today = formatter.format(date);
            editorUserId.putString("userId", "user_"+today);
            editorUserId.apply();
            String userId = prefSetUserId.getString("userId","");
            Log.d(TAG,userId);
            List<String> clicks = new ArrayList<>();
            Map<String,String> map = new HashMap<>();
            map.put("clicks","");
            FirebaseFirestore.getInstance().collection("Users")
                    .document(userId)
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG,"SUCCESSFULLY_CREATED");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,"FAILURE_CREATION : " + e.getMessage());
                }
            });

        } else if (currentVersionCode > savedVersionCode) {

            //This is an upgrade
            Log.d(TAG,"UPGRADE RUN");
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    public static void logClickDetails(Context context,String detail)
    {
        Date time = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String now = formatter.format(time);
        SharedPreferences prefSetUserId = context.getSharedPreferences("UserId",
                MODE_PRIVATE);
        String userId = prefSetUserId.getString("userId","");
        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .update("clicks", FieldValue.arrayUnion(now+"_"+detail))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"CLICK_UPDATE_SUCCESS");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"CLICK_UPDATE_FAILURE : " + e.getMessage());

            }
        });
    }
}
