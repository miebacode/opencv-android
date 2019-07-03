package tupiandemo.yang.com.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private EditText  bianji;
    private Button   submit;
    private Bitmap pic=null;
    private final int CODE_FOR_STORAGE = 1;
    private Bitmap bitmap;
    // opencv相关功能实现所需要的so库
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        bianji=findViewById(R.id.name);
        submit=findViewById(R.id.button8);
        imageView.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView:
//                Toast.makeText(this, "点击上传图片", Toast.LENGTH_SHORT).show();
                selectheader();
                break;
            case R.id.button8:
                blurImage(bitmap);
                isBlurByOpenCV(bitmap);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(this,"点击提交按钮",Toast.LENGTH_SHORT).show();
        }
    }
    //开始从相册选择图片
    public void selectheader() {
        initPermission();
    }
    //设置相册和手机存储权限
    private void initPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CODE_FOR_STORAGE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CODE_FOR_STORAGE);
                Toast.makeText(MainActivity.this, "请设置相应权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            selectImage();
        }
    }


    //设置确认获取权限后动作
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODE_FOR_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        }
    }
    //从相册选择图片
    private void selectImage() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1.9);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 1);
    }
    //根据选择的图片进行在页面的显示和获取图片的byte[]用于上传使用
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
//                    Bitmap bitmap = null;
                    Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmap = F_GetBitmap.getBitmapFromUri(uri, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (data.getExtras() != null) {
                        bitmap = data.getExtras().getParcelable("data");
                    }

                    if (bitmap == null) {
                        Toast.makeText(this, "获取图片失败", Toast.LENGTH_LONG).show();
                    } else {
                        pic = bitmap;
                        imageView.setImageBitmap(bitmap);
                        isBlurByOpenCV(bitmap);
                    }
                }
            }
        }
    }
    public static boolean isBlurByOpenCV(Bitmap image) {
        System.out.println("image.w=" + image.getWidth() + ",image.h=" + image.getHeight());
        int l = CvType.CV_8UC1; //8-bit grey scale image
        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY); // 图像灰度化

        Bitmap destImage;
        destImage = Bitmap.createBitmap(image);
        Mat dst2 = new Mat();
        Utils.bitmapToMat(destImage, dst2);
        Mat laplacianImage = new Mat();
        dst2.convertTo(laplacianImage, l);
        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U); // 拉普拉斯变换
        Mat laplacianImage8bit = new Mat();
        laplacianImage.convertTo(laplacianImage8bit, l);

        Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(), laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(laplacianImage8bit, bmp);
        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight()); // bmp为轮廓图

        int maxLap = -16777216; // 16m
        for (int pixel : pixels) {
            if (pixel > maxLap)
                maxLap = pixel;
        }
        int userOffset = -3881250; // 界线（严格性）降低一点
        int soglia = -6118750 + userOffset; // -6118750为广泛使用的经验值
        System.out.println("maxLap=" + maxLap);
        if (maxLap <= soglia) {
            System.out.println("这是一张模糊图片");
        }
        System.out.println("==============================================\n");
        soglia += 6118750 + userOffset;
        maxLap += 6118750 + userOffset;

        Log.d("清晰信息：","opencvanswers..result：image.w=" + image.getWidth() + ", image.h=" + image.getHeight()
                + "\nmaxLap= " + maxLap + "(清晰范围:0~" + (6118750 + userOffset) + ")"
                + "\n" + Html.fromHtml("<font color='#eb5151'><b>" + (maxLap <= soglia ? "模糊" : "清晰") + "</b></font>"));
        return maxLap <= soglia;
    }

    public static boolean isBlurByOpenCV(String picFilePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 通过path得到一个不超过2000*2000的Bitmap
        Bitmap image = decodeSampledBitmapFromFile(picFilePath, options, 2000, 2000);
        return isBlurByOpenCV(image);
    }

    public static Bitmap decodeSampledBitmapFromFile(String imgPath, BitmapFactory.Options options, int reqWidth, int reqHeight) {
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);
        // inSampleSize为缩放比例，举例：options.inSampleSize = 2表示缩小为原来的1/2，3则是1/3，以此类推
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgPath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
        System.out.println("inSampleSize=" + inSampleSize);
        return inSampleSize;
    }
    //传入任意bitmap, 返回模糊过后的bitmap
    private Bitmap blurImage(Bitmap origin) {
        Mat mat = new Mat();
        Utils.bitmapToMat(origin, mat);
        Imgproc.GaussianBlur(mat, mat, new Size(15, 15), 0);
        Utils.matToBitmap(mat, origin);
        return origin;
    }
}
