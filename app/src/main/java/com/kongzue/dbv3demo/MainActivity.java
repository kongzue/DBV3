package com.kongzue.dbv3demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.kongzue.dbv3.DB;
import com.kongzue.dbv3.data.DBData;
import com.kongzue.dbv3.util.DBHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        DB.init(this, "testDB", true);
        
        log("开始添加测试————————————————————————————————————————");
        
        DB.getTable("user")
                .add(new DBData().set("username", "张三").set("age", 13).set("gender", 0).set("isVIP", true).set("imageUrl", "http://www.example.com/a.jpg"))
                .add(new DBData().set("username", "李四").set("age", 13).set("gender", 0).set("isVIP", false).set("imageUrl", "http://www.example.com/b.jpg"))
                .add(new DBData().set("username", "王五").set("age", 13).set("gender", 1).set("isVIP", false).set("imageUrl", "http://www.example.com/c.jpg"))
                .add(new DBData().set("username", "赵六").set("age", 13).set("gender", 1).set("isVIP", false).set("imageUrl", "http://www.example.com/d.jpg"))
                .add(new DBData().set("username", "测试员").set("age", 14).set("gender", 0).set("isVIP", true).set("imageUrl", "http://www.example.com/e.jpg"))
                .add(new DBData().set("username", "王五2").set("age", 15).set("gender", 0).set("isVIP", true).set("imageUrl", "http://www.example.com/f.jpg"))
                .add(new DBData().set("username", "赵六2").set("age", 14).set("gender", 1).set("isVIP", true).set("imageUrl", "http://www.example.com/g.jpg"))
                .add(new DBData().set("username", "测试员2").set("age", 16).set("gender", 0).set("isVIP", true).set("imageUrl", "http://www.example.com/h.jpg"))
        ;
        
        log("开始查询测试————————————————————————————————————————");
        
        List<DBData> result = DB.getTable("uaser")
                .whereEqual("gender",0)
                .whereGreater("age", 13)
                .setSort(DB.SORT.DESC)
                .find();
        
        for (DBData dbData : result) {
            log(dbData);
        }
    }
    
    private void log(Object o) {
        if (DBHelper.DEBUGMODE) {
            Log.i("DB>>>", o.toString());
        }
    }
}
