package maze.manager.reveal.Fragments

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_list.*
import maze.manager.reveal.Adapters.StoryAdapter
import maze.manager.reveal.POJOs.Story
import maze.manager.reveal.R
import maze.manager.reveal.Viewer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

/**
 * Created by Orion Technologies on 10/12/2017.
 */
class ImageFragment(): Fragment() {

    var adapter:StoryAdapter? = null
    var listener:FragListener? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        listener = activity as FragListener
        return inflater?.inflate(R.layout.fragment_list,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StoryAdapter(activity){story,long ->
            if(long){
                listener?.onLongClick(story)
            }else{
                if(adapter!!.selection.isEmpty()){
                    activity.startActivity(Intent(activity, Viewer::class.java).putExtra("path",story.path))
                }else{
                    listener?.onClick(story = story)
                }
            }
            true
        }
        recycler.setHasFixedSize(true)
        recycler.layoutManager = GridLayoutManager(activity,3)
        recycler.adapter = adapter

        refresh.setOnRefreshListener {
            LoadFiles().execute("nothing")
        }

        LoadFiles().execute("nothing")
    }

    fun deleteSelection(){
        adapter!!.selection
                .map { File(Uri.parse(it.path).path) }
                .filter { it.exists() }
                .forEach { it.delete() }
        LoadFiles().execute("nothing")
    }

    fun saveSelection(){
        for (s in adapter!!.selection){
            copyFile(s)
        }
        LoadFiles().execute("nothing")
    }

    @Throws(IOException::class)
    fun copyFile(story: Story) {
        val sourceFile = File(Uri.parse(story?.path).path)
        if (!sourceFile.exists())return
        val destFile = File("${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories/${story?.title}")
        if (!destFile.parentFile.exists())
            destFile.parentFile.mkdirs()

        if (!destFile.exists()) {
            destFile.createNewFile()
        }

        var source: FileChannel? = null
        var destination: FileChannel? = null

        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination!!.transferFrom(source, 0, source!!.size())
            Toast.makeText(activity,"Saved",Toast.LENGTH_SHORT).show()
        } finally {
            if (source != null) {
                source.close()
            }
            if (destination != null) {
                destination.close()
            }
        }
    }


    inner class LoadFiles(): AsyncTask<String, Integer, MutableList<Story>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            refresh.isRefreshing = true
        }


        override fun onPostExecute(result: MutableList<Story>?) {
            super.onPostExecute(result)
            if(result!!.isNotEmpty()){
                adapter?.clear()
                adapter?.addStories(*result!!.toTypedArray())
            }
            refresh.isRefreshing = false
        }

        override fun doInBackground(vararg p0: String?): MutableList<Story> {
            val items = mutableListOf<Story>()
            val path = "${Environment.getExternalStorageDirectory().absolutePath}/WhatsApp/Media/.Statuses"
            val dir = File(path)

            if(dir.exists()){
                val files = dir.listFiles { _, s -> s.endsWith(".jpg",true) || s.endsWith(".png",true) }

                files.mapTo(items) {
                    val decodedImgUri = Uri.fromFile(File(it.absolutePath)).toString()
                    Story(it.name, decodedImgUri, it.lastModified(), it.length(),0,
                            File("${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories/${it.name}").exists()) }

                items.sortByDescending { it.time }
            }else{
                Log.e("RESULT","Dir does not exist")
            }


            return items
        }

    }



    public interface FragListener{
        fun onClick(story:Story)
        fun onLongClick(story:Story)
    }


}
