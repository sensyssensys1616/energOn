package com.nairan.batman_launcher.primitives;

import android.content.Context;
import android.os.Environment;

import com.nairan.batman_launcher.Batman_Launcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by nrzhang on 25/8/2015.
 */
public class LoggerSingleton {

    public static final String LOG      = "cpu time";
    public static final String OUTPUT   = "Output";
    public static final String CHECK    = "Check";
    public static final String ALL      = "All";

    public static final String LOGFILE      = "cpuLog.csv";
    public static final String OUTPUTFILE   = "output_final.csv";
    public static final String CHECKFILE    = "cpuCheck.csv";
    public static final String ALLFILE      = "all_final.csv";

    private static LoggerSingleton instance;

    private File mLogFile;
    private File mOutputFile;
    private File mCheckFile;
    private File mAllFile;
    private BufferedWriter mBw_log;
    private BufferedWriter mBw_output;
    private BufferedWriter mBw_check;
    private BufferedWriter mBw_all;

    public static LoggerSingleton getInstance(Context ctx){
        if (instance == null){
            // Create the instance
            instance = new LoggerSingleton(ctx);
        }

        // Return the instance
        return instance;
    }

    private LoggerSingleton(Context ctx){
        // Constructor hidden because this is a singleton
        // Initiate folder and files
        String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirName = absPath + "/batman_launcher";
        File dir = new File(dirName);
        dir.mkdirs();
        this.mLogFile = new File(dir, LOGFILE);
        this.mOutputFile = new File(dir, OUTPUTFILE);
        this.mCheckFile = new File(dir, CHECKFILE);
        this.mAllFile = new File(dir, ALLFILE);
        try {
            this.mBw_log = new BufferedWriter(new FileWriter(
                    this.mLogFile, true));
            this.mBw_output = new BufferedWriter(new FileWriter(
                    this.mOutputFile, true));
            this.mBw_check = new BufferedWriter(new FileWriter(
                    this.mCheckFile, true));
            this.mBw_all = new BufferedWriter(new FileWriter(
                    this.mAllFile, true));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void write(String tag, String s) {
        try {
            if(tag.equals(LOG)) {
                this.mBw_log.write(s + "\n");
                this.mBw_log.flush();
            }

            if(tag.equals(OUTPUT)) {
                this.mBw_output.write(s + "\n");
                this.mBw_output.flush();
            }

            if(tag.equals(CHECK)) {
                this.mBw_check.write(s + "\n");
                this.mBw_check.flush();
            }

            if(tag.equals(ALL)) {
                this.mBw_all.write(s + "\n");
                this.mBw_all.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
