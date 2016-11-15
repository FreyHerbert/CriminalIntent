package com.leiyun.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by LeiYun on 2016/10/30 0030.
 */

public class CrimeListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
