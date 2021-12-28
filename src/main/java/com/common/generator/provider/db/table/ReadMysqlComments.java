package com.common.generator.provider.db.table;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 读取mysql的注释
 * @author mark
 * @date 2015年3月14日下午2:59:47
 */
public class ReadMysqlComments {
		public static String tableName="";
		public static String tableComment="";
        public static Connection getMySQLConnection() throws Exception {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://200.200.200.50:3306/db_cms","root","123456");
                return conn;
        }
        
        public static Map getTableCommentByTableName(List tableName) throws Exception {
        	Map<String,String> map = new HashMap<String,String>();
                Connection conn = getMySQLConnection();
                Statement stmt = conn.createStatement();
                for(int i = 0; i < tableName.size(); i++) {
                        String table = (String)tableName.get(i);
                        ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table);
                        if(rs != null && rs.next()) {
                                String create = rs.getString(2);
                                String comment = parse(create);
                                map.put(table,comment);
                        }
                        rs.close();
                }
                stmt.close();
                conn.close();
                return map;
        }
        
        public static String getColumnCommentByTableName(String tableName) throws Exception {
            String comment = "";
            String create  = "";
        	Connection conn = getMySQLConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
            if(rs != null && rs.next()) {
              create = rs.getString(2);
              comment = parse(create);
            }
            tableComment = comment;
            rs.close();
            stmt.close();
            conn.close();
            return create;
    }
        
        public static String getTableCommentByTableName(String tableName) throws Exception {
            String comment = "";
            String create  = "";
        	Connection conn = getMySQLConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
            if(rs != null && rs.next()) {
              create = rs.getString(2);
              comment = parse(create);
            }
            tableComment = comment;
            rs.close();
            stmt.close();
            conn.close();
            return comment;
    }
        
        public static List getAllTableName() throws Exception{
                List tables = new ArrayList();
                Connection conn = getMySQLConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES ");
                while(rs.next()) {
                        String tableName = rs.getString(1);
                        tables.add(tableName);
                }
                rs.close();
                stmt.close();
                conn.close();
                return tables;
        }
        
        public static String parse(String all) throws Exception {
                String comment = null;
                int index = all.indexOf("COMMENT='");
                if(index < 0) {
                        return "";
                }
                comment = all.substring(index+9);
                comment = comment.substring(0,comment.length() - 1);
              //设置表注释
            	tableComment = comment;
                return comment;
        }
        /**
         * 解析某一列的注释信息
         * @param all
         * @return
         */
        public static Map<String,String> parseColumn(String all) throws Exception {
        	Map<String,String> map = new HashMap<String,String>();
        	//先去掉首尾不相关字符
        	int headIndex = all.indexOf("(");
        	String newString  = all.substring(headIndex);
        	
        	newString = newString.substring(1,newString.lastIndexOf(")"));
        	System.out.println("new:"+newString);
        	//再以逗号隔开
        	String[] comments  = newString.split(",");
        	String child = "";
        	String key,value = "";
        	//去掉最后一条
            for(int i= 0;i<comments.length-1;i++){
            	value = "";
            	child = comments[i];
            	key  = child.substring(child.indexOf("`")+1,child.lastIndexOf("`"));
            	int index = child.indexOf("'");
            	if(index>0){
            		value  = child.substring(child.indexOf("'")+1,child.lastIndexOf("'"));
            	}
            	System.out.println(key+"|"+value);
            	map.put(key, value);
            }
            return map;
        }
        
        public static void main(String[] args) throws Exception{
        		//获取某个表的所有列注释
                System.out.println("解析结果如下：");
                parseColumn(getColumnCommentByTableName("tb_topic"));
                System.out.println(getTableCommentByTableName("tb_topic"));
                /*
                 * 获取表注释
                List tables = getAllTableName();
 				Map tablesComment = getCommentByTableName(tables);
 				Set names = tablesComment.keySet();
  				Iterator iter = names.iterator();
                while(iter.hasNext()) {
                        String name = (String)iter.next();
                        System.out.println("Table Name: " + name + ", Comment: " + tablesComment.get(name));
                }
                */
        }

}
