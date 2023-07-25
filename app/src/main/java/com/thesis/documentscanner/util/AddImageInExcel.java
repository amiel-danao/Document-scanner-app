package com.thesis.documentscanner.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author javacodepoint.com
 *
 */
public class AddImageInExcel {

    private static final int CREATE_FILE = 1;
    /**
     * This Method loads the image from application resource and insert into the
     * Cell
     */
    private static void insertImageToCell(Workbook workbook, String fileType, int rowNum, Drawing drawing,
                                          Bitmap bitmap) throws IOException {

        //Loading image from application resource
        InputStream inputStream = BitmapUtils.bitmapToInputStream(bitmap, Bitmap.CompressFormat.PNG, 100);
//        InputStream is = AddImageInExcel.class.getClassLoader().getResourceAsStream(imageName);

        //Converting input stream into byte array
        byte[] inputImageBytes = IOUtils.toByteArray(inputStream);
        int inputImagePictureID = workbook.addPicture(inputImageBytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();

        ClientAnchor anchor = null;
        //Creating the client anchor based on file format
        if (fileType.equals("xls")) {
            anchor = new HSSFClientAnchor();
        } else {
            anchor = new XSSFClientAnchor();
        }
        anchor.setCol1(1);
        anchor.setCol2(2);
        anchor.setRow1(rowNum - 1);
        anchor.setRow2(rowNum);
        drawing.createPicture(anchor, inputImagePictureID);

    }

    public static File attachImageToExcel(Context context, File file, Bitmap bitmap) {

        Workbook workbook = null;
        FileOutputStream fos = null;
        try {
            String fileType;

            // Creating workbook object based on excel file format
            if (file.getName().endsWith(".xls")) {
                fileType = "xls";
            } else if (file.getName().endsWith(".xlsx")) {
                fileType = "xlsx";
            } else {
                System.err.println("File format should be XLS or XLSX only.");
                return null;
            }

            FileInputStream inputStream = new FileInputStream(file);
            workbook = WorkbookFactory.create(inputStream);
            Sheet sheet;
            int sheetsCount = workbook.getNumberOfSheets();
            if(sheetsCount == 0)
                sheet = workbook.createSheet("New worksheet");
            else
                sheet = workbook.getSheetAt(0);

            // Creating drawing object
            Drawing<?> drawing = sheet.createDrawingPatriarch();

            //Adding first row data
            Row row1 = sheet.createRow(0);
            row1.setHeight((short) ((25*128)/2.25));
            insertImageToCell(workbook, fileType, 1, drawing, bitmap);
            sheet.setColumnWidth(1, 25*128);
            inputStream.close();

            // Write the workbook to the excel file
            fos = new FileOutputStream(file);
            workbook.write(fos);
            System.out.println("Images have been added successfully.");

        } catch (IOException | RuntimeException e){
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }
}