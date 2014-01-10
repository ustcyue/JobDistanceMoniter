package com.dianping.warehouse.main;

import com.dianping.warehouse.util.*;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 下午5:53
 * To change this template use File | Settings | File Templates.
 */
public class PreJob {
    private int job_id;
    private Double distance = null;
    private int status = -999;
    private DateTime beginTime = null;
    private Double jobCost;
    public boolean delayed = false;
    private Double jobDelayTime = 0.0;

    public PreJob(int job_id, Double jobCost, double jobstd, int status, String beginTime){
        this.job_id = job_id;
        this.status = status;
        this.jobCost = jobCost;
        if (status == Constants.RUNNING){
            this.beginTime = DateUtils.formatterDatabase.parseDateTime(beginTime);
            delayCalcu(jobstd);
        }
    }

    private void delayCalcu(double jobstd) {
        Duration dur = new Duration(this.beginTime,DateUtils.getCurrentTime());
        if (dur.getStandardSeconds()>this.jobCost.intValue()){
            this.jobDelayTime = dur.getStandardSeconds()+jobstd;
            this.delayed = true;
        }
    }

    public Double getJobDelayTime() {
        return jobDelayTime;
    }

    public int getJob_id() {
        return job_id;
    }

    public DateTime getBeginTime() {
        return beginTime;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;

    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Double getJobCost() {
        return jobCost;
    }
}
