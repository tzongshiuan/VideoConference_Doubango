package com.gorilla.vc.view.ui.concall.whiteboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.gorilla.vc.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * DrawingView: Custom view that handles the drawing for the whiteboard
 */
public class DrawingView extends View {
    private static final int PEN_MODE = 0;
    private static final int ERASER_MODE = 1;
    private static final int TEXT_MODE = 2;
    private int drawingMode = PEN_MODE;

    private int currentColor = 0;      // Current pen color
    private int viewWidth = 0;         // Width of the screen in pixels

    // Objects to handle painting
    private Canvas drawCanvas;
    private Path drawPath = new Path();
    private Paint drawPaint = new Paint();
    private Paint drawTextPaint = new Paint();
    private Paint canvasPaint = new Paint(Paint.DITHER_FLAG);
    private Bitmap canvasBitmap;

    public static final int COLOR_BLACK  = 0;
    public static final int COLOR_GRAY   = 2;
    public static final int COLOR_WHITE  = 17;
    public static final int COLOR_RED    = 1;
    public static final int COLOR_ORANGE = 15;
    public static final int COLOR_YELLOW  = 5;
    public static final int COLOR_GREEN  = 8;
    public static final int COLOR_BLUE   = 11;
    public static final int COLOR_INDIGO = 16;
    public static final int COLOR_PURPLE = 14;


    // Colors for the pen
    private int[] colorIds = {
            R.color.black,
            R.color.red,
            R.color.gray,
            R.color.maroon,
            R.color.silver,
            R.color.yellow,
            R.color.olive,
            R.color.lime,
            R.color.green,
            R.color.aqua,
            R.color.teal,
            R.color.blue,
            R.color.navy,
            R.color.fuchsia,
            R.color.purple,
            R.color.orange,
            R.color.indigo,
            R.color.white};
    int numColors = colorIds.length;

    private int[] colors = new int[numColors];

    private int mPencilWidth = 3;
    private int mEraserWidth = 20;

    public static final int TEXT_SIZE_SMALL  = 40;
    public static final int TEXT_SIZE_MEDIUM = 80;
    public static final int TEXT_SIZE_BIG    = 160;
    private int mTextSize = TEXT_SIZE_MEDIUM;

    private String mInputText = "";

    // JSON Objects
    private JSONObject jsonObject = new JSONObject();
    private ArrayList<String> history = new ArrayList<>();
    private ArrayList<PointF> points = new ArrayList<>();

    private PointF textUpperLeftPoint = new PointF();

    // Reference to the associated WhiteboardActivity
    private WhiteBoardFragment fragment;

    private String TAG = WhiteBoardFragment.class.getSimpleName(); // TAG for logging

    /**
     * Constructor
     *
     * @param context the current Activity context
     * @param attrs   attribute set
     */
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    /**
     * Initialize objects necessary for drawing
     */
    private void setupDrawing() {
        int dp = 3; // Initial pen width

        // Make pixel width density independent
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);

        // Set initial parameters
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(pixelWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        drawTextPaint.setColor(Color.BLACK);
        drawTextPaint.setStrokeWidth(pixelWidth);
        drawTextPaint.setTextAlign(Paint.Align.LEFT);
        drawTextPaint.setTextSize(mTextSize);
    }

    /**
     * Overriding the onSizeChanged method of the View class
     *
     * @param w    new width
     * @param h    new height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Store view width for later
        viewWidth = w;

        // Make sure the height is 6/5 times the width
        this.getLayoutParams().height = h;// (int) (6 / 5f * viewWidth);

        // Create bitmap for the drawing
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    /**
     * Overrriding the onDraw method of the View class
     *
     * @param canvas the canvas to draw the view
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

        if (drawingMode == PEN_MODE || drawingMode == ERASER_MODE) {
            canvas.drawPath(drawPath, drawPaint);
        } else if (drawingMode == TEXT_MODE && isTextDrawing) {
            if (mInputText.isEmpty()) {
                canvas.drawText("|", textUpperLeftPoint.x, textUpperLeftPoint.y, drawTextPaint);
            } else {
                drawTextOnCanvas(canvas);
            }
        }
    }

    private void drawTextOnCanvas(Canvas canvas) {
        String[] lines = mInputText.split("\n");

        if (lines.length == 1) {
            canvas.drawText(mInputText, textUpperLeftPoint.x, textUpperLeftPoint.y, drawTextPaint);
        } else {
            int heightOffset = 0;
            for (String s: lines) {
                canvas.drawText(s, textUpperLeftPoint.x, textUpperLeftPoint.y + heightOffset, drawTextPaint);
                heightOffset += mTextSize;
            }
        }
    }

    /**
     * Create link back to the fragment
     *
     * @param WhiteBoardFragment reference to the associated fragment
     */
    public void setFragment(WhiteBoardFragment WhiteBoardFragment) {
        this.fragment = WhiteBoardFragment;

        if (fragment.getContext() != null) {
            for (int i = 0; i < numColors; i++) {
                colors[i] = ContextCompat.getColor(fragment.getContext(), colorIds[i]);
            }
        }
    }

    private boolean isTextDrawing = false;
    private void startEditInputText() {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }

        isTextDrawing = true;
        fragment.startEditInputText();
    }

    public void stopEditInputText() {
        isTextDrawing = false;
    }

    public void setInputTextContent(String text) {
        //Log.d(TAG, "setInputTextContent(), text: " + text);
        this.mInputText = text;
        invalidate();
    }

    public void commitDrawingText() {
        Log.d(TAG, "commitDrawingText()");
        drawTextOnCanvas(drawCanvas);

        try {
            jsonObject = new JSONObject();
            jsonObject.put("color", currentColor);
            jsonObject.put("type", "text");
            jsonObject.put("size", mTextSize);
            jsonObject.put("x", textUpperLeftPoint.x);
            jsonObject.put("y", textUpperLeftPoint.y);
            jsonObject.put("textContent", mInputText);
            String jString = jsonObject.toString();
            history.add(jString);
            fragment.callback(jString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overriding onTouchEvent method of the View class
     *
     * @param event the current touch event
     * @return true because event was handled
     */
    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Sending coordinates only to 3 decimal values
        DecimalFormat df = new DecimalFormat("#.###");

        // Create the JSON object to be sent and fill relevant fields
        try {
            jsonObject = new JSONObject();
            switch (drawingMode) {
                case PEN_MODE:
                    jsonObject.put("type", "pen");
                    jsonObject.put("width", mPencilWidth);
                    break;
                case ERASER_MODE:
                    jsonObject.put("type", "eraser");
                    jsonObject.put("width", mEraserWidth);
                    break;
                case TEXT_MODE:
                    break;
                default:
            }
            if (drawingMode != ERASER_MODE) {
                jsonObject.put("color", currentColor);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get the coordinates of the touch
        float touchX = event.getX();
        float touchY = event.getY();

        if (drawingMode == TEXT_MODE) {
            textUpperLeftPoint.x = touchX;
            textUpperLeftPoint.y = touchY;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isTextDrawing) {
                        startEditInputText();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    invalidate();
                    break;
            }
        } else {
            // Handle the type of event
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // First point in the stroke being drawn
                    // Position start of curve to position
                    drawPath.moveTo(touchX, touchY);

                    // Add point to the arraylist of points in this curve
                    points.add(new PointF(touchX, touchY));
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Handle point somewhere in middle of the stroke
                    drawPath.lineTo(touchX, touchY);

                    // Add point to the arraylist of points in current curve
                    points.add(new PointF(touchX, touchY));
                    break;

                case MotionEvent.ACTION_UP:
                    // Handle last point in the current stroke
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    try {
                        JSONArray coordinates = new JSONArray();
                        for (PointF p : points) {
                            JSONArray ja = new JSONArray();
                            ja.put(df.format(p.x / viewWidth));
                            ja.put(df.format(p.y / viewWidth));
                            coordinates.put(ja);
                        }
                        jsonObject.put("coordinates", coordinates);
                        points.clear();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jString = jsonObject.toString();
                    history.add(jString);
                    fragment.callback(jString);
                    break;

                default:
                    return false;
            }
        }

        invalidate(); // Trigger a redraw of the view
        return true;  // Because event has been handled
    }

    /**
     * Enable pencil button
     */
    public void setPencil() {
        setPenColor(currentColor);
        drawingMode = PEN_MODE;
    }

    /**
     * Enable eraser button
     */
    public void setEraser() {
        int dp = this.mEraserWidth; // Default size of eraser

        // Make eraser width density independent
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);

        // Set values to enable erasing
        //fragment.setButtonColor(Color.WHITE, COLOR_WHITE);
        drawPaint.setColor(Color.WHITE);
        drawPaint.setStrokeWidth(pixelWidth);
        drawingMode = ERASER_MODE;
    }

    /**
     * Enable user to input text
     */
    public void setInputText() {
        setTextColor(currentColor);
        drawingMode = TEXT_MODE;
    }

    void setPencilWidth(int width) {
        this.mPencilWidth = width;

        setPenColor(currentColor);
    }

    void setEraserWidth(int width) {
        this.mEraserWidth = width;

        if (drawingMode == ERASER_MODE) {
            setEraser();
        }
    }

    void setInputTextSize(int size) {
        this.mTextSize = size;

        setTextColor(currentColor);
    }

    // Change color of the pen
    void setColor(int c) {
        if (drawingMode == PEN_MODE) {
            setPenColor(c);
        } else if (drawingMode == TEXT_MODE) {
            setTextColor(c);
        }
    }

    void setPenColor(int c) {
        if (fragment == null || fragment.getContext() == null) {
            return;
        }

        int dp = this.mPencilWidth; // Default width of the pen

        // Make width density independent
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float pixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);

        currentColor = c;
        fragment.setButtonColor(colors[currentColor], c);
        drawPaint.setColor(colors[currentColor]);
        drawPaint.setStrokeWidth(pixelWidth);
    }

    // Change color of text
    void setTextColor(int c) {
        if (fragment == null || fragment.getContext() == null) {
            return;
        }

        currentColor = c;
        fragment.setButtonColor(colors[currentColor], c);
        drawTextPaint.setColor(colors[currentColor]);
        drawTextPaint.setTextSize(mTextSize);
    }

    public void getAllColors() {
        fragment.showAllColorOptions(colors, currentColor);
    }

    /**
     * Increment color of the pen
     */
    public void incrementColor() {
        if (drawingMode != ERASER_MODE && ++currentColor > numColors - 1) {
            currentColor = 0;
        }
        setColor(currentColor);
    }

    /**
     * Function to handle undo
     */
    public void undo() {
        // If history is empty, ignore
        if (history.isEmpty()) {
            Toast.makeText(fragment.getContext(), fragment.getString(R.string.no_more_history), Toast.LENGTH_SHORT).show();
            return;
        }

        // If history is not empty, go through it and delete last action by this user
        history.remove((history.size() - 1));
        try {
            jsonObject = new JSONObject();
            jsonObject.put("type", "undo");
            fragment.callback(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Erase canvas
        drawCanvas.drawColor(Color.WHITE);

        invalidate(); // Trigger redraw of the view


        int originDrawingMode = drawingMode;
        int originColor = currentColor;
        int originPenWidth = mPencilWidth;
        int originEraserWidth = mEraserWidth;
        int originTextSize = mTextSize;

        for (String str : history) {
            parseJSON(str, false);  //Re-perform all the actions in history
        }

        mPencilWidth = originPenWidth;
        mEraserWidth = originEraserWidth;
        mTextSize = originTextSize;
        mInputText = "";

        currentColor = originColor;
        drawingMode = originDrawingMode;
        switch(drawingMode) {
            case PEN_MODE:
                setPencil();
                break;
            case ERASER_MODE:
                setEraser();
                break;
            case TEXT_MODE:
                setInputText();
                break;
            default:
        }
    }

    /**
     * Function to clear the whiteboard
     */
    public void clear() {
        history.clear();
        drawCanvas.drawColor(Color.WHITE);
        invalidate();
        try {
            jsonObject = new JSONObject();
            jsonObject.put("type", "clear");
            fragment.callback(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper Function to parse and draw the action mentioned in the passed JSON string
     *
     * @param string the json representation of the action to be performed
     */
    public void callback(String string) {
        parseJSON(string, true);
    }

    /**
     * Function to parse and draw the action mentioned in the passed JSON string
     *
     * @param string       the json representation of the action to be performed
     * @param addToHistory if true, add the action to history
     */
    public void parseJSON(String string, boolean addToHistory) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            try {
                int colorBefore = currentColor;
                boolean isEraserBefore = (drawingMode == ERASER_MODE);
                String type = jsonObject.get("type").toString();

                Log.d(TAG, "parseJSON(), type = " + type);
                switch (type) {
                    case "pen":
                        // If type pen, extract color
                        currentColor = jsonObject.getInt("color");
                        mPencilWidth = jsonObject.getInt("width");
                        setPencil();
                        break;
                    case "eraser":
                        mEraserWidth = jsonObject.getInt("width");
                        setEraser();
                        break;
                    case "text":
                        currentColor = jsonObject.getInt("color");
                        mTextSize = jsonObject.getInt("size");
                        textUpperLeftPoint.x = (float) jsonObject.getDouble("x");
                        textUpperLeftPoint.y = (float) jsonObject.getDouble("y");
                        mInputText = jsonObject.getString("textContent");
                        setInputText();
                        break;
                    case "undo":
                        if (history.isEmpty()) {
                            return;
                        }
                        String userStr = jsonObject.getString("user");
                        for (int i = history.size() - 1; i >= 0; i--) {
                            if (history.get(i).contains("\"user\":\"" + userStr + "\"")) {
                                history.remove(i);
                                break;
                            }
                        }
                        drawCanvas.drawColor(Color.WHITE);
                        invalidate();

                        for (String str : history) {
                            parseJSON(str, false);
                        }
                        break;
                    case "clear":
                        history.clear();
                        drawCanvas.drawColor(Color.WHITE);
                        invalidate();
                        break;
                    default:
                        throw new JSONException("Unrecognized string: " + string);
                }

                if (type.equals("pen") || type.equals("eraser")) {
                    JSONArray coordinates = jsonObject.getJSONArray("coordinates");
                    JSONArray startPoint = coordinates.getJSONArray(0);
                    Path drawPath = new Path();
                    float touchX = (float) startPoint.getDouble(0) * viewWidth;
                    float touchY = (float) startPoint.getDouble(1) * viewWidth;
                    drawPath.moveTo(touchX, touchY);
                    for (int i = 1; i < coordinates.length(); i++) {
                        JSONArray point = coordinates.getJSONArray(i);
                        float x = (float) point.getDouble(0) * viewWidth;
                        float y = (float) point.getDouble(1) * viewWidth;
                        drawPath.lineTo(x, y);
                    }
                    drawCanvas.drawPath(drawPath, drawPaint);
                    invalidate();
                    drawPath.reset();
                    if (addToHistory) {
                        history.add(string);
                    }
                    if (isEraserBefore) {
                        setEraser();
                    } else {
                        setColor(colorBefore);
                    }
                } else if (type.equals("text")) {
                    drawCanvas.drawText(mInputText, textUpperLeftPoint.x, textUpperLeftPoint.y, drawTextPaint);
                }
            } catch (JSONException e) {
                Log.d(TAG, "JSON string error: " + string);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
