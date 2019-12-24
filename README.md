# KongzueDB V3
SQLite的封装，适合轻量使用数据库的场景，半自动化快速创建表结构

<a href="https://github.com/kongzue/DBV3/">
<img src="https://img.shields.io/badge/Kongzue%20DBV3-3.0.1-green.svg" alt="Kongzue DB">
</a>
<a href="https://bintray.com/myzchh/maven/DBV3/3.0.1/link">
<img src="https://img.shields.io/badge/Maven-3.0.1-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>

Demo下载地址：
https://fir.im/DBV3

Demo预览图如下：
![KongzueDBV3](https://github.com/kongzue/DBV3/raw/master/.github/dbv3demo.png)

## 使用前的约定与须知
- KongzueDB 是对 Android 中 SQLite 的封装，因简化存储方式以及采用自生成 SQL 语句的方式，仅可用于轻量需求的场景，如果需要多表联合查询等重度场景，以及对数据库中存储的数据类型有严格限定要求的项目，请勿使用本框架；

- KongzueDB 框架使用 DBData 对象进行数据库数据操作，其结构和使用方式与 Map 一致，可将查询的数据直接与 Adapter 做对接。

- KongzueDB 提供与 json 数据的完美对接，可输出为 json，亦可从 json 直接创建数据库数据。

- 本框架目的是解决 SQLite 上手难度的问题，对其增删改查进行了进一步的封装，使其更好用更易用，流程更轻松，但本质上依然是在数据库中进行操作，请注意至少具备数据库、表、项，以及数据库的基本常识的情况下进行使用；

- 本框架会对插入的数据中额外的插入一个名为“_id”的自增键，请勿以任何方式对其进行修改。

- 在初始化后首次使用，本框架会根据您添加的第一个 DBData 中的 keyArray 作为“列”来创建数据表，如果之后添加的 DBData 中添加了新的 key，数据表将**自动更新**。

- 在本框架中，您只需要关注 DB 这一个类即可，对于 DBHelper，不建议直接操作其中的方法。

## Maven仓库或Gradle的引用方式
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.db</groupId>
  <artifactId>dbv3</artifactId>
  <version>3.0.1</version>
  <type>pom</type>
</dependency>
```
Gradle：
在dependencies{}中添加引用：
```
implementation 'com.kongzue.db:dbv3:3.0.1'
```

## 使用方法

### 初始化

使用以下代码初始化数据库：
```
DB.init(this, "testDB");  
//或
DB.init(this, "testDB", true);  
```
对应的三个参数分别为 context、数据库名称和是否在 Logcat 打印日志。

第一个参数建议使用您的 Application，第二个参数请根据需要设置数据库名称，第三个参数可传可不传，不传则后续可通过 `setDEBUGMODE(boolean)` 方法来设置。

### 增

要添加一个数据，例如：
```
DB.getTable("user")                     //指定要操作的表
    .add(                               //添加
        new DBData()                    //设置要操作的数据
            .set("username", "张三")
            .set("age", 13)
            .set("gender", 0)
            .set("isVIP", true)
    );
```
即可完成一个数据的添加。

默认情况下，add 方法会返回 DB 的实例化对象，您可以通过不断连续执行 `.add(DBData)` 来一次性添加多个数据。

使用 `.add(DBData)` 方法添加数据时默认是不允许重复的，即添加的数据 DBData 如果内容已经在数据表中重复，则会忽略，要设置允许重复，可以按照如下方法编写：
```
DB.getTable("user")      
    .setAllowDuplicate(true)       //允许添加重复数据          
    .add(                              
        new DBData()                   
            .set("username", "张三")
            .set("age", 13)
            .set("gender", 0)
            .set("isVIP", true)
    );
```
也可通过方法 `.add(DBData, boolean)` 指定单次添加是都允许重复：
```
boolean addFlag = DB.getTable("user")       //addFlag 代表本次添加命令是否执行成功
    .add(                               
        new DBData()                    
            .set("username", "张三")
            .set("age", 13)
            .set("gender", 0)
            .set("isVIP", true),
        true                                //本次添加允许添加重复数据   
    );
```
 
另外，使用以下方法 `.add(DBData, boolean)` 来添加数据则会返回 boolean 类型的返回值，用于判断添加命令是否**执行成功**，注意这并不代表本次是否添加成功，例如，对于已经指定不允许重复的情况下添加了重复的数据，此情况属于命令执行成功但实际未添加，但对于数据库已关闭后执行添加操作，则会返回 false 代表命令执行失败。

### 查
通过以下指令查询全表数据，返回 List<DBData> 集合：
```
List<DBData> result = DB.getTable("user").find();
```

要添加查询条件，可通过 `.where` 开头的方法来进行：
```
List<DBData> result = DB.getTable("user")
        .whereEqual("name", "张三")               //添加查询条件，查询 name=张三 的结果
        .whereGreater("age", 10)                  //添加查询条件，查询 age>10 的结果
        .whereLess("grade", 100)                  //添加查询条件，查询 grade<100 的结果
        .whereLike("introduction","%做事认真%")    //添加查询条件，查询 introduction含有“做事认真” 的结果
        .whereNot("gender",1)                     //添加查询条件，查询 gender不为1 的结果
        .find();                                  //此时查询结果为上述条件全都符合的结果
```

除此之外，也可以自行编写查询条件：
```
List<DBData> result = DB.getTable("user")
        .where("name='李四'")                     //添加查询条件，name=李四 的结果
        .find();
```
自行编写查询条件时，查询条件语句不需要含有空格，且文本值需要自行添加单引号。

仅需要查询符合条件的数据数量，可使用以下语句：
```
long count = DB.getTable("user")
        .where("name='李四'")                     //条件可添加可不添加
        .getCount();
```

要清空已添加的查询条件，可使用如下方法：
```
.cleanWhere()                                     //清空查询条件
```

需要分页时只需要设置分页的起始位置和查询数量：
```
List<DBData> result = DB.getTable("user")
        .limit(0, 3)                              //从第一个，共 3 个，注意起始值为 0，数量可以超出实际数据量
        .find();
```

要清空已添加的分页条件，可使用如下方法：
```
.cleanLimit()                                     //清空分页条件
```

### 删
要删除数据，需要想通过查询获得已存在的 DBData，然后执行以下语句：
```
DB.getTable("user")
        .delete(dbData);                          //删除指定 dbData
```
对于未知的 DBData，或者想通过条件查询并删除的，可通过以下语句删除
```
DB.getTable("user")
        .delete(new DBData()                      //删除指定 dbData 中的键值对作为查询条件的查询结果的数据。
            .set("name", "李四")
        );                          
```
或者可通过 `.where` 开头的方法指定查询条件：
```
DB.getTable("user")
        .where("name='李四'")                     //添加删除查询条件
        .delete();
```

### 改
修改已存在的数据，可使用以下语句：
```
List<DBData> result = DB.getTable("user")                       
        .find();
        
for (DBData dbData : result) {                   //对于已存在的数据
    DB.getTable("user").update(
        dbData.set("isVIP", true)                //对查询出的数据进行修改
    );      
}
```

## 其他功能

设置正序或倒序查询
```
.setSort(SORT)          //可选值：SORT.ASC（正序）、SORT.DESC（倒序）
```

关闭数据库
```
DB.closeDB();
```

## Json 互转

对于一个 JsonObject，例如：
```
{
    "name":"张三",
    "age":16,
    "phone":18888888888
}
```
要将它转换为 DBData 对象，请使用：
```
String json = "{\"name\":\"张三\",\"age\":16,\"phone\":18888888888}";
DBData result = new DBData(json);
```

对于已查询出的对象，要将其转换为 json，使用 DBData 自带的  toString() 输出即可：
```
List<DBData> result = DB.getTable("user").find();
for (DBData dbData : result) {
    Log.i("json输出：", dbData.toString());
}
```

## 由 Map 对象创建 DBData

使用以下代码转换为 DBData：
```
DBData result = new DBData(map);
```

## 开源协议
```
Copyright KongzueDB V3

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 更新日志
v3.0.1:
- 新增使用 json 文本创建 DBData 方法；
- 新增 Map 对象创建 DBData 方法；
- bug修复。

v3.0.0:
- 全新版本更新。