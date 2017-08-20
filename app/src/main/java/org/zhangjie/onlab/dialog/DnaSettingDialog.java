package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.zhangjie.onlab.R;

/**
 * Created by Administrator on 2016/8/21.
 */
public class DnaSettingDialog extends DialogFragment {
    private TextView mFormaluTextView;
    private SpannableString mSp;

    private static final float METHOD1_WAVELENGTH1 = 260.0f;
    private static final float METHOD1_WAVELENGTH2 = 280.0f;
    private static final float METHOD1_WAVELENGTH_REF = 320.0f;
    private static final float METHOD1_F1 = 62.9f;
    private static final float METHOD1_F2 = 36;
    private static final float METHOD1_F3 = 1552;
    private static final float METHOD1_F4 = 757.3f;

    private static final float METHOD2_WAVELENGTH1 = 260.0f;
    private static final float METHOD2_WAVELENGTH2 = 230.0f;
    private static final float METHOD2_WAVELENGTH_REF = 320.0f;
    private static final float METHOD2_F1 = 49.1f;
    private static final float METHOD2_F2 = 3.48f;
    private static final float METHOD2_F3 = 183;
    private static final float METHOD2_F4 = 75.8f;

    private RadioGroup mRgMethod;
    private RadioButton mMethod1;
    private RadioButton mMethod2;
    private RadioButton mMethodCustom;

    private EditText mWavelength1;
    private EditText mWavelength2;
    private EditText mWavelengthRef;

    private EditText mF1;
    private EditText mF2;
    private EditText mF3;
    private EditText mF4;

    private OnDnaSettingListener mListener;

    public class DnaSettingParam {
        public float wavelength1;
        public float wavelength2;
        public float wavelengthRef;
        public float f1;
        public float f2;
        public float f3;
        public float f4;
    }

    public interface OnDnaSettingListener {
        public void onDnaSettingCallback(DnaSettingParam param, int error);
    }

    public void setListener(OnDnaSettingListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_dna_setting, null);

        Log.d("###", "SIZE = " + getResources().getDimensionPixelSize(R.dimen.formalu_text_size));

        mFormaluTextView = (TextView) view.findViewById(R.id.tv_formalu_title);
        mSp = new SpannableString(getString(R.string.dna_formalu_title));
        mSp.setSpan(new StyleSpan(Typeface.BOLD), 0, mSp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size), true), 0, mSp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new TypefaceSpan("monospace"), 0, mSp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 1, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 1, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 9, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 9, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 16, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 16, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 29, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 29, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 36, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 36, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSp.setSpan(new SubscriptSpan(), 46, 53, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true),46, 53, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 58, 61, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 58, 61, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 65, 69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 65, 69, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 78, 81, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 78, 81, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new SubscriptSpan(), 85, 89, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSp.setSpan(new AbsoluteSizeSpan((int)getResources().getDimension(R.dimen.formalu_text_size_down), true), 85, 89, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mFormaluTextView.setText(mSp);

        initView(view);
        mFormaluTextView.requestFocus();
        builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //check input is valid?
                if(checkInputValid()) {
                    if(mListener != null) {
                        DnaSettingParam param = new DnaSettingParam();
                        //get parameters and set into param
                        float wavelength1 = Float.parseFloat(mWavelength1.getEditableText().toString());
                        float wavelength2 = Float.parseFloat(mWavelength2.getEditableText().toString());
                        float wavelengthRef = Float.parseFloat(mWavelengthRef.getEditableText().toString());
                        float f1 = Float.parseFloat(mF1.getEditableText().toString());
                        float f2 = Float.parseFloat(mF2.getEditableText().toString());
                        float f3 = Float.parseFloat(mF3.getEditableText().toString());
                        float f4 = Float.parseFloat(mF4.getEditableText().toString());
                        param.wavelength1 = wavelength1;
                        param.wavelength2 = wavelength2;
                        param.wavelengthRef = wavelengthRef;
                        param.f1 = f1;
                        param.f2 = f2;
                        param.f3 = f3;
                        param.f4 = f4;

                        mListener.onDnaSettingCallback(param, 0);
                    }
                } else {
                    if(mListener != null) {
                        mListener.onDnaSettingCallback(null, -1);
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setView(view);

        return builder.create();
    }

    private boolean checkInputValid() {
        if((mWavelength1.getEditableText().toString().length() < 1) ||
                (mWavelength2.getEditableText().toString().length() < 1) ||
                (mWavelengthRef.getEditableText().toString().length() < 1) ||
                (mF1.getEditableText().toString().length() < 1) ||
                (mF2.getEditableText().toString().length() < 1) ||
                (mF3.getEditableText().toString().length() < 1) ||
                (mF4.getEditableText().toString().length() < 1)) {
            return false;
        }
        return true;
    }

    private void initView(View view) {
        mRgMethod = (RadioGroup)view.findViewById(R.id.rg_analysis_method);
        mMethod1 = (RadioButton)view.findViewById(R.id.rb_method_one);
        mMethod2 = (RadioButton)view.findViewById(R.id.rb_method_two);
        mMethodCustom = (RadioButton)view.findViewById(R.id.rb_method_custom);
        mWavelength1 = (EditText)view.findViewById(R.id.et_wavelength1);
        mWavelength2 = (EditText)view.findViewById(R.id.et_wavelength2);
        mWavelengthRef = (EditText)view.findViewById(R.id.et_wavelength_custom);
        mF1 = (EditText)view.findViewById(R.id.et_f1);
        mF2 = (EditText)view.findViewById(R.id.et_f2);
        mF3 = (EditText)view.findViewById(R.id.et_f3);
        mF4 = (EditText)view.findViewById(R.id.et_f4);

        mRgMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_method_one:
                        mWavelength1.setText("" + METHOD1_WAVELENGTH1);
                        mWavelength2.setText("" + METHOD1_WAVELENGTH2);
                        mWavelengthRef.setText("" + METHOD1_WAVELENGTH_REF);
                        mF1.setText("" + METHOD1_F1);
                        mF2.setText("" + METHOD1_F2);
                        mF3.setText("" + METHOD1_F3);
                        mF4.setText("" + METHOD1_F4);
                        mWavelength1.setEnabled(false);
                        mWavelength2.setEnabled(false);
                        mWavelengthRef.setEnabled(false);
                        mF1.setEnabled(false);
                        mF2.setEnabled(false);
                        mF3.setEnabled(false);
                        mF4.setEnabled(false);
                        break;
                    case R.id.rb_method_two:
                        mWavelength1.setText("" + METHOD2_WAVELENGTH1);
                        mWavelength2.setText("" + METHOD2_WAVELENGTH2);
                        mWavelengthRef.setText("" + METHOD2_WAVELENGTH_REF);
                        mF1.setText("" + METHOD2_F1);
                        mF2.setText("" + METHOD2_F2);
                        mF3.setText("" + METHOD2_F3);
                        mF4.setText("" + METHOD2_F4);
                        mWavelength1.setEnabled(false);
                        mWavelength2.setEnabled(false);
                        mWavelengthRef.setEnabled(false);
                        mF1.setEnabled(false);
                        mF2.setEnabled(false);
                        mF3.setEnabled(false);
                        mF4.setEnabled(false);
                        break;
                    case R.id.rb_method_custom:
                        mWavelength1.setEnabled(true);
                        mWavelength2.setEnabled(true);
                        mWavelengthRef.setEnabled(true);
                        mF1.setEnabled(true);
                        mF2.setEnabled(true);
                        mF3.setEnabled(true);
                        mF4.setEnabled(true);
                        mWavelength1.setText("");
                        mWavelength2.setText("");
                        mWavelengthRef.setText("");
                        mF1.setText("");
                        mF2.setText("");
                        mF3.setText("");
                        mF4.setText("");
                        break;
                    default:
                        break;
                }
            }
        });
        mMethod1.setChecked(true);

    }
}
