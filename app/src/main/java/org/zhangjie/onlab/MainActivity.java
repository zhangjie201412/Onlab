package org.zhangjie.onlab;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.zhangjie.onlab.device.DeviceManager;
import org.zhangjie.onlab.dialog.DevicesSelectDialog;
import org.zhangjie.onlab.dialog.WavelengthDialog;
import org.zhangjie.onlab.fragment.FragmentCallbackListener;
import org.zhangjie.onlab.fragment.HelloChartFragment;
import org.zhangjie.onlab.fragment.MainFragment;
import org.zhangjie.onlab.fragment.PhotometricMeasureFragment;
import org.zhangjie.onlab.fragment.QuantitativeAnalysisFragment;
import org.zhangjie.onlab.fragment.TimeScanFragment;
import org.zhangjie.onlab.fragment.WavelengthScanFragment;
import org.zhangjie.onlab.otto.BusProvider;
import org.zhangjie.onlab.otto.SetOperateModeEvent;
import org.zhangjie.onlab.otto.SetWavelengthEvent;

public class MainActivity extends AppCompatActivity implements WavelengthDialog.WavelengthInputListern,
        FragmentCallbackListener, View.OnClickListener {

    private static boolean isExit = false;
    private MainFragment mMain;
    private PhotometricMeasureFragment mPhotometricFragment;
    private TimeScanFragment mTimeScanFragment;
    private WavelengthScanFragment mWavelengthScanFragment;
    private QuantitativeAnalysisFragment mQuantitativeAnalysisFragment;
    private HelloChartFragment mHelloChart;
    private TextView mTitleTextView;
    private TextView mBottomWavelength;
    private TextView mBottomAbs;
    private TextView mBottomTrans;

    private final String TAG = "Onlab.MainActivity";
    private boolean mIsBluetoothConnected = false;
    private Toolbar mTopToolbar;
    private ImageView mSelectall;
    private ImageView mDelete;
    private boolean mOperateMode = false;

    private WavelengthDialog mWavelengthDialog;
    private DevicesSelectDialog mDeviceSelectDialog;
    private DeviceManager mDeviceManager;

    private Toast mToast;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private int permissionCheck;
    private BluetoothAdapter mBluetoothAdapter;
    //++++UV DATA
    private int[] mDark;
    private int mA;
    private int mI0 = 20000;
    //----

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DeviceManager.UI_MSG_DEVICE_CONNECTED:
                    mIsBluetoothConnected = true;
                    setBluetoothConnected(true);
                    break;
                case DeviceManager.UI_MSG_DEVICE_DISCONNECTED:
                    mIsBluetoothConnected = false;
                    setBluetoothConnected(false);
                    break;

                case DeviceManager.UI_MSG_DEVICE_SCAN:
                    Bundle bundle = msg.getData();
                    String name = bundle.getString("name");
                    String addr = bundle.getString("addr");
                    Log.d(TAG, "###name = " + name);
                    mDeviceSelectDialog.addDevice(name, addr);
                    break;
                case DeviceManager.UI_MSG_DEVICE_DATA:
                    Bundle data = msg.getData();
                    int entryFlag = msg.arg1;
                    String[] recvMsg = data.getStringArray("MSG");
                    process(entryFlag, recvMsg);
                    break;
            }
        }
    };

    private void process(int flag, String[] msg) {
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_INITIALIZE) != 0) {
            //initialzation ertry
            Log.d(TAG, "INITIALZE ENTRY");
            work_entry_initialize(msg);
            mDeviceManager.start();
        }
        if ((flag & DeviceManager.WORK_ENTRY_FLAG_UPDATE_STATUS) != 0) {
            mDeviceManager.clearFlag(DeviceManager.WORK_ENTRY_FLAG_INITIALIZE);
            //update status entry
            Log.d(TAG, "UPDATE STATUS ENTRY");
            work_entry_updatestatus(msg);
            mDeviceManager.clearFlag(DeviceManager.WORK_ENTRY_FLAG_UPDATE_STATUS);
        }
    }

    private void work_entry_initialize(String[] msg) {
        String tag = msg[0];

        Log.d(TAG, "tag = " + tag);

        if(tag.startsWith(DeviceManager.TAG_CONNECT)) {
            //connect
            if(msg[1].startsWith("ok.")) {
                Log.d(TAG, "connect successfully!");
            }
        } else if(tag.startsWith(DeviceManager.TAG_GET_WAVELENGTH)) {
            //get wavelength
            Log.d(TAG, "get wavelength = " + msg[1]);
        } else if(tag.startsWith(DeviceManager.TAG_GET_DARK)) {
            //get dark
            for(int i = 0; i < 8; i++) {
                msg[i + 1] = msg[i + 1].replaceAll("\\D+","").replaceAll("\r", "").replaceAll("\n", "").trim();
                mDark[i] = Integer.parseInt(msg[i + 1]);
            }
        } else if(tag.startsWith(DeviceManager.TAG_GET_A)) {
            //get a
            msg[1] = msg[1].replaceAll("\\D+","").replaceAll("\r", "").replaceAll("\n", "").trim();
            mA = Integer.parseInt(msg[1]);
        }
    }

    private void work_entry_updatestatus(String[] msgs) {

        String [] msg = msgs.clone();

        int[] energies = new int[10];
        int I1 = 0;
        float trans = 0;
        float abs = 0;

        for(int i = 0; i < 10; i++) {
            msg[i + 1] = msg[i + 1].replaceAll("\\D+","").replaceAll("\r", "").replaceAll("\n", "").trim();
            energies[i] = Integer.parseInt(msg[i + 1], 10);
            I1 += energies[i];
        }
        I1 /= 10;

        Log.d(TAG, "mA = " + mA);
        if(mA > 0) {
            trans = (float) (I1 - mDark[mA - 1]) / (float) (mI0 - mDark[mA - 1]);
            abs = (float) -Math.log10(trans);
            updateAbs(abs);
            updateTrans(trans);
        }
    }

    @Override
    public void onWavelengthInputComplete(String wavelength) {
        toastShow("get wavelength = " + wavelength);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String sss = "40727";
        int s = Integer.parseInt(sss);
        Log.d(TAG, "s = " + s);


        Log.d(TAG, "onCreate");
        initToolbar();
        initView();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toastShow(getString(R.string.ble_not_support));
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mIsBluetoothConnected = false;
        mWavelengthDialog = new WavelengthDialog();
        mWavelengthDialog.setListener(this);

        mDeviceSelectDialog = new DevicesSelectDialog();

        mDeviceManager = DeviceManager.getInstance();
        mDeviceManager.init(this, mUiHandler);

        checkLocationPermission();
        mDark = new int[8];
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDeviceManager.release();
        mDeviceManager = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {

        if (mOperateMode) {
            BusProvider.getInstance().post(new SetOperateModeEvent(false));
            setOperateMode(false);
            return;
        }

        int backStackCount = getFragmentManager().getBackStackEntryCount();
//        Log.d(TAG, "back stack count = " + backStackCount);
        // the main fragment is always on the bottom stack
        if (backStackCount > 1) {
            setTitle(getString(R.string.main));
            super.onBackPressed();
            return;
        }

        if (!isExit) {
            isExit = true;
            toastShow(getString(R.string.retry_to_exit));
            new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    isExit = false;
                }
            }.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    public boolean getOperateMode() {
        return mOperateMode;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        setBluetoothConnected(mIsBluetoothConnected);

        return true;
    }

    @Override
    public void onMainClick(int id) {
        switch (id + 100) {
            case MainFragment.ITEM_PHOTOMETRIC_MEASURE:
                addContentFragment(mPhotometricFragment);
                break;
            case MainFragment.ITEM_TIME_SCAN:
                addContentFragment(mTimeScanFragment);
                break;
            case MainFragment.ITEM_WAVELENGTH_SCAN:
                addContentFragment(mWavelengthScanFragment);
                break;
            case MainFragment.ITEM_QUANTITATIVE_ANALYSIS:
                addContentFragment(mQuantitativeAnalysisFragment);
                break;
            default:
                break;
        }
        setTitle(getResources().getStringArray(R.array.titles)[id]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_open:
                toastShow("open");
                break;
            case R.id.action_save:
                toastShow("save");
                break;
            case R.id.action_print:
                toastShow("print");
                break;
            case R.id.action_set_wavelength:
                mWavelengthDialog.show(getFragmentManager(), getString(R.string.wavelength));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        mTopToolbar = (Toolbar) findViewById(R.id.tb_top);
        setSupportActionBar(mTopToolbar);

        mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTopToolbar.setTitleTextColor(getResources().getColor(R.color.colorTitle));
        mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_disconnected));
        mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_disabled);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "on Navigation Click!");
                if (!mIsBluetoothConnected) {
                    mDeviceManager.scan();
                    mDeviceSelectDialog.show(getFragmentManager(), getString(R.string.select_devices));
                }

//                if (mIsBluetoothConnected) {
//                    mIsBluetoothConnected = false;
//                } else {
//                    mIsBluetoothConnected = true;
//                }
//                setBluetoothConnected(mIsBluetoothConnected);
            }
        });
    }

    private void setOperateMode(boolean enable) {
        if (enable) {
            mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorOperate));
            mSelectall.setVisibility(View.VISIBLE);
            mDelete.setVisibility(View.VISIBLE);
        } else {
            mTopToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mSelectall.setVisibility(View.GONE);
            mDelete.setVisibility(View.GONE);
        }
        mOperateMode = enable;
    }

    private void initView() {
        mBottomWavelength = (TextView)findViewById(R.id.tv_bottom_wavelength);
        mBottomAbs = (TextView)findViewById(R.id.tv_bottom_abs);
        mBottomTrans = (TextView)findViewById(R.id.tv_bottom_trans);
        mTitleTextView = (TextView) findViewById(R.id.tb_title);
        mSelectall = (ImageView) findViewById(R.id.iv_selectall);
        mDelete = (ImageView) findViewById(R.id.iv_delete);
        mSelectall.setVisibility(View.GONE);
        mDelete.setVisibility(View.GONE);
        mSelectall.setOnClickListener(this);
        mDelete.setOnClickListener(this);

        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
        mMain = new MainFragment();
        mPhotometricFragment = new PhotometricMeasureFragment();
        mTimeScanFragment = new TimeScanFragment();
        mWavelengthScanFragment = new WavelengthScanFragment();
        mQuantitativeAnalysisFragment = new QuantitativeAnalysisFragment();

        mHelloChart = new HelloChartFragment();

        if (getFragmentManager().getBackStackEntryCount() == 0) {
            addContentFragment(mMain);
        } else {
            Log.w(TAG, "Main fragment is add!!!");
        }

    }

    private void addContentFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        Log.d(TAG, "fragment count = " + fm.getBackStackEntryCount());
        Log.d(TAG, "addContentFragment");

        if (fragment != mMain) {
            transaction.setCustomAnimations(
                    R.animator.fragment_slide_left_enter,
                    R.animator.fragment_slide_right_exit);
        }

        transaction.add(R.id.layout_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setBluetoothConnected(boolean enable) {
        if (enable) {
            mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_connected));
            mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_connected);
        } else {
            mTopToolbar.setTitle(getResources().getString(R.string.bluetooth_disconnected));
            mTopToolbar.setNavigationIcon(R.mipmap.bluetooth_disabled);
        }
    }

    private void setTitle(String title) {
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
    }

    public void checkLocationPermission() {
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        switch (permissionCheck) {
            case PackageManager.PERMISSION_GRANTED:
                break;

            case PackageManager.PERMISSION_DENIED:

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show an explanation to user *asynchronouselly* -- don't block
                    //this thread waiting for the user's response! After user sees the explanation, try again to request the permission
                    toastShow("Location access is required to show Bluetooth devices nearby.");
                } else {
                    //No explanation needed, we can request the permission
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
                break;
        }
    }

    private void toastShow(String msg) {
        if (mToast != null) {
            mToast.setText(msg);
            mToast.show();
        }
    }

    private void updateAbs(float abs) {
        String absString = getString(R.string.abs) + ": " + String.format("%.2f", abs);
        mBottomAbs.setText(absString);
    }

    private void updateTrans(float trans) {
        String transString = getString(R.string.trans) + ": " + String.format("%.2f", trans);
        mBottomTrans.setText(transString);    }

    @Subscribe
    public void onSetWavelengthEvent(SetWavelengthEvent event) {
        mWavelengthDialog.show(getFragmentManager(), getString(R.string.wavelength));
    }

    @Subscribe
    public void onSetOperateModeEvent(SetOperateModeEvent event) {
        setOperateMode(event.isOperateMode);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_selectall) {
            toastShow("select all");
        } else if (v.getId() == R.id.iv_delete) {
            toastShow("delete");
        }
    }
}
