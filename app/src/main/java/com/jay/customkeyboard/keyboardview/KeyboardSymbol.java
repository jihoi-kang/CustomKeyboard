package com.jay.customkeyboard.keyboardview;

import static android.content.Context.AUDIO_SERVICE;

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

public class KeyboardSymbol {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final KeyboardInteractionListener keyboardInteractionListener;

    private LinearLayout symbolLayout;
    public InputConnection inputConnection = null;
    private Vibrator vibrator;
    private boolean isCaps = false;
    private final ArrayList<Button> buttons = new ArrayList<>();

    private final List<String> numpadText = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0");
    private final List<String> firstLineText = Arrays.asList("+", "×", "÷", "=", "/", "￦", "<", ">", "♡", "☆");
    private final List<String> secondLineText = Arrays.asList("!", "@", "#", "~", "%", "^", "&", "*", "(", ")");
    private final List<String> thirdLineText = Arrays.asList("\uD83D\uDE00", "-", "'", "\"", ":", ";", ",", "?", "DEL");
    private final List<String> fourthLineText = Arrays.asList("가", "한/영", ",", "space", ".", "Enter");
    private final ArrayList<List<String>> myKeysText = new ArrayList<>();
    private final ArrayList<LinearLayout> layoutLines = new ArrayList<>();
    private View downView = null;
    private int sound = 0;
    private int vibrate = 0;
    private ImageView capsView = null;
    private int animationMode = 0;

    public KeyboardSymbol(Context context, LayoutInflater layoutInflater, KeyboardInteractionListener keyboardInteractionListener) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.keyboardInteractionListener = keyboardInteractionListener;
    }

    public void init() {
        symbolLayout = (LinearLayout) layoutInflater.inflate(R.layout.keyboard_symbol, null);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Configuration config = context.getResources().getConfiguration();
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int height = sharedPreferences.getInt("keyboardHeight", 150);
        animationMode = sharedPreferences.getInt("theme", 0);
        sound = sharedPreferences.getInt("keyboardSound", -1);
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1);

        LinearLayout numpadLine = symbolLayout.findViewById(R.id.numpad_line);
        LinearLayout firstLine = symbolLayout.findViewById(R.id.first_line);
        LinearLayout secondLine = symbolLayout.findViewById(R.id.second_line);
        LinearLayout thirdLine = symbolLayout.findViewById(R.id.third_line);
        LinearLayout fourthLine = symbolLayout.findViewById(R.id.fourth_line);

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.7)));
        } else {
            firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
            thirdLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        }

        myKeysText.clear();
        myKeysText.add(numpadText);
        myKeysText.add(firstLineText);
        myKeysText.add(secondLineText);
        myKeysText.add(thirdLineText);
        myKeysText.add(fourthLineText);

        layoutLines.clear();
        layoutLines.add(numpadLine);
        layoutLines.add(firstLine);
        layoutLines.add(secondLine);
        layoutLines.add(thirdLine);
        layoutLines.add(fourthLine);

        setLayoutComponents();
    }

    public LinearLayout getLayout() {
        return symbolLayout;
    }

    private void modeChange() {
        if (isCaps) {
            isCaps = false;
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setText(buttons.get(i).getText().toString().toLowerCase());
            }
        } else {
            isCaps = true;
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setText(buttons.get(i).getText().toString().toUpperCase());
            }
        }
    }

    private void playClick(int i) {
        AudioManager am = (AudioManager) context.getSystemService(AUDIO_SERVICE);
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
                }

                switch (actionButton.getText().toString()) {
                    case "\uD83D\uDE00":
                        keyboardInteractionListener.modeChange(3);
                        break;
                    case "한/영":
                    case "가":
                        keyboardInteractionListener.modeChange(1);
                        break;
                    default:
                        playClick(Integer.valueOf(actionButton.getText().toString().toCharArray()[0]));
                        inputConnection.commitText(actionButton.getText().toString(), 1);
                        break;
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
                handler.postDelayed(this, normalInterval);
                clickListener.onClick(downView);
            }
        };

        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacks(handlerRunnable);
                        handler.postDelayed(handlerRunnable, initialInterval);
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
    }

    private void setLayoutComponents() {
        for (int line = 0; line < layoutLines.size(); line++) {
            LinearLayout linearLayout = layoutLines.get(line);
            List<String> myText = myKeysText.get(line);
            int itemCount = linearLayout.getChildCount();
            for (int item = 0; item < itemCount; item++) {
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
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener));
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
                    case "CAPS":
                        spacialKey.setImageResource(R.drawable.ic_caps_unlock);
                        spacialKey.setVisibility(View.VISIBLE);
                        actionButton.setVisibility(View.GONE);
                        capsView = spacialKey;
                        myOnClickListener = getCapsAction();
                        spacialKey.setOnClickListener(myOnClickListener);
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener));
                        spacialKey.setBackgroundResource(R.drawable.key_background);
                        break;
                    case "Enter":
                        spacialKey.setImageResource(R.drawable.ic_enter);
                        spacialKey.setVisibility(View.VISIBLE);
                        actionButton.setVisibility(View.GONE);
                        myOnClickListener = getEnterAction();
                        spacialKey.setOnClickListener(myOnClickListener);
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener));
                        spacialKey.setBackgroundResource(R.drawable.key_background);
                        break;
                    default:
                        actionButton.setText(myText.get(item));
                        buttons.add(actionButton);
                        myOnClickListener = getMyClickListener(actionButton);
                        actionButton.setOnTouchListener(getOnTouchListener(myOnClickListener));
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
                inputConnection.commitText(" ", 1);
            }
        };
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

    private View.OnClickListener getCapsAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVibrate();
                modeChange();
            }
        };
    }

    private View.OnClickListener getEnterAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVibrate();
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
