package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.preset_removal.view.*
import layout.DataHolder
import layout.DatabaseFunctionality
import org.w3c.dom.Text
import java.util.*

//this means the current object of the class!!


class MainActivity : AppCompatActivity() {
    val functionality = DatabaseFunctionality(this)
    var main_calories = 0

    private fun total_calorie_amount (mis_data :DataHolder):Int{
        var calorie_holder = 0
        for (calories in mis_data.calories){
            calorie_holder += calories
        }
        return calorie_holder
    }
    private fun start_speech_recog(){
        val action = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        action.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak")
        action.putExtra(RecognizerIntent.EXTRA_LANGUAGE ,Locale.getDefault())
        startActivityForResult(action,44333)
    }


    private fun configure_button(preset_button:Button , index: Int,preset_data:DataHolder){
        preset_button.text = preset_data.names[index]
        preset_button.setBackgroundColor(Color.GREEN)
    }

    private fun display_layout(layout :Int , mthd: (String) ->Unit, title:String){
        val dialog_view = LayoutInflater.from(this).inflate(layout,null)
        val dialog_builder = AlertDialog.Builder(this)
        dialog_builder.setView(dialog_view)
        dialog_builder.setTitle(title)
        dialog_builder.show()
        dialog_view.submit.setOnClickListener {
            mthd(dialog_view.Name_of_target.text.toString())
            this.recreate()
            PlaySound()
        }

    }
    private fun check_for_commands(command:String){
        if (command == "remove all presets" ||command== "Remove all presets" ){
            functionality.delete_everything(this, functionality.PRESET_TABLE_NAME, "All Favorite Foods Removed!!")
            this.recreate()
            PlaySound()
        }
        else if (command == "delete all calories" || command == "Delete all calories"){
            functionality.delete_everything(this,functionality.MIS_TABLE_NAME, "Today's Calories Removed!!")
            this.recreate()
            PlaySound()
        }
        else{
            Toast.makeText(this, "Speech Recognition picked up a non-number!!", Toast.LENGTH_LONG ).show()
        }
    }
    private fun on_food_button_click(name:String){
        if (name == "+"){
            val action = Intent(this , Preset_Pop_Up::class.java)
            startActivityForResult(action, 500)
            }
        else{
            //search for the food in the preset table and add it to the mis table  and then update ui
            val calories_of_button : Int = functionality.result_of_name(name)
            if (functionality.result_of_name(name) > 0 ){
                main_calories += calories_of_button
                functionality.insert_mis_data(calories_of_button.toString())
                Toast.makeText(this, "Calories Added!!" , Toast.LENGTH_LONG).show()
                this.recreate()
                PlaySound()
            }
            else{
                Toast.makeText(this, "Error 500" , Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun PlaySound(){
        // A sound effect for success
        var mediaplayer = MediaPlayer.create(this,R.raw.soundeff)
        mediaplayer.start()
    }

    private fun show_popup(view:View){
        val popup = PopupMenu(this , view)
        popup.inflate(R.menu.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

            if (it.itemId == R.id.deletecal) //delete all calories
            {
               functionality.delete_everything(this,functionality.MIS_TABLE_NAME, "Today's Calories Removed!!")
                this.recreate()
                PlaySound()
            }
            else if (it.itemId == R.id.Add)//add calories
            {
                display_layout(R.layout.calorie_adder,functionality::insert_mis_data,"Add Calories")
            }
            else if(it.itemId == R.id.deleteAllPresets){ //delete all presets
                functionality.delete_everything(this, functionality.PRESET_TABLE_NAME, "All Favorite Foods Removed!!")
                this.recreate()
                PlaySound()
            }
            else //remove single preset/favorite food
            {
               display_layout(R.layout.preset_removal,functionality::delete_preset ,"Remove Preset")
            }
         true
        })
        popup.show()
    }

    private fun add_calories_and_play_success_tone(result:String){
        functionality.insert_mis_data(functionality.id_result(result).toString())
        this.recreate()
        PlaySound()
        Toast.makeText(this, "Calories added!!", Toast.LENGTH_LONG).show()

    }
    private fun update_buttons(preset_data: DataHolder,buttons:Array<Button>){
        val len_of_rows = preset_data.names.size -1
          //changing the names of the buttons if there are saved  preset favorite foods in database
        if (preset_data.count == 0){
            Toast.makeText(this , "No Presets!!!! " ,Toast.LENGTH_LONG).show()
        }
        else {
                for (i in 0..len_of_rows){
                    configure_button(buttons[i] , i,preset_data)
            }


        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //data

        val buttons  = arrayOf(FoodButton1,FoodButton2,FoodButton3,FoodButton4, FoodButton5)
        val preset_data  = functionality.db_data(functionality.PRESET_TABLE_NAME,functionality::add_preset_data)
        val mis_data = functionality.db_data(functionality.MIS_TABLE_NAME , functionality::add_mis_data)

        //init
        main_calories += total_calorie_amount(mis_data)
        Calorieslabel.text = main_calories.toString()
        update_buttons(preset_data,buttons)
        Profilecard.setOnClickListener {
            start_speech_recog()
        }
        FoodButton1.setOnClickListener {
            on_food_button_click(FoodButton1.text.toString())
        }
        FoodButton2.setOnClickListener {
            on_food_button_click(FoodButton2.text.toString())
        }
        FoodButton3.setOnClickListener {
            on_food_button_click(FoodButton3.text.toString())
        }
        FoodButton4.setOnClickListener {
            on_food_button_click(FoodButton4.text.toString())
        }
        FoodButton5.setOnClickListener {
            on_food_button_click(FoodButton5.text.toString())
        }
        More.setOnClickListener {
            show_popup(More)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == 55){
            val id = data!!.getIntExtra("id", -1)
            val calories = data!!.getIntExtra("calories" ,  -2)
            val name = data!!.getStringExtra("name")
            val type = data!!.getStringExtra("type")
            functionality.insert_preset_data(id ,calories,name!!,type!! )
            this.recreate()
            PlaySound()
        }

        else if (requestCode == 44333 && resultCode == Activity.RESULT_OK && data != null){

                val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (results == null || results == emptyArray<String>()){
                    Toast.makeText(this, "Issue Understanding What You said", Toast.LENGTH_LONG).show()
                }
                else if ( functionality.id_result(results!![0]) == -2) //if we get a NumberFormatException in the id_result method
                {
                   check_for_commands(results[0])
                }
                else if(functionality.id_result(results!![0]) > 0){ //  if the id number matches a preset/favorite food
                 add_calories_and_play_success_tone(results[0])
                }
                else // there is no results
                {
                    val user_input = results[0]
                    Toast.makeText(this, "You have no favorite foods with the id:($user_input)", Toast.LENGTH_LONG).show()
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
