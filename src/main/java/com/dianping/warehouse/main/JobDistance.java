/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-6
 * Time: 上午11:51
 * To change this template use File | Settings | File Templates.
 */
package com.dianping.warehouse.main;
import com.dianping.warehouse.Datas.SlaJob;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.dianping.warehouse.util.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JobDistance {
    static{
 //       PropertyConfigurator.configure("/data/deploy/dp/dpalert/log4j.properties");
          PropertyConfigurator.configure(Constants.jarpath+"/log4j.properties");

    }
    Logger logger=Logger.getLogger("DpAlert");
    public DataBase dw = DataBase.getDataBase();
    private HashMap<Integer,Double> jobCosts = new HashMap<Integer, Double>();
    private HashMap<Integer,Double> jobStddev = new HashMap<Integer, Double>();
    private HashMap<Integer,String> jobRelations = new HashMap<Integer, String>();
    public HashMap<Integer,MnJob> Mnjobs  = new HashMap<Integer, MnJob>();
    public HashMap<Integer, SlaJob>  slaJobs = new HashMap<Integer, SlaJob>();
    public String process_day;
    public JobDistance(String day){
        this.process_day = day;
        init();
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

        initWarnDatabase();

//        tempTest();

    }
    private void tempTest(){
        SlaJob tempSla = new SlaJob(1,"test_mtd","06:00:00","04:30:00",1,this.process_day,500132);
        slaJobs.put(1,tempSla);
        tempSla = new SlaJob(2,"test_sales","06:00:00","04:30:00",1,this.process_day,500692);
        slaJobs.put(2,tempSla);
        MnJob tempmn = new MnJob(500132,"test_mtd_job", "04:30:00", this.process_day,this.jobCosts, this.jobStddev, this.jobRelations);
        Mnjobs.put(500132,tempmn);
        tempmn = new MnJob(500692,"test_sales_job", "04:30:00", this.process_day,this.jobCosts, this.jobStddev, this.jobRelations);
        Mnjobs.put(500692,tempmn);
    }
    private void initWarnDatabase() {
        //To change body of created methods use File | Settings | File Templates.
        String sql = "delete from mn_sla_report_list";
        dw.executeUpdate(sql);
        Iterator iter = this.slaJobs.entrySet().iterator();
        try{
            Statement stat = dw.getStat();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object val = entry.getValue();
                SlaJob temp = (SlaJob)val;
                stat.addBatch(temp.getInitSQL());
                stat.executeBatch();
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("error when initing warning database");
        }

    }


    private void loadJobCosts() throws IOException {
        //To change body of created methods use File | Settings | File Templates.
        ReadFile avg = new ReadFile(Constants.jarpath+"/dpmid_mn_jobavg.txt");
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
        result.close();
    }

    private void loadMnList() throws SQLException {
        String sql = "select a.sla_id, a.sla_name, b.task_id, b.task_name, a.warn_begin_time, a.warn_time, a.warn_type " +
                "from mn_sla_list a join mn_sla_joblist b on a.sla_id = b.sla_id";
        System.out.println(sql);
        ResultSet result = dw.executeQuery(sql);
        while (result.next()){
            int task_id = result.getInt("task_id");
            int slaId = result.getInt("sla_id");
            SlaJob tempSla = new SlaJob(slaId,result.getString("sla_name"),result.getString("warn_begin_time")
                    ,result.getString("warn_time"),result.getInt("warn_type"),this.process_day,task_id);
            if(!slaJobs.containsKey(slaId))
                slaJobs.put(slaId,tempSla);
            else{
                slaJobs.get(slaId).getKeyTasks().add(task_id);
            }
            if(!Mnjobs.containsKey(task_id))
            {
                MnJob temp = new MnJob(result.getInt("task_id"),result.getString("task_name"), result.getString("warn_time"), this.process_day,this.jobCosts, this.jobStddev, this.jobRelations);
                Mnjobs.put(task_id,temp);
            }
        }
        result.close();
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
        JobDistance t = new JobDistance(date);
        Moniter test = new Moniter(t);

//        String test = "test,aac";
//        String tests[] = test.split(",");
//        System.out.println(tests.length);
    }
}
