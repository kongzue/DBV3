package com.kongzue.dbv3.data;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
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
public class DBData extends LinkedHashMap<String, Serializable> {
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    public DBData() {
    
    }
    
    public DBData(Map anyMap) {
        Set<String> keys = anyMap.keySet();
        for (String key : keys) {
            Serializable value = anyMap.get(key) + "";
            put(key, value);
        }
    }
    
    public DBData(String jsonStr) {
        jsonStr = jsonStr.replace(LINE_SEPARATOR, "");
        try {
            if (jsonStr.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                Iterator keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next() + "";
                    String value = jsonObject.optString(key);
                    put(key, value);
                }
            }
        } catch (Exception e) {
        }
    }
    
    public String getString(String key) {
        Object value = get(key);
        return value == null ? "" : value + "";
    }
    
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    public int getInt(String key, int emptyValue) {
        int result = emptyValue;
        try {
            result = Integer.parseInt(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public boolean getBoolean(String key, boolean emptyValue) {
        boolean result = emptyValue;
        try {
            result = Boolean.parseBoolean(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public long getLong(String key) {
        return getLong(key, 0);
    }
    
    public long getLong(String key, long emptyValue) {
        long result = emptyValue;
        try {
            result = Long.parseLong(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }
    
    public short getShort(String key, short emptyValue) {
        short result = emptyValue;
        try {
            result = Short.parseShort(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public double getDouble(String key) {
        return getDouble(key, 0);
    }
    
    public double getDouble(String key, double emptyValue) {
        double result = emptyValue;
        try {
            result = Double.parseDouble(get(key) + "");
        } catch (Exception e) {
        }
        return result;
    }
    
    public float getFloat(String key) {
        return getFloat(key, 0);
    }
    
    public float getFloat(String key, float emptyValue) {
        float result = emptyValue;
        try {
            result = Float.parseFloat(get(key) + "");
        } catch (Exception e) {
        }
        return emptyValue;
    }
    
    public DBData set(String key, Serializable value) {
        put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return getPrintStr();
    }
    
    public String getPrintStr() {
        StringBuffer result = new StringBuffer();
        result.append("{");
        try {
            Set<String> keys = keySet();
            int i = 0;
            for (String key : keys) {
                i++;
                Object value = get(key);
                if (value instanceof DBData) {
                    result.append("\"" + key + "\":" + ((DBData) value).getPrintStr() + ", ");
                } else {
                    String valueStr = String.valueOf(value);
                    if (valueStr.startsWith("[") || valueStr.startsWith("{")) {
                        result.append("\"" + key + "\":" + (value == null ? "" : value));
                    } else {
                        result.append("\"" + key + "\":\"" + (value == null ? "" : value) + "\"");
                    }
                    if (i != keys.size()) {
                        result.append(", ");
                    }
                    
                }
            }
        } catch (Exception e) {
        }
        result.append("}");
        return result.toString();
    }
    
    public String getPrintTable() {
        StringBuffer result = new StringBuffer();
        result.append("[");
        try {
            Set<String> keys = keySet();
            for (String key : keys) {
                result.append(key + ",");
            }
        } catch (Exception e) {
        }
        result.append("]");
        return result.toString();
    }
}
