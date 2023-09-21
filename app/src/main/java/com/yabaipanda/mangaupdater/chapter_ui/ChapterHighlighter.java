package com.yabaipanda.mangaupdater.chapter_ui;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ChapterHighlighter extends Animation {
    private final View view;
    private final int darkColor, lightColor;
    public ChapterHighlighter (View view, int duration, String lightColor, String darkColor) {
        this.view = view;
        this.darkColor = Color.parseColor(darkColor);
        this.lightColor = Color.parseColor(lightColor);
        setDuration(duration);
        setRepeatCount(INFINITE);
        setRepeatMode(REVERSE);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        // Get the bounds of the view
        Rect bounds = view.getBackground().getBounds();

        // Calculate the radius of the radial gradient stroke
        int radius = Math.min(bounds.width(), bounds.height()) / 2;

        // Calculate the center coordinates for the radial gradient
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();

        // Create a paint object for the gradient stroke
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20f);
        boolean isDarkModeEnabled = (view.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        // Set colors based on the current theme mode
        int startColor = isDarkModeEnabled ? darkColor : lightColor;
        int endColor = Color.parseColor("#00000000");
        LinearGradient linearGradient = new LinearGradient(
                centerX - radius, centerY,
                centerX + radius, centerY,
                new int[]{startColor, endColor, startColor, endColor},
                new float[]{0f, .2f, .5f, .97f},
                Shader.TileMode.MIRROR);
        // Create a matrix for the gradient shader to apply a slant
        Matrix matrix = new Matrix();
        matrix.setRotate(interpolatedTime*80f, centerX, centerY);
        linearGradient.setLocalMatrix(matrix);
        paint.setShader(linearGradient);
        // Create a shape drawable with the gradient stroke
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.setShape(new RectShape());
        shapeDrawable.getPaint().set(paint);
        // Create a drawable layer list to combine the background and gradient stroke
        Drawable[] layers = new Drawable[2];
        layers[0] = new ColorDrawable(Color.parseColor(isDarkModeEnabled ? "#FF212121":"#FFF8F8F8"));
        layers[1] = shapeDrawable;

        // Create a layer drawable with the solid color background and the gradient stroke
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setLayerInset(1, 0, 0, 0, 0); // Set the inset for the gradient stroke

        // Set the layer drawable as the background of the view
        view.setBackground(layerDrawable);
    }
}
