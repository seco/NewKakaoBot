package com.astro.kakaobot;

import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;


public class SettingsPopup extends PopupWindow {
    public SettingsPopup(final Type.Project project, final MainActivity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        RelativeLayout popup = (RelativeLayout) layoutInflater.inflate(R.layout.popup_settings, null, false);

        SwitchCompat enableProject = (SwitchCompat) popup.findViewById(R.id.script_enable);
        enableProject.setChecked(project.enable);
        enableProject.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    FileUtils.saveData(project, "disabled", !b + "");

                    dismiss();
                    activity.reload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final TextInputEditText title = (TextInputEditText) popup.findViewById(R.id.edit_title);
        title.setText(project.title);

        final TextInputEditText subtitle = (TextInputEditText) popup.findViewById(R.id.edit_subtitle);
        subtitle.setText(project.subtitle);

        popup.findViewById(R.id.save_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileUtils.saveData(project, "title", title.getText().toString());

                    dismiss();
                    activity.reload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        popup.findViewById(R.id.save_subtitle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileUtils.saveData(project, "subtitle", subtitle.getText().toString());

                    dismiss();
                    activity.reload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        popup.findViewById(R.id.refresh_project).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                activity.reloadProject(project);
            }
        });
        popup.findViewById(R.id.delete_project).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(activity).create();
                dialog.setTitle(activity.getString(R.string.delete_check));

                dialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            FileUtils.delete(project);

                            activity.reload();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // null
                    }
                });
                dialog.show();

                dismiss();
            }
        });

        this.setContentView(popup);
        this.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(null);
        this.setOutsideTouchable(true);
    }
}
