package com.dianping.warehouse.main;

import com.dianping.warehouse.Datas.PreJob;
import com.dianping.warehouse.Datas.SlaJob;
import com.dianping.warehouse.util.Constants;
import com.dianping.warehouse.util.DataBase;
import com.dianping.warehouse.util.DateUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 下午5:26
 * To change this template use File | Settings | File Templates.
 */
public class Moniter {
    Logger logger = Logger.getLogger("DpAlert");
    public DataBase dw;
    public JobDistance initDatas;
    public ArrayList<PreJob> validJobs = new ArrayList<PreJob>();
    private String statusSQl;
    public Moniter(JobDistance initDatas){
        this.dw = initDatas.dw;
        this.initDatas = initDatas;
        this.statusSQl = initStatusSQL();
        while(DateUtils.getCurrentTime().isBefore(DateUtils.getEndTime(initDatas.process_day)))
        {
            start();
            try {
                Thread.sleep (Constants.defaultInterval) ;
            } catch (InterruptedException ie){}
        }

    }

    private String initStatusSQL() {
        if (initDatas.Mnjobs.size() == 0)
            return null;
        Iterator iter = initDatas.Mnjobs.keySet().iterator();
        String sql = "select task_id, end_time from etl_task_status where if_pre = 0 and status = 1 and time_id = '"+initDatas.process_day+"' and task_id in (";
        Object key = iter.next();
        sql += (Integer)key;
        while (iter.hasNext()){
            Object jobKey = iter.next();
            Integer keyJobId = (Integer)jobKey;
            sql += ","+keyJobId;
        }
        sql += ")";
        return sql;  //To change body of created methods use File | Settings | File Templates.
    }

    private void start() {
        //To change body of created methods use File | Settings | File Templates.
        getHalleyStatus();
        rtDistanceCalcu();
        updateSlaJobs();
        generateWarnInfo();
        close();

    }


    private void updateSlaJobs() {
        //To change body of created methods use File | Settings | File Templates.
        Iterator iter = initDatas.slaJobs.keySet().iterator();
        while(iter.hasNext()){
            Object slaJobId = iter.next();
            initDatas.slaJobs.get(slaJobId).update(initDatas.Mnjobs);
        }

    }

    private void close() {
        //To change body of created methods use File | Settings | File Templates.
        validJobs.clear();
    }

    private void generateWarnInfo() {
        try{
            Statement stat = dw.getStat();
            String historySql = "insert into  mn_sla_warn_history (sla_id, time_id, task_id, predict_time, key_task_id, key_task_status,record_time, report_status) values ";
            String valueList = "";
            Iterator iter = this.initDatas.slaJobs.entrySet().iterator();
            boolean flag = false;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                SlaJob temp = (SlaJob)val;
                String his = temp.getHistorySQL();
                if(his != null){
                    valueList += temp.getHistorySQL() +",";
                    flag = true;
                }

                stat.addBatch(temp.getUpdateSQL());
                System.out.println(temp.getUpdateSQL());
            }
            if(flag) {
                valueList = valueList.substring(0,valueList.length()-1);
                historySql += valueList;
                System.out.println(historySql);
                stat.addBatch(historySql);
            }
            stat.executeBatch();
            stat.close();
        }catch (Exception e){
            e.printStackTrace();
            logger.error("error when updating error information");
        }
    }



    private void getHalleyStatus() {
        String sql = "select task_id" +
        ",task_name, prio_lvl, recall_num" +
                ", status, run_num, recall_limit,start_time, end_time,owner " +
                "from etl_task_status where status in (-1,0,2,3,4,5,6,7)" +
                " and time_id = '"+this.initDatas.process_day+"'";
        ResultSet result = dw.executeQuery(sql);
        try {
            while(result.next()){
                validJobs.add(new PreJob(result.getInt("task_id"),initDatas.getJobCost(result.getInt("task_id"))
                        ,initDatas.getJobStddev(result.getInt("task_id")), result.getInt("status"), result.getString("start_time"), result.getString("task_name")));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            try {
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                logger.error("SQL error when getting job status.");
            }
        }

    }

    private void rtDistanceCalcu() {

        if(statusSQl != null){
            ResultSet result;
            try{
                result = dw.executeQuery(this.statusSQl);
                while(result.next()){
                    initDatas.Mnjobs.get(result.getInt("task_id")).is_success = true;
                    initDatas.Mnjobs.get(result.getInt("task_id")).finishTime = DateUtils.formatter.parseDateTime(result.getString("end_time"));
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error("error when updating success information");
            }
        }
        Iterator iter = initDatas.Mnjobs.keySet().iterator();
        while (iter.hasNext()){
            Object jobKey = iter.next();
            initDatas.Mnjobs.get(jobKey).predictFinishTime(validJobs);
        }

    }
}
