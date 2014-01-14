package com.dianping.warehouse.Datas;

import com.dianping.warehouse.main.MnJob;
import com.dianping.warehouse.util.Constants;
import com.dianping.warehouse.util.DateUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-10
 * Time: 下午2:04
 * To change this template use File | Settings | File Templates.
 */
public class SlaJob {

    Logger logger=Logger.getLogger("DpAlert");

    private int sla_id;

    private String sla_name;

    private String processDay;

    private DateTime warn_begin_time;

    private DateTime warn_time;

    private DateTime predictFinishTime;

    private int warnType;

    private PreJob keyPreJob = null;

    private int mnJobId;

    Integer status = 0;

    private ArrayList<Integer> keyTasks = new ArrayList();

    public ArrayList<Integer> getKeyTasks() {
        return keyTasks;
    }

    public SlaJob(int sla_id, String sla_name, String warn_begin_time, String warn_time, int warnType, String processDay, int keyTask) {
        this.sla_id = sla_id;
        this.sla_name = sla_name;
        setWarn_begin_time(processDay, warn_begin_time);
        setWarn_time(processDay,warn_time);
        this.warnType = warnType;
        this.predictFinishTime = this.warn_time;
        this.processDay = processDay;
        this.mnJobId = keyTask;
        this.keyTasks.add(keyTask);
    }

    public String getSla_name() {
        return sla_name;
    }

    public void setSla_name(String sla_name) {
        this.sla_name = sla_name;
    }

    public DateTime getWarn_begin_time() {
        return warn_begin_time;
    }

    public void setWarn_begin_time(String processDay, String warn_begin_time) {
        this.warn_begin_time = DateUtils.getTargetTime(processDay, warn_begin_time);
    }

    public DateTime getWarn_time() {
        return warn_time;
    }

    public void setWarn_time(String processDay,String warn_time) {
        this.warn_time = DateUtils.getTargetTime(processDay, warn_time);
    }

    public int getWarnType() {
        return warnType;
    }

    public void setWarnType(int warnType) {
        this.warnType = warnType;
    }

    public int getSla_id() {

        return sla_id;
    }

    public void setSla_id(int sla_id) {
        this.sla_id = sla_id;
    }

    public void update(HashMap<Integer, MnJob> mnjobs) {
        DateTime maxfinishTime = DateUtils.formatter.parseDateTime("2010-01-01 00:00:00");
        boolean isFinish = true;
        for(int i=0;i < keyTasks.size()&&isFinish; i++){
            MnJob mnjob =  mnjobs.get(keyTasks.get(i));
            if(mnjob.is_success){
               if(maxfinishTime.isBefore(mnjob.finishTime)){
                    maxfinishTime = mnjob.finishTime;
                }
            }
            else
                isFinish = false;
        }
        if(isFinish && (status == -1|| status == 2)){
            this.status = 2;
            this.keyPreJob  = new PreJob();
            this.predictFinishTime = maxfinishTime;
            return;
        }
        if(isFinish ){
            this.status = 1;
            this.keyPreJob = new PreJob();
            this.predictFinishTime = maxfinishTime;
            return;
        }
        this.status = 3;
        boolean flag = false;
        for (int i = 0; i < keyTasks.size(); i++){
            MnJob mnjob =  mnjobs.get(keyTasks.get(i));
//            System.out.println(keyTasks.get(i));

            if(mnjob.finishTime == null)
                continue;
            if(mnjob.finishTime.isAfter(maxfinishTime)){
                maxfinishTime = mnjob.finishTime;
                flag = true;
                this.keyPreJob = mnjob.keyPreJob;
                this.mnJobId = mnjob.job_id;
            }
        }
        if(flag){
            this.predictFinishTime = maxfinishTime;
            this.status = 0;
            logger.error("SLA: " + this.sla_id +" is predicted to be complete at " +this.predictFinishTime.toString(Constants.datepattern)
                    +"; the current bottleneck is " +this.keyPreJob.getTask_name()+" it is still in status " +this.keyPreJob.getStatus());
        }
        if(maxfinishTime.isAfter(this.warn_time)){
            this.status = -1;
        }
    }

    public String getUpdateSQL(){
        String sql = "update mn_sla_report_list set predict_time = '" + this.predictFinishTime.toString(Constants.datepattern)
                     +"', report_status = " + this.status;
        if(keyPreJob != null){
            sql += ", task_id = " + keyPreJob.getJob_id();
            sql += ", task_name = '" + keyPreJob.getTask_name()+"'";
            sql += ", task_status = '" +keyPreJob.getStatus() +"'";
        }
        sql += " where sla_id = " + this.sla_id;
        return sql;
    }

    public String getHistorySQL(){
        if(this.status == 3)
            return null;
        else{
            String sql = "("+ this.sla_id+",'"+ this.processDay +"',"+this.mnJobId+",'"+this.predictFinishTime.toString(Constants.datepattern)+"',"
                    +this.keyPreJob.getJob_id()+","+ this.keyPreJob.getStatus()+",'"+DateUtils.getCurrentTime().toString(Constants.datepattern)+"'," +this.status+")";
            return sql;
        }

    }
    public String getInitSQL(){
        String sql = "insert into mn_sla_report_list (sla_id, sla_name, time_id, warn_begin_time, warn_time) values "
                +"(" +this.sla_id+",'"+this.sla_name+"','"+this.processDay+"','"+this.warn_begin_time.toString(Constants.datepattern)+"','"+this.warn_time.toString(Constants.datepattern)+"')";
        return sql;
    }
}
