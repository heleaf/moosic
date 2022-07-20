package com.dev.moosic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.adapters.SongAdapter
import com.dev.moosic.models.Contact
import org.parceler.Parcels

class UserDetailActivity : AppCompatActivity() {
    lateinit var contact : Contact

    lateinit var songRv : RecyclerView
    lateinit var adapter : SongAdapter
    lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        contact = Parcels.unwrap(intent.getParcelableExtra(Util.INTENT_KEY_DETAIL_VIEW_USER))
        songRv = findViewById(R.id.userDetailSongsRv)
        toolbar = findViewById(R.id.userDetailToolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        title = contact.parseUsername
        toolbar.setNavigationOnClickListener {
            finish()
        }

    }
}