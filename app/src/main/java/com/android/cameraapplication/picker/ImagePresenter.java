package com.android.cameraapplication.picker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class ImagePresenter implements ImageContract.Presenter {

    private final ImageContract.View view;

    public ImagePresenter(ImageContract.View view) {
        this.view = view;
    }

    @Override
    public void ChooseGalleryClick() {
        Log.d("safiyas", "gallery");
        if (!view.checkPermission()) {
            view.showPermissionDialog();
            return;
        }

        view.chooseGallery();
    }

    @Override
    public void saveImage(Uri uri) {
        view.saveImage(uri);

    }

    @Override
    public void cancelImage() {
        view.cancelImage();
    }

    @Override
    public void permissionDenied() {
        view.permissionDenied();
    }

    @Override
    public void cropImage(File file) {
        view.cropImage(file);
    }

    @Override
    public void flipVertical(Bitmap bitmap) {
        view.flipVertical(bitmap);
    }

    @Override
    public void flipHorizontal(Bitmap bitmap) {
        view.flipHorizontal(bitmap);
    }

    @Override
    public void showExifInformation(String filename) {
        view.showExifInformation(filename);
    }

    @Override
    public void showPreview(String mFilePath) {
        view.displayImagePreview(mFilePath);
    }

    @Override
    public void showPreview(Uri mFileUri) {
        view.displayImagePreview(mFileUri);
    }
}
