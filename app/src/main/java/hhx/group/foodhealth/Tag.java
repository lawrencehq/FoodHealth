package hhx.group.foodhealth;

import android.support.annotation.NonNull;

/**
 * Created by Enthalqy Huang on 2017/10/5.
 */

public class Tag implements Comparable {
    public String name;
    public float confidence;

    public Tag() {

    }


    @Override
    public int compareTo(@NonNull Object o) {
        if (this.confidence - ((Tag) o).confidence > 0) {
            return -1;
        } else if(this.confidence - ((Tag) o).confidence == 0) {
            return 0;
        } else {
            return 1;
        }
    }
}
