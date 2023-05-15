package com.thesis.documentscanner.Views;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.Timestamp;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QRScannerFragment extends Fragment implements SurfaceHolder.Callback{

    private SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private View previewForm;
    private TextView txtUrl, txtFileName, txtFileExtension, txtVisibility, txtTimeStamp, txtUID, txtSender;
    private ImageButton downloadButton;
    private File scannedFile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        surfaceView = view.findViewById(R.id.surfaceView);
        previewForm = view.findViewById(R.id.previewForm);
        txtUrl = view.findViewById(R.id.txtUrl);
        txtFileName = view.findViewById(R.id.txtFileName);
        txtFileExtension = view.findViewById(R.id.txtFileExtension);
        txtVisibility = view.findViewById(R.id.txtVisibility);
        txtTimeStamp = view.findViewById(R.id.txtTimeStamp);
        txtUID = view.findViewById(R.id.txtUID);
        txtSender = view.findViewById(R.id.txtSender);
        ImageButton clearQRButton = view.findViewById(R.id.clearQRButton);

        clearQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scannedFile = null;
                txtUrl.setText("");
                txtFileName.setText("");
                txtFileExtension.setText("");
                txtVisibility.setText("");
                txtTimeStamp.setText("");
                txtUID.setText("");
                txtSender.setText("");
            }
        });
        downloadButton = view.findViewById(R.id.downloadButton);

        txtUrl.setOnClickListener(view1 -> {
            if (scannedFile != null) {
                Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedFile.getFileurl()));
                implicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(implicit);
            }
        });

        downloadButton.setOnClickListener(view12 -> {
            if (scannedFile == null){
                Toast.makeText(getContext(), "Please scan a valid qr first!", Toast.LENGTH_SHORT).show();
            }
            else{
                DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(scannedFile.getFileurl()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setTitle("File Download");
                request.setDescription("Downloading file...");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, scannedFile.getName() + "." + scannedFile.getFileType());

                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }
            }
        });
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                setupQRScanner();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            setupQRScanner();
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Explain why the camera permission is needed (optional)
        }
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void setupQRScanner() {
        barcodeDetector = new BarcodeDetector.Builder(requireContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(requireContext(), barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(this);

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0) {
                    String qrCodeText = qrCodes.valueAt(0).displayValue;

                    previewForm.setVisibility(View.VISIBLE);
                    downloadButton.setVisibility(View.VISIBLE);
                    try {
                        // Parse the JSON string
                        JSONObject jsonObject = new JSONObject(qrCodeText);

                        // Extract values from the JSON object
                        String url = jsonObject.getString("url");
                        String name = jsonObject.getString("fileName");
                        String fileType = jsonObject.getString("fileExtension");
                        String visibility = jsonObject.getString("visibility");
                        String timeStamp = jsonObject.getString("timestamp");
                        String sender = jsonObject.getString("sender");
                        String uid = jsonObject.getString("uid");
                        scannedFile = new File(url, "", name, fileType, visibility, Timestamp.now(), uid);
                        txtUrl.setText(url);
                        txtFileName.setText("File name: "+name);
                        txtFileExtension.setText("File type: "+fileType);
                        txtVisibility.setText("Visibility: "+visibility);
                        txtTimeStamp.setText("Timestamp: "+timeStamp);
                        txtSender.setText("Sender: "+sender);
                        txtUID.setText("UID: "+uid);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

//                    cameraSource.stop();
//                    requireActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(requireContext(), qrCodeText, Toast.LENGTH_SHORT).show();
//                            // Here you can use qrCodeText as needed, such as displaying it in a TextView or performing further processing.
//                        }
//                    });
                }
            }
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraSource.start(surfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        cameraSource.stop();
    }

    @Override
    public void onPause() {
        super.onPause();

        releaseCamera();
    }

    private void releaseCamera() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }
    }
}
