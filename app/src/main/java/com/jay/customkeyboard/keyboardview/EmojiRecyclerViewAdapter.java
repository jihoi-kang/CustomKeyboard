package com.jay.customkeyboard.keyboardview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jay.customkeyboard.R;

import java.util.ArrayList;

public class EmojiRecyclerViewAdapter extends RecyclerView.Adapter<EmojiRecyclerViewAdapter.Holder> {
    private final Context context;
    private final ArrayList<String> emojiList;
    private final InputConnection inputConnection;

    public EmojiRecyclerViewAdapter(
            Context context,
            ArrayList<String> emojiList,
            InputConnection inputConnection
    ) {
        this.context = context;
        this.emojiList = emojiList;
        this.inputConnection = inputConnection;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.emoji_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(emojiList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return emojiList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public Holder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.emoji_text);
        }

        void bind(String emoji, Context context) {
            textView.setText(emoji);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    inputConnection.commitText(textView.getText().toString(), 1);
                }
            });
        }

    }

}
