package com.dianping.warehouse.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: yxn
 * Date: 14-1-7
 * Time: 下午9:39
 * To change this template use File | Settings | File Templates.
 */
public class ReadFile {
    private String path;
    private FileReader reader;
    private BufferedReader br;
    public ReadFile(String filePath) throws FileNotFoundException {
        this.path = filePath;
        reader = new FileReader(this.path);
        br = new BufferedReader(reader);
    }
    public String readLine() throws IOException {
        return br.readLine();
    }
    public void close() throws IOException {
        br.close();
        reader.close();
    }
}
