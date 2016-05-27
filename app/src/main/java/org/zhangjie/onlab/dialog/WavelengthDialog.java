package org.zhangjie.onlab.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import org.zhangjie.onlab.MainActivity;
import org.zhangjie.onlab.R;

/**
 * Created by H151136 on 5/24/2016.
 */
public class WavelengthDialog extends DialogFragment {

    private EditText mWavelengthEditText;
    private int wavelength;
    private WavelengthInputListern mListener;

    public interface WavelengthInputListern {
        void onWavelengthInputComplete(String wavelength);
    }

    public void setListener(WavelengthInputListern listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_set_wavelength, null);
        mWavelengthEditText = (EditText) view.findViewById(R.id.dialog_et_wavelength);

        builder.setView(view).setPositiveButton(getString(R.string.ok_string),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onWavelengthInputComplete(mWavelengthEditText.getEditableText().toString());
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel_string), null)
                .setTitle(getString(R.string.action_set_wavelength)).setIcon(R.mipmap.ic_launcher);

        return builder.create();
    }
}
