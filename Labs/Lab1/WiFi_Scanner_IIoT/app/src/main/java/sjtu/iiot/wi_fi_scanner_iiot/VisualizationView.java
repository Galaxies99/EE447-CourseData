package sjtu.iiot.wi_fi_scanner_iiot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;

class VisualizationView extends View {

    public Canvas canvas;
    public Paint p;
    private Bitmap bitmap;
    private Intent getIntent;

    private final String[] FileNameGroup = {"SJTU", "406", "408", "507", "508"};
    private double[] x_WiFi = {0, -3.6, 4.5, 2.3, 3.2};
    private double[] y_WiFi = {0, 0.4, -2.3, 1.9, -5.2};
    private final int NumberOfWifi = FileNameGroup.length;
    private double positionX, positionY;

    int width, height;
    double padding_x, padding_y;
    int bgColor;

    private boolean[] isValid;
    private double[] radius;

    final int EDGE_HEIGHT = 1500;
    final int EDGE_WIDTH = 900;
    final int PIXELS_PER_METER = 30;

    public VisualizationView(Context context, Intent getIntent) {
        super(context);

        DisplayMetrics dm2 = getResources().getDisplayMetrics();
        width = dm2.widthPixels;
        height = dm2.heightPixels;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        canvas=new Canvas();
        canvas.setBitmap(bitmap);
        p = new Paint(Paint.DITHER_FLAG);
        p.setAntiAlias(true);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(8);

        this.getIntent = getIntent;
        initParameter();
        Draw();
        makeLabels();
    }

    // [-10, 10] range
    private void initParameter() {
        bgColor = Color.WHITE;
        padding_x = (double)(width - EDGE_WIDTH) / 2;
        padding_y = 50;
        radius = getIntent.getDoubleArrayExtra("radius");
        isValid = getIntent.getBooleanArrayExtra("isValid");
        assert(isValid != null);
        for (int index = 1; index <= NumberOfWifi; index++) {
            x_WiFi[index - 1] = padding_x + (x_WiFi[index - 1] + 10.0) * PIXELS_PER_METER;
            y_WiFi[index - 1] = padding_y + (y_WiFi[index - 1] + 10.0) * PIXELS_PER_METER;
            if (isValid[index - 1])
                radius[index - 1] *= PIXELS_PER_METER;
        }
        positionX = padding_x + (10.0 + getIntent.getDoubleExtra("positionX", 0)) * PIXELS_PER_METER;
        positionY = padding_y + (10.0 + getIntent.getDoubleExtra("positionY", 0)) * PIXELS_PER_METER;
    }

    private void Draw() {
        // background
        p.setColor(bgColor);
        canvas.drawRect(0, 0, width, height, p);
        p.setStyle(Paint.Style.STROKE);

        int color[] = {Color.YELLOW, Color.GREEN, Color.BLUE, Color.BLACK, Color.MAGENTA};

        for (int index = 1; index <= NumberOfWifi; index++) {
            if (!isValid[index - 1]) continue;
            p.setColor(color[index - 1]);
            p.setStrokeWidth(16);
            canvas.drawPoint((float)x_WiFi[index - 1], (float)y_WiFi[index - 1], p);
            p.setStrokeWidth(8);
            canvas.drawCircle((float)x_WiFi[index - 1], (float)y_WiFi[index - 1], (float)radius[index - 1], p);
        }

        p.setStrokeWidth(24);
        p.setColor(Color.RED);
        canvas.drawPoint((float)positionX, (float)positionY, p);

        p.setStrokeWidth(8);
        p.setColor(Color.BLACK);
        canvas.drawRect((float)padding_x, (float)padding_y, (float)(padding_x + EDGE_WIDTH), (float)(padding_y + EDGE_HEIGHT), p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(bgColor);
        canvas.drawRect(0, 0, width, (float)padding_y, p);
        canvas.drawRect(0, 0, (float)padding_x, height, p);
        canvas.drawRect(0, (float)(padding_y + EDGE_HEIGHT), width, height, p);
        canvas.drawRect((float)(padding_x + EDGE_WIDTH), 0, width, height, p);
    }

    private void makeLabels() {
        p.setColor(Color.BLACK);
        p.setTextSize(40);
        for (int index = 1; index <= NumberOfWifi; index++) {
            if (!isValid[index - 1]) continue;
            canvas.drawText(FileNameGroup[index - 1], (float) (x_WiFi[index - 1] + 20), (float) (y_WiFi[index - 1] - 20), p);
        }

        String pos = "(" + getIntent.getDoubleExtra("positionX", 0) +
                "," + getIntent.getDoubleExtra("positionY", 0) + ")";
        canvas.drawText(pos, (float)padding_x + 50, EDGE_HEIGHT - 100, p);
    }

    @Override
    public void onDraw(Canvas c) {
        c.drawBitmap(bitmap, 0, 0, null);
    }
}