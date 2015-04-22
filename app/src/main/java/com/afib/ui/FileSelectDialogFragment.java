package com.afib.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;


/**
 * Created by Logan on 4/21/2015.
 */
public class FileSelectDialogFragment extends DialogFragment {
    public final static int FILE_SELECTED_CODE = 999;
    public final static String FILE_NAME = "FILE_NAME";
    private String[] FileNames;
    private String NewFileName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select File")
                .setPositiveButton("New Recording", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = getActivity().getIntent();
                        i.putExtra(FILE_NAME, NewFileName);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), FILE_SELECTED_CODE, i);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Probably don't need to do anything here
                    }
                })
                .setItems(FileNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = getActivity().getIntent();
                        i.putExtra(FILE_NAME, FileNames[which]);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), FILE_SELECTED_CODE, i);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    //Set the existing file names
    public void setFileNames(ArrayList<String> fileNames)
    {
        FileNames = new String[fileNames.size()];
        int i = 0;
        for(String name : fileNames) {
            FileNames[i++] = name;
        }
    }

    //Set the name of a new file
    public void setNewFileName(String newFileName)
    {
        NewFileName = newFileName;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity != null && activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }
}
