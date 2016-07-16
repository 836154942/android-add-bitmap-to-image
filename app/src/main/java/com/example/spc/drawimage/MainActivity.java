package com.example.spc.drawimage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.spc.drawimage.view.ColorPickerView;
import com.example.spc.drawimage.view.StickerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mBackView;//背景图
    private StickerView mStickerView;//添加贴画的图层
    private AlertDialog mTextDialog;//输入文本dialog
    private EditText mDialogInput;//输入文本的ed
    private int mTextColor = Color.BLACK;//最终的text的颜色
    int defaultHeight, defaultWidth;// 屏幕宽高
    private View mDrawImage;//最终保存图片区域

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display currentDisplay = getWindowManager().getDefaultDisplay();
        defaultHeight = currentDisplay.getHeight();
        defaultWidth = currentDisplay.getWidth();
        initView();
    }

    private void initView() {
        mDrawImage = findViewById(R.id.create_iamge);
        mStickerView = (StickerView) findViewById(R.id.id_stickerew);
        mBackView = (ImageView) findViewById(R.id.im_back);
        findViewById(R.id.add_image).setOnClickListener(this);
        findViewById(R.id.add_text).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        setImage();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.add_image) {
            //add image
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mStickerView.addBitImage(imageBitmap);
        } else if (v.getId() == R.id.add_text) {
            //add text
            createTextDialog();
        } else if (v.getId() == R.id.btn_save) {

            mStickerView.prepareSave();
            mDrawImage.setDrawingCacheEnabled(true);
            mDrawImage.buildDrawingCache();
            Bitmap bm = mDrawImage.getDrawingCache();
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight());
            mDrawImage.destroyDrawingCache();
            try {
            Uri uri = saveBitmap2File(bm, ".jpg");
                Toast.makeText(MainActivity.this, "保存成功，路径"+uri.getPath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    //输入文本的dialog
    private void createTextDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_inputext_drawimage, null);
        mDialogInput = (EditText) view.findViewById(R.id.ed_dialog_input);
        ColorPickerView colorPickerView = new ColorPickerView(this, defaultWidth / 2, defaultWidth / 2);
        colorPickerView.setmListener(colorChangeListenre);
        view.addView(colorPickerView);
        mTextDialog = builder.setView(view).setTitle("请输入内容").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = mDialogInput.getText().toString();
                        DisplayMetrics dm = new DisplayMetrics();
                        dm = getResources().getDisplayMetrics();
                        float density = dm.density;  // 屏幕密度
                        Bitmap bitmap = Bitmap.createBitmap((int) (text.length() * density * 35),
                                (int) (70 * density), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();
                        paint.setColor(mTextColor);
                        paint.setTextSize(35 * density);
                        canvas.drawText(text, 0, 35 * density, paint);
                        mStickerView.addBitImage(bitmap);
                    }
                }).setNegativeButton("取消", null).create();
        mTextDialog.show();


    }

    ColorPickerView.OnColorChangedListener colorChangeListenre = new ColorPickerView.OnColorChangedListener() {
        @Override
        public void colorChanged(int color) {
            mTextColor = color;
            mDialogInput.setTextColor(color);
        }
    };


    //加载图片
    public void setImage() {
        Bitmap bitmap;
//由于返回的图像可能太大而无法完全加载到内存中。系统有限制，需要处理。
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inJustDecodeBounds = true;///只是为获取原始图片的尺寸，而不返回Bitmap对象
        try {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.big_image, bitmapFactoryOptions);

            int outHeight = bitmapFactoryOptions.outHeight;
            int outWidth = bitmapFactoryOptions.outWidth;
            int heightRatio = (int) Math.ceil((float) outHeight / defaultHeight);
            int widthRatio = (int) Math.ceil((float) outWidth / defaultWidth);

            if (heightRatio > 1 || widthRatio > 1) {
                if (heightRatio > widthRatio) {
                    bitmapFactoryOptions.inSampleSize = heightRatio;
                } else {
                    bitmapFactoryOptions.inSampleSize = widthRatio;
                }
            }
            bitmapFactoryOptions.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.big_image, bitmapFactoryOptions);
            mBackView.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //保存图片
    public Uri saveBitmap2File(Bitmap bmp, String mimeType) throws IOException {
        String format = ".jpg";
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        if (mimeType.contains("png")) {
            format = ".png";
            compressFormat = Bitmap.CompressFormat.PNG;
        }
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() +
                File.separator;
        String fileName = dir + System.currentTimeMillis() + format;
        OutputStream stream = new FileOutputStream(fileName);
        bmp.compress(compressFormat, 100, stream);
        stream.flush();
        stream.close();
        return Uri.fromFile(new File(fileName));
    }
}
