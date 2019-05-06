package com.pixelart.sceneformarcoreexample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment

    private val pointer = PointerDrawable()
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false

    private val andyURL = "https://poly.googleusercontent.com/downloads/c/fp/1554151376155320/9-bJ2cXrk8S/8ey98BspXsw/Andy.gltf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener {
            arFragment.onUpdate(it)
            onUpdate()
        }

        initialiseGallery()

        fab.setOnClickListener { view ->
            takePhoto()
        }
    }

    private fun onUpdate() {
        val trackingChanged = updateTracking()
        val contentView: View = findViewById(android.R.id.content)

        if(trackingChanged){
           if(isTracking){
               contentView.overlay.add(pointer)
           }else{
               contentView.overlay.remove(pointer)
           }
           contentView.invalidate()
        }

        if(isTracking){
            val hitTestChanged: Boolean = updateHitTest()
            if(hitTestChanged){
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }

    private fun updateTracking(): Boolean {
        val frame: Frame? = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame != null && frame.camera.trackingState == TrackingState.TRACKING

        return isTracking != wasTracking
    }

    private fun updateHitTest(): Boolean {
        val frame: Frame? = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false

        if(frame != null){
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for (hit in hits){
                val trackable: Trackable = hit.trackable
                if(trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)){
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): Point{
        val view: View = findViewById(android.R.id.content)
        return Point(view.width / 2, view.height / 2)
    }

    fun initialiseGallery(){
        val gallery: LinearLayout = findViewById(R.id.gallery_layout)

        val andy = ImageView(this)
        andy.setImageResource(R.drawable.droid_thumb)
        andy.contentDescription = "andy"
        andy.setOnClickListener { addObjectNetwork(Uri.parse(andyURL)) }
        gallery.addView(andy)

        val cabin = ImageView(this)
        cabin.setImageResource(R.drawable.cabin_thumb)
        cabin.contentDescription = "cabin"
        cabin.setOnClickListener { addObject(Uri.parse("Cabin.sfb")) }
        gallery.addView(cabin)

        val house = ImageView(this)
        house.setImageResource(R.drawable.house_thumb)
        house.contentDescription = "house"
        house.setOnClickListener { addObject(Uri.parse("House.sfb")) }
        gallery.addView(house)

        val igloo = ImageView(this)
        igloo.setImageResource(R.drawable.igloo_thumb)
        igloo.contentDescription = "igloo"
        igloo.setOnClickListener { addObject(Uri.parse("igloo.sfb")) }
        gallery.addView(igloo)
    }

    private fun addObject(model: Uri?) {
        val frame: Frame? = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>

        if(frame != null){
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for(hit in hits){
                val trackable: Trackable = hit.trackable
                if(trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)){
                    placeObject(arFragment, hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    private fun addObjectNetwork(model: Uri?) {
        val frame: Frame? = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>

        if(frame != null){
            hits = frame.hitTest(point.x.toFloat(), point.y.toFloat())
            for(hit in hits){
                val trackable: Trackable = hit.trackable
                if(trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)){
                    placeObjectNetwork(arFragment, hit.createAnchor(), model)
                    break
                }
            }
        }
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor?, model: Uri?) {

        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
            .exceptionally { throwable ->
                val builder = AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle("AR Example Error!")
                val dialog = builder.create()
                dialog.show()
                null
            }
    }

    private fun placeObjectNetwork(fragment: ArFragment, anchor: Anchor?, model: Uri?) {

        ModelRenderable.builder()
            .setSource(fragment.context, RenderableSource.builder()
                .setSource(this@MainActivity , model, RenderableSource.SourceType.GLTF2)
                .setScale(0.75f)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT).build())
            .setRegistryId(model.toString())
            .build()
            .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
            .exceptionally { throwable ->
                val builder = AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle("AR Example Error!")
                val dialog = builder.create()
                dialog.show()
                null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor?, renderable: Renderable?) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private fun takePhoto(){
        val filename = Util.INSTANCE.generateFilename()
        val view: ArSceneView = arFragment.arSceneView

        // Create a bitmap the size of the scene view.
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a handler thread to offload the processing of the image.
        val handlerThread = HandlerThread("PixelCopier");handlerThread.start()

        // Make the request to copy.
        PixelCopy.request(view, bitmap, {copyResult:Int ->
            if(copyResult == PixelCopy.SUCCESS){
                try {
                    Util.INSTANCE.saveBitmapToDisk(bitmap, filename)
                }catch (e: IOException){
                    Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
                    return@request
                }
                val snackbar: Snackbar = Snackbar.make(findViewById(android.R.id.content), "Photo Saved", Snackbar.LENGTH_LONG)
                snackbar.setAction("Open in Photos") { v->
                    val photoFile = File(filename)
                    val photoUri: Uri = FileProvider.getUriForFile(this@MainActivity,
                        this@MainActivity.packageName+ ".ar.codelab.name.provider", photoFile)

                    startActivity(Intent(Intent.ACTION_VIEW, photoUri)
                        .setDataAndType(photoUri, "image/*")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                }
                snackbar.show()
            }else{
                Toast.makeText(this, "Failed to copyPixels: $copyResult", Toast.LENGTH_LONG).show()
            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }
}
