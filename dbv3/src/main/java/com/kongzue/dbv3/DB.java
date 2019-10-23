package com.kongzue.dbv3;

import android.content.Context;
import android.util.Log;

import com.kongzue.dbv3.data.DBData;
import com.kongzue.dbv3.util.DBHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class DB {
    
    public enum SORT {
        ASC,        //正序
        DESC        //倒序
    }
    
    private String tableName;                   //操作的表的名称
    private List<String> whereConditions;       //自定义where条件队列
    private SORT sort = SORT.ASC;               //排序方式
    private boolean allowDuplicate;             //是否允许添加重复数据
    private long limitCount, limitStart = -1;   //分页条件
    
    private DB() {
    }
    
    public static DB getTable(String tableName) {
        DB db = new DB();
        db.create(tableName);
        return db;
    }
    
    private void create(String tableName) {
        this.tableName = tableName;
    }
    
    /**
     * 初始化方法
     * <p>
     * 日志打印可通过 {@link #setDEBUGMODE(boolean)} 设置是否开启
     *
     * @param context 上下文索引，建议使用您的 Application
     * @param DBName  表名
     */
    public static void init(Context context, String DBName) {
        DBHelper.getInstance().init(context, DBName);
    }
    
    /**
     * 初始化方法
     *
     * @param context  上下文索引，建议使用您的 Application
     * @param DBName   表名
     * @param showLogs 是否开启日志打印
     */
    public static void init(Context context, String DBName, boolean showLogs) {
        DBHelper.DEBUGMODE = showLogs;
        DBHelper.getInstance().init(context, DBName);
    }
    
    /**
     * 添加新的数据
     * <p>
     * 要设置是否允许相同的数据重复，请使用{@link #setAllowDuplicate(boolean)}.
     *
     * @param dbData 要添加的数据
     * @return 继续条件
     */
    public DB add(DBData dbData) {
        add(dbData, allowDuplicate);
        return this;
    }
    
    /**
     * 添加新的数据
     *
     * @param dbData         要添加的数据
     * @param allowDuplicate 是否允许相同的数据重复
     * @return 是否添加成功
     */
    public boolean add(DBData dbData, boolean allowDuplicate) {
        if (!DBHelper.getInstance().isHaveTable(tableName)) {
            boolean createFlag = DBHelper.getInstance().createNewTable(tableName, dbData);
            if (!createFlag) {
                error("严重错误：创建表失败，表模板" + dbData.getPrintTable());
                return false;
            }
        }
        boolean addFlag = DBHelper.getInstance().addData(tableName, dbData, allowDuplicate);
        if (!addFlag) {
            boolean updateFlag = DBHelper.getInstance().updateTable(tableName, dbData);
            if (!updateFlag) {
                error("严重错误：更新表失败，表模板" + dbData.getPrintTable());
                return false;
            }
            if (DBHelper.getInstance().addData(tableName, dbData, allowDuplicate)) {
                log("表 " + tableName + " 添加数据：" + dbData);
            } else {
                error("严重错误：添加数据失败：" + dbData);
                return false;
            }
        }
        log("表 " + tableName + " 添加数据：" + dbData);
        return true;
    }
    
    /**
     * 删除数据
     *
     * @param dbData 要删除的数据，或定义一个查询条件
     * @return 是否删除成功
     */
    public boolean delete(DBData dbData) {
        if (dbData.getInt("_id") == 0) {
            //查询条件删除
            List<DBData> findDataList = find(dbData);
            for (DBData readyDeleteData : findDataList) {
                delete(readyDeleteData);
            }
        } else {
            //直接删除
            return DBHelper.getInstance().delete(tableName, dbData, whereConditions);
        }
        return true;
    }
    
    /**
     * 删除一组数据
     *
     * @param deleteDataList 要删除的数据队列
     * @return 是否删除成功
     */
    public boolean delete(List<DBData> deleteDataList) {
        boolean isDeleteSuccess = true;
        for (DBData readyDeleteData : deleteDataList) {
            if (!delete(readyDeleteData)) {
                isDeleteSuccess = false;
            }
        }
        return isDeleteSuccess;
    }
    
    /**
     * 删除表内所有符合条件的数据
     * <p>
     * 条件：符合 where(...) 方法添加的条件
     * 如果未设置任何条件，则清空整个表
     *
     * @return 是否删除成功
     */
    public boolean delete() {
        return DBHelper.getInstance().delete(tableName, null, whereConditions);
    }
    
    /**
     * 查询该表内符合条件的数据
     *
     * @param findConditions 条件
     * @return 查询结果
     */
    public List<DBData> find(DBData findConditions) {
        return DBHelper.getInstance().findData(tableName, findConditions, sort, whereConditions, limitStart, limitCount);
    }
    
    /**
     * 查询该表内全部数据
     *
     * @return 查询结果
     */
    public List<DBData> find() {
        return DBHelper.getInstance().findData(tableName, null, sort, whereConditions, limitStart, limitCount);
    }
    
    /**
     * 设置分页查询条件
     *
     * @param start 开始位置
     * @param count 数量
     * @return 继续条件
     */
    public DB limit(long start, long count) {
        limitStart = start;
        limitCount = count;
        return this;
    }
    
    /**
     * 设置分页查询条件(强迫症的另一种分页写法)
     *
     * @param start 开始位置
     * @param count 数量
     * @return 继续条件
     */
    public DB subData(long start, long count) {
        return limit(start, count);
    }
    
    /**
     * 清除分页条件
     *
     * @return 继续条件
     */
    public DB cleanLimit() {
        limitStart = 0;
        limitCount = 0;
        return this;
    }
    
    /**
     * 获得表内数据总数量
     *
     * @return 数量
     */
    public long getCount() {
        return DBHelper.getInstance().findDataCount(tableName, null, whereConditions);
    }
    
    /**
     * 修改一个已存在的数据
     *
     * @param dbData 要修改的数据
     * @return 是否修改成功
     */
    public boolean update(DBData dbData) {
        return DBHelper.getInstance().update(tableName, dbData);
    }
    
    /**
     * 修改一个已存在的数据，如果不存在则创建它
     *
     * @param dbData 要修改/创建的数据
     * @return 是否修改/创建成功
     */
    public boolean updateAdd(DBData dbData) {
        if (!DBHelper.getInstance().update(tableName, dbData)) {
            return add(dbData, false);
        }
        return true;
    }
    
    /**
     * 添加 查询条件
     * <p>
     * 例如 参数为 name='test' 时，
     * 则对应查询语句为：Select * from emp where name='test';
     * <p>
     * 请注意无需加空格
     *
     * @param whereSQL 查询语句
     * @return 继续条件
     */
    public DB where(String whereSQL) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(whereSQL);
        return this;
    }
    
    /**
     * 添加 等于 条件
     * <p>
     * 例如 key = age 且 value = 1 时，
     * 则对应查询语句为：Select * from emp where age='1';
     * <p>
     * 请注意无需加空格和引号
     *
     * @param key   键
     * @param value 值
     * @return 继续条件
     */
    public DB whereEqual(String key, Serializable value) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(key + "=\'" + value + "\'");
        return this;
    }
    
    /**
     * 添加 除外 条件
     * <p>
     * 例如 key = age 且 value = 1 时，
     * 则对应查询语句为：Select * from emp where age not '1';
     * <p>
     * 请注意无需加空格和引号
     *
     * @param key   键
     * @param value 值
     * @return 继续条件
     */
    public DB whereNot(String key, Serializable value) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(key + " not \'" + value + "\'");
        return this;
    }
    
    /**
     * 添加 大于 条件
     * <p>
     * 例如 key = age 且 value = 1 时，
     * 则对应查询语句为：Select * from emp where age>'1';
     * <p>
     * 请注意无需加空格和引号
     *
     * @param key   键
     * @param value 值
     * @return 继续条件
     */
    public DB whereGreater(String key, Serializable value) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(key + ">\'" + value + "\'");
        return this;
    }
    
    /**
     * 添加 小于 条件
     * <p>
     * 例如 key = age 且 value = 1 时，
     * 则对应查询语句为：Select * from emp where age<1;
     * <p>
     * 请注意无需加空格和引号
     *
     * @param key   键
     * @param value 值
     * @return 继续条件
     */
    public DB whereLess(String key, Serializable value) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(key + "<\'" + value + "\'");
        return this;
    }
    
    /**
     * 添加 模糊查询 条件
     * <p>
     * 例如 key = ename 且 value = M% 时，
     * 则对应查询语句为：Select * from emp where ename like 'M%';
     * <p>
     * % 表示多个字值，_ 下划线表示一个字符；
     * M% : 为能配符，正则表达式，表示的意思为模糊查询信息为 M 开头的。
     * %M% : 表示查询包含M的所有内容。
     * %M_ : 表示查询以M在倒数第二位的所有内容。
     * <p>
     * 请注意无需加空格和引号
     *
     * @param key   键
     * @param value 值
     * @return 继续条件
     */
    public DB whereLike(String key, Serializable value) {
        if (whereConditions == null) {
            whereConditions = new ArrayList<>();
        }
        this.whereConditions.add(key + "LIKE\'" + value + "\'");
        return this;
    }
    
    /**
     * 清空查询条件
     *
     * @return 继续条件
     */
    public DB cleanWhere() {
        whereConditions = null;
        return this;
    }
    
    public SORT getSort() {
        return sort;
    }
    
    /**
     * 设置排序方式
     *
     * @param sort 正序或倒序
     * @return 继续条件
     */
    public DB setSort(SORT sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isAllowDuplicate() {
        return allowDuplicate;
    }
    
    public boolean getAllowDuplicate() {        //强迫症免疫
        return allowDuplicate;
    }
    
    /**
     * 设置添加时，是否允许重复
     *
     * @param allowDuplicate 允许添加相同的数据
     * @return 继续条件
     */
    public DB setAllowDuplicate(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
        return this;
    }
    
    /**
     * 关闭数据库
     */
    public void closeDB() {
        DBHelper.getInstance().closeDB();
    }
    
    /**
     * 设置是否开启日志
     *
     * @param showLogs 是否打印日志
     * @return 继续条件
     */
    public DB setDEBUGMODE(boolean showLogs) {
        DBHelper.DEBUGMODE = showLogs;
        return this;
    }
    
    /**
     * 是否已开启日志
     *
     * @return 日志模式状态
     */
    public boolean isDEBUGMODE() {
        return DBHelper.DEBUGMODE;
    }
    
    private void log(Object o) {
        if (DBHelper.DEBUGMODE) {
            Log.i("DB>>>", o.toString());
        }
    }
    
    private void error(Object o) {
        if (DBHelper.DEBUGMODE) {
            Log.e("DB>>>", o.toString());
        }
    }
}
