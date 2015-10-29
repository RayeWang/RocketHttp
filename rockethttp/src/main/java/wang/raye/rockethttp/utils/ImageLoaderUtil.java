package wang.raye.rockethttp.utils;

import android.content.Context;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;

/**
 * 图片加载器
 * Created by Raye on 2015/10/29.
 */
public class ImageLoaderUtil {
    private DisplayImageOptions options;
    private ImageLoader loader;
    private ImageLoadingListener imageLoadingListener;
    private ImageLoadingProgressListener imageLoadingProgressListener;

    private static ImageLoaderUtil imageLoaderUtil;

    private ImageLoaderUtil(Context context){
        loader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        loader.init(config);
    }

    public static ImageLoaderUtil getInstance(Context context,DisplayImageOptions options){
        if(imageLoaderUtil == null){
            imageLoaderUtil = new ImageLoaderUtil(context);

        }

        imageLoaderUtil.options = options;
        return imageLoaderUtil;
    }


    public void setOptions(DisplayImageOptions options) {
        this.options = options;
    }

    public void setImageLoadingListener(ImageLoadingListener imageLoadingListener) {
        this.imageLoadingListener = imageLoadingListener;
    }

    public void setImageLoadingProgressListener(ImageLoadingProgressListener imageLoadingProgressListener) {
        this.imageLoadingProgressListener = imageLoadingProgressListener;
    }

    /**
     * 获取图片
     * @param view
     * @param url
     */
    public void displayImage(ImageView view,String url){

        loader.displayImage(url, view, options, imageLoadingListener,
                imageLoadingProgressListener);

    }

    /**
     * 清除缓存
     */
    public void cleanCache(){
        loader.getDiskCache().clear();
        loader.getMemoryCache().clear();
    }

    /**
     * 获取磁盘缓存大小
     * @return
     */
    public long getCacheSize(){
        long all = 0;
        File floder = loader.getDiskCache().getDirectory();
        if (floder != null) {
            for (File file : floder.listFiles()) {
                if (file.isFile()) {
                    all += file.length();
                }
            }
        }
        return all;
    }
}
