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
import kotlinx.android.synthetic.main.fragment_list.*
import maze.manager.reveal.Adapters.StoryAdapter
import maze.manager.reveal.Commoners.isImage
import maze.manager.reveal.Commoners.isVideo
import maze.manager.reveal.POJOs.Story
import maze.manager.reveal.R
import maze.manager.reveal.Viewer
import java.io.File

/**
 * Created by Orion Technologies on 10/12/2017.
 */
class SavedFragment(): Fragment() {

    var adapter: StoryAdapter? = null
    var listener: ImageFragment.FragListener? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        listener = activity as ImageFragment.FragListener
        return inflater?.inflate(R.layout.fragment_list,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StoryAdapter(activity) { story, long ->
            if(long){
                listener?.onLongClick(story)
            }else{
                if(adapter!!.selection.isEmpty()){
                    activity.startActivity(Intent(activity, Viewer::class.java).putExtra("path", story.path).putExtra("type",-1).putExtra("saved",true))
                }else{
                    listener?.onClick(story = story)
                }
            }
            true
        }
        recycler.setHasFixedSize(true)
        recycler.layoutManager = GridLayoutManager(activity, 3)
        recycler.adapter = adapter

        refresh.setOnRefreshListener {
            LoadFiles().execute()
        }

        LoadFiles().execute()
    }


    inner class LoadFiles(): AsyncTask<String, Integer, MutableList<Story>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            refresh.isRefreshing = true
        }

        override fun onProgressUpdate(vararg values: Integer?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: MutableList<Story>?) {
            super.onPostExecute(result)
            adapter?.clear()
            adapter?.addStories(*result!!.toTypedArray())
            refresh.isRefreshing = false
        }

        override fun doInBackground(vararg p0: String?): MutableList<Story> {
            val items = mutableListOf<Story>()
            val path = "${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories"
            val dir = File(path)

            if(dir.exists()){
                val files = dir.listFiles { _, s -> s.isImage || s.isVideo}

                files.mapTo(items) {
                    var decodedImgUri = Uri.fromFile(File(it.absolutePath)).toString()
                    decodedImgUri = Uri.decode(decodedImgUri)
                    Log.e("PATH",decodedImgUri)
                    Story(it.name, decodedImgUri, it.lastModified(), it.length(), if(it.name.isImage) 0 else 1, true)
                }

                items.sortByDescending { it.time }

            }else{
                Log.e("RESULT", "Dir does not exist")
            }


            return items
        }

    }


    fun deleteSelection(){
        adapter!!.selection
                .map { File(Uri.parse(it.path).path) }
                .filter { it.exists() }
                .forEach { it.delete() }
        LoadFiles().execute()
    }



}