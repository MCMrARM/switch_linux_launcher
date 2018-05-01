package io.mrarm.switchlinuxlauncher.log.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import io.mrarm.switchlinuxlauncher.R;
import io.mrarm.switchlinuxlauncher.log.LogOutput;

public class LogView extends RecyclerView implements LogOutput {

    private LogAdapter adapter = new LogAdapter();

    public LogView(Context context) {
        this(context, null);
    }

    public LogView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.logViewStyle);
    }

    public LogView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(adapter);
    }


    @Override
    public void log(int level, String tag, String message) {
        post(() -> {
            adapter.log(level, tag, message);
            scrollToPosition(adapter.getItemCount() - 1);
        });
    }

}
