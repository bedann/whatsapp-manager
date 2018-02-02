package maze.manager.reveal

import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import kotlinx.android.synthetic.main.activity_viewer.*
import maze.manager.reveal.Adapters.StoryAdapter
import maze.manager.reveal.Commoners.PagerAdapter
import maze.manager.reveal.Commoners.isImage
import maze.manager.reveal.Commoners.isVideo
import maze.manager.reveal.Fragments.ViewFragment
import maze.manager.reveal.POJOs.Story
import java.io.File

class Viewer : AppCompatActivity() {

    var path:String? = null
    var type:Int? = null
    var adapter:PagerAdapter? = null
    var storyAdaper:StoryAdapter? = null
    var index:Int? = null
    var actionButton:MenuItem? = null
    var exists = false
    var saved = false
    var ad_counter = 0
    var interstitial:InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        if(!ImageLoader.getInstance().isInited){
            val config = ImageLoaderConfiguration.Builder(this).build()
            ImageLoader.getInstance().init(config)
        }
        path = intent.getStringExtra("path")
        type = intent.getIntExtra("type",0)
        saved = intent.getBooleanExtra("saved",false)
        Log.e("PATH",path)
        init()
        val items = LoadFiles().execute(path).get()
        storyAdaper = StoryAdapter(this,true){ story,long ->
            (0 until items.size)
                    .filter { items[it].path == story.path }
                    .forEach { viewPager.currentItem = it }
            true
        }
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        recycler.adapter = storyAdaper
    }


    fun init(){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adapter = PagerAdapter(supportFragmentManager,this)
        viewPager.adapter = adapter

        interstitial = InterstitialAd(this)
        interstitial?.adUnitId = getString(R.string.viewer_interstitial)
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

        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                try {
                    recycler.smoothScrollToPosition(position)
                    imageExists((adapter!!.getItem(position) as ViewFragment).exists)
                    ad_counter++
                    if(ad_counter % 3 == 0){
                        interstitial?.show()
                    }
                }catch (v:Exception){

                }
            }

        })
    }


    inner class LoadFiles : AsyncTask<String, Integer, MutableList<Story>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progress.visibility = View.VISIBLE
        }


        override fun onPostExecute(result: MutableList<Story>?) {
            super.onPostExecute(result)
            result?.forEach {
                adapter?.addFragment(ViewFragment.newInstance(it))
            }
            adapter?.notifyDataSetChanged()
            storyAdaper?.addStories(*result!!.toTypedArray())
            viewPager.setCurrentItem(index?:0,false)
            progress.visibility = View.GONE
            imageExists((adapter!!.getItem(index?:0) as ViewFragment).exists)
        }

        override fun doInBackground(vararg p0: String?): MutableList<Story> {
            var target = p0[0]
            val items = mutableListOf<Story>()
            val path = if(saved){
                "${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories"
            }else{
                "${Environment.getExternalStorageDirectory().absolutePath}/WhatsApp/Media/.Statuses"
            }
            val dir = File(path)

            if(dir.exists()){
                val files = when(type){
                    0-> dir.listFiles { _, s ->  s.isImage }
                    1-> dir.listFiles { _, s ->  s.isVideo }
                    else-> dir.listFiles { _, s ->  s.isImage || s.isVideo}
                }

                for(file in files){
                    var decodedImgUri = Uri.fromFile(File(file.absolutePath)).toString()
                    decodedImgUri = Uri.decode(decodedImgUri)
                    val story = Story(file.name, decodedImgUri, file.lastModified(), file.length(), if(file.name.isImage) 0 else 1)
                    items.add(story)
                }

                items.sortByDescending { it.time }

                (0 until items.size)
                        .filter { items[it].path == target }
                        .forEach { index = it }
            }else{
                Log.e("RESULT","Dir does not exist")
            }



            return items
        }

    }

    fun imageExists(exists: Boolean) {
        this@Viewer.exists = exists
        if(exists){
            actionButton?.icon = IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_delete).color(Color.RED).sizeDp(25)
        }else{
            actionButton?.icon = IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_download).color(Color.WHITE).sizeDp(25)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_viewer,menu)
        actionButton = menu?.findItem(R.id.save)
        menu?.findItem(R.id.share)?.icon = IconicsDrawable(this, MaterialDesignIconic.Icon.gmi_share).color(Color.WHITE).sizeDp(25)
        actionButton?.icon = IconicsDrawable(this,if(saved) MaterialDesignIconic.Icon.gmi_delete else MaterialDesignIconic.Icon.gmi_download).color(if(saved) Color.RED else Color.WHITE).sizeDp(25)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.share -> {
                (adapter?.getItem(viewPager.currentItem) as ViewFragment).share()
            }
            else ->{
                if(exists){
                    (adapter?.getItem(viewPager.currentItem) as ViewFragment).delete()
                }else{
                    (adapter?.getItem(viewPager.currentItem) as ViewFragment).save()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onPause() {
        super.onPause()
    }
}
