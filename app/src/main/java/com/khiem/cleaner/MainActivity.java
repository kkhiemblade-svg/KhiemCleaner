package com.khiem.cleaner;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.View;
import android.widget.*;
import java.io.File;

public class MainActivity extends Activity {
    TextView ramTv, storTv, cacheTv, msgTv;
    ProgressBar ramBar, storBar;

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    TextView text(String s, int sp, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(Color.WHITE);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(0, dp(6), 0, dp(6));
        return t;
    }

    ProgressBar bar() {
        ProgressBar b = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        b.setMax(100);
        return b;
    }

    Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s); b.setOnClickListener(l);
        return b;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Color.parseColor("#121826"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(28));
        sv.addView(root);

        root.addView(text("KhiemCleaner", 26, true));
        ramTv = text("", 15, false); ramBar = bar();
        storTv = text("", 15, false); storBar = bar();
        cacheTv = text("", 15, false);
        msgTv = text("", 14, false); msgTv.setTextColor(Color.parseColor("#7CFC9A"));

        root.addView(text("RAM", 18, true)); root.addView(ramTv); root.addView(ramBar);
        root.addView(text("Bộ nhớ trong", 18, true)); root.addView(storTv); root.addView(storBar);
        root.addView(text("Dọn dẹp", 18, true)); root.addView(cacheTv);

        root.addView(btn("Xóa cache của KhiemCleaner", v -> {
            long before = cacheSize();
            deleteContents(getCacheDir());
            File ext = getExternalCacheDir();
            if (ext != null) deleteContents(ext);
            long freed = Math.max(0, before - cacheSize());
            msgTv.setText("Đã xóa thật: " + Formatter.formatFileSize(this, freed));
            refresh();
        }));
        root.addView(btn("Xóa cache ứng dụng khác (mở Cài đặt)", v -> open(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)));
        root.addView(btn("Cài đặt bộ nhớ Android", v -> open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
        root.addView(btn("Làm mới", v -> { msgTv.setText(""); refresh(); }));
        root.addView(msgTv);
        root.addView(text("Lưu ý: Android không root không cho ứng dụng tự xóa cache/RAM của app khác, nên mục này mở Cài đặt để bạn tự xóa.", 12, false));

        setContentView(sv);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    void open(String action) {
        try { startActivity(new Intent(action)); }
        catch (Exception e) { msgTv.setText("Không mở được trang cài đặt này trên máy bạn."); }
    }

    void refresh() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long used = mi.totalMem - mi.availMem;
        ramTv.setText("Tổng " + fs(mi.totalMem) + " · Đang dùng " + fs(used) + " · Còn trống " + fs(mi.availMem));
        ramBar.setProgress((int) (used * 100 / mi.totalMem));

        StatFs st = new StatFs(Environment.getDataDirectory().getPath());
        long total = st.getTotalBytes(), free = st.getAvailableBytes(), u = total - free;
        storTv.setText("Tổng " + fs(total) + " · Đã dùng " + fs(u) + " · Còn trống " + fs(free));
        storBar.setProgress((int) (u * 100 / total));

        cacheTv.setText("Cache của KhiemCleaner: " + fs(cacheSize()));
    }

    String fs(long n) { return Formatter.formatFileSize(this, n); }

    long cacheSize() {
        long s = size(getCacheDir());
        File ext = getExternalCacheDir();
        return ext != null ? s + size(ext) : s;
    }

    long size(File f) {
        if (f == null || !f.exists()) return 0;
        if (f.isFile()) return f.length();
        long s = 0;
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) s += size(k);
        return s;
    }

    void deleteContents(File dir) {
        File[] kids = dir.listFiles();
        if (kids == null) return;
        for (File k : kids) {
            if (k.isDirectory()) deleteContents(k);
            k.delete();
        }
    }
                     }
