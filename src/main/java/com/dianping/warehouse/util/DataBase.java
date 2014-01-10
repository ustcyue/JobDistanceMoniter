package com.dianping.warehouse.util;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-6
 * Time: 下午1:24
 * To change this template use File | Settings | File Templates.
 */
public class DataBase {
    // 声明connection
    private Connection conn = null;
    // 声明ResultSet
    private ResultSet rs;
    // 声明Statement
    private Statement stmt;
    // Oracle驱动
    private String className = "com.mysql.jdbc.Driver";
    // Oracle连接字符串
    private String url = "jdbc:mysql://127.0.0.1:3306/dpalert?useUnicode=true&characterEncoding=utf-8";
    // 数据库用户名
    private String userName = "root";
    // 数据库密码
    private String password = "yxnabc1988";


    // 声明DBManager的私有对象db
    private static DataBase db;
    private static Statement stat;
    /**
     * 创建私有构造函数
     */
    private DataBase() {
        try {
            // 加载驱动
            Class.forName(className);
            stat = getStmt();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提供一个静态方法
     * @return 返回本类的实例
     */
    public static synchronized DataBase getDataBase() {
        if (db==null) {
            db=new DataBase();
        }
        return db;
    }

    /**
     * 获取连接
     * @return conn
     */
    public Connection getConn() {
        try {
            // 获取连接
            conn = DriverManager.getConnection(url, userName, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 获取Statement记录
     * @return stmt
     */
    public Statement getStmt() {
        try {
            // 获取连接
            conn=getConn();
            // 获取Statement记录
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stmt;
    }


    /**
     * 执行一句查询的sql语句
     * @param sql sqi语句
     * @return 结果集
     */
    public ResultSet executeQuery(String sql) {
        // 利用Statement对象执行参数的sql
        try {
            rs = getStmt().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * 执行单句INSERT、UPDATE 或 DELETE 语句
     * @param sql sql语句
     */
    public void executeUpdate(String sql) {
        //执行SQL语句
        try {
            getStmt().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     */
    public void closed() {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null)
                stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

