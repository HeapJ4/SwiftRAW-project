package app.swiftnef;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.DocumentsContract;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class MainActivity extends Activity {
    GalleryAdapter adapter;
    TextView empty;
    Button export;
    GridView grid;
    File previews;
    ProgressBar progress;
    TextView subtitle;
    File thumbs;
    final ArrayList<Photo> photos = new ArrayList<>();

    /* JADX INFO: renamed from: io */
    ExecutorService f1io = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
    ExecutorService thumbIo = Executors.newFixedThreadPool(2);
    int yellow = -13520;

    /* JADX INFO: renamed from: bg */
    int f0bg = -16119027;
    int panel = -15263459;
    int muted = -6578006;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(this.f0bg);
        getWindow().setNavigationBarColor(this.f0bg);
        this.thumbs = new File(getCacheDir(), "raw_thumbnails");
        this.thumbs.mkdirs();
        this.previews = new File(getCacheDir(), "raw_previews");
        this.previews.mkdirs();
        if (getPreferences(0).getInt("setupVersion", 0) >= 4) {
            enterGallery();
        } else {
            showPermissionSetup();
        }
    }

    /* JADX INFO: renamed from: lambda$safeTop$0$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ WindowInsets m20lambda$safeTop$0$appswiftnefMainActivity(int i, View view, WindowInsets windowInsets) {
        view.setPadding(view.getPaddingLeft(), windowInsets.getSystemWindowInsetTop() + m0dp(i), view.getPaddingRight(), view.getPaddingBottom());
        return windowInsets;
    }

    void safeTop(View view, final int i) {
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda12
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                return MainActivity.this.m20lambda$safeTop$0$appswiftnefMainActivity(i, view2, windowInsets);
            }
        });
    }

    void enterGallery() {
        buildUi();
        String string = getPreferences(0).getString("tree", null);
        if (string != null) {
            scan(Uri.parse(string));
        }
    }

    LinearLayout setupShell(String str, String str2, String str3) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(1);
        linearLayout.setPadding(m0dp(26), 0, m0dp(26), m0dp(28));
        linearLayout.setBackgroundColor(this.f0bg);
        safeTop(linearLayout, 28);
        linearLayout.addView(label("SWIFTRAW  •  " + str, 12, this.yellow, 1), new LinearLayout.LayoutParams(-1, m0dp(38)));
        TextView textViewLabel = label(str2, 30, -1, 1);
        textViewLabel.setGravity(17);
        linearLayout.addView(textViewLabel, new LinearLayout.LayoutParams(-1, m0dp(90)));
        TextView textViewLabel2 = label(str3, 16, this.muted, 0);
        textViewLabel2.setGravity(17);
        linearLayout.addView(textViewLabel2, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        return linearLayout;
    }

    /* JADX INFO: renamed from: lambda$showPermissionSetup$1$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m29lambda$showPermissionSetup$1$appswiftnefMainActivity(View view) {
        choose();
    }

    void showPermissionSetup() {
        LinearLayout linearLayout = setupShell("1 OF 4", "Let’s find your photos", "SwiftRAW only needs access to the SD card folder you choose.\n\nChoose the card root and it will safely search every folder without changing your originals.");
        TextView textViewLabel = label("▣", 72, this.yellow, 1);
        textViewLabel.setGravity(17);
        linearLayout.addView(textViewLabel, 1);
        Button button = button("CHOOSE SD CARD ROOT", this.yellow, -15265024);
        button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda30
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m29lambda$showPermissionSetup$1$appswiftnefMainActivity(view);
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-1, m0dp(56)));
        setContentView(linearLayout);
    }

    /* JADX INFO: renamed from: lambda$showBrands$2$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m26lambda$showBrands$2$appswiftnefMainActivity(String str, View view) {
        getPreferences(0).edit().putString("brand", str).apply();
        showPresets();
    }

    void showBrands() {
        LinearLayout linearLayout = setupShell("2 OF 4", "What camera brand do you have?", "SwiftRAW will prioritize your camera’s proprietary RAW formats.");
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        String[] strArr = {"Nikon  •  NEF / NRW", "Canon  •  CR2 / CR3", "Sony  •  ARW / SR2 / SRF", "Fujifilm  •  RAF", "Olympus / OM System  •  ORF", "Panasonic  •  RW2 / RAW", "Pentax / Ricoh  •  PEF / DNG", "Leica  •  RWL / DNG", "Hasselblad  •  3FR / FFF", "Phase One  •  IIQ", "Sigma  •  X3F", "Samsung  •  SRW", "Kodak / Minolta / Epson", "Other / scan every RAW format"};
        for (int i = 0; i < 14; i++) {
            final String str = strArr[i];
            Button button = button(str, this.panel, -1);
            button.setAllCaps(false);
            button.setGravity(19);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, m0dp(56));
            layoutParams.setMargins(0, m0dp(4), 0, m0dp(4));
            button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda20
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m26lambda$showBrands$2$appswiftnefMainActivity(str, view);
                }
            });
            linearLayout2.addView(button, layoutParams);
        }
        scrollView.addView(linearLayout2);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 3.0f));
        setContentView(linearLayout);
    }

    /* JADX INFO: renamed from: lambda$showPresets$3$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m30lambda$showPresets$3$appswiftnefMainActivity(int i, View view) {
        getPreferences(0).edit().putInt("preset", i).apply();
        showDone();
    }

    void showPresets() {
        LinearLayout linearLayout = setupShell("3 OF 4", "How do you shoot?", "This only adjusts gallery density and how much guidance appears. You can still use every feature.");
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        String[][] strArr = {new String[]{"NEW TO RAW", "Big previews • extra hints • safest defaults"}, new String[]{"I KNOW WHAT I’M DOING", "Balanced gallery • clean controls"}, new String[]{"EXPERT", "Dense gallery • minimal guidance"}};
        for (int index = 0; index < 3; index++) {
            final int i = index;
            Button button = button(strArr[i][0] + "\n" + strArr[i][1], i == 1 ? this.yellow : this.panel, i == 1 ? -15265024 : -1);
            button.setGravity(19);
            button.setAllCaps(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, m0dp(72));
            layoutParams.setMargins(0, m0dp(6), 0, m0dp(6));
            button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda16
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    MainActivity.this.m30lambda$showPresets$3$appswiftnefMainActivity(i, view);
                }
            });
            linearLayout2.addView(button, layoutParams);
        }
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(-1, 0, 2.0f));
        setContentView(linearLayout);
    }

    /* JADX INFO: renamed from: lambda$showDone$4$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m28lambda$showDone$4$appswiftnefMainActivity(View view) {
        getPreferences(0).edit().putInt("setupVersion", 4).apply();
        enterGallery();
    }

    void showDone() {
        LinearLayout linearLayout = setupShell("4 OF 4", "You’re set!", "Your SD card is connected and SwiftRAW is ready to index, zoom and inspect your RAW photos.");
        TextView textViewLabel = label("✓", 110, -9248881, 1);
        textViewLabel.setGravity(17);
        textViewLabel.setBackground(round(-15257312, 999));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(m0dp(170), m0dp(170));
        layoutParams.setMargins(0, m0dp(12), 0, m0dp(40));
        linearLayout.addView(textViewLabel, layoutParams);
        Button button = button("OPEN MY GALLERY", this.yellow, -15265024);
        button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m28lambda$showDone$4$appswiftnefMainActivity(view);
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-1, m0dp(56)));
        setContentView(linearLayout);
    }

    void buildUi() {
        View.OnClickListener onClickListener;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(this.f0bg);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setPadding(m0dp(22), 0, m0dp(20), m0dp(18));
        safeTop(linearLayout2, 30);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setGravity(16);
        linearLayout3.addView(label("SwiftRAW", 28, -1, 1), new LinearLayout.LayoutParams(0, m0dp(52), 1.0f));
        Button button = button("OPEN SD CARD", this.yellow, -15265024);
        button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda31
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m6lambda$buildUi$5$appswiftnefMainActivity(view);
            }
        });
        linearLayout3.addView(button, new LinearLayout.LayoutParams(m0dp(148), m0dp(44)));
        linearLayout2.addView(linearLayout3);
        this.subtitle = label("Nikon RAW gallery", 14, this.muted, 0);
        linearLayout2.addView(this.subtitle, new LinearLayout.LayoutParams(-1, m0dp(38)));
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setGravity(16);
        TextView textViewLabel = label("NEF previews • decoded in RAM", 12, -3025960, 0);
        textViewLabel.setPadding(m0dp(13), 0, m0dp(13), 0);
        textViewLabel.setBackground(round(-14407891, 99));
        linearLayout4.addView(textViewLabel, new LinearLayout.LayoutParams(0, m0dp(34), 1.0f));
        this.export = button("EXPORT JPGs", -14407891, -1);
        this.export.setEnabled(false);
        this.export.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda32
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.m7lambda$buildUi$6$appswiftnefMainActivity(view);
            }
        });
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(m0dp(132), m0dp(40));
        layoutParams.setMargins(m0dp(8), 0, 0, 0);
        linearLayout4.addView(this.export, layoutParams);
        linearLayout2.addView(linearLayout4);
        linearLayout.addView(linearLayout2);
        this.progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        this.progress.setMax(1000);
        this.progress.setProgressTintList(ColorStateList.valueOf(this.yellow));
        linearLayout.addView(this.progress, new LinearLayout.LayoutParams(-1, m0dp(3)));
        FrameLayout frameLayout = new FrameLayout(this);
        this.grid = new GridView(this);
        int i = getPreferences(0).getInt("preset", 1);
        this.grid.setNumColumns(-1);
        this.grid.setColumnWidth(m0dp(i == 0 ? 185 : i == 1 ? 145 : 112));
        this.grid.setHorizontalSpacing(m0dp(3));
        this.grid.setVerticalSpacing(m0dp(3));
        this.grid.setPadding(m0dp(14), m0dp(12), m0dp(14), m0dp(106));
        this.grid.setClipToPadding(false);
        this.grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        this.adapter = new GalleryAdapter();
        this.grid.setAdapter((ListAdapter) this.adapter);
        this.grid.setOnScrollListener(new AbsListView.OnScrollListener() { // from class: app.swiftnef.MainActivity.1
            @Override // android.widget.AbsListView.OnScrollListener
            public void onScroll(AbsListView absListView, int i2, int i3, int i4) {
                if (MainActivity.this.adapter != null) {
                    MainActivity.this.adapter.trim(i2, i3);
                }
            }

            @Override // android.widget.AbsListView.OnScrollListener
            public void onScrollStateChanged(AbsListView absListView, int i2) {
            }
        });
        this.grid.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda33
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i2, long j) {
                MainActivity.this.m8lambda$buildUi$7$appswiftnefMainActivity(adapterView, view, i2, j);
            }
        });
        frameLayout.addView(this.grid, new FrameLayout.LayoutParams(-1, -1));
        this.empty = label("Insert the SD-card adapter, then tap OPEN SD CARD\n\nYou can choose the card root — SwiftNEF searches every folder.", 16, this.muted, 0);
        this.empty.setGravity(17);
        this.empty.setPadding(m0dp(35), 0, m0dp(35), m0dp(80));
        frameLayout.addView(this.empty, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setGravity(17);
        linearLayout5.setPadding(m0dp(8), m0dp(5), m0dp(8), m0dp(5));
        linearLayout5.setBackground(round(-298173117, 999));
        String[] strArr = {"▣  Photos", "▤  Albums", "⇩  Export", "☰  Menu"};
        for (int i2 = 0; i2 < 4; i2++) {
            onClickListener = null;
            String str = strArr[i2];
            TextView textViewLabel2 = label(str, 12, str.contains("Photos") ? -1 : -3091754, str.contains("Photos") ? 1 : 0);
            textViewLabel2.setGravity(17);
            if (str.contains("Export")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda34
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.m9lambda$buildUi$8$appswiftnefMainActivity(view);
                    }
                };
            } else {
                if (str.contains("Menu")) {
                    onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda35
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            MainActivity.this.m10lambda$buildUi$9$appswiftnefMainActivity(view);
                        }
                    };
                }
            }
            textViewLabel2.setOnClickListener(onClickListener);
            linearLayout5.addView(textViewLabel2, new LinearLayout.LayoutParams(0, -1, 1.0f));
        }
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-1, m0dp(66), 80);
        layoutParams2.setMargins(m0dp(30), 0, m0dp(30), m0dp(22));
        frameLayout.addView(linearLayout5, layoutParams2);
        linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        setContentView(linearLayout);
    }

    /* JADX INFO: renamed from: lambda$buildUi$5$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m6lambda$buildUi$5$appswiftnefMainActivity(View view) {
        choose();
    }

    /* JADX INFO: renamed from: lambda$buildUi$6$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m7lambda$buildUi$6$appswiftnefMainActivity(View view) {
        exportAll();
    }

    /* JADX INFO: renamed from: lambda$buildUi$7$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m8lambda$buildUi$7$appswiftnefMainActivity(AdapterView adapterView, View view, int i, long j) {
        open(this.photos.get(i));
    }

    /* JADX INFO: renamed from: lambda$buildUi$8$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m9lambda$buildUi$8$appswiftnefMainActivity(View view) {
        exportAll();
    }

    /* JADX INFO: renamed from: lambda$buildUi$9$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m10lambda$buildUi$9$appswiftnefMainActivity(View view) {
        showCacheMenu();
    }

    Button button(String str, int i, int i2) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(12.0f);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(i2);
        button.setBackground(round(i, 12));
        button.setPadding(m0dp(12), 0, m0dp(12), 0);
        return button;
    }

    /* JADX INFO: renamed from: dp */
    int m0dp(int i) {
        return (int) ((i * getResources().getDisplayMetrics().density) + 0.5f);
    }

    TextView label(String str, int i, int i2, int i3) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        textView.setGravity(16);
        if (i3 > 0) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    GradientDrawable round(int i, int i2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(m0dp(i2));
        return gradientDrawable;
    }

    void choose() {
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION), 10);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 10 && i2 == -1 && intent != null) {
            Uri data = intent.getData();
            getContentResolver().takePersistableUriPermission(data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getPreferences(0).edit().putString("tree", data.toString()).apply();
            if (getPreferences(0).getInt("setupVersion", 0) < 4) {
                showBrands();
            } else {
                scan(data);
            }
        }
    }

    static /* synthetic */ int lambda$scan$10(Photo photo, Photo photo2) {
        return (photo2.modified > photo.modified ? 1 : (photo2.modified == photo.modified ? 0 : -1));
    }

    static /* synthetic */ void lambda$scan$13(ProgressBar[] progressBarArr, int i, TextView[] textViewArr, int i2) {
        progressBarArr[0].setProgress(i);
        textViewArr[0].setText("Caching thumbnails at full speed\n" + i + " / " + i2 + " ready");
    }

    /* JADX INFO: renamed from: lambda$scan$11$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m21lambda$scan$11$appswiftnefMainActivity() {
        this.progress.setIndeterminate(false);
        this.empty.setVisibility(View.VISIBLE);
        this.empty.setText("No supported photos found below this folder.");
        this.subtitle.setText("Choose the SD-card root and try again");
    }

    /* JADX INFO: renamed from: lambda$scan$12$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m22lambda$scan$12$appswiftnefMainActivity(ArrayList arrayList, Dialog[] dialogArr, int i, TextView[] textViewArr, ProgressBar[] progressBarArr, CountDownLatch countDownLatch) {
        this.photos.addAll(arrayList);
        this.adapter.notifyDataSetChanged();
        dialogArr[0] = loadingGate(arrayList.size(), i, textViewArr, progressBarArr);
        countDownLatch.countDown();
    }

    /* JADX INFO: renamed from: lambda$scan$14$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m23lambda$scan$14$appswiftnefMainActivity(Photo photo, int[] iArr, final ProgressBar[] progressBarArr, final TextView[] textViewArr, final int i) {
        preload(photo);
        synchronized (iArr) {
            final int i2 = iArr[0] + 1;
            iArr[0] = i2;
            runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.lambda$scan$13(progressBarArr, i2, textViewArr, i);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$scan$15$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m24lambda$scan$15$appswiftnefMainActivity(Dialog[] dialogArr, ArrayList arrayList, int i, int i2) {
        dialogArr[0].dismiss();
        this.progress.setIndeterminate(false);
        this.progress.setProgress(0);
        this.export.setEnabled(true);
        this.empty.setVisibility(View.GONE);
        this.subtitle.setText(arrayList.size() + " photos • " + i + " cached • more load as you scroll");
        this.adapter.notifyDataSetChanged();
        continueCaching(arrayList, i, i2);
    }

    /* JADX INFO: renamed from: lambda$scan$16$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m25lambda$scan$16$appswiftnefMainActivity(Uri uri) {
        Runnable runnable;
        final ArrayList<Photo> arrayList = new ArrayList<>();
        walk(uri, DocumentsContract.getTreeDocumentId(uri), arrayList);
        Collections.sort(arrayList, new Comparator() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda21
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return MainActivity.lambda$scan$10((MainActivity.Photo) obj, (MainActivity.Photo) obj2);
            }
        });
        if (arrayList.isEmpty()) {
            runnable = new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda22
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m21lambda$scan$11$appswiftnefMainActivity();
                }
            };
        } else {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final Dialog[] dialogArr = new Dialog[1];
            final TextView[] textViewArr = new TextView[1];
            final ProgressBar[] progressBarArr = new ProgressBar[1];
            final int iMax = Math.max(1, (arrayList.size() + 1) / 2);
            runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda23
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m22lambda$scan$12$appswiftnefMainActivity(arrayList, dialogArr, iMax, textViewArr, progressBarArr, countDownLatch);
                }
            });
            try {
                countDownLatch.await();
            } catch (Exception e) {
            }
            final int iMax2 = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors()));
            ExecutorService executorServiceNewFixedThreadPool = Executors.newFixedThreadPool(iMax2);
            ArrayList arrayList2 = new ArrayList();
            final int[] iArr = {0};
            int i = 0;
            while (i < iMax) {
                final Photo photo = arrayList.get(i);
                final ProgressBar[] progressBarArr2 = progressBarArr;
                arrayList2.add(executorServiceNewFixedThreadPool.submit(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda24
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.m23lambda$scan$14$appswiftnefMainActivity(photo, iArr, progressBarArr2, textViewArr, iMax);
                    }
                }));
                i++;
            }
            Iterator it = arrayList2.iterator();
            while (it.hasNext()) {
                try {
                    ((Future) it.next()).get();
                } catch (Exception e2) {
                }
            }
            executorServiceNewFixedThreadPool.shutdown();
            runnable = new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda25
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m24lambda$scan$15$appswiftnefMainActivity(dialogArr, arrayList, iMax, iMax2);
                }
            };
        }
        runOnUiThread(runnable);
    }

    void scan(final Uri uri) {
        this.photos.clear();
        this.adapter.evict();
        this.adapter.notifyDataSetChanged();
        this.empty.setVisibility(View.GONE);
        this.export.setEnabled(false);
        this.subtitle.setText("Searching the SD card…");
        this.progress.setIndeterminate(true);
        this.f1io.submit(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m25lambda$scan$16$appswiftnefMainActivity(uri);
            }
        });
    }

    void continueCaching(ArrayList<Photo> arrayList, final int i, int i2) {
        if (i >= arrayList.size()) {
            return;
        }
        final ExecutorService executorServiceNewFixedThreadPool = Executors.newFixedThreadPool(Math.max(1, Math.min(2, i2 / 2)));
        final int size = arrayList.size();
        final int i3 = size - i;
        final int[] iArr = {0};
        for (int i4 = i; i4 < size; i4++) {
            final Photo photo = arrayList.get(i4);
            executorServiceNewFixedThreadPool.submit(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda13
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m13lambda$continueCaching$18$appswiftnefMainActivity(photo, iArr, i3, size, i, executorServiceNewFixedThreadPool);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$continueCaching$17$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m12lambda$continueCaching$17$appswiftnefMainActivity(int i, int i2, int i3) {
        this.subtitle.setText(i + " photos • background cache " + (i2 + i3) + " / " + i);
        this.adapter.notifyDataSetChanged();
    }

    /* JADX INFO: renamed from: lambda$continueCaching$18$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m13lambda$continueCaching$18$appswiftnefMainActivity(Photo photo, int[] iArr, int i, final int i2, final int i3, ExecutorService executorService) {
        Process.setThreadPriority(10);
        preload(photo);
        synchronized (iArr) {
            final int i4 = iArr[0] + 1;
            iArr[0] = i4;
            if (i4 % 8 == 0 || i4 == i) {
                runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda14
                    @Override // java.lang.Runnable
                    public final void run() {
                        MainActivity.this.m12lambda$continueCaching$17$appswiftnefMainActivity(i2, i3, i4);
                    }
                });
            }
            if (i4 == i) {
                executorService.shutdown();
            }
        }
    }

    Dialog loadingGate(int i, int i2, TextView[] textViewArr, ProgressBar[] progressBarArr) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);
        linearLayout.setPadding(m0dp(34), m0dp(60), m0dp(34), m0dp(60));
        linearLayout.setBackgroundColor(this.f0bg);
        TextView textViewLabel = label("SWIFTRAW", 14, this.yellow, 1);
        textViewLabel.setGravity(17);
        linearLayout.addView(textViewLabel, new LinearLayout.LayoutParams(-1, m0dp(45)));
        TextView textViewLabel2 = label("Warming up your gallery", 28, -1, 1);
        textViewLabel2.setGravity(17);
        linearLayout.addView(textViewLabel2, new LinearLayout.LayoutParams(-1, m0dp(85)));
        TextView textViewLabel3 = label("Found " + i + " photos\nPreparing the newest half before opening…", 16, this.muted, 0);
        textViewLabel3.setGravity(17);
        textViewArr[0] = textViewLabel3;
        linearLayout.addView(textViewLabel3, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        linearLayout.addView(loadingFun(), new LinearLayout.LayoutParams(Math.min(m0dp(540), getResources().getDisplayMetrics().widthPixels - m0dp(50)), m0dp(190)));
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(i2);
        progressBar.setProgressTintList(ColorStateList.valueOf(this.yellow));
        progressBarArr[0] = progressBar;
        linearLayout.addView(progressBar, new LinearLayout.LayoutParams(Math.min(m0dp(520), getResources().getDisplayMetrics().widthPixels - m0dp(68)), m0dp(8)));
        TextView textViewLabel4 = label("Maximum processing mode • screen stays awake", 13, this.muted, 0);
        textViewLabel4.setGravity(17);
        linearLayout.addView(textViewLabel4, new LinearLayout.LayoutParams(-1, m0dp(70)));
        dialog.setContentView(linearLayout);
        dialog.setCancelable(false);
        dialog.getWindow();
        dialog.show();
        dialog.getWindow().addFlags(128);
        return dialog;
    }

    /* JADX INFO: renamed from: lambda$showCacheMenu$19$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m27lambda$showCacheMenu$19$appswiftnefMainActivity(DialogInterface dialogInterface, int i) {
        boolean z;
        if (i == 0) {
            z = false;
        } else {
            z = true;
            if (i != 1) {
                if (i == 2) {
                    choose();
                    return;
                }
                return;
            }
        }
        confirmRecache(z);
    }

    void showCacheMenu() {
        new AlertDialog.Builder(this).setTitle("SwiftRAW cache").setItems(new String[]{"Rebuild thumbnails", "Rebuild thumbnails + RAW previews", "Choose another SD-card folder", "Cancel"}, new DialogInterface.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda15
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m27lambda$showCacheMenu$19$appswiftnefMainActivity(dialogInterface, i);
            }
        }).show();
    }

    void confirmRecache(final boolean z) {
        new AlertDialog.Builder(this).setTitle(z ? "Rebuild all caches?" : "Rebuild thumbnails?").setMessage((z ? "This rebuilds every thumbnail and full RAW preview." : "This rebuilds the small gallery thumbnails but keeps full RAW previews.") + "\n\nOriginal photos are never changed.").setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).setPositiveButton("Recache", new DialogInterface.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.m11lambda$confirmRecache$20$appswiftnefMainActivity(z, dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: renamed from: lambda$confirmRecache$20$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m11lambda$confirmRecache$20$appswiftnefMainActivity(boolean z, DialogInterface dialogInterface, int i) {
        recache(z);
    }

    void recache(boolean z) {
        clearCacheFiles(this.thumbs);
        if (z) {
            clearCacheFiles(this.previews);
        }
        this.adapter.evict();
        this.adapter.notifyDataSetChanged();
        String string = getPreferences(0).getString("tree", null);
        if (string != null) {
            scan(Uri.parse(string));
        } else {
            choose();
        }
    }

    void clearCacheFiles(File file) {
        File[] fileArrListFiles = file.listFiles();
        if (fileArrListFiles == null) {
            return;
        }
        for (File file2 : fileArrListFiles) {
            if (file2.isFile()) {
                file2.delete();
            }
        }
    }

    View loadingFun() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(new RunnerGame(this), new FrameLayout.LayoutParams(-1, -1));
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(this.yellow));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(m0dp(32), m0dp(32), 53);
        layoutParams.setMargins(0, m0dp(8), m0dp(8), 0);
        frameLayout.addView(progressBar, layoutParams);
        return frameLayout;
    }

    File previewFile(Photo photo) {
        try {
            return new File(this.previews, sha(photo.uri.toString() + ":" + photo.modified + ":preview-v4") + ".jpg");
        } catch (Exception e) {
            return new File(this.previews, Integer.toHexString(photo.uri.toString().hashCode()) + ".jpg");
        }
    }

    byte[] cachedRawPreview(Photo photo) throws Exception {
        File filePreviewFile = previewFile(photo);
        if (filePreviewFile.length() > 122880) {
            return readFile(filePreviewFile);
        }
        byte[] bArrJpegFast = jpegFast(photo.uri);
        File file = new File(filePreviewFile.getPath() + "." + Thread.currentThread().getId() + ".tmp");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            fileOutputStream.write(bArrJpegFast);
            fileOutputStream.close();
            if (!file.renameTo(filePreviewFile)) {
                file.delete();
            }
            return bArrJpegFast;
        } catch (Throwable th) {
            try {
                fileOutputStream.close();
            } catch (Throwable th2) {
                Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
            }
            throw th;
        }
    }

    byte[] readFile(File file) throws Exception {
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file), 131072);
             ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(Integer.MAX_VALUE, file.length()))) {
            byte[] buffer = new byte[131072];
            int count;
            while ((count = input.read(buffer)) > 0) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    boolean largeEnough(byte[] bArr) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        return Math.max(options.outWidth, options.outHeight) >= 2000;
    }

    File thumbFile(Photo photo) {
        try {
            return new File(this.thumbs, sha(photo.uri.toString() + ":" + photo.modified + ":thumb-v2") + ".jpg");
        } catch (Exception e) {
            return new File(this.thumbs, Integer.toHexString(photo.uri.toString().hashCode()) + ".jpg");
        }
    }

    String sha(String str) throws Exception {
        byte[] bArrDigest = MessageDigest.getInstance("SHA-1").digest(str.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : bArrDigest) {
            sb.append(String.format(Locale.US, "%02x", Byte.valueOf(b)));
        }
        return sb.toString();
    }

    void preload(Photo photo) {
        if (!photo.normal) {
            if (previewFile(photo).length() < 122880) {
                try {
                    cachedRawPreview(photo);
                    return;
                } catch (Exception e) {
                    return;
                }
            }
            return;
        }
        File fileThumbFile = thumbFile(photo);
        if (fileThumbFile.length() > 1024) {
            return;
        }
        Bitmap bitmapDecodeNormalThumb = null;
        try {
            bitmapDecodeNormalThumb = DocumentsContract.getDocumentThumbnail(getContentResolver(), photo.uri, new Point(1024, 768), null);
        } catch (Exception e2) {
        }
        if (bitmapDecodeNormalThumb == null) {
            try {
                bitmapDecodeNormalThumb = decodeNormalThumb(photo.uri);
            } catch (Exception e3) {
                fileThumbFile.delete();
                return;
            }
        }
        if (bitmapDecodeNormalThumb == null) {
            return;
        }
        int width = bitmapDecodeNormalThumb.getWidth();
        int height = bitmapDecodeNormalThumb.getHeight();
        if (width > 1024) {
            Bitmap bitmapCreateScaledBitmap = Bitmap.createScaledBitmap(bitmapDecodeNormalThumb, 1024, Math.max(1, (height * 1024) / width), true);
            if (bitmapCreateScaledBitmap != bitmapDecodeNormalThumb) {
                bitmapDecodeNormalThumb.recycle();
            }
            bitmapDecodeNormalThumb = bitmapCreateScaledBitmap;
        }
        File file = new File(fileThumbFile.getPath() + "." + Thread.currentThread().getId() + ".tmp");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            bitmapDecodeNormalThumb.compress(Bitmap.CompressFormat.JPEG, 92, fileOutputStream);
        } catch (IOException ignored) {
            file.delete();
            bitmapDecodeNormalThumb.recycle();
            return;
        }
        bitmapDecodeNormalThumb.recycle();
        if (!file.renameTo(fileThumbFile)) {
            file.delete();
        }
    }

    Bitmap decodeThumb(byte[] bArr) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options);
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = 1;
        int iMax = Math.max(options.outWidth, options.outHeight);
        while (iMax / options2.inSampleSize > 1400) {
            options2.inSampleSize *= 2;
        }
        return BitmapFactory.decodeByteArray(bArr, 0, bArr.length, options2);
    }

    Bitmap decodeNormalThumb(Uri uri) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(getContentResolver().openInputStream(uri), 131072);
        try {
            BitmapFactory.decodeStream(bufferedInputStream, null, options);
            bufferedInputStream.close();
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = 1;
            int iMax = Math.max(options.outWidth, options.outHeight);
            while (iMax / options2.inSampleSize > 1400) {
                options2.inSampleSize *= 2;
            }
            BufferedInputStream bufferedInputStream2 = new BufferedInputStream(getContentResolver().openInputStream(uri), 131072);
            try {
                Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(bufferedInputStream2, null, options2);
                bufferedInputStream2.close();
                return bitmapDecodeStream;
            } catch (Throwable th) {
                try {
                    bufferedInputStream2.close();
                } catch (Throwable th2) {
                    Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                bufferedInputStream.close();
            } catch (Throwable th4) {
                Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th3, th4);
            }
            throw th3;
        }
    }

    Bitmap decodePreviewFile(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = 1;
        int iMax = Math.max(options.outWidth, options.outHeight);
        while (iMax / options2.inSampleSize > 1400) {
            options2.inSampleSize *= 2;
        }
        return BitmapFactory.decodeFile(file.getPath(), options2);
    }

    HashSet<String> extensions() {
        String str;
        String string = getPreferences(0).getString("brand", "Other");
        if (string.startsWith("Nikon")) {
            str = "nef nrw";
        } else if (string.startsWith("Canon")) {
            str = "cr2 cr3";
        } else if (string.startsWith("Sony")) {
            str = "arw sr2 srf";
        } else if (string.startsWith("Fujifilm")) {
            str = "raf";
        } else if (string.startsWith("Olympus")) {
            str = "orf";
        } else if (string.startsWith("Panasonic")) {
            str = "rw2 raw";
        } else if (string.startsWith("Pentax")) {
            str = "pef dng";
        } else if (string.startsWith("Leica")) {
            str = "rwl dng";
        } else if (string.startsWith("Hasselblad")) {
            str = "3fr fff";
        } else if (string.startsWith("Phase")) {
            str = "iiq";
        } else if (string.startsWith("Sigma")) {
            str = "x3f";
        } else if (string.startsWith("Samsung")) {
            str = "srw";
        } else {
            str = string.startsWith("Kodak") ? "dcr kdc mrw erf" : "nef nrw cr2 cr3 arw sr2 srf raf orf rw2 raw pef dng rwl 3fr fff iiq x3f srw dcr kdc mrw erf mos mef bay cap";
        }
        return new HashSet<>(Arrays.asList((str + " jpg jpeg png webp heic heif tif tiff bmp").split(" ")));
    }

    boolean normal(String str) {
        return Arrays.asList("jpg", "jpeg", "png", "webp", "heic", "heif", "tif", "tiff", "bmp").contains(str.substring(str.lastIndexOf(46) + 1).toLowerCase(Locale.ROOT));
    }

    void walk(Uri uri, String str, ArrayList<Photo> arrayList) {
        Uri uriBuildChildDocumentsUriUsingTree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, str);
        HashSet<String> hashSetExtensions = extensions();
        try {
            Cursor cursorQuery = getContentResolver().query(uriBuildChildDocumentsUriUsingTree, new String[]{"document_id", "_display_name", "mime_type", "last_modified", "_size"}, null, null, null);
            if (cursorQuery == null) {
                if (cursorQuery != null) {
                    cursorQuery.close();
                    return;
                }
                return;
            }
            while (cursorQuery.moveToNext()) {
                try {
                    String string = cursorQuery.getString(0);
                    String string2 = cursorQuery.getString(1);
                    String string3 = cursorQuery.getString(2);
                    long j = cursorQuery.getLong(3);
                    long j2 = cursorQuery.getLong(4);
                    if ("vnd.android.document/directory".equals(string3)) {
                        walk(uri, string, arrayList);
                    } else if (string2 != null && string2.lastIndexOf(46) > 0 && hashSetExtensions.contains(string2.substring(string2.lastIndexOf(46) + 1).toLowerCase(Locale.ROOT))) {
                        arrayList.add(new Photo(DocumentsContract.buildDocumentUriUsingTree(uri, string), string2, j, j2, normal(string2)));
                    }
                } catch (Throwable th) {
                    if (cursorQuery == null) {
                        throw th;
                    }
                    try {
                        cursorQuery.close();
                        throw th;
                    } catch (Throwable th2) {
                        Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                        throw th;
                    }
                }
            }
            if (cursorQuery != null) {
                cursorQuery.close();
            }
        } catch (Exception e) {
        }
    }

    byte[] jpegFast(Uri uri) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(getContentResolver().openInputStream(uri), 262144);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr = new byte[262144];
                ByteArrayOutputStream byteArrayOutputStream2 = null;
                int i = -1;
                while (true) {
                    int i2 = bufferedInputStream.read(bArr);
                    if (i2 <= 0) {
                        if (byteArrayOutputStream.size() < 1024) {
                            throw new IOException("No embedded preview");
                        }
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        if (!largeEnough(byteArray)) {
                            byte[] bArrJpeg = jpeg(uri);
                            if (largeEnough(bArrJpeg)) {
                                byteArrayOutputStream.close();
                                bufferedInputStream.close();
                                return bArrJpeg;
                            }
                        }
                        byteArrayOutputStream.close();
                        bufferedInputStream.close();
                        return byteArray;
                    }
                    for (int i3 = 0; i3 < i2; i3++) {
                        int i4 = bArr[i3] & 255;
                        if (i == 255 && i4 == 216) {
                            byteArrayOutputStream2 = new ByteArrayOutputStream(524288);
                            byteArrayOutputStream2.write(255);
                            byteArrayOutputStream2.write(216);
                            i = -1;
                        } else {
                            if (byteArrayOutputStream2 != null) {
                                byteArrayOutputStream2.write(i4);
                                if (i == 255 && i4 == 217) {
                                    if (byteArrayOutputStream2.size() >= 122880) {
                                        byte[] byteArray2 = byteArrayOutputStream2.toByteArray();
                                        if (largeEnough(byteArray2)) {
                                            byteArrayOutputStream.close();
                                            bufferedInputStream.close();
                                            return byteArray2;
                                        }
                                    }
                                    if (byteArrayOutputStream2.size() > byteArrayOutputStream.size()) {
                                        byteArrayOutputStream.reset();
                                        byteArrayOutputStream2.writeTo(byteArrayOutputStream);
                                    }
                                    byteArrayOutputStream2 = null;
                                }
                            }
                            i = i4;
                        }
                    }
                }
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                bufferedInputStream.close();
            } catch (Throwable th4) {
                Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th3, th4);
            }
            throw th3;
        }
    }

    byte[] jpeg(Uri uri) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(getContentResolver().openInputStream(uri), 262144);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr = new byte[262144];
                ByteArrayOutputStream byteArrayOutputStream2 = null;
                int i = -1;
                while (true) {
                    int i2 = bufferedInputStream.read(bArr);
                    if (i2 <= 0) {
                        break;
                    }
                    for (int i3 = 0; i3 < i2; i3++) {
                        int i4 = bArr[i3] & 255;
                        if (i == 255 && i4 == 216) {
                            byteArrayOutputStream2 = new ByteArrayOutputStream(1048576);
                            byteArrayOutputStream2.write(255);
                            byteArrayOutputStream2.write(216);
                            i = -1;
                        } else {
                            if (byteArrayOutputStream2 != null) {
                                byteArrayOutputStream2.write(i4);
                                if (i == 255 && i4 == 217) {
                                    if (byteArrayOutputStream2.size() > byteArrayOutputStream.size()) {
                                        byteArrayOutputStream.reset();
                                        byteArrayOutputStream2.writeTo(byteArrayOutputStream);
                                    }
                                    byteArrayOutputStream2 = null;
                                }
                            }
                            i = i4;
                        }
                    }
                }
                if (byteArrayOutputStream.size() < 1024) {
                    throw new IOException("No embedded preview");
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                bufferedInputStream.close();
                return byteArray;
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                bufferedInputStream.close();
            } catch (Throwable th4) {
                Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th3, th4);
            }
            throw th3;
        }
    }

    /* JADX INFO: renamed from: lambda$open$23$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m17lambda$open$23$appswiftnefMainActivity(Dialog dialog, ZoomImage zoomImage, Bitmap bitmap, TextView textView, String str) {
        if (dialog.isShowing()) {
            zoomImage.setImageBitmap(bitmap);
            zoomImage.resetView();
            textView.setText(str);
        }
        this.subtitle.setText(this.photos.size() + " photos");
    }

    /* JADX INFO: renamed from: lambda$open$24$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m18lambda$open$24$appswiftnefMainActivity(TextView textView, Photo photo) {
        textView.setText(photo.name + "\nFull image unavailable — cached thumbnail shown");
        this.subtitle.setText("Opened cached preview");
    }

    /* JADX INFO: renamed from: lambda$open$25$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m19lambda$open$25$appswiftnefMainActivity(final Photo photo, final Dialog dialog, final ZoomImage zoomImage, final TextView textView) {
        Bitmap bitmapDecodeByteArray;
        String strMetadata;
        try {
            if (photo.normal) {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(getContentResolver().openInputStream(photo.uri), 262144);
                try {
                    bitmapDecodeByteArray = BitmapFactory.decodeStream(bufferedInputStream);
                    bufferedInputStream.close();
                } catch (Throwable th) {
                    try {
                        bufferedInputStream.close();
                    } catch (Throwable th2) {
                        Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                    }
                    throw th;
                }
            } else {
                byte[] bArrCachedRawPreview = cachedRawPreview(photo);
                bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArrCachedRawPreview, 0, bArrCachedRawPreview.length);
            }
            final Bitmap bitmap = bitmapDecodeByteArray;
            byte[] prefix = readPrefix(photo.uri, 4194304);
            if (photo.normal) {
                StringBuilder sbAppend = new StringBuilder().append(photo.name).append("\n");
                Locale locale = Locale.US;
                double d = photo.size;
                Double.isNaN(d);
                strMetadata = sbAppend.append(String.format(locale, "%.1f MB", Double.valueOf(d / 1048576.0d))).append(" • Standard image\nPinch to zoom • drag to pan").toString();
            } else {
                strMetadata = metadata(prefix, photo);
            }
            final String str = strMetadata;
            runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m17lambda$open$23$appswiftnefMainActivity(dialog, zoomImage, bitmap, textView, str);
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.this.m18lambda$open$24$appswiftnefMainActivity(textView, photo);
                }
            });
        }
    }

    void open(final Photo photo) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        FrameLayout frameLayout = new FrameLayout(this);
        final ZoomImage zoomImage = new ZoomImage(this);
        File fileThumbFile = (photo.normal || previewFile(photo).length() <= 1024) ? thumbFile(photo) : previewFile(photo);
        if (fileThumbFile.length() > 1024) {
            zoomImage.setImageBitmap(BitmapFactory.decodeFile(fileThumbFile.getPath()));
        }
        frameLayout.addView(zoomImage, new FrameLayout.LayoutParams(-1, -1));
        addZoomPad(frameLayout, zoomImage);
        addNavigator(frameLayout, zoomImage);
        final TextView textViewLabel = label(photo.name + "\nLoading full image and metadata…", 13, -1, 0);
        textViewLabel.setPadding(m0dp(18), m0dp(12), m0dp(18), m0dp(12));
        textViewLabel.setBackgroundColor(-871296490);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2, 80);
        layoutParams.setMargins(m0dp(14), 0, m0dp(14), m0dp(250));
        frameLayout.addView(textViewLabel, layoutParams);
        Button button = button("×", -1440471509, -1);
        button.setTextSize(24.0f);
        button.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda27
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(m0dp(50), m0dp(50), 53);
        layoutParams2.setMargins(0, m0dp(28), m0dp(18), 0);
        frameLayout.addView(button, layoutParams2);
        textViewLabel.setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda28
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                textViewLabel.setVisibility(View.GONE);
            }
        });
        dialog.setContentView(frameLayout);
        dialog.show();
        this.subtitle.setText("Opening " + photo.name + "…");
        this.f1io.submit(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda29
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m19lambda$open$25$appswiftnefMainActivity(photo, dialog, zoomImage, textViewLabel);
            }
        });
    }

    void addZoomPad(FrameLayout frameLayout, final ZoomImage zoomImage) {
        View.OnClickListener onClickListener;
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(3);
        gridLayout.setPadding(m0dp(5), m0dp(5), m0dp(5), m0dp(5));
        gridLayout.setBackground(round(-584504270, 999));
        int i = 9;
        int i2 = 1;
        String[] strArr = {"−", "↑", "+", "←", "●", "→", "", "↓", ""};
        int i3 = 0;
        while (i3 < i) {
            onClickListener = null;
            String str = strArr[i3];
            TextView textViewLabel = label(str, 22, -1, i2);
            textViewLabel.setGravity(17);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            String[] strArr2 = strArr;
            layoutParams.width = m0dp(43);
            layoutParams.height = m0dp(43);
            textViewLabel.setLayoutParams(layoutParams);
            if (str.equals("+")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda5
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        zoomImage.zoomBy(1.6f);
                    }
                };
            } else if (str.equals("−")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda6
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        zoomImage.zoomBy(0.625f);
                    }
                };
            } else if (str.equals("↑")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda7
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.m2lambda$addZoomPad$28$appswiftnefMainActivity(zoomImage, view);
                    }
                };
            } else if (str.equals("↓")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda8
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.m3lambda$addZoomPad$29$appswiftnefMainActivity(zoomImage, view);
                    }
                };
            } else if (str.equals("←")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda9
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.m4lambda$addZoomPad$30$appswiftnefMainActivity(zoomImage, view);
                    }
                };
            } else if (str.equals("→")) {
                onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda10
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.m5lambda$addZoomPad$31$appswiftnefMainActivity(zoomImage, view);
                    }
                };
            } else {
                if (str.equals("●")) {
                    onClickListener = new View.OnClickListener() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda11
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            zoomImage.resetView();
                        }
                    };
                }
            }
            textViewLabel.setOnClickListener(onClickListener);
            gridLayout.addView(textViewLabel);
            i3++;
            strArr = strArr2;
            i = 9;
            i2 = 1;
        }
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(m0dp(139), m0dp(139), 83);
        layoutParams2.setMargins(m0dp(18), 0, 0, m0dp(176));
        frameLayout.addView(gridLayout, layoutParams2);
        zoomImage.setPad(gridLayout);
    }

    /* JADX INFO: renamed from: lambda$addZoomPad$28$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m2lambda$addZoomPad$28$appswiftnefMainActivity(ZoomImage zoomImage, View view) {
        zoomImage.panBy(0.0f, m0dp(70));
    }

    /* JADX INFO: renamed from: lambda$addZoomPad$29$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m3lambda$addZoomPad$29$appswiftnefMainActivity(ZoomImage zoomImage, View view) {
        zoomImage.panBy(0.0f, -m0dp(70));
    }

    /* JADX INFO: renamed from: lambda$addZoomPad$30$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m4lambda$addZoomPad$30$appswiftnefMainActivity(ZoomImage zoomImage, View view) {
        zoomImage.panBy(m0dp(70), 0.0f);
    }

    /* JADX INFO: renamed from: lambda$addZoomPad$31$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m5lambda$addZoomPad$31$appswiftnefMainActivity(ZoomImage zoomImage, View view) {
        zoomImage.panBy(-m0dp(70), 0.0f);
    }

    void addNavigator(FrameLayout frameLayout, ZoomImage zoomImage) {
        ZoomNavigator zoomNavigator = new ZoomNavigator(this, zoomImage);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(m0dp(160), m0dp(112), 85);
        layoutParams.setMargins(0, 0, m0dp(18), m0dp(120));
        frameLayout.addView(zoomNavigator, layoutParams);
        zoomImage.setNavigator(zoomNavigator);
    }

    byte[] readPrefix(Uri uri, int i) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(getContentResolver().openInputStream(uri), 262144);
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(i);
            try {
                byte[] bArr = new byte[262144];
                while (i > 0) {
                    int i2 = bufferedInputStream.read(bArr, 0, Math.min(i, 262144));
                    if (i2 <= 0) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i2);
                    i -= i2;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                bufferedInputStream.close();
                return byteArray;
            } catch (Throwable th) {
                try {
                    byteArrayOutputStream.close();
                } catch (Throwable th2) {
                    Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th, th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                bufferedInputStream.close();
            } catch (Throwable th4) {
                Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class).invoke(th3, th4);
            }
            throw th3;
        }
    }

    byte[] readAll(Uri uri) throws Exception {
        try (BufferedInputStream input = new BufferedInputStream(getContentResolver().openInputStream(uri), 262144);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[262144];
            int count;
            while ((count = input.read(buffer)) > 0) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        }
    }

    byte[] jpeg(byte[] bArr) throws Exception {
        int i = 0;
        int i2 = -1;
        int i3 = -1;
        int i4 = -1;
        while (true) {
            int i5 = i + 1;
            if (i5 >= bArr.length) {
                break;
            }
            if ((bArr[i] & 255) == 255 && (bArr[i5] & 255) == 216) {
                i4 = i;
            }
            if (i4 >= 0 && (bArr[i] & 255) == 255 && (bArr[i5] & 255) == 217) {
                if (i2 < 0 || (i + 2) - i4 > i3 - i2) {
                    i3 = i + 2;
                    i2 = i4;
                }
                i4 = -1;
            }
            i = i5;
        }
        if (i2 >= 0) {
            return Arrays.copyOfRange(bArr, i2, i3);
        }
        throw new IOException("No JPEG preview");
    }

    String metadata(byte[] bArr, Photo photo) {
        StringBuilder sb = new StringBuilder(photo.name);
        StringBuilder sbAppend = sb.append("\n");
        Locale locale = Locale.US;
        double d = photo.size;
        Double.isNaN(d);
        Double dValueOf = Double.valueOf(d / 1048576.0d);
        boolean z = true;
        sbAppend.append(String.format(locale, "%.1f MB", dValueOf));
        try {
            if (bArr[0] != 73 || bArr[1] != 73) {
                z = false;
            }
            int iI32 = i32(bArr, 4, z);
            HashMap<Integer, String> map = new HashMap<>();
            parseIfd(bArr, iI32, z, map);
            int iTagInt = tagInt(bArr, iI32, 34665, z);
            if (iTagInt > 0) {
                parseIfd(bArr, iTagInt, z, map);
            }
            String str = map.get(271);
            String str2 = map.get(272);
            String str3 = map.get(36867);
            String str4 = map.get(34855);
            String str5 = map.get(33434);
            String str6 = map.get(33437);
            String str7 = map.get(37386);
            if (str != null || str2 != null) {
                String strTrim = "";
                StringBuilder sbAppend2 = sb.append("\n").append(str == null ? "" : str.trim() + " ");
                if (str2 != null) {
                    strTrim = str2.trim();
                }
                sbAppend2.append(strTrim);
            }
            if (str3 != null) {
                sb.append("  •  ").append(str3.trim());
            }
            sb.append("\n");
            if (str5 != null) {
                sb.append(str5).append("s  ");
            }
            if (str6 != null) {
                sb.append("f/").append(str6).append("  ");
            }
            if (str4 != null) {
                sb.append("ISO ").append(str4).append("  ");
            }
            if (str7 != null) {
                sb.append(str7).append("mm");
            }
        } catch (Exception e) {
        }
        sb.append("\nPinch to zoom • drag to pan • tap info to hide");
        return sb.toString();
    }

    /* JADX WARN: Code duplicated, block: B:42:0x00eb  */
    void parseIfd(byte[] bArr, int i, boolean z, HashMap<Integer, String> map) {
        int i2;
        int i3;
        Integer numValueOf;
        String strValueOf;
        if (i < 0 || (i2 = i + 2) > bArr.length) {
            return;
        }
        int iU16 = u16(bArr, i, z);
        int i4 = 0;
        int i5 = 0;
        while (i5 < iU16) {
            numValueOf = null;
            strValueOf = null;
            int i6 = (i5 * 12) + i2;
            if (i6 + 12 > bArr.length) {
                return;
            }
            int iU17 = u16(bArr, i6, z);
            int iU18 = u16(bArr, i6 + 2, z);
            int iI32 = i32(bArr, i6 + 4, z);
            int iI33 = ((iU18 != 3 || iI32 > 2) && !(iU18 == 4 && iI32 == 1)) ? i32(bArr, i6 + 8, z) : i6 + 8;
            if (iI33 < 0) {
                i3 = i5;
            } else if (iI33 < bArr.length) {
                if (iU18 == 2) {
                    int iMin = Math.min(bArr.length, iI32 + iI33);
                    numValueOf = Integer.valueOf(iU17);
                    strValueOf = new String(bArr, iI33, Math.max(i4, iMin - iI33)).replace("\u0000", "");
                } else if (iU18 == 3) {
                    numValueOf = Integer.valueOf(iU17);
                    strValueOf = String.valueOf(u16(bArr, iI33, z));
                } else if (iU18 != 5 || iI33 + 8 > bArr.length) {
                    i3 = i5;
                } else {
                    long jI32 = ((long) i32(bArr, iI33, z)) & 4294967295L;
                    i3 = i5;
                    long jI33 = ((long) i32(bArr, iI33 + 4, z)) & 4294967295L;
                    if (jI33 > 0) {
                        double d = jI32;
                        double d2 = jI33;
                        Double.isNaN(d);
                        Double.isNaN(d2);
                        double d3 = d / d2;
                        map.put(Integer.valueOf(iU17), (iU17 != 33434 || d3 >= 1.0d) ? String.format(Locale.US, "%.1f", Double.valueOf(d3)) : "1/" + Math.round(1.0d / d3));
                    }
                }
                if (numValueOf != null && strValueOf != null) {
                    map.put(numValueOf, strValueOf);
                }
                i3 = i5;
            } else {
                i3 = i5;
            }
            i5 = i3 + 1;
            i4 = 0;
        }
    }

    int i32(byte[] bArr, int i, boolean z) {
        int i2;
        int i3;
        if (z) {
            i2 = (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << 16);
            i3 = (bArr[i + 3] & 255) << 24;
        } else {
            i2 = ((bArr[i] & 255) << 24) | ((bArr[i + 1] & 255) << 16) | ((bArr[i + 2] & 255) << 8);
            i3 = bArr[i + 3] & 255;
        }
        return i3 | i2;
    }

    int tagInt(byte[] bArr, int i, int i2, boolean z) {
        int iU16 = u16(bArr, i, z);
        for (int i3 = 0; i3 < iU16; i3++) {
            int i4 = i + 2 + (i3 * 12);
            if (i4 + 12 <= bArr.length && u16(bArr, i4, z) == i2) {
                return i32(bArr, i4 + 8, z);
            }
        }
        return 0;
    }

    int u16(byte[] bArr, int i, boolean z) {
        int i2;
        int i3;
        if (z) {
            i2 = bArr[i] & 255;
            i3 = (bArr[i + 1] & 255) << 8;
        } else {
            i2 = (bArr[i] & 255) << 8;
            i3 = bArr[i + 1] & 255;
        }
        return i3 | i2;
    }

    class GalleryAdapter extends BaseAdapter {
        LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(65536) { // from class: app.swiftnef.MainActivity.GalleryAdapter.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.util.LruCache
            public int sizeOf(String str, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        HashSet<String> loading = new HashSet<>();

        GalleryAdapter() {
        }

        void evict() {
            this.cache.evictAll();
            this.loading.clear();
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return MainActivity.this.photos.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return MainActivity.this.photos.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView imageView;
            int i2;
            FrameLayout frameLayout = view instanceof FrameLayout ? (FrameLayout) view : new FrameLayout(MainActivity.this);
            if (frameLayout.getChildCount() == 0) {
                imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                frameLayout.addView(imageView, new FrameLayout.LayoutParams(-1, -1));
                TextView textViewLabel = MainActivity.this.label("", 12, -1, 1);
                textViewLabel.setPadding(MainActivity.this.m0dp(9), MainActivity.this.m0dp(18), MainActivity.this.m0dp(7), MainActivity.this.m0dp(4));
                textViewLabel.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0, -1157627904}));
                frameLayout.addView(textViewLabel, new FrameLayout.LayoutParams(-1, MainActivity.this.m0dp(52), 80));
            } else {
                imageView = (ImageView) frameLayout.getChildAt(0);
            }
            final ImageView imageView2 = imageView;
            int i3 = MainActivity.this.getPreferences(0).getInt("preset", 1);
            int i4 = MainActivity.this.getResources().getDisplayMetrics().widthPixels;
            MainActivity mainActivity = MainActivity.this;
            if (i3 == 0) {
                i2 = 185;
            } else {
                i2 = i3 == 1 ? 145 : 112;
            }
            int iMax = Math.max(1, i4 / mainActivity.m0dp(i2));
            frameLayout.setLayoutParams(new AbsListView.LayoutParams(-1, Math.max(MainActivity.this.m0dp(90), (((i4 - (MainActivity.this.m0dp(3) * (iMax + 1))) / iMax) * 3) / 4)));
            final Photo photo = MainActivity.this.photos.get(i);
            ((TextView) frameLayout.getChildAt(1)).setText(i3 != 2 ? photo.name : "");
            final String string = photo.uri.toString();
            frameLayout.setTag(string);
            Bitmap bitmap = this.cache.get(string);
            if (bitmap != null) {
                imageView2.setImageBitmap(bitmap);
            } else {
                File fileThumbFile = photo.normal ? MainActivity.this.thumbFile(photo) : MainActivity.this.previewFile(photo);
                if (fileThumbFile.length() > 1024) {
                    Bitmap bitmapDecodeFile = photo.normal ? BitmapFactory.decodeFile(fileThumbFile.getPath()) : MainActivity.this.decodePreviewFile(fileThumbFile);
                    if (bitmapDecodeFile != null) {
                        this.cache.put(string, bitmapDecodeFile);
                        imageView2.setImageBitmap(bitmapDecodeFile);
                    }
                } else {
                    imageView2.setImageDrawable(null);
                    if (this.loading.add(string)) {
                        final FrameLayout frameLayout2 = frameLayout;
                        MainActivity.this.thumbIo.submit(new Runnable() { // from class: app.swiftnef.MainActivity$GalleryAdapter$$ExternalSyntheticLambda0
                            @Override // java.lang.Runnable
                            public final void run() {
                                GalleryAdapter.this.m32lambda$getView$1$appswiftnefMainActivity$GalleryAdapter(photo, string, frameLayout2, imageView2);
                            }
                        });
                    }
                }
            }
            return frameLayout;
        }

        /* JADX INFO: renamed from: lambda$getView$0$app-swiftnef-MainActivity$GalleryAdapter, reason: not valid java name */
        /* synthetic */ void m31lambda$getView$0$appswiftnefMainActivity$GalleryAdapter(String str, FrameLayout frameLayout, Photo photo, ImageView imageView) {
            if (str.equals(frameLayout.getTag())) {
                File fileThumbFile = photo.normal ? MainActivity.this.thumbFile(photo) : MainActivity.this.previewFile(photo);
                Bitmap bitmapDecodeFile = photo.normal ? BitmapFactory.decodeFile(fileThumbFile.getPath()) : MainActivity.this.decodePreviewFile(fileThumbFile);
                if (bitmapDecodeFile != null) {
                    this.cache.put(str, bitmapDecodeFile);
                    imageView.setImageBitmap(bitmapDecodeFile);
                }
            }
        }

        /* JADX INFO: renamed from: lambda$getView$1$app-swiftnef-MainActivity$GalleryAdapter, reason: not valid java name */
        /* synthetic */ void m32lambda$getView$1$appswiftnefMainActivity$GalleryAdapter(final Photo photo, final String str, final FrameLayout frameLayout, final ImageView imageView) {
            MainActivity.this.preload(photo);
            this.loading.remove(str);
            MainActivity.this.runOnUiThread(new Runnable() { // from class: app.swiftnef.MainActivity$GalleryAdapter$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    GalleryAdapter.this.m31lambda$getView$0$appswiftnefMainActivity$GalleryAdapter(str, frameLayout, photo, imageView);
                }
            });
        }

        void trim(int i, int i2) {
            if (i2 <= 0) {
                return;
            }
            int iMin = Math.min(MainActivity.this.photos.size(), i + i2 + 8);
            HashSet hashSet = new HashSet();
            for (int iMax = Math.max(0, i - 8); iMax < iMin; iMax++) {
                hashSet.add(MainActivity.this.photos.get(iMax).uri.toString());
            }
            for (String str : new HashSet<String>(this.cache.snapshot().keySet())) {
                if (!hashSet.contains(str)) {
                    this.cache.remove(str);
                }
            }
        }
    }

    void exportAll() {
        this.export.setEnabled(false);
        this.progress.setProgress(0);
        final File file = new File(getExternalFilesDir(null), "Converted");
        file.mkdirs();
        this.f1io.submit(new Runnable() { // from class: app.swiftnef.MainActivity$$ExternalSyntheticLambda36
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m16lambda$exportAll$35$appswiftnefMainActivity(file);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$exportAll$33$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m14lambda$exportAll$33$appswiftnefMainActivity(int i, int i2) {
        this.progress.setProgress((i * 1000) / Math.max(1, i2));
        this.subtitle.setText("Exporting JPGs • " + i + " / " + i2);
    }

    /* JADX INFO: renamed from: lambda$exportAll$34$app-swiftnef-MainActivity, reason: not valid java name */
    /* synthetic */ void m15lambda$exportAll$34$appswiftnefMainActivity(int i) {
        this.export.setEnabled(true);
        this.subtitle.setText("Exported " + i + " JPGs to Android/data/app.swiftnef/files/Converted");
        Toast.makeText(this, "Export complete", Toast.LENGTH_LONG).show();
    }

    /* synthetic */ void m16lambda$exportAll$35$appswiftnefMainActivity(File file) {
        final int size = this.photos.size();
        int completed = 0;
        for (int index = 0; index < size; index++) {
            Photo photo = this.photos.get(index);
            try (FileOutputStream output = new FileOutputStream(new File(file, photo.name.replaceAll("(?i)\\.[^.]+$", ".jpg")))) {
                output.write(jpeg(photo.uri));
                completed++;
            } catch (Exception ignored) {
            }
            final int progress = index + 1;
            runOnUiThread(() -> m14lambda$exportAll$33$appswiftnefMainActivity(progress, size));
        }
        final int exported = completed;
        runOnUiThread(() -> m15lambda$exportAll$34$appswiftnefMainActivity(exported));
    }

    static class RunnerGame extends View {
        boolean dead;
        long last;
        float obstacle;

        /* JADX INFO: renamed from: p */
        Paint f2p;
        boolean running;
        int score;

        /* JADX INFO: renamed from: vy */
        float f3vy;

        /* JADX INFO: renamed from: y */
        float f4y;

        RunnerGame(Context context) {
            super(context);
            this.f2p = new Paint(3);
            this.running = false;
            this.dead = false;
            this.f4y = 0.0f;
            this.f3vy = 0.0f;
            this.obstacle = 1.15f;
            this.score = 0;
            this.last = 0L;
            this.f2p.setTypeface(Typeface.DEFAULT_BOLD);
            setBackgroundColor(-15592424);
            setOnClickListener(new View.OnClickListener() { // from class: app.swiftnef.MainActivity$RunnerGame$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    RunnerGame.this.m33lambda$new$0$appswiftnefMainActivity$RunnerGame(view);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$new$0$app-swiftnef-MainActivity$RunnerGame, reason: not valid java name */
        /* synthetic */ void m33lambda$new$0$appswiftnefMainActivity$RunnerGame(View view) {
            if (!this.dead) {
                if (this.running) {
                    if (this.f4y == 0.0f) {
                        this.f3vy = -720.0f;
                    }
                }
                invalidate();
            }
            this.dead = false;
            this.score = 0;
            this.obstacle = 1.15f;
            this.running = true;
            invalidate();
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            String str;
            super.onDraw(canvas);
            long jCurrentTimeMillis = System.currentTimeMillis();
            float fMin = this.last == 0 ? 0.0f : Math.min(0.04f, (jCurrentTimeMillis - this.last) / 1000.0f);
            this.last = jCurrentTimeMillis;
            float height = getHeight() - 34;
            if (this.running && !this.dead) {
                this.f3vy += 1900.0f * fMin;
                this.f4y = Math.min(0.0f, this.f4y + (this.f3vy * fMin));
                if (this.f4y == 0.0f && this.f3vy > 0.0f) {
                    this.f3vy = 0.0f;
                }
                this.obstacle -= fMin * (Math.min(0.25f, this.score / 300.0f) + 0.42f);
                if (this.obstacle < -0.08f) {
                    this.obstacle = 1.08f;
                    this.score++;
                }
                float width = this.obstacle * getWidth();
                if (width < 92.0f && width > 37.0f && this.f4y > -38.0f) {
                    this.dead = true;
                    this.running = false;
                }
            }
            this.f2p.setColor(-13354944);
            this.f2p.setStrokeWidth(3.0f);
            canvas.drawLine(12.0f, height, getWidth() - 12, height, this.f2p);
            this.f2p.setColor(-13264);
            float f = height - 38.0f;
            float f2 = this.f4y + f;
            canvas.drawRect(42.0f, f2, 76.0f, height + this.f4y, this.f2p);
            canvas.drawRect(67.0f, f2 - 17.0f, 94.0f, f2 + 8.0f, this.f2p);
            canvas.drawRect(49.0f, height + this.f4y, 55.0f, this.f4y + height + 12.0f, this.f2p);
            canvas.drawRect(69.0f, height + this.f4y, 75.0f, this.f4y + height + 12.0f, this.f2p);
            this.f2p.setColor(-2565151);
            float width2 = this.obstacle * getWidth();
            canvas.drawRect(width2, f, width2 + 13.0f, height, this.f2p);
            canvas.drawRect(width2 - 8.0f, height - 25.0f, width2 + 6.0f, height - 18.0f, this.f2p);
            this.f2p.setTextSize(15.0f);
            this.f2p.setColor(-5327939);
            this.f2p.setTextAlign(Paint.Align.CENTER);
            if (this.dead) {
                str = "Crashed — tap to retry";
            } else {
                str = this.running ? "Tap to jump  •  " + this.score : "Optional tiny runner — tap to start";
            }
            canvas.drawText(str, getWidth() / 2, 24.0f, this.f2p);
            if (this.running) {
                postInvalidateDelayed(16L);
            }
        }
    }

    static class ZoomNavigator extends View {
        ZoomImage image;
        Paint paint;

        ZoomNavigator(Context context, ZoomImage zoomImage) {
            super(context);
            this.paint = new Paint(3);
            this.image = zoomImage;
            setBackgroundColor(-585820647);
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Drawable drawable = this.image.getDrawable();
            if (drawable == null) {
                return;
            }
            float width = getWidth() - 10;
            float height = getHeight() - 10;
            float intrinsicWidth = drawable.getIntrinsicWidth();
            float intrinsicHeight = drawable.getIntrinsicHeight();
            float fMin = Math.min(width / intrinsicWidth, height / intrinsicHeight);
            float f = intrinsicWidth * fMin;
            float width2 = (getWidth() - f) / 2.0f;
            float f2 = intrinsicHeight * fMin;
            float height2 = (getHeight() - f2) / 2.0f;
            Rect rect = new Rect(drawable.getBounds());
            drawable.setBounds((int) width2, (int) height2, (int) (f + width2), (int) (f2 + height2));
            drawable.draw(canvas);
            drawable.setBounds(rect);
            Matrix matrix = new Matrix();
            if (this.image.matrix.invert(matrix)) {
                float[] fArr = {0.0f, 0.0f, this.image.getWidth(), this.image.getHeight()};
                matrix.mapPoints(fArr);
                float fMax = (Math.max(0.0f, Math.min(intrinsicWidth, fArr[0])) * fMin) + width2;
                float fMax2 = (Math.max(0.0f, Math.min(intrinsicHeight, fArr[1])) * fMin) + height2;
                float fMax3 = width2 + (Math.max(0.0f, Math.min(intrinsicWidth, fArr[2])) * fMin);
                float fMax4 = height2 + (Math.max(0.0f, Math.min(intrinsicHeight, fArr[3])) * fMin);
                this.paint.setStyle(Paint.Style.STROKE);
                this.paint.setStrokeWidth(4.0f);
                this.paint.setColor(-13276);
                canvas.drawRect(Math.min(fMax, fMax3), Math.min(fMax2, fMax4), Math.max(fMax, fMax3), Math.max(fMax2, fMax4), this.paint);
            }
        }
    }

    static class Photo {
        long modified;
        String name;
        boolean normal;
        long size;
        Uri uri;

        Photo(Uri uri, String str, long j, long j2, boolean z) {
            this.uri = uri;
            this.name = str;
            this.modified = j;
            this.size = j2;
            this.normal = z;
        }
    }

    static class ZoomImage extends ImageView {
        float factor;
        float lastX;
        float lastY;
        Matrix matrix;
        ZoomNavigator navigator;
        View pad;
        ScaleGestureDetector scale;

        ZoomImage(Context context) {
            super(context);
            this.factor = 1.0f;
            this.matrix = new Matrix();
            setScaleType(ImageView.ScaleType.MATRIX);
            this.scale = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() { // from class: app.swiftnef.MainActivity.ZoomImage.1
                @Override // android.view.ScaleGestureDetector.SimpleOnScaleGestureListener, android.view.ScaleGestureDetector.OnScaleGestureListener
                public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                    float f = ZoomImage.this.factor;
                    ZoomImage.this.factor = Math.max(1.0f, Math.min(64.0f, ZoomImage.this.factor * scaleGestureDetector.getScaleFactor()));
                    float f2 = ZoomImage.this.factor / f;
                    ZoomImage.this.matrix.postScale(f2, f2, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
                    ZoomImage.this.constrain();
                    ZoomImage.this.setImageMatrix(ZoomImage.this.matrix);
                    ZoomImage.this.updatePad();
                    return true;
                }
            });
        }

        void constrain() {
            Drawable drawable = getDrawable();
            if (drawable == null || getWidth() == 0 || getHeight() == 0) {
                return;
            }
            RectF bounds = new RectF(0.0f, 0.0f, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            this.matrix.mapRect(bounds);
            float dx;
            float dy;
            if (bounds.width() <= getWidth()) {
                dx = (getWidth() - bounds.width()) / 2.0f - bounds.left;
            } else if (bounds.left > 0.0f) {
                dx = -bounds.left;
            } else if (bounds.right < getWidth()) {
                dx = getWidth() - bounds.right;
            } else {
                dx = 0.0f;
            }
            if (bounds.height() <= getHeight()) {
                dy = (getHeight() - bounds.height()) / 2.0f - bounds.top;
            } else if (bounds.top > 0.0f) {
                dy = -bounds.top;
            } else if (bounds.bottom < getHeight()) {
                dy = getHeight() - bounds.bottom;
            } else {
                dy = 0.0f;
            }
            this.matrix.postTranslate(dx, dy);
        }

        @Override // android.view.View
        protected void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            resetView();
        }

        @Override // android.view.View
        public boolean onTouchEvent(MotionEvent motionEvent) {
            this.scale.onTouchEvent(motionEvent);
            if (motionEvent.getPointerCount() == 1 && !this.scale.isInProgress()) {
                if (motionEvent.getAction() == 0) {
                    this.lastX = motionEvent.getX();
                    this.lastY = motionEvent.getY();
                } else if (motionEvent.getAction() == 2 && this.factor > 1.0f) {
                    this.matrix.postTranslate(motionEvent.getX() - this.lastX, motionEvent.getY() - this.lastY);
                    constrain();
                    setImageMatrix(this.matrix);
                    updatePad();
                    this.lastX = motionEvent.getX();
                    this.lastY = motionEvent.getY();
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                performClick();
            }
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        void panBy(float f, float f2) {
            this.matrix.postTranslate(f, f2);
            constrain();
            setImageMatrix(this.matrix);
            updatePad();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void resetView() {
            this.factor = 1.0f;
            Drawable drawable = getDrawable();
            if (drawable != null && getWidth() > 0) {
                float fMin = Math.min(getWidth() / drawable.getIntrinsicWidth(), getHeight() / drawable.getIntrinsicHeight());
                this.matrix.reset();
                this.matrix.postScale(fMin, fMin);
                this.matrix.postTranslate((getWidth() - (drawable.getIntrinsicWidth() * fMin)) / 2.0f, (getHeight() - (drawable.getIntrinsicHeight() * fMin)) / 2.0f);
                setImageMatrix(this.matrix);
            }
            updatePad();
        }

        void setNavigator(ZoomNavigator zoomNavigator) {
            this.navigator = zoomNavigator;
            updatePad();
        }

        void setPad(View view) {
            this.pad = view;
            updatePad();
        }

        void updatePad() {
            if (this.pad != null) {
                this.pad.setVisibility(this.factor > 1.5f ? View.VISIBLE : View.GONE);
            }
            if (this.navigator != null) {
                this.navigator.setVisibility(this.factor <= 1.5f ? View.GONE : View.VISIBLE);
                this.navigator.invalidate();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void zoomBy(float f) {
            float f2 = this.factor;
            this.factor = Math.max(1.0f, Math.min(64.0f, this.factor * f));
            float f3 = this.factor / f2;
            this.matrix.postScale(f3, f3, getWidth() / 2.0f, getHeight() / 2.0f);
            constrain();
            setImageMatrix(this.matrix);
            updatePad();
        }
    }
}
