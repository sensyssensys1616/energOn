package com.nairan.batman_launcher.primitives;

import java.util.HashMap;

/**
 * Created by nrzhang on 9/9/2015.
 */
public class ProcessLog {
    private int pid;
    private HashMap<String, Integer> perProcess;
    private int cpu_process;

    public ProcessLog(int pid, HashMap<String, Integer> perProcess, int cpu_process){
        this.pid = pid;
        this.perProcess = perProcess;
        this.cpu_process = cpu_process;
    }

    public int getPid(){
        return this.pid;
    }

    public HashMap<String, Integer> getPerProcess(){
        return this.perProcess;
    }

    public int getProcessCpu(){
        return this.cpu_process;
    }
}
