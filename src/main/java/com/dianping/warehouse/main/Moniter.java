package com.dianping.warehouse.main;

import com.dianping.warehouse.util.DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 下午5:26
 * To change this template use File | Settings | File Templates.
 */
public class Moniter {
    public DataBase dw;
    public JobDistance initDatas;
    public ArrayList<PreJob> validJobs = new ArrayList<PreJob>();
    public Moniter(JobDistance initDatas){
        this.dw = initDatas.dw;
        this.initDatas = initDatas;
        start();
    }

    private void start() {
        //To change body of created methods use File | Settings | File Templates.
        getHalleyStatus();
        rtDistanceCalcu();
    }



    private void getHalleyStatus() {
        String sql = "select task_id" +
        ",task_name, prio_lvl, recall_num" +
                ", status, run_num, recall_limit,start_time, end_time,owner " +
                "from etl_task_status where status in (-1,2,3,4,5,6,7)";
        ResultSet result = dw.executeQuery(sql);
        try {
            while(result.next()){
                validJobs.add(new PreJob(result.getInt("task_id"),initDatas.getJobCost(result.getInt("task_id"))
                        ,initDatas.getJobStddev(result.getInt("task_id")), result.getInt("status"), result.getString("start_time")));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    private void rtDistanceCalcu() {
        //To change body of created methods use File | Settings | File Templates.
        for(int i=0;i<initDatas.Mnjobs.size();i++){
            initDatas.Mnjobs.get(i).predictFinishTime(validJobs);
        }
    }
}
