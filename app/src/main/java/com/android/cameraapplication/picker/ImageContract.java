package com.android.cameraapplication.picker;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

/**
 * Created on : Jan 06, 2019
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class ImageContract {
    public interface View {

        boolean checkPermission();

        void showPermissionDialog();

        File getFilePath();

        void openSettings();


        void chooseGallery();

        void showNoSpaceDialog();

        int availableDisk();

        void saveImage(Uri uri);

        void cancelImage();

        void permissionDenied();

        void cropImage(File file);

        void flipVertical(Bitmap bitmap);

        void flipHorizontal(Bitmap bitmap);

        void showExifInformation(String filename);


        File newFile();

        void showErrorDialog();

        void displayImagePreview(String mFilePath);

        void displayImagePreview(Uri mFileUri);

        String getRealPathFromUri(Uri contentUri);
    }

    public interface Presenter {


        void ChooseGalleryClick();

        void saveImage(Uri uri);

        void cancelImage();

        void permissionDenied();

        void cropImage(File file);

        void flipVertical(Bitmap bitmap);

        void flipHorizontal(Bitmap bitmap);

        void showExifInformation(String filename);

        void showPreview(String mFilePath);

        void showPreview(Uri mFileUri);
    }
}
