package io.mrarm.switchlinuxlauncher.log.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.mrarm.switchlinuxlauncher.R;
import io.mrarm.switchlinuxlauncher.log.LogOutput;
import io.mrarm.switchlinuxlauncher.log.Logger;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ItemHolder> implements LogOutput {

    private List<Item> items = new ArrayList<>();

    @Override
    public void log(int level, String tag, String message) {
        items.add(new Item(level, tag, message));
        notifyItemInserted(items.size() - 1);
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_log, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class Item {

        int level;
        String tag;
        String message;

        public Item(int level, String tag, String message) {
            this.level = level;
            this.tag = tag;
            this.message = message;
        }

    }

    public static class ItemHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public ItemHolder(View view) {
            super(view);
            textView = (TextView) view;
        }

        private static int getLevelColorRes(int level) {
            switch (level) {
                case Logger.VERBOSE:
                    return R.color.color_dark_verbose;
                case Logger.DEBUG:
                    return R.color.color_dark_debug;
                case Logger.INFO:
                    return R.color.color_dark_info;
                case Logger.WARN:
                    return R.color.color_dark_warn;
                case Logger.ERROR:
                    return R.color.color_dark_error;
                default:
                    return R.color.color_dark_error;
            }
        }

        public void bind(Item item) {
            SpannableString str = new SpannableString("[" + item.tag + "] " + item.message);
            str.setSpan(new ForegroundColorSpan(textView.getContext().getResources().getColor(
                    getLevelColorRes(item.level))), 0, str.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(str);
        }

    }

}
