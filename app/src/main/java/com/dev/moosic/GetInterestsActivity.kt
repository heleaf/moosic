package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.adapters.InterestItemAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.SeedsGenres
import org.parceler.Parcels
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

// get interests....
// get some genres and artists from spotify
// let the user select some interests
// store the seeds in the user's profile..?

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

        setTitle("What are your favorite genres?")

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
                // finish
                // send list of user's favorite genres back
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