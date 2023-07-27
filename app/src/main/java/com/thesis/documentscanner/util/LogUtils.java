package com.thesis.documentscanner.util;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;

public class LogUtils {
    public static void writeLog(String logMessage, String uid){
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        DocumentReference currentUserRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(uid);

        com.thesis.documentscanner.Models.Log log = new com.thesis.documentscanner.Models.Log(new Date(), currentUserRef, logMessage);

        DocumentReference newLogReference = FirebaseFirestore.getInstance().collection("Logs").document();
        // Add the document to the batch
        batch.set(newLogReference, log, SetOptions.merge());
        batch.commit();
    }
}
