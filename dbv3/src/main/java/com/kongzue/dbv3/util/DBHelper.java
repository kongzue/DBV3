package com.kongzue.dbv3.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kongzue.dbv3.DB;
import com.kongzue.dbv3.data.DBData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Kongzue 数据库工具 V3
 * 本工具库受 Apache License 2.0 协议保护
 *
 * @author: Kongzue
 * @github: https://github.com/kongzue/
 * @homepage: http://kongzue.com/
 * @mail: myzcxhh@live.cn
 * @createTime: 2019/10/22 16:38
 */
public class DBHelper {
    
    public static boolean DEBUGMODE = false;
    private String dbName = "";
    
    private DBHelper() {
    }
    
    private static DBHelper helper;
    private SQLiteDatabase db;
    private Context context;
    
    public void init(Context context, String dbName) {
        helper.context = context;
        helper.dbName = dbName;
        
        try {
            SQLiteOpenHelper sqLiteOpenHelper = new SQLiteHelperImpl(context, dbName, oldVer);
            helper.db = sqLiteOpenHelper.getWritableDatabase();
            if (db == null) {
                error("初始化数据库失败");
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
        }
    }
    
    private int oldVer = 1;
    
    public void init(Context context, String dbName, int oldVer) {
        helper.context = context;
        helper.dbName = dbName;
        helper.oldVer=oldVer;
        
        try {
            SQLiteOpenHelper sqLiteOpenHelper = new SQLiteHelperImpl(context, dbName, oldVer);
            helper.db = sqLiteOpenHelper.getWritableDatabase();
            if (db == null) {
                error("初始化数据库失败");
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
        }
    }
    
    public void init(Context context, File dbFile) {
        helper.context = context;
        helper.dbName = dbName;
        
        try {
            helper.db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            if (db == null) {
                error("初始化数据库失败");
            }
        } catch (Exception e) {
            if (DEBUGMODE) {
                e.printStackTrace();
            }
        }
    }
    
    public static DBHelper getInstance() {
        synchronized (DBHelper.class) {
            if (helper == null) {
                helper = new DBHelper();
            }
            return helper;
        }
    }
    
    public SQLiteDatabase getDb() {
        return db;
    }
    
    public boolean isHaveTable(String tableName) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return false;
            }
            db.beginTransaction();
            Cursor c = null;
            try {
                c = db.rawQuery("select name from sqlite_master where type='table';", null);
                while (c.moveToNext()) {
                    if (c.getString(0).equals(tableName)) {
                        c.close();
                        db.setTransactionSuccessful();  //设置事务成功完成
                        return true;
                    }
                }
                db.setTransactionSuccessful();  //设置事务成功完成
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            } finally {
                if (c != null) {
                    c.close();
                }
                db.endTransaction();    //结束事务
            }
            return false;
        }
    }
    
    public boolean addData(String tableName, DBData data, boolean allowDuplicate) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return false;
            }
            if (!allowDuplicate) {
                if (DBHelper.getInstance().findDataCount(tableName, data, null) != 0) {
                    error("重复数据：" + data);
                    return true;
                }
            }
            db.beginTransaction();
            try {
                String sql = "INSERT INTO " + tableName + " (";
                Set<String> set = data.keySet();
                for (String key : set) {
                    if (!"_id".equals(key)) {
                        sql = sql + "\'" + key + "\'" + " ,";
                    }
                }
                if (sql.endsWith(",")) {
                    sql = sql.substring(0, sql.length() - 1);
                }
                sql = sql + ") VALUES (";
                for (String key : set) {
                    if (!"_id".equals(key)) {
                        String value = data.get(key).toString();
                        sql = sql + "\'" + value + "\'" + " ,";
                    }
                }
                if (sql.endsWith(",")) {
                    sql = sql.substring(0, sql.length() - 1);
                }
                sql = sql + ")";
                log("SQL.exec: " + sql);
                db.execSQL(sql);
                db.setTransactionSuccessful();  //设置事务成功完成
                return true;
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            } finally {
                db.endTransaction();    //结束事务
            }
        }
    }
    
    //创建表
    public boolean createNewTable(String tableName, DBData dbData) {
        synchronized (DBHelper.class) {
            if (isHaveTable(tableName)) {
                return true;
            }
            StringBuffer newTableSQLCommandBuffer = new StringBuffer("CREATE TABLE IF NOT EXISTS " + tableName + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
            Set<String> set = dbData.keySet();
            for (String key : set) {
                if (!"_id".equals(key)) {
                    newTableSQLCommandBuffer.append(" " + key + " TEXT,");
                }
            }
            String newTableSQLCommand = newTableSQLCommandBuffer.toString();
            if (newTableSQLCommand.endsWith(",")) {
                newTableSQLCommand = newTableSQLCommand.substring(0, newTableSQLCommand.length() - 1);
            }
            newTableSQLCommand = newTableSQLCommand + ")";
            log("SQL.exec: " + newTableSQLCommand);
            try {
                if (db != null) {
                    db.execSQL(newTableSQLCommand);
                }
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            }
            restartDB();
            return true;
        }
    }
    
    //更新表
    public boolean updateTable(String tableName, DBData dbData) {
        synchronized (DBHelper.class) {
            String updateTableSQLCommand = "ALTER TABLE " + tableName + " ADD ";
            List<String> newKeys = new ArrayList<>();
            List<String> oldKeys = getTableAllKeys(tableName);
            for (String key : dbData.keySet()) {
                if (!"_id".equals(key)) {
                    if (!oldKeys.contains(key)) {
                        newKeys.add(key);
                    }
                }
            }
            for (String key : newKeys) {
                updateTableSQLCommand = updateTableSQLCommand + " " + key + " VARCHAR,";
            }
            if (updateTableSQLCommand.endsWith(",")) {
                updateTableSQLCommand = updateTableSQLCommand.substring(0, updateTableSQLCommand.length() - 1);
            }
            log("SQL.exec: " + updateTableSQLCommand);
            try {
                if (db != null) {
                    db.execSQL(updateTableSQLCommand);
                }
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            }
            restartDB();
            return true;
        }
    }
    
    //获取一个表内的所有字段名
    private List<String> getTableAllKeys(String tableName) {
        synchronized (DBHelper.class) {
            List<String> result = new ArrayList<>();
            Cursor c = null;
            try {
                c = db.rawQuery("SELECT * FROM " + tableName + " WHERE 0", null);
                String[] columnNames = c.getColumnNames();
                for (String s : columnNames) {
                    result.add(s);
                }
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return result;
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return result;
        }
    }
    
    //根据查询条件dbData获取一个表内的所有数据
    public List<DBData> findData(String tableName, DBData findConditions, DB.SORT sort, List<String> whereConditions, long start, long count) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return new ArrayList<>();
            }
            List<DBData> result = new ArrayList<>();
            StringBuffer sql = new StringBuffer("SELECT * FROM " + tableName);
            if (findConditions != null || whereConditions != null) {
                sql.append(" where ");
                if (findConditions != null) {
                    Set<String> set = findConditions.keySet();
                    for (String key : set) {
                        String value = findConditions.get(key).toString();
                        sql.append(" " + key + " = \'" + value + "\' AND");
                    }
                }
                if (whereConditions != null) {
                    for (String condition : whereConditions) {
                        condition = condition.trim();
                        sql.append(" " + condition + " AND");
                    }
                }
                if (sql.toString().endsWith("AND")) {
                    sql = new StringBuffer(sql.substring(0, sql.length() - 3));
                }
            }
            if (sql.toString().endsWith("AND")) {
                sql = new StringBuffer(sql.substring(0, sql.length() - 3));
            }
            if (sort != null) {
                sql.append(" ORDER BY _id ");
                sql.append(sort.name());
            }
            if (start != -1 && count != 0) {
                sql.append(" limit ");
                sql.append(start);
                sql.append(",");
                sql.append(count);
            }
            log("SQL.exec: " + sql.toString());
            Cursor c = null;
            try {
                c = db.rawQuery(sql.toString(), null);
                while (c.moveToNext()) {
                    DBData data = new DBData();
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        String key = c.getColumnName(i);
                        data.set(key, c.getString(c.getColumnIndex(key)));
                    }
                    result.add(data);
                }
                c.close();
            } catch (Exception e) {
                error("查询错误：" + sql.toString());
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return result;
        }
    }
    
    //根据查询条件dbData获取一个表内的所有符合条件数据的数量
    public long findDataCount(String tableName, DBData findData, List<String> whereConditions) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return 0;
            }
            StringBuffer sql = new StringBuffer("SELECT * FROM " + tableName);
            if (findData != null || whereConditions != null) {
                sql.append(" where ");
                if (findData != null) {
                    Set<String> set = findData.keySet();
                    for (String key : set) {
                        String value = findData.get(key).toString();
                        sql.append(" " + key + " = \'" + value + "\' AND");
                    }
                }
                if (whereConditions != null) {
                    for (String condition : whereConditions) {
                        condition = condition.trim();
                        sql.append(" " + condition + " AND");
                    }
                }
                if (sql.toString().endsWith("AND")) {
                    sql = new StringBuffer(sql.substring(0, sql.length() - 3));
                }
            }
            log("SQL.exec: " + sql.toString());
            long count = 0;
            Cursor c = null;
            try {
                c = db.rawQuery(sql.toString(), null);
                c.moveToFirst();
                count = c.getLong(0);
            } catch (Exception e) {
                //此错误忽略不计的原因是为此方法多用于添加前校验是否存在，肯定不存在的情况下这里必然抛异常，此异常完全可以忽略
//            if (DEBUGMODE) {
//                e.printStackTrace();
//            }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return count;
        }
    }
    
    public long rawCount(String sql) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return 0;
            }
            long result = 0;
            Cursor c = null;
            try {
                c = db.rawQuery(sql, null);
                c.moveToFirst();
                result = c.getLong(0);
            } catch (Exception e) {
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return result;
        }
    }
    
    public boolean delete(String tableName, DBData dbData, List<String> whereConditions) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return false;
            }
            if (dbData != null) {
                if (dbData.getInt("_id") == 0) {
                    error("只能对已存在的数据（使用find查询出来的数据）进行删除");
                    return false;
                }
            }
            db.beginTransaction();
            try {
                StringBuffer sql = new StringBuffer("delete from ");
                sql.append(tableName);
                if (dbData != null || whereConditions != null) {
                    sql.append(" where ");
                    if (dbData != null) {
                        sql.append("_id=\'" + dbData.getInt("_id") + "\' AND");
                    }
                    if (whereConditions != null) {
                        for (String condition : whereConditions) {
                            condition = condition.trim();
                            sql.append(" " + condition + " AND");
                        }
                    }
                    if (sql.toString().endsWith("AND")) {
                        sql = new StringBuffer(sql.substring(0, sql.length() - 3));
                    }
                } else {
                    db.execSQL("update sqlite_sequence set seq=0 where name='" + tableName + "';");          //将自增键初始化为0
                }
                log("SQL.exec: " + sql.toString());
                db.execSQL(sql.toString());
                db.setTransactionSuccessful();  //设置事务成功完成
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            } finally {
                db.endTransaction();    //结束事务
            }
            return true;
        }
    }
    
    public boolean update(String tableName, DBData dbData) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return false;
            }
            if (dbData.getInt("_id") == 0) {
                error("只能对已存在的数据（使用find查询出来的数据）进行修改");
                return false;
            }
            db.beginTransaction();
            try {
                String sql = "update " + tableName + " set ";
                Set<String> set = dbData.keySet();
                for (String key : set) {
                    if (!"_id".equals(key)) {
                        String value = dbData.getString(key);
                        sql = sql + " " + key + " = \'" + value + "\' ,";
                    }
                }
                if (sql.endsWith(",")) {
                    sql = sql.substring(0, sql.length() - 1);
                }
                sql = sql + " where _id=\"" + dbData.getInt("_id") + "\"";
                log("SQL.exec: " + sql);
                db.execSQL(sql);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            } finally {
                db.endTransaction();
            }
            return true;
        }
    }
    
    public boolean deleteTable(String tableName) {
        synchronized (DBHelper.class) {
            if (db == null) {
                error("警告：数据库未初始化");
                return false;
            }
            if (!isHaveTable(tableName)) {
                log("不存在要删除的表：" + tableName);
                return true;
            }
            db.beginTransaction();
            try {
                String sql = "drop table " + tableName;
                log("SQL.exec: " + sql);
                db.execSQL(sql);
                db.execSQL("update sqlite_sequence set seq=0 where name='" + tableName + "';");          //将自增键初始化为0
                db.setTransactionSuccessful();
            } catch (Exception e) {
                if (DEBUGMODE) {
                    e.printStackTrace();
                }
                return false;
            } finally {
                db.endTransaction();
            }
            return true;
        }
    }
    
    private class SQLiteHelperImpl extends SQLiteOpenHelper {
        
        public SQLiteHelperImpl(Context context, String dbName, int dbVersion) {
            //CursorFactory设置为null,使用默认值
            super(context, dbName + ".db", null, dbVersion == 0 ? dbVersion + 1 : dbVersion);
        }
        
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //if (!isNull(createTableSQLCommand)) {
            //    sqLiteDatabase.execSQL(createTableSQLCommand);
            //    createTableSQLCommand = null;
            //}
        }
        
        //数据库升级用
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            //if (oldVersion != dbVersion) {
            //    if (!isNull(updateTableSQLCommand)) {
            //        sqLiteDatabase.execSQL(updateTableSQLCommand);
            //        updateTableSQLCommand = null;
            //    }
            //    if (!isNull(newTableSQLCommand)) {
            //        sqLiteDatabase.execSQL(newTableSQLCommand);
            //        newTableSQLCommand = null;
            //    }
            //}
        }
    }
    
    public void closeDB() {
        synchronized (DBHelper.class) {
            if (db != null) {
                db.close();
            }
            context = null;
            db = null;
        }
    }
    
    public void restartDB() {
        log("# restartDB");
        synchronized (DBHelper.class) {
            if (db != null) {
                db.close();
            }
            db = null;
            SQLiteOpenHelper sqLiteOpenHelper = new SQLiteHelperImpl(context, dbName, oldVer);
            db = sqLiteOpenHelper.getWritableDatabase();
        }
    }
    
    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s)) {
            return true;
        }
        return false;
    }
    
    private void log(Object o) {
        if (DEBUGMODE) {
            Log.i("DB>>>", o.toString());
        }
    }
    
    private void error(Object o) {
        if (DEBUGMODE) {
            Log.e("DB>>>", o.toString());
        }
    }
    
}
