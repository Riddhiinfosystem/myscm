package com.riddhi.myscm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.riddhi.myscm.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import support.ImageResizer;
import support.SharedValues;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Context mCon;
    WebSettings webSettings;
    ValueCallback<Uri> mUploadMessage;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private String companyCode = "", url;
    private Uri mCapturedImageURI = null;
    private String mCameraPhotoPath;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> mFilePathCallback;
    String sourceFilePath, version;
    int pd = 0;
    File newFile;
    String imageFileName, pdfFileName;
    private int webViewPreviousState;
    private final int PAGE_STARTED = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mCon = this;

        try {
            webSettings = binding.webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            //  binding.webView.setWebViewClient(new WebViewClient());
            final WebAppInterface myJavaScriptInterface
                    = new WebAppInterface(MainActivity.this);
            binding.webView.addJavascriptInterface(myJavaScriptInterface, "Android");

            webSettings.setBuiltInZoomControls(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            binding.webView.requestFocusFromTouch();
            binding.webView.setWebViewClient(new PQClient());
            binding.webView.setWebChromeClient(new PQChromeClient());
            binding.webView.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());

            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            companyCode = new SharedValues(mCon).loadSharedPreferences_CompanyCode();

            if (!companyCode.equals("")) {
                url = "http://www.myscm.co.in/" + companyCode + "?ccode=" + companyCode;
                //url = "http://192.168.0.73/"+companyCode+"?ccode="+companyCode;//192.168.0.73/
                Log.e("companyCode ", "url==> " + url);
            }

            binding.webView.clearCache(true);
            binding.webView.clearHistory();
            binding.webView.clearView();
            //binding.webView.destroy();
            binding.webView.loadUrl(url);

            binding.webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading File...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * Show a toast from the web page
         */
        @JavascriptInterface
        public void OpenNewWindow(String urlString) {
            //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.0.73/"+companyCode+"/"+urlString));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.myscm.co.in/" + companyCode + "/" + urlString));
            startActivity(browserIntent);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            boolean isResized = false;
            Uri defaultUri = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri[] results = null;

                // Check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        // If there is not data, then we may have taken a photo
                        if (mCameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        }
                    } else {
                        defaultUri = data.getData();
                        String dataString = data.getDataString();

                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }

                Uri[] resultArray = null;

                if (results != null)
                    resultArray = new Uri[results.length];

                if (results != null)
                    for (int i = 0; i < results.length; i++) {
                        try {
                            Uri uri = results[i];
                            String sourceImg = uri.getEncodedPath();
                            String destImg;

                            if (sourceImg.contains("/document/image")) {
                                destImg = sourceFilePath;
                                destImg = destImg.replace(".jpg", "_lower.jpg");
                            } else {
                                if (sourceImg.contains("DCIM") || sourceImg.contains("Camera")) {
                                    destImg = sourceImg.replace("DCIM/Camera", "MYSCM");
                                    destImg = destImg.replace(".jpg", "_lower.jpg");
                                } else {
                                    destImg = sourceImg.replace(".jpg", "_lower.jpg");
                                }
                            }

                            try {
                                if (data != null) {
                                    isResized = new ImageResizer(this).resizeImageBitmap(destImg, defaultUri, MainActivity.this);
                                } else {
                                    isResized = new ImageResizer(this).resizeImage(sourceImg, destImg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (isResized) {
                                File dfile = new File(destImg);
                                resultArray[i] = Uri.fromFile(dfile);

                                /*File dfile = convertToPdf(destImg);

                                resultArray[i] = Uri.fromFile(dfile);*/
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                if (resultArray != null && isResized) {
                    mFilePathCallback.onReceiveValue(resultArray);//resultArray
                    mFilePathCallback = null;
                } else {
                    mFilePathCallback.onReceiveValue(results);//resultArray
                    mFilePathCallback = null;
                }
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                if (requestCode == FILECHOOSER_RESULTCODE) {

                    if (null == this.mUploadMessage) {
                        return;
                    }

                    Uri result = null;

                    try {
                        if (resultCode != RESULT_OK) {
                            result = null;
                        } else {
                            // retrieve from the private variable if the intent is null
                            //result = data == null ? mCapturedImageURI : data.getData();
                            if (data == null) {
                                // If there is not data, then we may have taken a photo
                                if (mCapturedImageURI != null) {
                                    result = mCapturedImageURI;
                                    defaultUri = mCapturedImageURI;
                                }
                            } else {
                                defaultUri = data.getData();
                                String dataString = data.getDataString();
                                if (dataString != null) {
                                    result = Uri.parse(dataString);
                                }
                            }

                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "activity :" + e,
                                Toast.LENGTH_LONG).show();
                    }

                    try {
                        String sourceImg = result.getEncodedPath();
                        String destImg = sourceImg.replace(".jpg", "_lower.jpg");

                        if (data != null) {
                            destImg = new ImageResizer(this).getRealPathFromURI_API11to18(MainActivity.this, defaultUri);
                            destImg = destImg.replace(".jpg", "_lower.jpg");
                            isResized = new ImageResizer(this).resizeImageBitmap(destImg, defaultUri, MainActivity.this);
                        } else {
                            isResized = new ImageResizer(this).resizeImage(sourceImg, destImg);
                        }

                        if (isResized) {
                             File dfile = new File(destImg);
                            result = Uri.fromFile(dfile);
                            /*File dfile = convertToPdf(destImg);
                            result = Uri.fromFile(dfile);*/


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
            }
        } catch (Exception e) {
            Log.e("exception", e.getMessage());
        }
    }


    //---developed by vaibhav050421
    private File convertToPdf(String destImg) {
        File dfile = null;
        Bitmap bitmap;
        String srcImg;
        try {
            File f = new File(destImg);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);

            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();

            PdfDocument.Page page = pdfDocument.startPage(myPageInfo);
            Canvas canvas = page.getCanvas();
            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
            canvas.drawBitmap(bitmap, 0, 0, null);
            pdfDocument.finishPage(page);

            File root = new File(Environment.getExternalStorageDirectory(), "PDF FOLDER");

            if (!root.exists()) {
                root.mkdir();
            }
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            pdfFileName = "IMG_" + timeStamp + ".pdf";

            File file = new File(root, pdfFileName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                pdfDocument.writeTo(fileOutputStream);
                String sts = file.getAbsolutePath();
                Uri result;
                result = Uri.parse(sts);
                srcImg = result.getEncodedPath();
                dfile = new File(srcImg);

            } catch (Exception e) {
                e.printStackTrace();
            }

            pdfDocument.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dfile;
    }

    public class PQChromeClient extends WebChromeClient {

        // For Android 5.0
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
            // Double check that we don't have any existing callbacks
            //mFilePathCallback = null;
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }

            mFilePathCallback = filePath;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    sourceFilePath = photoFile.getAbsolutePath();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    //  Log.e("UnableCreate_Image_File", ex.toString());
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

            mUploadMessage = uploadMsg;

            // Create AndroidExampleFolder at sdcard
            File imageStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES)
                    , "MYSCM");

            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs();
            }

            // Create camera captured image file path and name
            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + System.currentTimeMillis()
                            + ".jpg");

            mCapturedImageURI = Uri.fromFile(file);

            // Camera capture image intent
            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            // Create file chooser intent
            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[]{captureIntent});

            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
        }

        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {

            openFileChooser(uploadMsg, acceptType);
        }
    }

    public class PQClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.clearCache(true);
            view.clearHistory();
            if (url != null && !url.contains(" ")) {
                view.loadUrl(url);
                return true;
            } else {
                view.loadUrl(url);
                return false;
            }
        }

        Dialog loadingDialog = new Dialog(MainActivity.this);

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webViewPreviousState = PAGE_STARTED;

            if (loadingDialog == null || !loadingDialog.isShowing())
                loadingDialog = ProgressDialog.show(MainActivity.this, "",
                        "Loading Please Wait", true, true,
                        dialog -> {
                            // do something
                        });

            loadingDialog.setCancelable(false);
        }


       /* @Override
        public void onLoadResource(WebView view, String url) {
          *//*  if (progressDialog == null && pd == 0) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
            //  Log.d("url", "=========" + url);
            binding.progress.setVisibility(View.GONE);*//*
            if (webViewPreviousState == PAGE_STARTED) {

                if (null != loadingDialog) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        }*/

        // Called when all page resources loaded
        public void onPageFinished(WebView view, String url) {

            if (webViewPreviousState == PAGE_STARTED) {

                if (null != loadingDialog) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Exit!")
                            .setMessage("Are you sure you want to close?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                finish();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "IMG_" + timeStamp + ".jpg";

        File storageDir = new File(Environment.getExternalStorageDirectory() + "/MYSCM");
        if (!storageDir.exists())
            storageDir.mkdirs();

        newFile = new File(storageDir, imageFileName);

        return newFile;
    }

}