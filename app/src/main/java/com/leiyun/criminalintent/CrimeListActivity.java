package com.leiyun.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by LeiYun on 2016/10/30 0030.
 */

public class CrimeListActivity extends SingleFragmentActivity
implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks{
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    /**
     * CrimeListFragment.Callbacks接口的方法
     * @param crime crime的实例
     */
    @Override
    public void onCrimeSelected(Crime crime) {
        // 按照不同的布局界面分别处理
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());

            // 通过FragmentManager替换界面
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, newDetail)
                    .commit();
        }
    }

    /**
     * CrimeFragment.Callbacks接口的方法
     * @param crime
     */
    @Override
    public void onCrimeUpdated(Crime crime) {
        // 获取CrimeListFragment的实例
        // 对新创建的Crime，或是修改的Crime进行更新
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
