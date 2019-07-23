package org.horaapps.leafpic.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

public class SparseBooleanArrayParcelable extends SparseBooleanArray implements Parcelable {
    public static Parcelable.Creator<SparseBooleanArrayParcelable> CREATOR = new Parcelable.Creator<SparseBooleanArrayParcelable>() {
        @Override
        public SparseBooleanArrayParcelable createFromParcel(Parcel source) {
            SparseBooleanArray read = source.readSparseBooleanArray();

            return new SparseBooleanArrayParcelable(read);
        }

        @Override
        public SparseBooleanArrayParcelable[] newArray(int size) {
            return new SparseBooleanArrayParcelable[size];
        }
    };

    public SparseBooleanArrayParcelable() {

    }

    public SparseBooleanArrayParcelable(SparseBooleanArray sparseBooleanArray) {
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            this.put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(i));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSparseBooleanArray(this);
    }
}