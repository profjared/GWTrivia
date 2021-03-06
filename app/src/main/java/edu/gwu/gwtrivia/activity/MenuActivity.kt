package edu.gwu.gwtrivia.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.gwu.trivia.Utilities
import edu.gwu.gwtrivia.utils.PersistanceManager
import edu.gwu.gwtrivia.R
import kotlinx.android.synthetic.main.activity_menu.*
import org.jetbrains.anko.activityUiThread
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class MenuActivity : AppCompatActivity() {
    private val TAG = "MenuActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 777

    private lateinit var persistanceManager: PersistanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        //initialize persistance manager
        persistanceManager = PersistanceManager(this)

        //add listener for play button
        play_button.setOnClickListener {
            loadGameData()
        }

        //add listener for high scores button
        high_scores_button.setOnClickListener {
            //take user to scores activity
            val intent = Intent(this@MenuActivity, ScoresActivity::class.java)
            startActivity(intent)
        }

        requestPermissionsIfNecessary()
    }

    override fun onResume() {
        super.onResume()

        //fetch high score and display it
        val highScore = persistanceManager.highScore()
        val highScoreValue = highScore?.score ?: 0
        high_score.text = "${getString(R.string.high_score)}: $highScoreValue"
    }

    fun loadGameData() {
        doAsync {
            //parse game data from csv
            val gameData = Utilities.loadGameData("presidents.csv",this@MenuActivity)
            if(gameData != null && gameData.questions.isNotEmpty()) {

                activityUiThread {
                    //take user to game activity and pass game data
                    val intent = Intent(this@MenuActivity, GameActivity::class.java)

                    intent.putExtra("gameData", gameData)
                    startActivity(intent)
                }

                Log.d(TAG, "Number of questions ${gameData.questions.count().toString()}")
            }
            else {
                Log.d(TAG, "problem")
            }
        }
    }

    private fun requestPermissionsIfNecessary() {
        //check for locatin permission, if not already granted
        val checkSelfPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //if user declines location permission, let them know that there will be consequences :)
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if(grantResults.isNotEmpty() && grantResults.first() != PackageManager.PERMISSION_GRANTED) {
                toast(R.string.permission_declined)
            }
        }
    }
}
