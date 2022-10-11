# SQLbatis
sqlite helper for android

### 项目导入
  
```
// SQLbatis功能sdk，主要类有：SQLbatisProvider、SQLbatisHelper
implementation 'com.github.z-Antonio.SQLbatis:SQLbatis:$new_version'
// 当使用注解标注的包需要添加此依赖，用来自动生成数据表结构
kapt 'com.github.z-Antonio.SQLbatis:SQLbatisProcessor:$new_version'
```

### 使用
#### 数据模型
通过对class添加注解，自动创建表结构  
（所有数据库名、表名、列名都会将驼峰转化为下划线小写的形式）  
* Database _该类保存为数据库结构，类名即为表名_    
	* name _对应表所在的数据库名称_  
* ColumnName _对应表中的一列，方法名即为列名_  
	* primaryKey _是否是主键，默认false_  
	* autoIncr _是否是自增长，只有dataType为integer有效，默认false_  
	* dataType _数据类型，目前只有integer、real、text(默认)、blob_  
	* nonNull _该列不能为null，默认false_  
	* defaultValue _默认值，不配置则无，当nonNull=true，会根据数据类型自动填充默认的defaultValue_  
	
代码示例： [TableA.kt](/app/src/main/java/com/sqlbatis/android/app/db/TableA.kt)
   
```
@Database("TestDb1")
class TableA2 {
    @ColumnName(primaryKey = true, autoIncr = true, dataType = DataType.integer)
    var idA: Int = 0
    
    @ColumnName(dataType = DataType.integer)
    var intNull: Int = 0
    
    @ColumnName(dataType = DataType.real)
    var realNull: Float = 0F
    
    @ColumnName(dataType = DataType.text)
    var textNull: String = ""
    
    @ColumnName(dataType = DataType.blob)
    var blobNull: Array<Byte> = arrayOf(0)
    
    @ColumnName(nonNull = true, dataType = DataType.integer)
    var intNoNull: Int = 0
    
    @ColumnName(nonNull = true, dataType = DataType.real)
    var realNoNull: Float = 0F
    
    @ColumnName(nonNull = true, dataType = DataType.text)
    var textNoNull: String = ""
    
    @ColumnName(nonNull = true, dataType = DataType.blob)
    var blobNoNull: ByteArray = byteArrayOf(0)
    
    @ColumnName(nonNull = true, dataType = DataType.integer, defaultValue = "1")
    var intNoNullDef: Int = 0
    
    @ColumnName(nonNull = true, dataType = DataType.real, defaultValue = "1.0")
    var realNoNullDef: Float = 0F
    
    @ColumnName(nonNull = true, dataType = DataType.text, defaultValue = "\"-\"")
    var textNoNullDef: String = ""
    
    @ColumnName(nonNull = true, dataType = DataType.blob, defaultValue = "1")
    var blobNoNullDef: Array<Byte> = arrayOf(0)
}
```
  
#### 数据库操作
下列方法的T对象都必须被@Database注解标注
+ SQLbatis.insertList(Context, List<T>) 批量插入对象
+ SQLbatis.insertOrUpdate(Context, T) 插入或更新对象
+ SQLbatis.updateList(Context, List<T>) 批量更新对象，标注有primaryKey=true的字段必须有值，否则不更新
+ SQLbatis.delete(Context, T) 删除对象，如果标注primaryKey=true的字段有值着根据此字段删除，没有根据其他字段匹配
+ SQLbatis.deleteList(Context, List<T>) 批量删除对象，只会根据标注有primaryKey=true的column进行删除
+ SQLbatis.query<T>(Context, selection, selectionArgs, sortOrder) 根据输入的类型自动匹配数据库和表，并根据条件(可null)返回对象列表
+ SQLbatis.getDatabase(Context, dbName) 根据数据库名称获取SQLiteDatabase，自由进行数据库操作
  
#### Provider使用 
##### Uri
content://custom.authorities/dbName/tableName

#### Provider  
manifest注册：[AndroidManifest.xml](/app/src/main/AndroidManifest.xml)  
    
```
<provider
      android:authorities="custom.authorities"
      android:name="com.sqlbatis.android.provider.SQLbatisProvider"
      android:process=":database">
 </provider>
```  

#### 获取Database
可以通过调用 SQLbatis.getDatabase() 方法获取Database对象
  
```
	// java 调用
	Database db = SQLbatis.companion.getDatabase(context, "dbName");

	// kotlin 调用
	val db = SQLbatis.getDatabase(context, "dbName")
```  

#### 自定义
也支持通过继承 SQLbatisHelper 和 SQLbatisProvider 实现更多自定义功能（比如触发器）  
示例：  
[MySqlBatisHelper.kt](/app/src/main/java/com/sqlbatis/android/app/MySqlBatisHelper.kt)  
[MySqlBatisProvider.kt](/app/src/main/java/com/sqlbatis/android/app/MySqlBatisProvider.kt)   

### 优势
1. 自动生成数据库以及创建表结构、表升级，支持多库多表
2. ContentProvider支持更便捷：注册即可使用，无需额外代码
3. 支持多数据库多表，数据模型更清晰，可支持跨进程

### 注意
* dbName、tableName、columnName最终生成的数据库中都是将驼峰转下划线小写的形式，使用ContentValues已经做了转化，手写语句的时候需要注意