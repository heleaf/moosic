package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.adapters.InterestItemAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.SeedsGenres
import org.parceler.Parcels
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

private const val TAG = "GetInterestActivity"
private const val ACTION_BAR_TITLE = "Favorite Genres"

private const val GRID_ADAPTER_SPAN_COUNT = 2

private const val TOAST_FAILED_TO_GET_GENRES = "Failed to pull genres from Spotify api, " +
        "please restart the app and authorize your Spotify account."
private const val TOAST_PICK_AT_LEAST_ONE_GENRE = "Please pick at least one genre to continue"

class GetInterestsActivity : AppCompatActivity() {
    lateinit var rvGenres : RecyclerView
    var genreList : ArrayList<String> = ArrayList()
    lateinit var adapter: InterestItemAdapter
    val context = this
    lateinit var accessToken: String
    var userPickedGenres : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_interests)

        title = ACTION_BAR_TITLE
        rvGenres = findViewById(R.id.rvGenres)

        adapter = InterestItemAdapter(context, genreList, userPickedGenres)
        rvGenres.adapter = adapter
        val gridLayoutManager = GridLayoutManager(context, GRID_ADAPTER_SPAN_COUNT)
        rvGenres.layoutManager = gridLayoutManager

        accessToken = intent.getStringExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN).toString()

        val api = SpotifyApi()
        api.setAccessToken(accessToken)
        api.service.getSeedsGenres(object: Callback<SeedsGenres> {
            override fun success(seedGenreObject: SeedsGenres?, response: Response?) {
                if (seedGenreObject != null){
                    genreList.addAll(seedGenreObject.genres)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun failure(error: RetrofitError?) {
                Toast.makeText(this@GetInterestsActivity,
                    TOAST_FAILED_TO_GET_GENRES,
                Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, TOAST_PICK_AT_LEAST_ONE_GENRE,
                    Toast.LENGTH_LONG).show()
                    return true
                }
                val data = Intent()
                data.putExtra(Util.INTENT_KEY_USER_PICKED_GENRES, Parcels.wrap(userPickedGenres))
                setResult(RESULT_OK, data)
                finish()
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

}