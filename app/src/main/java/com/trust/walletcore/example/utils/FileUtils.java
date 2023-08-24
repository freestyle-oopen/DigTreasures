package com.trust.walletcore.example.utils;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtils {
    public static String readStringFromFile(String file, String conent) {
        return null;
    }
    public static void appendStringIntoFile(String file, String conent) {
        BufferedWriter out = null;
        try {
            File fil = new File(file);
            if(!fil.exists()){
                fil.getParentFile().mkdirs();
                fil.createNewFile();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void appendStringIntoFileWithoutClose(String file, String conent) {
        BufferedWriter out = null;
        try {
            File fil = new File(file);
            if(!fil.exists()){
                fil.getParentFile().mkdirs();
                fil.createNewFile();
                Log.e("feedTypeFile","创建成功");
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
