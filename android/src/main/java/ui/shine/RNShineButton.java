
package ui.shine;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.text.ReactFontManager;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.Random;

public class RNShineButton extends ViewGroupManager<ViewGroup> {

  public static final String REACT_CLASS = "RNShineButton";

  private ThemedReactContext reactContext;

  private final String SHAPE_HEART = "heart";
  private final String SHAPE_LIKE = "like";
  private final String SHAPE_SMILE = "smile";
  private final String SHAPE_STAR = "star";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected FrameLayout createViewInstance(final ThemedReactContext reactContext) {
    this.reactContext = reactContext;

    int randomId;

    Random rand = new Random();
    while (reactContext.getCurrentActivity().findViewById(randomId = rand.nextInt(Integer.MAX_VALUE) + 1) != null);
    final int viewId = randomId;

    final ShineButton shineButton = new ShineButton(reactContext.getCurrentActivity());
    final FrameLayout frameLayout = new FrameLayout(reactContext.getCurrentActivity());
    frameLayout.addView(shineButton);

    shineButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(View view, boolean checked) {
       int id = frameLayout.getId();

        reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
                new ShineButtonEvent(
                        id,
                        checked));
      }
    });

    return frameLayout;
  }

  @ReactProp(name = "size")
  public void setSize(FrameLayout shineButtonFrame, int size) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);

    float density = shineButtonFrame.getContext().getResources().getDisplayMetrics().density;
    int dpSize = Math.round(size * density);

    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dpSize, dpSize);
    shineButton.setLayoutParams(layoutParams);
  }

  @ReactProp(name = "value")
  public void setValue(FrameLayout shineButtonFrame, boolean on){
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setChecked(on);
  }

  @ReactProp(name = "disabled", defaultBoolean = false)
  public void setEnabled(FrameLayout shineButtonFrame, boolean disabled) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setEnabled(!disabled);
  }

  @ReactProp(name = "color")
  public void setColor(FrameLayout shineButtonFrame, String color) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setBtnColor(Color.parseColor(color));
  }

  @ReactProp(name = "fillColor")
  public void setFillColor(FrameLayout shineButtonFrame, String fillColor) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setBtnFillColor(Color.parseColor(fillColor));
  }

  @ReactProp(name = "bigShineColor")
  public void setBigShineColor(FrameLayout shineButtonFrame, String fillColor) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setBigShineColor(Color.parseColor(fillColor));
  }

  @ReactProp(name = "smallShineColor")
  public void setSmallShineColor(FrameLayout shineButtonFrame, String fillColor) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setSmallShineColor(Color.parseColor(fillColor));
  }

  @ReactProp(name = "allowRandomColor")
  public void setAllowRandomColor(FrameLayout shineButtonFrame, boolean random) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);
    shineButton.setEnabled(!random);
  }

  @ReactProp(name = "shape")
  public void setShape(FrameLayout shineButtonFrame, ReadableMap shapeProps) {
    ShineButton shineButton = (ShineButton) shineButtonFrame.getChildAt(0);

    String shape = null;
    Drawable drawable = null;

    if (shapeProps.hasKey("shape")) {
      shape = shapeProps.getString("shape");
    } else {
      drawable = generateVectorIcon(shapeProps);
      shape = shapeProps.getString("name");
    }

    switch (shape) {
      case SHAPE_HEART:
        shineButton.setShapeResource(R.raw.heart);
        break;
      case SHAPE_LIKE:
        shineButton.setShapeResource(R.raw.like);
        break;
      case SHAPE_SMILE:
        shineButton.setShapeResource(R.raw.smile);
        break;
      case SHAPE_STAR:
        shineButton.setShapeResource(R.raw.star);
        break;
      default:
        if (drawable != null) {
          shineButton.setShape(drawable);
        }
    }

    if (shineButton.isChecked()) {
      shineButton.setChecked(true);
    }
  }

  @TargetApi(21)
  private Drawable generateVectorIcon(ReadableMap icon) {
    try {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);

      String family = icon.getString("family");
      String name = icon.getString("name");
      String glyph = icon.getString("glyph");
      String color = icon.getString("color");
      int size = icon.getInt("size");

      if (name != null && name.length() > 0 && name.contains(".")) {
        Resources resources = this.reactContext.getResources();
        name = name.substring(0, name.lastIndexOf("."));

        final int resourceId = resources.getIdentifier(name, "drawable", this.reactContext.getPackageName());
        return this.reactContext.getDrawable(resourceId);
      }

      float scale = this.reactContext.getResources().getDisplayMetrics().density;
      String scaleSuffix = "@" + (scale == (int) scale ? Integer.toString((int) scale) : Float.toString(scale)) + "x";
      int fontSize = Math.round(size * scale);

      Typeface typeface = ReactFontManager.getInstance().getTypeface(family, 0, this.reactContext.getAssets());
      Paint paint = new Paint();
      paint.setTypeface(typeface);

      if (color != null && color.length() == 4) {
        color = color + color.substring(1);
      }

      if (color != null && color.length() > 0) {
        paint.setColor(Color.parseColor(color));
      }

      paint.setTextSize(fontSize);
      paint.setAntiAlias(true);
      Rect textBounds = new Rect();
      paint.getTextBounds(glyph, 0, glyph.length(), textBounds);

      Bitmap bitmap = Bitmap.createBitmap(textBounds.width(), textBounds.height(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawText(glyph, -textBounds.left, -textBounds.top, paint);

      return new BitmapDrawable(this.reactContext.getResources(), bitmap);
    } catch (Exception exception) {
      return null;
    }
  }
}
