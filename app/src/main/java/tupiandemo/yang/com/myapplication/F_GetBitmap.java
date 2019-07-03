package tupiandemo.yang.com.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class F_GetBitmap {
    public static Bitmap bitmap = null;
    public static String filePath;
    public static String picFilePath;
    public static File file;
    public static boolean isEmpty(String picName) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            filePath = Environment.getExternalStorageDirectory()
                    + "/sign";
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            picFilePath = Environment.getExternalStorageDirectory().toString()
                    + "/sign" + "/" + picName;
            file = new File(picFilePath);
            if (file.exists()) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public static Bitmap getSDBitmap(String picName) {
        picFilePath = Environment.getExternalStorageDirectory()+ "/sign" + "/" + picName;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = null;
        options.inSampleSize = 1;
        Bitmap bit = BitmapFactory.decodeFile(picFilePath, options);
        return bit;
    }

    public static void setInSDBitmap(byte[] bb, String picName) {
        filePath = Environment.getExternalStorageDirectory()+ "/sign/";
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        input = new ByteArrayInputStream(bb);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
        bitmap = (Bitmap) softRef.get();
        if (bb != null) {
            bb = null;
        }try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
        FileOutputStream fos = null;
        file = new File(filePath + "/" + picName);
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            bitmap.recycle();
            System.gc();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmap(String path) throws IOException{
        try {
            URL url=new URL(path);
            HttpURLConnection conn=(HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            Bitmap bitmap = null;
            if(conn.getResponseCode()==200) {
                InputStream inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);

                // 压缩图片
                Matrix matrix = new Matrix();
                matrix.setScale(0.5f, 0.5f);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                return bitmap;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromUri(Uri uri, Activity activity) throws IOException {
        InputStream inputStream = null;
        inputStream = activity.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        if (originalWidth == -1 || originalHeight == -1) {
            return null;
        }

        float height = 800f;
        float width = 480f;
        int be = 1; //be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > width) {
            be = (int) (originalWidth / width);
        } else if (originalWidth < originalHeight && originalHeight > height) {
            be = (int) (originalHeight / height);
        }

        if (be <= 0) {
            be = 1;
        }
        BitmapFactory.Options bitmapOptinos = new BitmapFactory.Options();
        bitmapOptinos.inSampleSize = be;
        bitmapOptinos.inDither = true;
        bitmapOptinos.inPreferredConfig = Bitmap.Config.ARGB_8888;
        inputStream = activity.getContentResolver().openInputStream(uri);

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptinos);
        inputStream.close();

//        return compressImage(bitmap);
        return bitmap;
    }

    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        int options = 100;
        while (byteArrayOutputStream.toByteArray().length / 1024 > 100) {
            byteArrayOutputStream.reset();
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差 ，第三个参数：保存压缩后的数据的流
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, byteArrayOutputStream);
            options -= 10;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        Bitmap bitmapImage = BitmapFactory.decodeStream(byteArrayInputStream, null, null);
        return bitmapImage;
    }
}

