package org.zhangjie.onlab.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.zhangjie.onlab.DeviceApplication;
import org.zhangjie.onlab.R;
import org.zhangjie.onlab.dialog.SettingEditDialog;
import org.zhangjie.onlab.utils.SharedPreferenceUtils;

/**
 * Created by H151136 on 6/6/2016.
 */
public class QuantitativeAnalysisSettingActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RESULT_OK = 0;
    public static final int RESULT_CANCEL = 1;

    public static final int FITTING_METHOD_ONE_ZERO = 0;
    public static final int FITTING_METHOD_ONE = 1;
    public static final int FITTING_METHOD_TWO = 2;
    public static final int FITTING_METHOD_THREE = 3;

    public static final int CONCENTRATION_UNIT_UG_ML = 0;
    public static final int CONCENTRATION_UNIT_MG_ML = 1;
    public static final int CONCENTRATION_UNIT_MG_L = 2;
    public static final int CONCENTRATION_UNIT_G_L = 3;
    public static final int CONCENTRATION_UNIT_mM_L = 4;
    public static final int CONCENTRATION_UNIT_M_L = 5;
    public static final int CONCENTRATION_UNIT_PPM = 6;
    public static final int CONCENTRATION_UNIT_PPB = 7;
    public static final int CONCENTRATION_UNIT_PER = 8;
    public static final int CONCENTRATION_UNIT_IU = 9;

    public static final int WAVELENGTH_ONE = 0;
    public static final int WAVELENGTH_TWO = 1;
    public static final int WAVELENGTH_THREE = 2;

    public static final int CALC_TYPE_SAMPLE = 0;
    public static final int CALC_TYPE_FORMALU = 1;

    private static final String TAG = "Onlab.QuantitativeAna";
    private Toolbar mToolbar;
    private LinearLayout mResetLayout;

    private TextView mFormaluDetail;
    private Spinner mWavelengthSettingSpinner;
    private LinearLayout mFittingMethodLayout;
    private TextView mFittingMethodValue;
    private LinearLayout mConcUnitLayout;
    private TextView mConcUnitValue;
    private RadioGroup mRadioGroup;
    private RadioButton mStandardSampleButton;
    private RadioButton mFormaluRatioButton;

    private EditText mK0EditText;
    private EditText mK1EditText;

    private EditText mWavelength1EditText;
    private EditText mWavelength2EditText;
    private EditText mWavelength3EditText;

    private EditText mRatio1EditText;
    private EditText mRatio2EditText;
    private EditText mRatio3EditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.setting_fragment_quantitativeanalysis);
        initView();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.tb_qa_setting);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditTextValid()) {
                    QuantitativeAnalysisSettingActivity.this.setResult(RESULT_OK);
                    finish();
                }
            }
        });

        mResetLayout = (LinearLayout) findViewById(R.id.layout_qa_reset);
        mResetLayout.setOnClickListener(this);

        mFormaluDetail = (TextView) findViewById(R.id.tv_formalu_detail);
        mWavelengthSettingSpinner = (Spinner) findViewById(R.id.sp_wavelength_setting);
        mWavelengthSettingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DeviceApplication.getInstance().getSpUtils().setKeyQaWavelengthSetting(position);
                loadPreference();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mFittingMethodLayout = (LinearLayout) findViewById(R.id.layout_fitting_method);
        mFittingMethodLayout.setOnClickListener(this);
        mFittingMethodValue = (TextView) findViewById(R.id.fitting_method_value);
        mConcUnitLayout = (LinearLayout) findViewById(R.id.layout_conc_unit);
        mConcUnitLayout.setOnClickListener(this);
        mConcUnitValue = (TextView) findViewById(R.id.conc_unit_value);
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_qa_setting);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();
                if (id == R.id.rb_formalu_ratio) {
                    DeviceApplication.getInstance().getSpUtils().setKeyQaCalcType(CALC_TYPE_FORMALU);
                    loadPreference();
                } else if (id == R.id.rb_standard_sample_fitting) {
                    DeviceApplication.getInstance().getSpUtils().setKeyQaCalcType(CALC_TYPE_SAMPLE);
                    loadPreference();
                }
            }
        });
        mStandardSampleButton = (RadioButton) findViewById(R.id.rb_standard_sample_fitting);
        mFormaluRatioButton = (RadioButton) findViewById(R.id.rb_formalu_ratio);

        mK0EditText = (EditText) findViewById(R.id.et_k0);
        mK1EditText = (EditText) findViewById(R.id.et_k1);
        mWavelength1EditText = (EditText) findViewById(R.id.ed_wavelength1);
        mWavelength2EditText = (EditText) findViewById(R.id.ed_wavelength2);
        mWavelength3EditText = (EditText) findViewById(R.id.ed_wavelength3);
        mRatio1EditText = (EditText) findViewById(R.id.et_ratio1);
        mRatio2EditText = (EditText) findViewById(R.id.et_ratio2);
        mRatio3EditText = (EditText) findViewById(R.id.et_ratio3);
        mK0EditText.addTextChangedListener(new EditTextDoneListener());
        mK1EditText.addTextChangedListener(new EditTextDoneListener());
        mWavelength1EditText.addTextChangedListener(new EditTextDoneListener());
        mWavelength2EditText.addTextChangedListener(new EditTextDoneListener());
        mWavelength3EditText.addTextChangedListener(new EditTextDoneListener());
        mRatio1EditText.addTextChangedListener(new EditTextDoneListener());
        mRatio2EditText.addTextChangedListener(new EditTextDoneListener());
        mRatio3EditText.addTextChangedListener(new EditTextDoneListener());

        loadPreference();
    }

    private void loadPreference() {
        SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();
        int fitting_method = sp.getQAFittingMethod();
        int conc_unit = sp.getQAConcUnit();
        int calc_type = sp.getQACalcType();
        float k0 = sp.getQAK0();
        float k1 = sp.getQAK1();
        float wavelength1 = sp.getQAWavelength1();
        float wavelength2 = sp.getQAWavelength2();
        float wavelength3 = sp.getQAWavelength3();
        float ratio1 = sp.getQARatio1();
        float ratio2 = sp.getQARatio2();
        float ratio3 = sp.getQARatio3();
        int wavelength_setting = sp.getQAWavelengthSetting();

        mFittingMethodValue.setText(getResources().getStringArray(R.array.fitting_methods)[fitting_method]);
        if (fitting_method == FITTING_METHOD_ONE) {
            mFormaluDetail.setText(getString(R.string.formalu_detail2));
            mK1EditText.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tv_k1)).setVisibility(View.VISIBLE);
        } else if (fitting_method == FITTING_METHOD_ONE_ZERO) {
            mFormaluDetail.setText(getString(R.string.formalu_detail1));
            mK1EditText.setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.tv_k1)).setVisibility(View.INVISIBLE);
        }
        mConcUnitValue.setText(getResources().getStringArray(R.array.concs)[conc_unit]);
        if (calc_type == CALC_TYPE_SAMPLE) {
            mStandardSampleButton.setChecked(true);
            mFormaluRatioButton.setChecked(false);
            mK0EditText.setEnabled(false);
            mK1EditText.setEnabled(false);
            ((TextView) findViewById(R.id.tv_k0)).setEnabled(false);
            ((TextView) findViewById(R.id.tv_k1)).setEnabled(false);
        } else if (calc_type == CALC_TYPE_FORMALU) {
            mStandardSampleButton.setChecked(false);
            mFormaluRatioButton.setChecked(true);
            mK0EditText.setEnabled(true);
            mK1EditText.setEnabled(true);
            ((TextView) findViewById(R.id.tv_k0)).setEnabled(true);
            ((TextView) findViewById(R.id.tv_k1)).setEnabled(true);
        }
        mK0EditText.setText("" + k0);
        mK1EditText.setText("" + k1);
        mWavelength1EditText.setText("" + wavelength1);
        mWavelength2EditText.setText("" + wavelength2);
        mWavelength3EditText.setText("" + wavelength3);
        mRatio1EditText.setText("" + ratio1);
        mRatio2EditText.setText("" + ratio2);
        mRatio3EditText.setText("" + ratio3);
        mWavelengthSettingSpinner.setSelection(wavelength_setting, true);
        if (wavelength_setting == WAVELENGTH_ONE) {
            mWavelength1EditText.setEnabled(true);
            mWavelength2EditText.setEnabled(false);
            mWavelength3EditText.setEnabled(false);
            mRatio1EditText.setEnabled(true);
            mRatio2EditText.setEnabled(false);
            mRatio3EditText.setEnabled(false);
        } else if (wavelength_setting == WAVELENGTH_TWO) {
            mWavelength1EditText.setEnabled(true);
            mWavelength2EditText.setEnabled(true);
            mWavelength3EditText.setEnabled(false);
            mRatio1EditText.setEnabled(true);
            mRatio2EditText.setEnabled(true);
            mRatio3EditText.setEnabled(false);
        } else if (wavelength_setting == WAVELENGTH_THREE) {
            mWavelength1EditText.setEnabled(true);
            mWavelength2EditText.setEnabled(true);
            mWavelength3EditText.setEnabled(true);
            mRatio1EditText.setEnabled(true);
            mRatio2EditText.setEnabled(true);
            mRatio3EditText.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.layout_qa_reset) {
            Log.d(TAG, "reset");
        } else if (v.getId() == R.id.layout_fitting_method) {
            final String[] items = getResources().getStringArray(R.array.fitting_methods);
            showSelectDialog(getString(R.string.title_fitting_method), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //set value
                            mFittingMethodValue.setText(items[which]);
                            DeviceApplication.getInstance().getSpUtils().setKeyQaFittingMethod(which);
                            loadPreference();
                        }
                    });
        } else if (v.getId() == R.id.layout_conc_unit) {
            final String[] items = getResources().getStringArray(R.array.concs);
            showSelectDialog(getString(R.string.title_conc_unit), items,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            //set value
                            mConcUnitValue.setText(items[which]);
                            DeviceApplication.getInstance().getSpUtils().setKeyQaConcUnit(which);
                            loadPreference();
                        }
                    });
        }
    }

    private void showSelectDialog(String title, final String[] items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, listener);
        builder.create().show();
    }

    class EditTextDoneListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //check all the edittext is null?
            if (!isEditTextValid()) {
                return;
            }
        }
    }

    private boolean isEditTextValid() {
        if ((mK0EditText.getEditableText().toString().length() < 1)
                || mK1EditText.getEditableText().toString().length() < 1
                || mWavelength1EditText.getEditableText().toString().length() < 1
                || mWavelength2EditText.getEditableText().toString().length() < 1
                || mWavelength3EditText.getEditableText().toString().length() < 1
                || mRatio1EditText.getEditableText().toString().length() < 1
                || mRatio2EditText.getEditableText().toString().length() < 1
                || mRatio3EditText.getEditableText().toString().length() < 1) {
            Toast.makeText(QuantitativeAnalysisSettingActivity.this, getString(R.string.notice_edit_null),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            try {
                SharedPreferenceUtils sp = DeviceApplication.getInstance().getSpUtils();
                sp.setKeyQaK0(Float.parseFloat(mK0EditText.getEditableText().toString()));
                sp.setKeyQaK1(Float.parseFloat(mK1EditText.getEditableText().toString()));
                sp.setKeyQaWavelength1(Float.parseFloat(mWavelength1EditText.getEditableText().toString()));
                sp.setKeyQaWavelength2(Float.parseFloat(mWavelength2EditText.getEditableText().toString()));
                sp.setKeyQaWavelength3(Float.parseFloat(mWavelength3EditText.getEditableText().toString()));
                sp.setKeyQaRatio1(Float.parseFloat(mRatio1EditText.getEditableText().toString()));
                sp.setKeyQaRatio2(Float.parseFloat(mRatio2EditText.getEditableText().toString()));
                sp.setKeyQaRatio3(Float.parseFloat(mRatio3EditText.getEditableText().toString()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}
