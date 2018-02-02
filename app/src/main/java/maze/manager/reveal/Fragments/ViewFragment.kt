package maze.manager.reveal.Fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.fragment_viewer.*
import maze.manager.reveal.POJOs.Story
import maze.manager.reveal.R
import java.io.File
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel


/**
 * Created by Orion Technologies on 10/12/2017.
 */
class ViewFragment(): Fragment() {

    companion object {
        fun newInstance(story:Story):ViewFragment{
            val frag = ViewFragment()
            val args = Bundle()
            args.putString("path",story.path)
            args.putInt("type",story.type)
            args.putString("title",story.title)
            frag.arguments = args
            return frag
        }
    }

    var story:Story? = null
    var exists = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        story = Story(title = arguments.getString("title"), path = arguments.getString("path"), type = arguments.getInt("type"))
        exists = File("${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories/${story?.title}").exists()
        return inflater?.inflate(R.layout.fragment_viewer,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (story?.type?:0 == 0){
            ImageLoader.getInstance().displayImage(story?.path,image)
            videoplayer.visibility = View.GONE
        }else{
            image.visibility = View.GONE
            videoplayer.setSource(Uri.parse(story?.path))
            videoplayer.enableSwipeGestures()
            ImageLoader.getInstance().displayImage(story?.path,videoplayer.thumb)
        }
    }


    fun save(){
        val file = File(Uri.parse(story?.path).path)
        if(file.exists()){
            val dest = File("${Environment.getExternalStorageDirectory().absolutePath}/Whatsapp Stories/${story?.title}")
            copyFile(file,dest)
        }else{
            Log.e("FILES","file no exist")
        }
    }

    fun share(){
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(story?.path))
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, "Send via"))
    }

    fun delete(){
        val dialog = AlertDialog.Builder(activity).setTitle("Delete story").setMessage("Are you sure you want to delete this story?")
                .setPositiveButton("DELETE"){ _, _ ->
                    val file = File(Uri.parse(story?.path).path)
                    file.delete()
                    image.setImageDrawable(IconicsDrawable(activity,MaterialDesignIconic.Icon.gmi_image_alt).color(Color.GRAY).sizeDp(100))
                    Toast.makeText(activity,"Deleted",Toast.LENGTH_SHORT).show()
                }.setNegativeButton("CANCEL"){ _, _ ->

        }.create()
        dialog.show()
    }


    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
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


    override fun onDestroyView() {
        super.onDestroyView()
        try{
            videoplayer.pause()
        }catch(c:Exception){

        }
    }

}