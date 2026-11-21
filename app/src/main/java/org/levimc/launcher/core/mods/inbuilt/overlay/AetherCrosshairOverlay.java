package org.levimc.launcher.core.mods.inbuilt.overlay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** A passive, non-interactive center-screen crosshair for visual consistency. */
public final class AetherCrosshairOverlay {
    private final Activity activity;
    private final WindowManager windowManager;
    private View view;

    public AetherCrosshairOverlay(Activity activity) {
        this.activity = activity;
        this.windowManager = (WindowManager) activity.getSystemService(Activity.WINDOW_SERVICE);
    }

    public void show() {
        if (view != null || activity.isFinishing() || activity.isDestroyed()) return;
        final float density = activity.getResources().getDisplayMetrics().density;
        view = new CrosshairView();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                Math.round(40 * density), Math.round(40 * density),
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        params.token = activity.getWindow().getDecorView().getWindowToken();
        try {
            windowManager.addView(view, params);
        } catch (RuntimeException error) {
            view = null;
            android.util.Log.w("AetherPvP", "Unable to show crosshair overlay", error);
        }
    }

    public void hide() {
        if (view == null) return;
        try {
            windowManager.removeView(view);
        } catch (RuntimeException ignored) {
            // The activity window may already be gone.
        }
        view = null;
    }

    private static final class CrosshairView extends View {
        private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);

        CrosshairView() {
            super(null);
            outline.setColor(Color.BLACK);
            outline.setStrokeWidth(4.5f);
            outline.setStrokeCap(Paint.Cap.SQUARE);
            accent.setColor(Color.WHITE);
            accent.setStrokeWidth(2f);
            accent.setStrokeCap(Paint.Cap.SQUARE);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            drawSegments(canvas, outline, cx, cy);
            drawSegments(canvas, accent, cx, cy);
        }

        private void drawSegments(Canvas canvas, Paint paint, float cx, float cy) {
            canvas.drawLine(cx, cy - 10, cx, cy - 4, paint);
            canvas.drawLine(cx, cy + 4, cx, cy + 10, paint);
            canvas.drawLine(cx - 10, cy, cx - 4, cy, paint);
            canvas.drawLine(cx + 4, cy, cx + 10, cy, paint);
        }
    }
}
