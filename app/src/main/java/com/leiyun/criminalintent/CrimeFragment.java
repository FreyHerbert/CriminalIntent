package com.leiyun.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by LeiYun on 2016/10/29 0029.
 */

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String EXTRA_CRIME_ID_KEY =
            "com.leiyun.criminalintent.criminalintent.crime_id_key";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;

    private EditText mTitleField;
    private Button mDateButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private CheckBox mSolvedCheckBox;
    private File mPhotoFile;

    private Crime mCrime;
    private Callbacks mCallbacks;

    /**
     * fragment回调接口
     */
    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static UUID getCrimeId(Intent intent) {
        return (UUID) intent.getSerializableExtra(EXTRA_CRIME_ID_KEY);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments()
                .getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime); // 保存图片的存储位置
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTitle(charSequence.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getSimpleDate(mCrime.getDate()));
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCrime.setSolved(b);
                updateCrime();
            }
        });

        //首先以资源ID引用Send Crime Report按钮并为其设置一个监听器。
        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //创建一个隐式intent并传入 startActivity(Intent) 方法
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report)); //创建一个选择器显示响应隐式intent的全部activity
                startActivity(i);
            }
        });

        //操作为Intent.ACTION_PICK,联系人数据获取位置为ContactsContract.Contacts.CONTENT_URI
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI); //就是请Android帮忙从联系人数据库里获取某个具体联系人
        //引用资源，设置监听器
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) { // 调用startActivityForResult(...)方法，使应用能够接收联系人的信息
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) { // 如果suspect有信息就将信息显示到button上
            mSuspectButton.setText(mCrime.getSuspect());
        }

        // 获取PackageManager
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            // 如果搜到目标，它会返回 ResolveInfo 告诉你找到了哪个activity。
            // 如果找不到的话，必须禁用嫌疑人按钮，否则应用就会崩溃。
            mSuspectButton.setEnabled(false);
        }

        //引用mPhotoView资源
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);


        //创建一个Intent，使用这个Intent来启动相机
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 对隐式Intent进行检查，查看运行的Android机有没有相机应用
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        //引用mPhotoButton资源,设置点击事件
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        mPhotoButton.setOnClickListener(new View.OnClickListener(){
            // 点击Button打开相机
            @Override
            public void onClick(View view) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        updatePotoView();

        return v;
    }

    /**
     * 获取fragment的回调接口的实例
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    /**
     * 设置mCallbacks为空
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) { // 日历
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) { // 判断是否是是从联系人活动中返回的
            Uri contactUri = data.getData(); // 这个URI是个数据定位符，指向用户所选联系人
            // 指定你想查询的字段，将它返回
            // 字段值为
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // 执行查询，contactUri就像where条件
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null); // 通过内容提供器查询联系人数据
            try {
                // 仔细检查所获得的结果
                if (c.getCount() == 0) {
                    return;
                }

                // 取出第一行第一列的数据，这个数据就是suspect的名称
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally{
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updateCrime();
            updatePotoView();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getSimpleDate(mCrime.getDate()));
    }

    /**
     * getCrimeReport()方法创建四段字符串信息，并返回拼接完整的消息
     * @return 返回一个字符串
     */
    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM, dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    /**
     * 这是一个队拍摄好照片后，对ImageView的刷新
     */
    private void updatePotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
//            mPhotoView.setImageBitmap(null);
            return;
        }else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),
                    getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    /**
     * 更新crime
     */
    private void updateCrime() {
        //更新数据库里的crime信息
        CrimeLab.get(getActivity())
                .updateCrime(mCrime);

        mCallbacks.onCrimeUpdated(mCrime);
    }
}
