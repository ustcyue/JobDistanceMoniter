package com.dianping.warehouse.main;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-10
 * Time: 下午2:47
 * To change this template use File | Settings | File Templates.
 */
public class WarnInfo {
    private int task_id;
    private ArrayList<Integer> slajobIds;
    private DateTime predictTime;
    private PreJob keyPreJob;
    private int keyPreJobstatus;

    public WarnInfo(int task_id, ArrayList<Integer> slajobIds, DateTime predictTime,PreJob keyPreJob) {
        this.task_id = task_id;
        this.slajobIds = slajobIds;
        this.predictTime = predictTime;
        this.keyPreJob = keyPreJob;
    }
}
