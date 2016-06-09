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
public class SettingEditDialog extends DialogFragment {

    private EditText mEditText;
    private SettingInputListern mListener;
    private TextView mTextView;
    private String mTitle;
    private String mText;
    private AlertDialog.Builder builder;
    private int mIndex;

    public interface SettingInputListern {
        void onSettingInputComplete(int index, String wavelength);
    }

    public void init(int index, String title, String text, SettingInputListern listener) {
        mIndex = index;
        mTitle = title;
        mText = text;
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_setting, null);
        mEditText = (EditText) view.findViewById(R.id.dialog_et);
        mTextView = (TextView) view.findViewById(R.id.dialog_tv);
        mTextView.setText(mText);

        builder.setView(view).setPositiveButton(getString(R.string.ok_string),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onSettingInputComplete(mIndex, mEditText.getEditableText().toString());
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel_string), null)
                .setTitle(mTitle).setIcon(R.mipmap.ic_launcher);

        return builder.create();
    }
}
