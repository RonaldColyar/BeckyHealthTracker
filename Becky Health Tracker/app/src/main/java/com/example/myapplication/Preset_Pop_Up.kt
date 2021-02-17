package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_preset__pop__up.*

class Preset_Pop_Up : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset__pop__up)




        AddButton.setOnClickListener {
            if(idEntry.text.isNullOrEmpty() ||
                CaloriesEntry.text.isNullOrEmpty() ||
                NameEntry.text.isNullOrEmpty() ||
                typeEntry.text.isNullOrEmpty()){
                Toast.makeText(this , "None of the entries can be blank!!", Toast.LENGTH_LONG).show()
            }

            else{
                val data = Intent()
                //try since the use could enter a non Int
                try {
                    val id =  idEntry.text.toString().toInt()
                    val calories = CaloriesEntry.text.toString().toInt()
                    val name = NameEntry.text.toString()
                    val type = typeEntry.text.toString()
                    data.putExtra("id",id)
                    data.putExtra("calories",calories)
                    data.putExtra("name",name)
                    data.putExtra("type", type)
                    setResult(55,  data )
                    this.finish()

                }
                catch (e:Throwable){
                    Toast.makeText(this , "Error 202 , Please make sure your entries are correct!! ," +
                            " ID and Calories can only contain numbers." , Toast.LENGTH_LONG).show()

                }



            }


        }
    }
}