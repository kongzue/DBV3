package com.kongzue.dbv3demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kongzue.dbv3.DB;
import com.kongzue.dbv3.data.DBData;
import com.kongzue.dbv3.util.DBHelper;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    
    private CheckBox chkAllowDuplicate;
    private Button btnAddDemoData;
    private EditText editName;
    private EditText editAge;
    private RadioGroup rgpGender;
    private RadioButton genderMale;
    private RadioButton genderFemale;
    private CheckBox chkVip;
    private Button btnRandom;
    private Button btnAddCustomData;
    private Spinner spinnerWhereKey;
    private Spinner spinnerWhereCondition;
    private EditText editWhereValue;
    private RadioGroup rgpWhereGender;
    private RadioButton genderWhereMale;
    private RadioButton genderWhereFemale;
    private CheckBox chkWhereVip;
    private Button btnFindData;
    private Button btnDeleteData;
    private Button btnAddDataSpecial;
    private Button btnUpdate;
    private TextView logs;
    
    private int selectWhereKey;
    private String condition;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        chkAllowDuplicate = findViewById(R.id.chk_allowDuplicate);
        btnAddDemoData = findViewById(R.id.btn_add_demo_data);
        editName = findViewById(R.id.edit_name);
        editAge = findViewById(R.id.edit_age);
        rgpGender = findViewById(R.id.rgp_gender);
        genderMale = findViewById(R.id.gender_male);
        genderFemale = findViewById(R.id.gender_female);
        chkVip = findViewById(R.id.chk_vip);
        btnRandom = findViewById(R.id.btn_random);
        btnAddCustomData = findViewById(R.id.btn_add_custom_data);
        spinnerWhereKey = findViewById(R.id.spinner_where_key);
        spinnerWhereCondition = findViewById(R.id.spinner_where_condition);
        editWhereValue = findViewById(R.id.edit_where_value);
        rgpWhereGender = findViewById(R.id.rgp_where_gender);
        genderWhereMale = findViewById(R.id.gender_where_male);
        genderWhereFemale = findViewById(R.id.gender_where_female);
        chkWhereVip = findViewById(R.id.chk_where_vip);
        btnFindData = findViewById(R.id.btn_find_data);
        btnDeleteData = findViewById(R.id.btn_delete_data);
        btnAddDataSpecial = findViewById(R.id.btn_add_data_special);
        btnUpdate = findViewById(R.id.btn_update);
        logs = findViewById(R.id.logs);
        
        String[] arr = {"姓名", "年龄", "性别", "VIP"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        spinnerWhereKey.setAdapter(adapter);
        spinnerWhereKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectWhereKey = position;
                final String[] arr;
                ArrayAdapter<String> adapter;
                switch (position) {
                    case 0:
                        arr = new String[]{"="};
                        condition = arr[0];
                        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arr);
                        spinnerWhereCondition.setAdapter(adapter);
                        editWhereValue.setText("");
                        editWhereValue.setInputType(InputType.TYPE_CLASS_TEXT);
                        editWhereValue.setVisibility(View.VISIBLE);
                        rgpWhereGender.setVisibility(View.GONE);
                        chkWhereVip.setVisibility(View.GONE);
                        break;
                    case 1:
                        arr = new String[]{"=", ">", "<"};
                        condition = arr[0];
                        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arr);
                        spinnerWhereCondition.setAdapter(adapter);
                        spinnerWhereCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                condition = arr[position];
                            }
                            
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            
                            }
                        });
                        editWhereValue.setText("20");
                        editWhereValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        editWhereValue.setVisibility(View.VISIBLE);
                        rgpWhereGender.setVisibility(View.GONE);
                        chkWhereVip.setVisibility(View.GONE);
                        break;
                    case 2:
                        arr = new String[]{"="};
                        condition = arr[0];
                        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arr);
                        spinnerWhereCondition.setAdapter(adapter);
                        editWhereValue.setVisibility(View.GONE);
                        rgpWhereGender.setVisibility(View.VISIBLE);
                        chkWhereVip.setVisibility(View.GONE);
                        break;
                    case 3:
                        arr = new String[]{"="};
                        condition = arr[0];
                        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arr);
                        spinnerWhereCondition.setAdapter(adapter);
                        editWhereValue.setVisibility(View.GONE);
                        editWhereValue.setVisibility(View.GONE);
                        rgpWhereGender.setVisibility(View.GONE);
                        chkWhereVip.setVisibility(View.VISIBLE);
                        break;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            
            }
        });
        
        //初始化
        DB.init(this, "testDB", true);
        
        //打印现有的数据
        logAllData();
        
        //添加固定测试数据
        btnAddDemoData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDemoData();
            }
        });
        
        //添加自定义数据
        btnAddCustomData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNull(editName.getText().toString().trim())) {
                    toast("请输入姓名");
                    return;
                }
                if (isNull(editAge.getText().toString().trim())) {
                    toast("请输入年龄");
                    return;
                }
                boolean addFlag = DB.getTable("user").add(
                        new DBData()
                                .set("username", editName.getText().toString())
                                .set("age", editAge.getText().toString())
                                .set("gender", genderMale.isChecked() ? "0" : "1")
                                .set("isVIP", chkVip.isChecked()),
                        chkAllowDuplicate.isChecked()
                );
                toast("操作执行：" + (addFlag ? "执行成功" : "执行失败"));
                logAllData();
            }
        });
        
        //随机生成一个数据
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int gender = getRandom(0, 2);
                editName.setText(NameUtil.getRandomName(gender));
                editAge.setText(getRandom(15, 30) + "");
                if (gender == 0) {
                    rgpGender.check(R.id.gender_male);
                } else {
                    rgpGender.check(R.id.gender_female);
                }
                chkVip.setChecked(getRandom(0, 2) == 1 ? true : false);
            }
        });
        
        //根据条件查询
        btnFindData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DBData> result = DB.getTable("user")
                        .where(getCondition())
                        .find();
                
                StringBuffer stringBuffer = new StringBuffer();
                for (DBData dbData : result) {
                    stringBuffer.append(dbData);
                    stringBuffer.append("\n");
                }
                logs.setText(stringBuffer.toString());
            }
        });
        
        //根据条件删除
        btnDeleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean deleteFlag = DB.getTable("user")
                        .where(getCondition())
                        .delete();
                
                logAllData();
                toast("删除：" + (deleteFlag ? "执行成功" : "执行失败"));
            }
        });
        
        //新增有额外列的数据
        btnAddDataSpecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB.getTable("user")
                        .setAllowDuplicate(false)
                        .add(new DBData().set("username", "我多了一列").set("age", 27).set("gender", 0).set("isVIP", true).set("special","Surprise!"));
                logAllData();
            }
        });
        
        //修改所有人都是VIP
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DBData> result = DB.getTable("user")
                        .find();
    
                for (DBData dbData : result) {
                    DB.getTable("user").update(dbData.set("isVIP", true));
                }
                
                logAllData();
            }
        });
    }
    
    private void addDemoData() {
        DB.getTable("user")
                .setAllowDuplicate(chkAllowDuplicate.isChecked())
                .add(new DBData().set("username", "张三").set("age", 13).set("gender", 0).set("isVIP", true))
                .add(new DBData().set("username", "李四").set("age", 13).set("gender", 0).set("isVIP", false))
                .add(new DBData().set("username", "王五").set("age", 13).set("gender", 1).set("isVIP", false))
                .add(new DBData().set("username", "赵六").set("age", 13).set("gender", 1).set("isVIP", false))
                .add(new DBData().set("username", "测试员").set("age", 14).set("gender", 0).set("isVIP", true))
                .add(new DBData().set("username", "王五2").set("age", 15).set("gender", 0).set("isVIP", true))
                .add(new DBData().set("username", "赵六2").set("age", 14).set("gender", 1).set("isVIP", true))
                .add(new DBData().set("username", "测试员2").set("age", 16).set("gender", 0).set("isVIP", true))
        ;
        toast("批量操作已完成");
        logAllData();
    }
    
    private String getCondition() {
        StringBuffer finder = new StringBuffer();
        switch (selectWhereKey) {
            case 0:
                finder.append("username");
                finder.append(condition);
                finder.append("\'" + editWhereValue.getText().toString().trim() + "\'");
                break;
            case 1:
                finder.append("age");
                finder.append(condition);
                finder.append("\'" + editWhereValue.getText().toString().trim() + "\'");
                break;
            case 2:
                finder.append("gender");
                finder.append(condition);
                finder.append("\'" + (genderWhereMale.isChecked() ? "0" : "1") + "\'");
                break;
            case 3:
                finder.append("isVIP");
                finder.append(condition);
                finder.append("\'" + (chkWhereVip.isChecked() ? "true" : "false") + "\'");
                break;
        }
        return finder.toString();
    }
    
    private void logAllData() {
        List<DBData> result = DB.getTable("user")
                .find();
        
        StringBuffer stringBuffer = new StringBuffer();
        for (DBData dbData : result) {
            stringBuffer.append(dbData);
            stringBuffer.append("\n");
        }
        logs.setText(stringBuffer.toString());
    }
    
    private void toast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }
    
    private void log(Object o) {
        if (DBHelper.DEBUGMODE) {
            Log.i("DB>>>", o.toString());
        }
    }
    
    public int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }
    
    private boolean isNull(String s) {
        if (s == null || s.trim().isEmpty() || "null".equals(s)) {
            return true;
        }
        return false;
    }
}
