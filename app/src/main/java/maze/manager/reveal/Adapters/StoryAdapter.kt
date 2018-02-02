package maze.manager.reveal.Adapters

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import com.nostra13.universalimageloader.core.ImageLoader
import maze.manager.reveal.POJOs.Story
import maze.manager.reveal.R

/**
 * Created by Orion Technologies on 10/12/2017.
 */
class StoryAdapter(context:Context,var small:Boolean = false,var listener:(Story,Boolean)->Boolean): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val inflater = LayoutInflater.from(context)
    val stories = mutableListOf<Story>()
    val selection = mutableListOf<Story>()
    val loader = ImageLoader.getInstance()
    var context = context
    var animate = true

    fun addStory(v:Story){
        stories.add(v)
    }

    fun clear(){
        stories.clear()
    }


    fun addStories(vararg story: Story){
        stories.addAll(story)
        notifyDataSetChanged()
    }

    fun highlight(i:Int){
        selection.clear()
        selection.add(stories[i])
        notifyDataSetChanged()
    }

    fun clearSelection(){
        if(selection.isEmpty())return
        animate = false
        selection.clear()
        notifyDataSetChanged()
        Handler().postDelayed({
            animate = true
        },500)
    }

    fun selectAll(){
        animate = false
        selection.clear()
        selection.addAll(stories)
        notifyDataSetChanged()
        Handler().postDelayed({
            animate = true
        },500)
    }

    fun allSelected():Boolean = selection.size == stories.size

    fun toggleSelection(story:Story){
        animate = false
        if(selection.contains(story)){
            selection.remove(story)
        }else{
            selection.add(story)
        }
        notifyItemChanged(stories.indexOf(story))
        Handler().postDelayed({
            animate = true
        },500)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val story = stories[position]
        when(holder.itemViewType){
            0 -> {
                val holder = holder as ImageHolder
                loader.displayImage(story.path,holder.image)
                holder.saved.visibility = if(story.saved) View.VISIBLE else View.GONE
                holder.selected.visibility = if(selection.contains(story)) View.VISIBLE else View.GONE
                holder.itemView.setOnClickListener { listener(story,false) }
                holder.itemView.setOnLongClickListener { listener(story,true) }
            }
            else -> {
                val holder2 = holder as VideoHolder
                holder2.image.setImageBitmap(null)
                loader.displayImage(story.path,holder2.image)
                holder2.saved.visibility = if(story.saved) View.VISIBLE else View.GONE
                holder2.selected.visibility = if(selection.contains(story)) View.VISIBLE else View.GONE
                holder2.itemView.setOnClickListener { listener(story,false) }
                holder2.itemView.setOnLongClickListener { listener(story,true) }
            }
        }
        if(!small && animate){
            holder.itemView.scaleX = 0.5f
            holder.itemView.scaleY = 0.5f
            holder.itemView.animate().scaleY(1f).scaleX(1f).setDuration(500).interpolator = DecelerateInterpolator()
        }
    }

    override fun getItemCount(): Int = stories.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder = when(viewType){
            0-> ImageHolder(inflater.inflate(if(small) R.layout.row_image_small else R.layout.row_image,parent,false))
            else -> VideoHolder(inflater.inflate(if(small) R.layout.row_image_small else R.layout.row_video,parent,false))
        }


    override fun getItemViewType(position: Int): Int {
        return stories[position].type
    }


    class ImageHolder(v: View): RecyclerView.ViewHolder(v) {
        val image = v.findViewById<ImageView>(R.id.image)
        val saved = v.findViewById<ImageView>(R.id.saved)
        val selected = v.findViewById<ImageView>(R.id.selection)
    }

    class VideoHolder(v: View): RecyclerView.ViewHolder(v) {
        val image = v.findViewById<ImageView>(R.id.image)
        val saved = v.findViewById<ImageView>(R.id.saved)
        val selected = v.findViewById<ImageView>(R.id.selection)
        val play = v.findViewById<ImageView>(R.id.play).setImageDrawable(IconicsDrawable(v.context,MaterialDesignIconic.Icon.gmi_play_circle).color(Color.WHITE).sizeDp(40))
    }


}