package com.nairan.batman_launcher.primitives;

import android.content.Context;
import android.util.Log;

import com.nairan.batman_launcher.Batman_Launcher;

import org.apache.commons.math.stat.regression.SimpleRegression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by nrzhang on 25/8/2015.
 */
public class LoaderSingleton {

    private static final String TAG         = "Batman loader";
    public static final String PROC_DIR		= "/proc/";
    public static final String STAT         = "/stat";
    private static final String TASK        = "/task/";

    private static LoaderSingleton instance;

    private Context mCtx;
    private BufferedWriter mBw;
    private BufferedReader mBr;
    private HashMap<String, ProcessLog> prev_snapshot
            = new HashMap<String, ProcessLog>();

    public HashMap<String, ProcessLog> getPrevSnapshot(){
        return this.prev_snapshot;
    }

    public static LoaderSingleton getInstance(Context ctx){
        if (instance == null){
            // Create the instanceh
            instance = new LoaderSingleton(ctx);
        }

        // Return the instance
        return instance;
    }

    private LoaderSingleton(Context ctx){
        // Constructor hidden because this is a singleton
        this.mCtx = ctx;
        setPrevSnapshot( loadonce() );
    }

    public HashMap<String, ProcessLog> loadonce() {
        // TODO Auto-generated method stub
        File dir            							= new File(PROC_DIR);
        String[] children   							= dir.list();
        HashMap<String, ProcessLog> ret 	= new HashMap<String, ProcessLog>();

        try {

            for(int i = 0; i < children.length; ++i){

                try {
                    int pid = Integer.parseInt(children[i]);
                    /**
                     * Get app name
                     * CPUtime_app: per-app CPU time
                     */
                    File statFile			= new File(PROC_DIR.concat(children[i]).concat(STAT));
                    this.mBr				= new BufferedReader(new FileReader(statFile));
                    String processStat 		= this.mBr.readLine();
                    this.mBr.close();
                    String tmp				= processStat.substring(0, processStat.indexOf(")"));
                    String broakenName		= tmp.substring(tmp.indexOf("(") + 1);
                    //Log.i(TAG, "Broken name: " + broakenName);

                    /**
                     * Process only user-level installed app's
                     */
                    String fullName = PkgHandlerSingleton.getInstance(mCtx).isIstalled(broakenName);
                    if( !broakenName.equals("sh") && !broakenName.equals("cs") && (fullName != null) ){
                        /**
                         * for all threads belonging to the current app
                         *
                         * I.e., install a per-process data structure perProcess, with
                         * 		key is a thread name, and
                         * 		value is thread cpu reading
                         *
                         */
                        String taskDir          			= PROC_DIR.concat(children[i] + TASK);
                        String[] threads    				= new File(taskDir).list();
                        HashMap<String, Integer> perProcess = new HashMap<String, Integer>();
                        int cpu_process 					= 0;
                        for(int j = 0; j < threads.length; ++j){
                            try{
                                /**
                                 * Read the stat file
                                 */
                                statFile 			= new File(taskDir.concat(threads[j]).concat(STAT));
                                this.mBr			= new BufferedReader(new FileReader(statFile));
                                String line			= this.mBr.readLine();
                                this.mBr.close();

                                /**
                                 * Parse the stat file
                                 */
                                String partA		= line.substring(0, line.indexOf(")") + 1);
                                String partB		= line.substring(line.indexOf(")") + 2);
                                //String tid			= partA.substring(0, partA.indexOf(" "));
                                //String name			= partA.substring(partA.indexOf("("));
                                String[] partBArray = partB.split(" ");
                                Integer cpuTime		= Integer.parseInt(partBArray[11]) + Integer.parseInt(partBArray[12]);

                                /**
                                 * Add this thread's CPU time into per-process data structure "perProcess"
                                 */
                                if(!perProcess.containsKey(partA)){
                                    //Log.d(TAG, "\tThread: " + partA + " that have cpu time: " + cpuTime);
                                    perProcess.put(partA, cpuTime);
                                    cpu_process += cpuTime;
                                }

                            } catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        /**
                         * Push into per round hashmap
                         */
                        if(!ret.containsKey(fullName)){
                            //Log.d(TAG, "Process: " + fullName + " that have total cpu time: " + cpu_process);
                            ret.put(fullName, new ProcessLog(pid, perProcess, cpu_process));
                            //LoggerSingleton.getInstance(mCtx).write(perProcess.toString());
                        }
                    }
                } catch(FileNotFoundException | NumberFormatException e){
                    //  skip silently
                }
            } //end for, all folders in proc dir

        } catch(IOException e){}

        return ret;

    }

    public void setPrevSnapshot(HashMap<String, ProcessLog> curr) {
        this.prev_snapshot = curr;
    }
}
