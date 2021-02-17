package layout

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.lang.NumberFormatException
import kotlin.reflect.KFunction2


const val DATABASE_NAME = "MyDB"
class DataHolder(){
    var ids  = mutableListOf<Int>()
    var calories  = mutableListOf<Int>()
    var names  = mutableListOf<String>()
    var types = mutableListOf<String>()
    var count = 0
}
class DatabaseFunctionality(val context: Context ):SQLiteOpenHelper(context, DATABASE_NAME , null,1)

{
    val PRESET_TABLE_NAME = "FavFood"
    val MIS_TABLE_NAME = "ExtraCalories"

    override fun onCreate(db: SQLiteDatabase?) {
        val create_preset = "CREATE TABLE $PRESET_TABLE_NAME (id int, calories int , name VARCHAR(255)  , type VARCHAR(255)) "
        val create_mis = "CREATE TABLE $MIS_TABLE_NAME (calories int)"
        db?.execSQL(create_preset)
        db?.execSQL(create_mis)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    private fun name_query_result(cursor: Cursor):Int{
        if (cursor.count < 1){
            return -500000
        }
        else{
            val query_result = cursor.getInt(cursor.getColumnIndexOrThrow("calories"))
            return query_result
        }
    }

    private fun id_query_result(cursor: Cursor):Int{
        if (cursor.count > 0) {
            val query_result = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            return query_result
        } else {
            return -1
        }
    }

    open fun id_result(id:String):Int{
        try {
            val target = id.toInt()
            val db = this.readableDatabase
            val cursor = db.query(
                PRESET_TABLE_NAME,
                arrayOf("calories", "id"), //columns
                "id=?", // where
                arrayOf(id), // where's value
                null, //group by
                null, //having
                null // order by
            )
            cursor.moveToNext()
            return id_query_result(cursor)
        }
        catch (e:NumberFormatException){
            return -2
        }


    }

    open fun result_of_name(name: String):Int {

        val db = this.readableDatabase
        val cursor = db.query(PRESET_TABLE_NAME,
            arrayOf("calories"), //columns
            "name=?",  // where
            arrayOf(name),     // where's value
            null,     //group by
            null,     // having
            null )   // order by

        cursor.moveToNext() // since it always start at -1
        return name_query_result(cursor)

    }



    private fun save_new_data(values:ContentValues,name:String){
        val db  = this.writableDatabase
        try {
            db?.insert(name,null,values)
        }
        catch (e:Throwable){
            Toast.makeText(context , "Error 202" , Toast.LENGTH_LONG).show()
        }
    }




    open fun delete_everything(context: Context,table_name:String, message:String){
        var db = this.writableDatabase
        db.delete(table_name, null,null)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    open fun delete_preset(name:String ){
        var db = this.writableDatabase
        db.delete(PRESET_TABLE_NAME,"name=?" , arrayOf(name))
    }

    open fun db_data(name:String, mthd: (Cursor, DataHolder) -> Unit):DataHolder{
        var data_item = DataHolder()
        var db  =  this.readableDatabase
        val cursor = db.query(name,
            null , //columns
            null, // where
            null, // where's value
            null, //group by
            null,  // having
            null) // order by

        data_item.count = cursor.count
        with(cursor){
            while (this.moveToNext()){// since it always start at -1
                mthd(this,data_item)
            }
        }
        return data_item

    }

    private fun check_for_presets(name: String):Boolean{
        var bool = false
        val db = this.readableDatabase
        val rows = DatabaseUtils.queryNumEntries(db,name )
        if (rows.toInt() >= 5){
            bool = true
        }
        return bool
    }


    //used as the 'mthd' in 'db_data' for abstraction
    open fun add_preset_data(cursor:Cursor , database_item:DataHolder){
        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        val calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val type  = cursor.getString(cursor.getColumnIndexOrThrow("type"))
        database_item.ids.add(id)
        database_item.calories.add(calories)
        database_item.names.add(name)
        database_item.types.add(type)
    }
    open fun add_mis_data(cursor: Cursor,database_item: DataHolder){
        val calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"))
        database_item.calories.add(calories)
    }

    //saving data
    open fun insert_preset_data (id :Int , calories :Int , name:String , type:String){
        val values = ContentValues().apply {
            this.put("id" , id)
            this.put("calories", calories)
            this.put("name" , name)
            this.put("type", type)
        }
        if (check_for_presets(PRESET_TABLE_NAME) == false){
            save_new_data(values, PRESET_TABLE_NAME)
        }
        else{
            Toast.makeText(context, "error 444", Toast.LENGTH_LONG).show()
        }
    }

    open fun insert_mis_data(calories: String){

        try {
            val values = ContentValues().apply{
                this.put("calories" , calories.toInt())
            }
            save_new_data(values, MIS_TABLE_NAME)

        }
        catch (e:Throwable){
            Toast.makeText(context, "Unable To Add Calories!!", Toast.LENGTH_LONG).show()
        }


    }


}

