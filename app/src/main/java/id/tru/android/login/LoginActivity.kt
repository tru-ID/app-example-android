package id.tru.android.login

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import id.tru.android.R
import id.tru.android.services.api.ApiManager
import id.tru.android.services.api.RedirectManager
import id.tru.android.services.network.Client
import id.tru.sdk.TruSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Client.setContext(this.applicationContext)
        TruSDK.initializeSdk(this.applicationContext)

        setContentView(R.layout.activity_login)

    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        // Do something in response to button
        val phoneNumber = findViewById<EditText>(R.id.phone_number)
        val resultView = findViewById<TextView>(R.id.resultView)
        val loading = findViewById<ProgressBar>(R.id.loading)

        println("phoneNumber " + phoneNumber.text.toString())
        resultView.text = ""
        loading.visibility = View.VISIBLE;
        doCheck(phoneNumber.text.toString(),resultView, loading  )
    }

    private fun doCheck(phone: String, view: TextView, load: ProgressBar) {
        val apiManager = ApiManager()
        val redirectManager = RedirectManager()
        val startTime = System.currentTimeMillis()

        // Step 1: Create a Phone Check
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var phoneCheck = apiManager.getPhoneCheck(phone)
                println("phoneCheck " + phoneCheck)
                val currentTime = System.currentTimeMillis()
                withContext(Dispatchers.Main) {
                    view.apply { text = "- Initiating Phone Verification\n- Creating Mobile Data Session [".plus(currentTime-startTime).plus("]") }
                }

                // Step 2: Open the check_url
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        redirectManager.openCheckUrl(phoneCheck.check_url)
                        println("redirect done ")
                        val currentTime = System.currentTimeMillis()
                        withContext(Dispatchers.Main) {
                            view.apply { text = view.text.toString().plus("\n- Retrieving Result [").plus(currentTime-startTime).plus("]") }
                        }

                        // Step 3: Get Phone Check Result
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                var phoneCheckResult = apiManager.getPhoneCheckResult(phoneCheck.check_id)
                                println("phoneCheckResult  " + phoneCheckResult)
                                val currentTime = System.currentTimeMillis()
                                withContext(Dispatchers.Main) {
                                    load.visibility = View.GONE;
                                    if (phoneCheckResult.match) {
                                        view.apply { text =  view.text.toString().plus("\n- Phone Number match: true [").plus(currentTime-startTime).plus("]") }
                                    } else {
                                        view.apply { text = view.text.toString().plus("\n- Phone Number match: failed [").plus(currentTime-startTime).plus("]") }
                                    }
                                }
                            } catch (e: Throwable) {
                                println("exception caught $e")
                                view.apply { text = "- Error " }
                            }

                        }

                    } catch (e: Throwable) {
                        println("exception caught $e")
                        view.apply { text = "- Error " }
                    }
                }
            } catch (e: Throwable) {
                println("exception caught $e")
                view.apply { text = "- Error " }
                load.visibility = View.GONE;
            }
        }
    }


}

