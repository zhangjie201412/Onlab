package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 5/24/2016.
 */
public class CalcSpeedDialog extends DialogFragment {

    private EditText mStartEditText;
    private EditText mEndEditText;
    private EditText mCalcRatioEditText;
    private CalcSpeedListener mListener;

    public interface CalcSpeedListener {
        void onCalcSpeedInput(String start, String end, String calcRatio);
    }

    public void init(CalcSpeedListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_calc_speed, null);
        mStartEditText = (EditText) view.findViewById(R.id.dialog_et_start_time);
        mEndEditText = (EditText) view.findViewById(R.id.dialog_et_end_time);
        mCalcRatioEditText = (EditText) view.findViewById(R.id.dialog_et_calc_ratio);

        builder.setView(view).setPositiveButton(getString(R.string.ok_string),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onCalcSpeedInput(mStartEditText.getEditableText().toString(),
                                    mEndEditText.getEditableText().toString(),
                                    mCalcRatioEditText.getEditableText().toString());
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel_string), null)
                .setTitle(R.string.calc_speed_title).setIcon(R.mipmap.ic_launcher);

        return builder.create();
    }
}
