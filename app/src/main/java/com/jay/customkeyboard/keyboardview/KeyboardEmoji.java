package com.jay.customkeyboard.keyboardview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jay.customkeyboard.KeyboardInteractionListener;
import com.jay.customkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardEmoji {
    private final LinearLayout emojiLayout;
    private final InputConnection inputConnection;
    private final KeyboardInteractionListener keyboardInteractionListener;
    private final Context context;
    private final Vibrator vibrator;

    private EmojiRecyclerViewAdapter emojiRecyclerViewAdapter;
    private final List<String> fourthLineText = Arrays.asList("한/영", getEmojiByUnicode(0x1F600), getEmojiByUnicode(0x1F466), getEmojiByUnicode(0x1F91A), getEmojiByUnicode(0x1F423), getEmojiByUnicode(0x1F331), getEmojiByUnicode(0x1F682), "DEL");
    private int vibrate = 0;
    private int sound = 0;

    public static KeyboardEmoji newInstance(
            Context context,
            LayoutInflater layoutInflater,
            InputConnection inputConnection,
            KeyboardInteractionListener keyboardInteractionListener
    ) {
        return new KeyboardEmoji(context, layoutInflater, inputConnection, keyboardInteractionListener);
    }

    private KeyboardEmoji(
            Context context,
            LayoutInflater layoutInflater,
            InputConnection inputConnection,
            KeyboardInteractionListener keyboardInteractionListener
    ) {
        emojiLayout = (LinearLayout) layoutInflater.inflate(R.layout.keyboard_emoji, null);
        this.context = context;
        this.inputConnection = inputConnection;
        this.keyboardInteractionListener = keyboardInteractionListener;

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        vibrate = sharedPreferences.getInt("vibrate", -1);
        sound = sharedPreferences.getInt("sound", -1);

        LinearLayout fourthLine = emojiLayout.findViewById(R.id.fourth_line);
        int childCount = fourthLine.getChildCount();
        for (int item = 0; item < childCount; item++) {
            Button actionButton = fourthLine.getChildAt(item).findViewById(R.id.key_button);
            ImageView spacialKey = fourthLine.getChildAt(item).findViewById(R.id.spacial_key);

            if (fourthLineText.get(item).equals("DEL")) {
                actionButton.setBackgroundResource(R.drawable.del);
                View.OnClickListener myOnClickListener = getDeleteAction();
                actionButton.setOnClickListener(myOnClickListener);
            } else {
                actionButton.setText(fourthLineText.get(item));
                actionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (actionButton.getText().toString().equals("한/영")) {
                            keyboardInteractionListener.modeChange(1);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F600))) {
                            setLayoutComponents(0x1F600, 79);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F466))) {
                            setLayoutComponents(0x1F466, 88);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F91A))) {
                            setLayoutComponents(0x1F91A, 88);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F423))) {
                            setLayoutComponents(0x1F423, 35);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F331))) {
                            setLayoutComponents(0x1F331, 88);
                        } else if (actionButton.getText().toString().equals(getEmojiByUnicode(0x1F682))) {
                            setLayoutComponents(0x1F682, 64);
                        }
                    }
                });
            }
        }

        setLayoutComponents(0x1F600, 79);
    }

    public LinearLayout getLayout() {
        return emojiLayout;
    }

    private void playVibrate() {
        if (vibrate > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate));
            } else {
                vibrator.vibrate(70);
            }
        }
    }

    private void setLayoutComponents(int unicode, int count) {
        RecyclerView recyclerView = emojiLayout.findViewById(R.id.emoji_recyclerview);
        ArrayList<String> emojiList = new ArrayList<>();
        Configuration config = context.getResources().getConfiguration();
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int height = sharedPreferences.getInt("keyboardHeight", 150);

        for (int i = 0; i < count; i++) {
            emojiList.add(getEmojiByUnicode(unicode + i));
        }

        emojiRecyclerViewAdapter = new EmojiRecyclerViewAdapter(context, emojiList, inputConnection);
        recyclerView.setAdapter(emojiRecyclerViewAdapter);
        GridLayoutManager gm = new GridLayoutManager(context, 8);
        gm.setItemPrefetchEnabled(true);
        recyclerView.setLayoutManager(gm);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * 5));
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    private View.OnClickListener getDeleteAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVibrate();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    inputConnection.deleteSurroundingTextInCodePoints(1, 0);
                } else {
                    inputConnection.deleteSurroundingText(1, 0);
                }
            }
        };
    }

}
