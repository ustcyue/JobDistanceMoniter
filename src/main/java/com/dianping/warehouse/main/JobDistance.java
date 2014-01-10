/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-6
 * Time: 上午11:51
 * To change this template use File | Settings | File Templates.
 */
package com.dianping.warehouse.main;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.dianping.warehouse.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class JobDistance {
    static{
        PropertyConfigurator.configure("/Users/yxn/work/e_workspace/dpalert/src/main/resources/log4j.properties");
    }
    Logger logger=Logger.getLogger("DpAlert");
    public DataBase dw = DataBase.getDataBase();
    private HashMap<Integer,Double> jobCosts = new HashMap<Integer, Double>();
    private HashMap<Integer,Double> jobStddev = new HashMap<Integer, Double>();
    private HashMap<Integer,String> jobRelations = new HashMap<Integer, String>();
    public ArrayList<MnJob> Mnjobs  = new ArrayList<MnJob>();
    public HashMap<Integer, SlaJob>  slaJobs = new HashMap<Integer, SlaJob>();
    private String process_day;
    public JobDistance(String day){
        this.process_day = day;
        init();
    }
    public void queryFailJobs(){
        String sql = "select task_status_id, task_id" +
                     ",task_name, prio_lvl, recall_num" +
                     ", run_num, recall_limit,start_time, end_time,owner " +
                     "from etl_task_status where status=-1";
        ResultSet result = dw.executeQuery(sql);
        try {
            while(result.next()){
                System.out.println(result.getString("task_status_id")+" ");
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    private void init() {
        try {
            loadJobCosts();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            logger.error("job_run_time file read filed");
        }
        try {
            loadJobRel();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            logger.error("SQL error when accessing the etl_taskrela_cfg table");
        }
        try {
            loadMnList();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private void loadJobCosts() throws IOException {
        //To change body of created methods use File | Settings | File Templates.
        ReadFile avg = new ReadFile("/Users/yxn/work/e_workspace/JobDistance/dpmid_mn_jobavg.txt");
        String str=null;
        while((str = avg.readLine()) != null){
            String[] temp = str.split("\t");
//            System.out.println(Integer.valueOf(temp[0]) + "  " + Double.valueOf(temp[1]));
            if(temp[1].equals("NULL")||temp[2].equals("NULL")){
                continue;
            }
            else{
                jobCosts.put(Integer.valueOf(temp[0]), Double.valueOf(temp[1]));
                jobStddev.put(Integer.valueOf(temp[0]), Double.valueOf(temp[2]));
            }
        }
    }

    private void loadJobRel() throws SQLException {
        String sql = "select task_id, group_concat(task_pre_id) task_pre_ids " +
                "from etl_taskrela_cfg where remark is not NULL group by task_id";
        System.out.println(sql);
        ResultSet result = dw.executeQuery(sql);
        while(result.next()){
            jobRelations.put(result.getInt("task_id"),result.getString("task_pre_ids"));
        }
    }

    private void loadMnList() throws SQLException {
        String sql = "select sla_id, sla_name, task_id,warn_begin_time, warn_time, warn_type " +
                "from mn_sla_joblist";
        System.out.println(sql);
        ResultSet result = dw.executeQuery(sql);
        while (result.next()){
            int task_id = result.getInt("task_id");
            MnJob t = Mnjobs.get(task_id);
            int slaId = result.getInt("sla_id");
            SlaJob tempSla = new SlaJob(slaId,result.getString("sla_name"),result.getString("warn_begin_time")
                    ,result.getString("warn_time"),result.getInt("warn_type"),this.process_day);
            if(!slaJobs.containsKey(slaId))
                slaJobs.put(slaId,tempSla);
            if(t!=null)
            {
              t.slajobIds.add(slaId);
              if(t.warn_time.isAfter(tempSla.getWarn_time()))
                  t.warn_time = tempSla.getWarn_time();
            }
            else{
                MnJob temp = new MnJob(result.getInt("task_id"), result.getString("warn_time"), this.process_day,this.jobCosts, this.jobStddev, this.jobRelations);
                Mnjobs.add(temp);
            }
        }
    }

    public double getJobCost(int job_id){
        Double temp = this.jobCosts.get(job_id);
        if(temp == null){
            temp = Constants.defaultCost;
        }
        return temp;
    }
    public double getJobStddev(int job_id){
        Double temp = this.jobStddev.get(job_id);
        if(temp == null){
            temp = Constants.defaultCost;
        }
        return temp;
    }

    public static void main(String args[]){
        String date;
        if(args.length<1){
            date = DateUtils.getDefaultDay();
        }
        else{
            date = args[0];
        }
        JobDistance t = new JobDistance("2014-01-08");
        Moniter test = new Moniter(t);

//        String test = "test,aac";
//        String tests[] = test.split(",");
//        System.out.println(tests.length);
    }
}
