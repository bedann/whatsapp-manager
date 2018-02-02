package maze.manager.reveal.Commoners

/**
 * Created by Orion Technologies on 10/12/2017.
 */
val String.isImage
    get() = this.endsWith(".jpg",true) || this.endsWith(".png",true)

val String.isVideo
    get() = this.endsWith(".mp4",true) || this.endsWith(".3gp",true)