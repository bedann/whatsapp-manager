package maze.manager.reveal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import maze.manager.reveal.Commoners.PagerAdapter
import maze.manager.reveal.Fragments.ImageFragment
import maze.manager.reveal.Fragments.SavedFragment
import maze.manager.reveal.Fragments.VideoFragment
import maze.manager.reveal.POJOs.Story
import android.Manifest
import android.os.Build
import android.util.Log

class MainActivity : AppCompatActivity(),ImageFragment.FragListener {

    var imageFrag:ImageFragment? = null
    var videoFrag:VideoFragment? = null
    var savedFrag:SavedFragment? = null

    var actionModeCallback = ActionModeCallback()
    var actionMode:ActionMode? = null
    var save:MenuItem? = null
    var interstitial:InterstitialAd? = null
    var inited = false
    var perms = arrayOf(Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val config = ImageLoaderConfiguration.Builder(this).build()
        ImageLoader.getInstance().init(config)
        MobileAds.initialize(this,getString(R.string.ad_app_id))


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permission_granted(perms[2])){
                init()
            }else{
                checkforPerms()
            }
        }else{
            init()
        }
    }



    fun init(){
        if(inited)return
        inited = true
        setSupportActionBar(toolbar)

        val pagerAdapter = PagerAdapter(supportFragmentManager,this)
        imageFrag = ImageFragment()
        videoFrag = VideoFragment()
        savedFrag = SavedFragment()
        pagerAdapter.addAllFrags(imageFrag,videoFrag,savedFrag)
        pagerAdapter.addAllTitles("IMAGES","VIDEOS","SAVED")
        viewPager.adapter = pagerAdapter
        tablayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if(actionMode != null){
                    imageFrag?.adapter?.clearSelection()
                    videoFrag?.adapter?.clearSelection()
                    savedFrag?.adapter?.clearSelection()
                }
                actionMode?.finish()
                save?.isVisible = position != 2
            }
        })


        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adView.visibility = View.VISIBLE
            }
        }
        val adRequest =  AdRequest.Builder().addTestDevice(getString(R.string.test_device)).build()
        adView.loadAd(adRequest)

        interstitial = InterstitialAd(this)
        interstitial?.adUnitId = getString(R.string.home_interstitial)
        interstitial?.adListener = object: AdListener() {
            override fun onAdClosed() {
                super.onAdClosed()
                interstitial?.loadAd(AdRequest.Builder().build())
            }

            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                interstitial?.loadAd(AdRequest.Builder().build())

            }
        }
        interstitial?.loadAd(AdRequest.Builder().addTestDevice(getString(R.string.test_device)).build())
    }

    override fun onClick(story: Story) {
        if(actionMode != null) {
            toggleSelection(story)
        }
    }

    override fun onLongClick(story: Story) {
        if(actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)
        }
        toggleSelection(story)
    }

    fun toggleSelection(story: Story) {
        var count = 0
        when(viewPager.currentItem) {
            0 -> {
                imageFrag?.adapter?.toggleSelection(story)
                count = imageFrag?.adapter?.selection?.size?:0
            }
            1 -> {
                videoFrag?.adapter?.toggleSelection(story)
                count = videoFrag?.adapter?.selection?.size?:0
            }
            2 -> {
                savedFrag?.adapter?.toggleSelection(story)
                count = savedFrag?.adapter?.selection?.size?:0
            }
        }
        if(count == 0) {
            actionMode?.finish()
        }else {
            actionMode?.title = "$count Selected"
            actionMode?.invalidate()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        menu?.findItem(R.id.share)?.icon = IconicsDrawable(this,MaterialDesignIconic.Icon.gmi_share).color(Color.WHITE).sizeDp(20)
        menu?.findItem(R.id.rate)?.icon = IconicsDrawable(this,MaterialDesignIconic.Icon.gmi_star).color(Color.WHITE).sizeDp(25)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, you can now download Whatsapp stories with this app\nhttps://play.google.com/store/apps/details?id=maze.manager.reveal")
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "Send via"))
            }
            else ->{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://play.google.com/store/apps/details?id=maze.manager.reveal")
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }


    override fun onBackPressed() {
        if(actionMode != null){
            actionMode?.finish()
        }else{
            super.onBackPressed()
            if(interstitial!!.isLoaded){
                interstitial?.show()
            }
        }
    }

    inner class ActionModeCallback : ActionMode.Callback {
        val TAG = "ACTIONMODE"


        override fun onCreateActionMode(mode: ActionMode, menu:Menu):Boolean {
            mode.menuInflater.inflate (R.menu.menu_select, menu);
            val del =  IconicsDrawable(baseContext, MaterialDesignIconic.Icon.gmi_delete).color(Color.WHITE).sizeDp(20);
            val sel =  IconicsDrawable(baseContext, MaterialDesignIconic.Icon.gmi_select_all).color(Color.WHITE).sizeDp(20);
            val sav =  IconicsDrawable(baseContext, MaterialDesignIconic.Icon.gmi_save).color(Color.WHITE).sizeDp(20);
            val delete = menu.findItem(R.id.delete)
            delete.icon = del
            val select = menu.findItem(R.id.select)
            select.icon = sel
            save = menu.findItem(R.id.save)
            save?.icon = sav
            save?.isVisible = viewPager.currentItem != 2
            return true
        }


        override fun onPrepareActionMode(mode: ActionMode, menu:Menu):Boolean {
            save?.isVisible = viewPager.currentItem != 2
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item:MenuItem):Boolean {
            when (item.itemId) {
                R.id.delete -> {
                    val dialog = AlertDialog.Builder(this@MainActivity).setTitle("Delete selection").setMessage("Are you sure you want to delete these stories?")
                            .setPositiveButton("DELETE"){ _, _ ->
                                when(viewPager.currentItem) {
                                    0-> imageFrag?.deleteSelection()
                                    1-> videoFrag?.deleteSelection()
                                    else -> savedFrag?.deleteSelection()
                                }
                                if (actionMode != null){
                                    actionMode?.finish()
                                }
                                Toast.makeText(baseContext,"Deleted",Toast.LENGTH_SHORT).show()
                            }.setNegativeButton("CANCEL"){ _, _ ->

                    }.create()
                    dialog.show()
                    return true
                }
                R.id.select ->{
                    val adp = when(viewPager.currentItem) {
                        0->imageFrag!!.adapter
                        1->videoFrag!!.adapter
                        else->savedFrag!!.adapter
                    }
                    if (adp!!.allSelected()) {
                        adp.clearSelection()
                        mode.finish()
                    } else {
                        adp.selectAll()
                        actionMode?.title = "All Selected"
                        actionMode?.invalidate()
                    }
                    return true
                }
                R.id.save -> {
                    when(viewPager.currentItem) {
                        0-> imageFrag?.saveSelection()
                        1-> videoFrag?.saveSelection()
                        else -> savedFrag?.deleteSelection()
                    }
                    Toast.makeText(baseContext,"Saved",Toast.LENGTH_SHORT).show()
                    mode.finish()
                    return true
                }
                else -> {

                    return false
                }
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            when(viewPager.currentItem) {
                0->imageFrag?.adapter?.clearSelection()
                1->videoFrag?.adapter?.clearSelection()
                else->savedFrag?.adapter?.clearSelection()
            }
            actionMode = null
        }
    }


    fun checkforPerms(){
        val toAsk = arrayListOf<String>()
        perms.filterNotTo(toAsk) { permission_granted(it) }
        if (!toAsk.isEmpty()){
            ActivityCompat.requestPermissions(this, toAsk.toTypedArray(),
                    909)
        }
    }

    fun permission_granted(perm:String): Boolean {
        val granted = ContextCompat.checkSelfPermission(this,
                perm) == PackageManager.PERMISSION_GRANTED
        Log.e("PERMISSION GRANTED", granted.toString())
        return granted
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            909 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                } else {
                    finish()
                    Toast.makeText(this, "Permissions are required", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


}



