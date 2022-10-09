# SQLbatis
sqlite helper for android

### 项目导入
  
```
implementation 'com.github.z-Antonio.SQLbatis:SQLbatis:1.0.0'
kapt 'com.github.z-Antonio.SQLbatis:SQLbatisProcessor:1.0.0'
```

### 使用
#### 数据模型
通过对class添加注解，自动创建表结构  
* Database    
	* name  
* ColumnName  
	* primaryKey  
	* autoIncr  
	* dataType  
	* nonNull  
	* defaultValue  
	
代码示例： [TableA.kt](/app/src/main/java/com/antonio/android/sqlbatis/app/db/TableA.kt)
   
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
  
  
#### Provider使用 
##### Uri
content://custom.authorities/dbName/tableName

#### Provider  
manifest注册：  
    
```
<provider
      android:authorities="custom.authorities"
      android:name="com.antonio.android.sqlbatis.provider.SQLbatisProvider"
      android:process=":database">
 </provider>
```  
也支持通过继承 SQLbatisProvider  
自定义其他sqlite语句的创建（比如触发器），从而实现更多的功能   

### 优势
1. ContentProvider支持更便捷：注册即可使用，无需额外代码
2. 支持多数据库多表，数据模型更清晰

### 注意
* dbName、tableName、columnName最终生成的数据库中都是将驼峰转下划线小写的形式，使用ContentValues已经做了转化，手写语句的时候需要注意