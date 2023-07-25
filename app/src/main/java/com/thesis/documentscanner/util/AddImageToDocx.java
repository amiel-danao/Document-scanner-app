package com.thesis.documentscanner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AddImageToDocx {

    public static File addImageToDocx(Context context, File existingDocxFile, Bitmap bitmap) {

        try(FileInputStream fis = new FileInputStream(existingDocxFile)) {
            XWPFDocument document = new XWPFDocument(fis);

            // Convert Bitmap to a byte array (PNG format in this example)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close(); // Close the ByteArrayOutputStream

            // Get the first section of the document
            List<XWPFHeader> headers = document.getHeaderList();
            XWPFHeader header = (headers != null && headers.size() > 0) ? headers.get(0) : document.createHeader(HeaderFooterType.DEFAULT);
            XWPFParagraph paragraph = header.createParagraph();

            // Create an inline image with the image bytes
            int imgWidthEMU = Units.toEMU(64); // Adjust the width as needed
            int imgHeightEMU = Units.toEMU(64); // Adjust the height as needed
            String imgID = document.addPictureData(imageBytes, Document.PICTURE_TYPE_PNG);
            XWPFPicture picture = paragraph.createRun().addPicture(new ByteArrayInputStream(imageBytes), Document.PICTURE_TYPE_PNG, imgID, imgWidthEMU, imgHeightEMU);


            try(FileOutputStream fos = new FileOutputStream(existingDocxFile)) {
                // Save the modified XWPFDocument
                document.write(fos);
            }


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (InvalidFormatException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        return existingDocxFile;
    }


    private static byte[] getByteArrayFromImage(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return imageBytes;
    }
}