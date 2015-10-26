package wang.raye.rockethttp;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 用来传递的课序列化对象
 * Created by Raye on 2015/10/26.
 */
public class SerializableBean implements Parcelable{
    private Object data;

    public SerializableBean(Object data) {
        this.data = data;
    }

    protected SerializableBean(Parcel in) {
    }

    public static final Creator<SerializableBean> CREATOR = new Creator<SerializableBean>() {
        @Override
        public SerializableBean createFromParcel(Parcel in) {
            return new SerializableBean(in);
        }

        @Override
        public SerializableBean[] newArray(int size) {
            return new SerializableBean[size];
        }
    };

    public Object getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(data);
    }
}
