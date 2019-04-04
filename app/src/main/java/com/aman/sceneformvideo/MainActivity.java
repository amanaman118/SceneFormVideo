package com.aman.sceneformvideo;

import android.app.ProgressDialog;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private MediaPlayer mediaPlayer;

    // The color to filter out of the video.
    private static final Color CHROMA_KEY_COLOR = new Color(0.1843f, 1.0f, 0.098f);
    private static final float VIDEO_HEIGHT_METERS = 0.85f;

    private ModelRenderable videoRenderable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        ExternalTexture texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this, R.raw.lion_chroma);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        ModelRenderable.builder()
                .setSource(this, R.raw.chroma_key_video)
                .build()
                .thenAccept(renderable -> {
                    System.out.println("aman check video renderable");
                    videoRenderable = renderable;
                    renderable.getMaterial().setExternalTexture("videoTexture", texture);
                    renderable.getMaterial().setFloat4("keyColor", CHROMA_KEY_COLOR);
                })
                .exceptionally(throwable -> {
                    System.out.println("aman check video exception : " + throwable.getLocalizedMessage());
                    Toast toast = Toast.makeText(this, "Unable to load video renderable", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    return null;
                });

        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            System.out.println("Aman check tap detected");
            if (videoRenderable == null)
                return;

            System.out.println("Aman check tap captured");

            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            Node videoNode = new Node();
            videoNode.setParent(anchorNode);

            float videoWidth = mediaPlayer.getVideoWidth();
            float videoHeight = mediaPlayer.getVideoHeight();

            videoNode.setLocalScale(new Vector3(
                    VIDEO_HEIGHT_METERS * (videoWidth / videoHeight), VIDEO_HEIGHT_METERS, 1.0f));

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

                System.out.println("aman check not playing");

                texture.getSurfaceTexture()
                        .setOnFrameAvailableListener((SurfaceTexture surfaceTexture) -> {
                            System.out.println("aman check frame available");
                            videoNode.setRenderable(videoRenderable);
                            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
                        });
            } else {
                System.out.println("aman check playing");
                videoNode.setRenderable(videoRenderable);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
