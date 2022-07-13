package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.adapters.InterestItemAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.SeedsGenres
import org.parceler.Parcels
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

class GetInterestsActivity : AppCompatActivity() {
    val TAG = "GetInterestActivity"

    var rvGenres : RecyclerView? = null
    var genreList : ArrayList<String> = ArrayList()
    var adapter: InterestItemAdapter? = null
    val context = this
    var accessToken: String? = null
    var userPickedGenres : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_interests)

        setTitle("Favorite Genres")
        rvGenres = findViewById(R.id.rvGenres)

        adapter = InterestItemAdapter(context, genreList, userPickedGenres)
        rvGenres?.adapter = adapter
        val gridLayoutManager = GridLayoutManager(context, 2)
        rvGenres?.layoutManager = gridLayoutManager

        accessToken = intent.getStringExtra("accessToken")

        val api = SpotifyApi()
        api.setAccessToken(accessToken)
        api.service.getSeedsGenres(object: Callback<SeedsGenres> {
            override fun success(t: SeedsGenres?, response: Response?) {
                Log.d(TAG, "got seed genres successfully")
                if (t != null){
                    genreList.addAll(t.genres)
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "failed to get seed genres: " + error?.message)
                Toast.makeText(this@GetInterestsActivity,
                    "Failed to pull genres from Spotify api, " +
                            "please restart the app and authorize your Spotify account.",
                Toast.LENGTH_LONG).show()
            //   TODO: authorize here?
            //   SpotifyAuthController(this@GetInterestsActivity).authorizeUser()
            }
        })
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_get_interests, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finishPickingGenres -> {
                if (userPickedGenres.size == 0){
                    Toast.makeText(context, "Please pick at least one genre to continue",
                    Toast.LENGTH_LONG).show()
                    return true
                }
                val data = Intent()
                data.putExtra("userPickedGenres", Parcels.wrap(userPickedGenres))
                setResult(RESULT_OK, data)
                finish()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }


}