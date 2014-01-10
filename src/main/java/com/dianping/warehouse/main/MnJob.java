package com.dianping.warehouse.main;

import com.dianping.warehouse.util.Constants;
import com.dianping.warehouse.util.DateUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 上午11:27
 * To change this template use File | Settings | File Templates.
 */
public class MnJob {
    Logger logger=Logger.getLogger("DpAlert");
    private int job_id;
    private boolean isDelayed = false;
    private PreJob keyPreJob;
    public DateTime warn_time;
    private DateTime finishTime = null;
    public HashMap<Integer,Double> distances = new HashMap<Integer, Double>();
    public ArrayList<Integer> slajobIds = new ArrayList<Integer>();
    HashMap<Integer, Double> jobCosts;
    HashMap<Integer, String> jobRelations;
    HashMap<Integer, Double> jobstds;
    public MnJob(int taskId, String time, String process_day, HashMap<Integer, Double> jobCosts,HashMap<Integer, Double> jobstds, HashMap<Integer, String> jobRelations){
        this.job_id = taskId;
        this.warn_time = DateUtils.getTargetTime(process_day, time);
        this.jobCosts = jobCosts;
        this.jobRelations = jobRelations;
        this.jobstds = jobstds;
        create_distance(job_id,0);
//        System.out.println(distances);
        System.out.println(distances.size());
        this.jobCosts = null;
        this.jobRelations = null;
        this.jobstds = null;
    }

    private void create_distance(int job_id, double pre_distance) {
        //To change body of created methods use File | Settings | File Templates.
        Double currentBase = getJobCost(job_id);
        String temp = jobRelations.get(job_id);
        if (temp != null){
            String[] pre_jobs = temp.split(",");
            for(String pre_job : pre_jobs){
                if(Integer.valueOf(pre_job) != job_id)
                    create_distance(Integer.valueOf(pre_job), currentBase+pre_distance);
            }
            addNodeToDistance(job_id, pre_distance + currentBase);
            return;
        }
        else{
            addNodeToDistance(job_id, pre_distance + currentBase);
            return;
        }
     //   create_distance()
    }

    private void addNodeToDistance(int job_id, double newValue) {
        //To change body of created methods use File | Settings | File Templates.
        Double temp = distances.get(job_id);
        if(temp!=null && temp > newValue){
            return;
        }
        else{
            distances.put(job_id,newValue);
            System.out.println(this.job_id+"  "+job_id+" \t"+temp +"   "+ newValue);
        }
    }

    public double getJobCost(int job_id){
        Double temp = this.jobCosts.get(job_id)+this.jobstds.get(job_id)*Constants.defaultDelayPercent;
        if(temp == null){
            temp = Constants.defaultCost;
        }
        return temp;
    }

    public WarnInfo predictFinishTime(ArrayList<PreJob> prejobs){
        DateTime maxfinishTime = DateUtils.formatter.parseDateTime("2010-01-01 00:00:00");
        int flag = 0;
        for(int i = 0; i<prejobs.size(); i++){
            PreJob job = prejobs.get(i);
            DateTime finishTime;
            int preJobId = job.getJob_id();
            int status = job.getStatus();
            if(distances.containsKey(preJobId)){
                if(status == Constants.RUNNING)
                {
                    finishTime = job.getBeginTime().plusSeconds(distances.get(preJobId).intValue()+job.getJobDelayTime().intValue());
                }
                else if(status == Constants.Fail)
                {
                     finishTime = DateUtils.getCurrentTime().plusSeconds(distances.get(preJobId).intValue() + Constants.defaultRepairTime);
                }
                else{
                    finishTime = DateUtils.getCurrentTime().plusSeconds(distances.get(preJobId).intValue());
                }
                if (finishTime.isAfter(maxfinishTime)){
                    maxfinishTime = finishTime;
                    keyPreJob = job;
                }
                flag = 1;
            }
        }
        if(flag == 1){
            this.finishTime = maxfinishTime;
            System.out.println(keyPreJob.getJob_id()+" "+distances.get(keyPreJob.getJob_id()));
            if(this.finishTime.isAfter(this.warn_time)){
                this.isDelayed = true;
                logger.error("KeyJob: " +job_id+" will be delayed because job: " + keyPreJob.getJob_id()
                        +" is still in status " + keyPreJob.getStatus() + " at time :" + DateUtils.getCurrentTime().toString(Constants.datepattern)
                        + ". predicted finishTime is "+finishTime.toString(Constants.datepattern));
                return new WarnInfo(this.job_id,slajobIds,finishTime,keyPreJob);
            }
        }
        return null;
    }
}
