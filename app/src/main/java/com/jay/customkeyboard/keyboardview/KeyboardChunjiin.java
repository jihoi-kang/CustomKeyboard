package com.jay.customkeyboard.keyboardview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jay.customkeyboard.KeyboardInteractionListener;
import com.jay.customkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardChunjiin {
    private final LinearLayout chunjiinLayout;
    private final InputConnection inputConnection;
    private final KeyboardInteractionListener keyboardInteractionListener;
    private final Context context;
    private final Vibrator vibrator;
    private final ChunjiinMaker chunjiinMaker;
    private final ArrayList<Button> buttons = new ArrayList<>();

    private final List<String> firstLineText = Arrays.asList("ㅣ", "·", "ㅡ", "DEL");
    private final List<String> secondLineText = Arrays.asList("ㄱㅋ", "ㄴㄹ", "ㄷㅌ", "Enter");
    private final List<String> thirdLineText = Arrays.asList("ㅂㅍ", "ㅅㅎ", "ㅈㅊ", ".,?!");
    private final List<String> fourthLineText = Arrays.asList("한/영", "ㅇㅁ", "space", "!#1");
    private final ArrayList<List<String>> myKeysText = new ArrayList();
    private final ArrayList<LinearLayout> layoutLines = new ArrayList();
    View downView = null;
    int sound = 0;
    int vibrate = 0;

    public static KeyboardChunjiin newInstance(
            Context context,
            LayoutInflater layoutInflater,
            InputConnection inputConnection,
            KeyboardInteractionListener keyboardInteractionListener
    ) {
        return new KeyboardChunjiin(context, layoutInflater, inputConnection, keyboardInteractionListener);
    }

    private KeyboardChunjiin(
            Context context,
            LayoutInflater layoutInflater,
            InputConnection inputConnection,
            KeyboardInteractionListener keyboardInteractionListener
    ) {
        chunjiinLayout = (LinearLayout) layoutInflater.inflate(R.layout.keyboard_chunjiin, null);
        this.inputConnection = inputConnection;
        this.keyboardInteractionListener = keyboardInteractionListener;
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        chunjiinMaker = new ChunjiinMaker(inputConnection);

        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int height = sharedPreferences.getInt("keyboardHeight", 150);
        sound = sharedPreferences.getInt("keyboardSound", -1);
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1);
        Configuration config = context.getResources().getConfiguration();

        LinearLayout firstLine = chunjiinLayout.findViewById(R.id.first_line);
        LinearLayout secondLine = chunjiinLayout.findViewById(R.id.second_line);
        LinearLayout thirdLine = chunjiinLayout.findViewById(R.id.third_line);
        LinearLayout fourthLine = chunjiinLayout.findViewById(R.id.fourth_line);

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            fourthLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
        } else {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            fourthLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        }

        myKeysText.clear();
        myKeysText.add(firstLineText);
        myKeysText.add(secondLineText);
        myKeysText.add(thirdLineText);
        myKeysText.add(fourthLineText);

        layoutLines.clear();
        layoutLines.add(firstLine);
        layoutLines.add(secondLine);
        layoutLines.add(thirdLine);
        layoutLines.add(fourthLine);

        setLayoutComponents();
    }

    public LinearLayout getLayout() {
        return chunjiinLayout;
    }

    private void playClick(int i) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1F);
                break;
        }
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

    private View.OnClickListener getMyClickListener(Button actionButton) {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    inputConnection.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE);
                }
                playVibrate();
                CharSequence cursors = inputConnection.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES);
                if (cursors != null && cursors.length() >= 2) {
                    long eventTime = SystemClock.uptimeMillis();
                    inputConnection.finishComposingText();
                    inputConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                            KeyEvent.FLAG_SOFT_KEYBOARD));
                    inputConnection.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                            KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                            KeyEvent.FLAG_SOFT_KEYBOARD));
                    chunjiinMaker.clear();
                }
                switch (actionButton.getText().toString()) {
                    case "한/영":
                        keyboardInteractionListener.modeChange(0);
                        chunjiinMaker.clear();
                        chunjiinMaker.clearChunjiin();
                    case "!#1":
                        keyboardInteractionListener.modeChange(2);
                        chunjiinMaker.clear();
                        chunjiinMaker.clearChunjiin();
                    case ".,?!":
                        chunjiinMaker.commonKeywordCommit();
                    default:
                        playClick(Integer.valueOf(actionButton.getText().toString().toCharArray()[0]));
                        try {
                            chunjiinMaker.directlyCommit();
                            inputConnection.commitText(actionButton.getText().toString(), 1);
                        } catch (NumberFormatException e) {
                            chunjiinMaker.commit(actionButton.getText().toString().toCharArray()[0]);
                        }
                }

            }
        };

        actionButton.setOnClickListener(clickListener);
        return clickListener;
    }

    private View.OnTouchListener getOnTouchListener(View.OnClickListener clickListener) {
        Handler handler = new Handler();
        int initialInterval = 500;
        int normalInterval = 100;
        Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, Long.valueOf(normalInterval));
                clickListener.onClick(downView);
            }
        };

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacks(handlerRunnable);
                        handler.postDelayed(handlerRunnable, Long.valueOf(initialInterval));
                        downView = view;
                        clickListener.onClick(view);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacks(handlerRunnable);
                        downView = null;
                        return true;
                }
                return false;
            }
        };

        return onTouchListener;
    }

    private void setLayoutComponents() {
        for (int line = 0; line < layoutLines.size(); line++) {
            LinearLayout linearLayout = layoutLines.get(line);
            int count = linearLayout.getChildCount();
            List<String> myText = myKeysText.get(line);

            for (int item = 0; item < count; item++) {
                Button actionButton = linearLayout.getChildAt(item).findViewById(R.id.key_button);
                ImageView spacialKey = linearLayout.getChildAt(item).findViewById(R.id.spacial_key);
                View.OnClickListener myOnClickListener = null;

                switch (myText.get(item)) {
                    case "space":
                        spacialKey.setImageResource(R.drawable.ic_space_bar);
                        spacialKey.setVisibility(View.VISIBLE);
                        actionButton.setVisibility(View.GONE);
                        myOnClickListener = getSpaceAction();
                        spacialKey.setOnClickListener(myOnClickListener);
                        spacialKey.setBackgroundResource(R.drawable.key_background);
                        break;
                    case "DEL":
                        spacialKey.setImageResource(R.drawable.del);
                        spacialKey.setVisibility(View.VISIBLE);
                        actionButton.setVisibility(View.GONE);
                        myOnClickListener = getDeleteAction();
                        spacialKey.setOnClickListener(myOnClickListener);
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener));
                        break;
                    case "Enter":
                        spacialKey.setImageResource(R.drawable.ic_enter);
                        spacialKey.setVisibility(View.VISIBLE);
                        actionButton.setVisibility(View.GONE);
                        myOnClickListener = getEnterAction();
                        spacialKey.setOnClickListener(myOnClickListener);
                        spacialKey.setBackgroundResource(R.drawable.key_background);
                        break;
                    default:
                        actionButton.setText(myText.get(item));
                        buttons.add(actionButton);
                        myOnClickListener = getMyClickListener(actionButton);
                        break;
                }

                linearLayout.getChildAt(item).setOnClickListener(myOnClickListener);
            }
        }
    }

    private View.OnClickListener getSpaceAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playClick(Integer.valueOf('ㅂ'));
                playVibrate();
                if (chunjiinMaker.keywordExpect) {
                    inputConnection.finishComposingText();
                    chunjiinMaker.keywordExpect = false;
                } else if (chunjiinMaker.isEmpty()) {
                    inputConnection.commitText(" ", 1);
                } else {
                    chunjiinMaker.directlyCommit();
                }
                chunjiinMaker.clearChunjiin();
            }
        };
    }

    private View.OnClickListener getDeleteAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVibrate();
                chunjiinMaker.delete();
            }
        };
    }


    private View.OnClickListener getEnterAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVibrate();
                chunjiinMaker.directlyCommit();
                long eventTime = SystemClock.uptimeMillis();
                inputConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD));
                inputConnection.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD));
            }
        };
    }

}
