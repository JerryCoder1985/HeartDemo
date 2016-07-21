package com.jerry.studio.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jerry.studio.R;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

/**
 * Created by general on 16/7/20.
 */
public class BitmapPool {

    public static Bitmap[] otherBitmaps;

    public static Bitmap selfBitmap;

    public static final void fillPool(final Context context) {
        ReplaySubject subject = ReplaySubject.create();
        subject.subscribeOn(Schedulers.computation()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                int otherLength = 4;
                otherBitmaps = new Bitmap[otherLength];
                otherBitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.mipmap.blue);
                otherBitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.mipmap.purple);
                otherBitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.mipmap.green);
                otherBitmaps[3] = BitmapFactory.decodeResource(context.getResources(), R.mipmap.red);
                selfBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.yellow);
            }
        });

        subject.onNext(new Object());
    }

}
