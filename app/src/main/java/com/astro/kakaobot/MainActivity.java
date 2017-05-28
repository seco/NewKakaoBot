package com.astro.kakaobot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.astro.kakaobot.script.JSScriptEngine;
import com.astro.kakaobot.script.JSUtil;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_ALL = 3;
    private static final int PERMISSION_INTERNET = 2;
    private static final int PERMISSION_OVERLAY = 4;
    private static final int PERMISSION_READ = 0;
    private static final int PERMISSION_WRITE = 1;
    private static Activity context;
    private FloatingActionButton fabAdd;
    private LinearLayout fabContainer;
    private FloatingActionButton fabReload;

    private ArrayList<Type.Project> projects = new ArrayList<>();

    public static void UIThread(Runnable runnable) {
        context.runOnUiThread(runnable);
    }

    public static int dp(float dips) {
        return (int) (dips * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static Context getContext() {
        return context;
    }

    public boolean hasPermissions(String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void initEngines() {
        projects = FileUtils.getProjectList();
        KakaoTalkListener.clearEngine();

        int i = 0;
        for (Type.Project project : projects) {
            if (!project.enable) {
                switch (project.type) {
                    case JS:
                        JSScriptEngine jsScriptEngine = new JSScriptEngine();
                        try {
                            jsScriptEngine.setScript(FileUtils.getProjectScript(project));
                            jsScriptEngine.setName((String) FileUtils.readData(project, "title"));
                            KakaoTalkListener.addJsEngine(jsScriptEngine);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Snackbar.make(fabContainer, getString(R.string.file_not_found), Snackbar.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            projects.get(i).isError = e.toString();
                        }
                        break;
                }

                Log.d("KakaoBot/initEngine", project.title + ": Load succeed");
            }

            i++;
        }
    }

    private void initRecyclerView() {
        ScriptProjectAdapter adapter = new ScriptProjectAdapter(projects, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.project_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        JSUtil.setContext(this);
        FileUtils.init();

        fabContainer = (LinearLayout) findViewById(R.id.fab_container);

        fabAdd = (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProjectDialog();
            }
        });

        fabReload = (FloatingActionButton) findViewById(R.id.fab_reload);
        fabReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initEngines();
                try {
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.project_list);
                    ScriptProjectAdapter adapter = (ScriptProjectAdapter) recyclerView.getAdapter();
                    adapter.setList(projects);
                    Snackbar.make(fabContainer, getString(R.string.reloaded), Snackbar.LENGTH_LONG).show();
                } catch (NullPointerException e) {
                    initRecyclerView();
                }
            }
        });

        String[] permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(permissions)) {
                requestPermissions(permissions, PERMISSION_ALL);
            }
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), PERMISSION_OVERLAY);
            }
        }

        try {
            reload();
        } catch (Exception e) {
        }
        prepare();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if(id == R.id.action_feedback) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:astr36@naver.com")));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, PERMISSION_READ);
                    Toast.makeText(this, getString(R.string.no_read_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initEngines();
                    initRecyclerView();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,}, PERMISSION_READ);
                    Toast.makeText(this, getString(R.string.no_read_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO
                } else {
                    // TODO
                }
                break;
            case PERMISSION_INTERNET:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO
                } else {
                    // TODO
                }
                break;
            case PERMISSION_OVERLAY:
                if (Settings.canDrawOverlays(this)) {
                    // TODO
                } else {
                    // TODO
                }
                break;
        }
    }

    private void prepare() {
        PackageInfo[] apps = this.getPackageManager().getInstalledPackages(0).toArray(new PackageInfo[0]);

        boolean wearCheck = false;
        for (PackageInfo info : apps) {
            if (info.packageName.equals("com.google.android.wearable.app")) {
                wearCheck = true;
            }
        }

        if (!wearCheck) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.wearable.app")));
            Toast.makeText(this, getString(R.string.need_wear_app), Toast.LENGTH_SHORT).show();
        }

        String notifiPermission = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
        if ((notifiPermission != null && !notifiPermission.contains("com.astro.kakaobot")) || notifiPermission == null) {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            Toast.makeText(this, getString(R.string.need_permissions), Toast.LENGTH_SHORT).show();
        }
    }

    public void reload() {
        initEngines();
        initRecyclerView();
    }

    public void reloadProject(Type.Project project) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).equals(project)) {
                projects.remove(i);
                projects.add(i, project);
            }
            i++;
        }

        initRecyclerView();
        Snackbar.make(fabContainer, getString(R.string.reloaded), Snackbar.LENGTH_SHORT).show();
    }

    private void showProjectDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getString(R.string.create_project));

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.script_create_dialog, null, false);

        final TextInputEditText titleEdit = (TextInputEditText) layout.findViewById(R.id.title_text);
        final TextInputEditText subtitleEdit = (TextInputEditText) layout.findViewById(R.id.subtitle_text);
        final RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.type_group);

        dialog.setView(layout);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Type.ProjectType type = Type.ProjectType.JS;

                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.type_js:
                        type = Type.ProjectType.JS;
                        break;
                }
                try {
                    if (titleEdit.getText().toString().trim().equals("")) {
                        showProjectDialog();
                        Toast.makeText(context, getString(R.string.no_space_title), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Type.Project project = new Type.Project();
                    project.title = titleEdit.getText().toString();
                    project.subtitle = subtitleEdit.getText().toString();
                    project.type = type;
                    project.enable = true;

                    FileUtils.createProject(project);
                    initEngines();
                    initRecyclerView();

                    Snackbar.make(fabContainer, getString(R.string.create_succeed), Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(fabContainer, getString(R.string.create_failed), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        dialog.show();
    }
}
