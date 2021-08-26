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
import android.util.Log;
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

public class KeyboardKorean {
    private final Context context;
    private final LayoutInflater layoutInflater;
    private final KeyboardInteractionListener keyboardInteractionListener;

    private LinearLayout koreanLayout;
    public InputConnection inputConnection = null;
    private HangulMaker hangulMaker;
    private Vibrator vibrator;
    private SharedPreferences sharedPreferences;
    private boolean isCaps = false;
    private final ArrayList<Button> buttons = new ArrayList<>();

    private final List<String> numpadText = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "0");
    private final List<String> firstLineText = Arrays.asList("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ");
    private final List<String> secondLineText = Arrays.asList("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ");
    private final List<String> thirdLineText = Arrays.asList("CAPS", "ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ", "DEL");
    private final List<String> fourthLineText = Arrays.asList("!#1", "한/영", ",", "space", ".", "Enter");
    private final List<String> firstLongClickText = Arrays.asList("!", "@", "#", "$", "%", "^", "&", "*", "(", ")");
    private final List<String> secondLongClickText = Arrays.asList("~", "+", "-", "×", "♥", ":", ";", "'", "\"");
    private final List<String> thirdLongClickText = Arrays.asList("", "_", "<", ">", "/", ",", "?");
    private final ArrayList<List<String>> myKeysText = new ArrayList<>();
    private final ArrayList<List<String>> myLongClickKeysText = new ArrayList<>();
    private final ArrayList<LinearLayout> layoutLines = new ArrayList<>();
    private View downView = null;
    private int sound = 0;
    private int vibrate = 0;
    private ImageView capsView = null;

    public KeyboardKorean(Context context, LayoutInflater layoutInflater, KeyboardInteractionListener keyboardInteractionListener) {
        this.context = context;
        this.layoutInflater = layoutInflater;
        this.keyboardInteractionListener = keyboardInteractionListener;
    }

    public void init() {
        koreanLayout = (LinearLayout) layoutInflater.inflate(R.layout.keyboard_action, null);
        hangulMaker = new HangulMaker(inputConnection);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Configuration config = context.getResources().getConfiguration();
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int height = sharedPreferences.getInt("keyboardHeight", 150);
        sound = sharedPreferences.getInt("keyboardSound", -1);
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1);

        LinearLayout numpadLine = koreanLayout.findViewById(R.id.numpad_line);
        LinearLayout firstLine = koreanLayout.findViewById(R.id.first_line);
        LinearLayout secondLine = koreanLayout.findViewById(R.id.second_line);
        LinearLayout thirdLine = koreanLayout.findViewById(R.id.third_line);
        LinearLayout fourthLine = koreanLayout.findViewById(R.id.fourth_line);

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

        myLongClickKeysText.clear();
        myLongClickKeysText.add(firstLongClickText);
        myLongClickKeysText.add(secondLongClickText);
        myLongClickKeysText.add(thirdLongClickText);

        layoutLines.clear();
        layoutLines.add(numpadLine);
        layoutLines.add(firstLine);
        layoutLines.add(secondLine);
        layoutLines.add(thirdLine);
        layoutLines.add(fourthLine);

        setLayoutComponents();
    }

    public LinearLayout getLayout() {
        hangulMaker = new HangulMaker(inputConnection);
        setLayoutComponents();
        return koreanLayout;
    }

    private void modeChange() {
        if (isCaps) {
            isCaps = false;
            capsView.setImageResource(R.drawable.ic_caps_unlock);
            for (int i = 0; i < buttons.size(); i++) {
                switch (buttons.get(i).getText().toString()) {
                    case "ㅃ":
                        buttons.get(i).setText("ㅂ");
                        break;
                    case "ㅉ":
                        buttons.get(i).setText("ㅈ");
                        break;
                    case "ㄸ":
                        buttons.get(i).setText("ㄷ");
                        break;
                    case "ㄲ":
                        buttons.get(i).setText("ㄱ");
                        break;
                    case "ㅆ":
                        buttons.get(i).setText("ㅅ");
                        break;
                    case "ㅒ":
                        buttons.get(i).setText("ㅐ");
                        break;
                    case "ㅖ":
                        buttons.get(i).setText("ㅔ");
                        break;
                }
            }
        } else {
            isCaps = true;
            for (int i = 0; i < buttons.size(); i++) {
                switch (buttons.get(i).getText().toString()) {
                    case "ㅂ":
                        buttons.get(i).setText("ㅃ");
                        break;
                    case "ㅈ":
                        buttons.get(i).setText("ㅉ");
                        break;
                    case "ㄷ":
                        buttons.get(i).setText("ㄸ");
                        break;
                    case "ㄱ":
                        buttons.get(i).setText("ㄲ");
                        break;
                    case "ㅅ":
                        buttons.get(i).setText("ㅆ");
                        break;
                    case "ㅐ":
                        buttons.get(i).setText("ㅒ");
                        break;
                    case "ㅔ":
                        buttons.get(i).setText("ㅖ");
                        break;
                }
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
                Log.d("jay kang", "onClick passed!");
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
                    hangulMaker.clear();
                }

                playClick(Integer.valueOf(actionButton.getText().toString().toCharArray()[0]));
                try {
                    Integer.parseInt(actionButton.getText().toString());
                    hangulMaker.directlyCommit();
                    inputConnection.commitText(actionButton.getText().toString(), 1);
                } catch (NumberFormatException e) {
                    hangulMaker.commit(actionButton.getText().toString().toCharArray()[0]);
                }
                if (isCaps) {
                    modeChange();
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
                    case "한/영":
                        actionButton.setText(myText.get(item));
                        buttons.add(actionButton);
                        myOnClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                keyboardInteractionListener.modeChange(0);
                            }
                        };
                        actionButton.setOnClickListener(myOnClickListener);
                        break;
                    case "!#1":
                        actionButton.setText(myText.get(item));
                        buttons.add(actionButton);
                        myOnClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                keyboardInteractionListener.modeChange(2);
                            }
                        };
                        actionButton.setOnClickListener(myOnClickListener);
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
                hangulMaker.commitSpace();
            }
        };
    }

    private View.OnClickListener getDeleteAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    hangulMaker.clear();
                } else {
                    hangulMaker.delete();
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
