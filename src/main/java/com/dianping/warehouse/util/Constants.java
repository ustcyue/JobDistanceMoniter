package com.dianping.warehouse.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-8
 * Time: 下午1:42
 * To change this template use File | Settings | File Templates.
 */
public class Constants {
    public static  String endTime = "13:00:00";
    public static final int RUNNING = 2;
    public static final int Fail = -1;
    public static final String datepattern = "yyyy-MM-dd HH:mm:ss";
    public static Double defaultCost = 100.0;
    public static Integer defaultRepairTime = 3600;
    public static Double defaultDelayPercent = 0.15;
    public static final int DELAY = -1;
    public static int defaultInterval = 60*1000;
    public static String jarpath;
    static{
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File("/data/deploy/dp/dpalert/alertParameters.xml");
        try {
            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            defaultCost = Double.valueOf(rootNode.getChildText("defaultCost"));
            defaultInterval = Integer.valueOf(rootNode.getChildText("defaultInterval"));
            defaultRepairTime = Integer.valueOf(rootNode.getChildText("defaultRepairTime"));
            defaultDelayPercent = Double.valueOf(rootNode.getChildText("defaultDelayPercent"));
            jarpath = rootNode.getChildText("jarpath");
            endTime = rootNode.getChildText("endTime");
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
    }

}
