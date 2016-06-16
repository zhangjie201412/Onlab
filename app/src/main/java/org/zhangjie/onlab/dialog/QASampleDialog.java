package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 5/24/2016.
 */
public class QASampleDialog extends DialogFragment {

    private EditText mName;
    private EditText mConc;
    String name = "", conc = "";
    private QASampleDialogCallback mCallback;
    private boolean isNew = true;
    private int index = 0;

    public interface QASampleDialogCallback {
        void onCompleteInput(int type, String name, String conc);
    }

    ;

    public static final int TYPE_OK = 0;
    public static final int TYPE_CANCEL = 1;
    public static final int TYPE_TEST = 2;

    public void setCallback(QASampleDialogCallback callback) {
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_qa_sample, null);

        mName = (EditText) view.findViewById(R.id.dialog_et_qa_name);
        mConc = (EditText) view.findViewById(R.id.dialog_et_qa_conc);
        if (name.length() < 1) {

        } else {
            mName.setText(name);
        }
        mConc.setText(conc);

        builder.setView(view).setPositiveButton(getString(R.string.ok_string),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onCompleteInput(TYPE_OK, mName.getEditableText().toString(), mConc.getEditableText().toString());
                    }
                }).setNegativeButton(getString(R.string.cancel_string), null)
                .setNeutralButton(R.string.test, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onCompleteInput(TYPE_TEST, mName.getEditableText().toString(), mConc.getEditableText().toString());
                    }
                })
                .setTitle(getString(R.string.sample_conc_setting)).setIcon(R.mipmap.ic_launcher);

        return builder.create();
    }

    public void setData(String name, String conc) {
        this.name = name;
        this.conc = conc;
    }

    public void setIsNew(boolean n) {
        isNew = n;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
