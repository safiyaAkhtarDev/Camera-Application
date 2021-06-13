package com.android.cameraapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.android.cameraapplication.picker.ImageContract;
import com.android.cameraapplication.picker.ImagePresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageContract.View {

    static final int REQUEST_GALLERY_PHOTO = 102;
    static final int REQUEST_CROP_PHOTO = 103;
    static String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    AppCompatButton btn_select_image;
    AppCompatButton btn_save;
    AppCompatButton btn_cancel;
    LinearLayout layout_save;
    AppCompatImageView img_info;
    AppCompatImageView img_crop;
    AppCompatImageView img_rotate_horizontal;
    AppCompatImageView img_rotate_vertical;
    AppCompatImageView img_SelectedImage;
    ConstraintLayout cl_function;
    private ImagePresenter mPresenter;
    Uri photoURI;

    boolean flipvertical = false, fliphorizontal = false;

    private String exif_DATETIME = "";
    private String exif_FLASH = "";
    private String exif_FOCAL_LENGTH = "";
    private String exif_IMAGE_LENGTH = "";
    private String exif_IMAGE_WIDTH = "";
    private String exif_MODEL = "";
    private String exif_ORIENTATION = "";
    private String mFileName;
    private File galleryFile;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter = new ImagePresenter(this);
        btn_select_image = findViewById(R.id.btn_select_image);
        img_crop = findViewById(R.id.img_crop);
        img_info = findViewById(R.id.img_info);
        img_rotate_horizontal = findViewById(R.id.img_rotate_horizontal);
        img_rotate_vertical = findViewById(R.id.img_rotate_vertical);
        img_SelectedImage = findViewById(R.id.img_SelectedImage);
        layout_save = findViewById(R.id.layout_save);
        btn_save = findViewById(R.id.btn_save);
        btn_cancel = findViewById(R.id.btn_cancel);
        cl_function = findViewById(R.id.cl_function);

        layout_save.setVisibility(View.GONE);
        btn_select_image.setVisibility(View.VISIBLE);
        cl_function.setVisibility(View.GONE);

        btn_select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.cancelImage();
            }
        });
        img_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.cropImage(galleryFile);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.saveImage(photoURI);
            }
        });
        img_rotate_vertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.flipVertical(bitmap);

            }
        });
        img_rotate_horizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.flipHorizontal(bitmap);
            }
        });
        img_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.showExifInformation(mFileName);
            }
        });
    }

    @Override
    public void chooseGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                String mPhotoPath = getRealPathFromUri(selectedImage);
                galleryFile = new File(mPhotoPath);
                bitmap = BitmapFactory.decodeFile(mPhotoPath);

                img_SelectedImage.setVisibility(View.VISIBLE);
                btn_select_image.setVisibility(View.GONE);
                mPresenter.showPreview(mPhotoPath);
            } else if (requestCode == REQUEST_CROP_PHOTO) {
                Bundle extras = data.getExtras();
                bitmap = extras.getParcelable("data");
                img_SelectedImage.setImageBitmap(bitmap);

            }
        }
    }

    @Override
    public boolean checkPermission() {
        for (String mPermission : permissions) {
            int result = ActivityCompat.checkSelfPermission(this, mPermission);
            if (result == PackageManager.PERMISSION_DENIED) return false;
        }
        return true;
    }

    @Override
    public void showPermissionDialog() {
        Dexter.withContext(this).withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            mPresenter.permissionDenied();

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(error -> showErrorDialog())
                .onSameThread()
                .check();
    }

    @Override
    public File getFilePath() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_need_permission));
        builder.setMessage(getString(R.string.message_grant_permission));
        builder.setPositiveButton(getString(R.string.label_setting), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void showNoSpaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_message_no_more_space));
        builder.setMessage(getString(R.string.error_message_insufficient_space));
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int availableDisk() {
        File mFilePath = getFilePath();
        long freeSpace = mFilePath.getFreeSpace();
        return Math.round(freeSpace / 1048576);

    }

    @Override
    public void saveImage(Uri uri) {
        layout_save.setVisibility(View.GONE);
        btn_select_image.setVisibility(View.GONE);
        cl_function.setVisibility(View.VISIBLE);

    }

    @Override
    public void cancelImage() {
        layout_save.setVisibility(View.VISIBLE);
        btn_select_image.setVisibility(View.VISIBLE);
        img_SelectedImage.setVisibility(View.GONE);
        layout_save.setVisibility(View.GONE);
        cl_function.setVisibility(View.GONE);
    }

    @Override
    public void permissionDenied() {
        showSettingsDialog();
    }

    @Override
    public void cropImage(File file) {
        try {
            //Start Crop Activity
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            Uri photoURI = FileProvider.getUriForFile
                    (this, getApplicationContext().getPackageName() + ".provider", file);


            cropIntent.setDataAndType(photoURI, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_CROP_PHOTO);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void flipVertical(Bitmap bitmap) {

        Matrix matrix = new Matrix();
        if (!flipvertical) {
            flipvertical = true;
            matrix.preScale(1.0f, -1.0f);
        } else {
            flipvertical = false;
            matrix.preScale(1.0f, 1.0f);
        }

        Bitmap bOutput = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        img_SelectedImage.setImageBitmap(bOutput);
    }

    @Override
    public void flipHorizontal(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        if (!fliphorizontal) {
            fliphorizontal = true;
            matrix.preScale(-1.0f, 1.0f);
        } else {
            fliphorizontal = false;
            matrix.preScale(1.0f, 1.0f);
        }

        Bitmap bOutput = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        img_SelectedImage.setImageBitmap(bOutput);
    }

    @Override
    public void showExifInformation(String filename) {
        try {
            ExifInterface exif = new ExifInterface(galleryFile);
            ShowExif(exif);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, "Error!",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public File newFile() {
        Calendar cal = Calendar.getInstance();
        long timeInMillis = cal.getTimeInMillis();
        mFileName = String.valueOf(timeInMillis) + ".jpeg";
        File mFilePath = getFilePath();
        try {
            File newFile = new File(mFilePath.getAbsolutePath());
            mFileName = newFile.getName();
            Log.d("safiyas filename", mFileName);
            newFile.createNewFile();
            return newFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void showErrorDialog() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayImagePreview(String mFilePath) {
        layout_save.setVisibility(View.VISIBLE);
        img_SelectedImage.setVisibility(View.VISIBLE);
        cl_function.setVisibility(View.GONE);
        btn_select_image.setVisibility(View.GONE);
        Glide.with(MainActivity.this).load(mFilePath)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background))
                .into(img_SelectedImage);

    }

    @Override
    public void displayImagePreview(Uri mFileUri) {
        Glide.with(MainActivity.this).load(mFileUri)
                .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background)).into(img_SelectedImage);
    }

    /**
     * Get real file path from URI
     *
     * @param contentUri
     * @return
     */
    @Override
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            mFileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void selectImage() {
        final CharSequence[] items = {
                getString(R.string.choose_gallery),
                getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals(getString(R.string.choose_gallery))) {
                mPresenter.ChooseGalleryClick();
            } else if (items[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void performCrop() {

    }


    public static String dateFormat(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd");
            Date newDate = format.parse(date);

            format = new SimpleDateFormat("MMM dd,yyyy");
            return format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    public static String dateTimeFormat(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd H:m:s");
            Date newDate = format.parse(date);

            format = new SimpleDateFormat("E H:m a");
            return format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    private void ShowExif(ExifInterface exifInterface) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exif_info, null);
        dialog.setContentView(dialogView);
        dialog.getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        AppCompatImageView img_back = dialog.findViewById(R.id.img_back);
        AppCompatTextView txt_date = dialog.findViewById(R.id.txt_date);
        AppCompatTextView txt_date_time = dialog.findViewById(R.id.txt_date_time);
        AppCompatTextView txt_name = dialog.findViewById(R.id.txt_name);
        AppCompatTextView txt_size = dialog.findViewById(R.id.txt_size);
        ConstraintLayout cl_other = dialog.findViewById(R.id.cl_other);
        ConstraintLayout cl_date = dialog.findViewById(R.id.cl_date);

        AppCompatTextView txt_other = dialog.findViewById(R.id.txt_other);
        AppCompatTextView txt_other_detail = dialog.findViewById(R.id.txt_other_detail);
        exif_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        exif_FLASH = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
        exif_FOCAL_LENGTH = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION);

        exif_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        exif_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        exif_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        if (exif_DATETIME != null) {
            txt_date.setText(dateFormat(exif_DATETIME));
            txt_date_time.setText(dateTimeFormat(exif_DATETIME));
        } else {
            cl_date.setVisibility(View.GONE);
        }
        txt_name.setText(mFileName);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        long lengthbmp = imageInByte.length;
        try {
            Double focalmm = Double.parseDouble(exifInterface.getAttribute
                    (ExifInterface.TAG_FOCAL_LENGTH).split("/")[0]) / Double.parseDouble(exifInterface.getAttribute
                    (ExifInterface.TAG_FOCAL_LENGTH).split("/")[1]);

            txt_other_detail.setText(Integer.parseInt(String.valueOf(lengthbmp / 1024000)) + "MP  " +
                    focalmm + "mm");
        } catch (Exception e) {
            cl_other.setVisibility(View.GONE);
            e.printStackTrace();
        }
        String textsize = null;
        if (exif_IMAGE_LENGTH != null) {
            textsize = exif_IMAGE_LENGTH + " x "
                    + exif_IMAGE_WIDTH + "  ";
        }
        if (exif_MODEL != null) {
            textsize += exif_MODEL + "  ";
        }
        if (String.valueOf(lengthbmp / 1024 / 1024) == "0") {
            textsize += lengthbmp / 1024 / 1024 + "MB";
        }

        txt_size.setText(textsize);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }
}
