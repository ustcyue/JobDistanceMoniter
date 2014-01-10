package com.dianping.warehouse.main;

import com.dianping.warehouse.util.DateUtils;
import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-10
 * Time: 下午2:04
 * To change this template use File | Settings | File Templates.
 */
public class SlaJob {
    private int sla_id;

    private String sla_name;

    private DateTime warn_begin_time;

    private DateTime warn_time;

    private int warnType;

    public SlaJob(int sla_id, String sla_name, String warn_begin_time, String warn_time, int warnType, String processDay) {
        this.sla_id = sla_id;
        this.sla_name = sla_name;
        setWarn_begin_time(processDay, warn_begin_time);
        setWarn_time(processDay,warn_time);
        this.warnType = warnType;
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
}
