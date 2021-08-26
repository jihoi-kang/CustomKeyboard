package com.jay.customkeyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.jay.customkeyboard.keyboardview.KeyboardChunjiin;
import com.jay.customkeyboard.keyboardview.KeyboardEmoji;
import com.jay.customkeyboard.keyboardview.KeyboardEnglish;
import com.jay.customkeyboard.keyboardview.KeyboardKorean;
import com.jay.customkeyboard.keyboardview.KeyboardNumpad;
import com.jay.customkeyboard.keyboardview.KeyboardSymbol;

public class KeyboardService extends InputMethodService implements KeyboardInteractionListener {
    private LinearLayout keyboardView;
    private FrameLayout keyboardFrame;
    private KeyboardKorean keyboardKorean;
    private KeyboardEnglish keyboardEnglish;
    private KeyboardSymbol keyboardSymbol;
    int isQwerty = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        keyboardView = (LinearLayout) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        keyboardFrame = keyboardView.findViewById(R.id.keyboard_frame);
    }

    @Override
    public View onCreateInputView() {
        keyboardKorean = new KeyboardKorean(getApplicationContext(), getLayoutInflater(), this);
        keyboardKorean.inputConnection = getCurrentInputConnection();
        keyboardKorean.init();
        keyboardEnglish = new KeyboardEnglish(getApplicationContext(), getLayoutInflater(), this);
        keyboardEnglish.inputConnection = getCurrentInputConnection();
        keyboardEnglish.init();
        keyboardSymbol = new KeyboardSymbol(getApplicationContext(), getLayoutInflater(), this);
        keyboardSymbol.inputConnection = getCurrentInputConnection();
        keyboardSymbol.init();

        return keyboardView;
    }

    @Override
    public void updateInputViewShown() {
        super.updateInputViewShown();
        getCurrentInputConnection().finishComposingText();
        if (getCurrentInputEditorInfo().inputType == EditorInfo.TYPE_CLASS_NUMBER) {
            keyboardFrame.removeAllViews();
            KeyboardNumpad keyboardNumpad = KeyboardNumpad.newInstance(getApplicationContext(), getLayoutInflater(), getCurrentInputConnection(), this);
            keyboardFrame.addView(keyboardNumpad.getLayout());
        } else {
            modeChange(1);
        }
    }

    @Override
    public void modeChange(int mode) {
        getCurrentInputConnection().finishComposingText();
        keyboardFrame.removeAllViews();

        switch (mode) {
            case 0:
                keyboardFrame.removeAllViews();
                keyboardEnglish.inputConnection = getCurrentInputConnection();
                keyboardFrame.addView(keyboardEnglish.getLayout());
                break;
            case 1:
                if (isQwerty == 0) {
                    keyboardFrame.removeAllViews();
                    keyboardKorean.inputConnection = getCurrentInputConnection();
                    keyboardFrame.addView(keyboardKorean.getLayout());
                } else {
                    keyboardFrame.removeAllViews();
                    keyboardFrame.addView(KeyboardChunjiin.newInstance(getApplicationContext(), getLayoutInflater(), getCurrentInputConnection(), this).getLayout());
                }
                break;
            case 2:
                keyboardFrame.removeAllViews();
                keyboardSymbol.inputConnection = getCurrentInputConnection();
                keyboardFrame.addView(keyboardSymbol.getLayout());
                break;
            case 3:
                keyboardFrame.removeAllViews();
                keyboardFrame.addView(KeyboardEmoji.newInstance(getApplicationContext(), getLayoutInflater(), getCurrentInputConnection(), this).getLayout());
                break;
        }
    }
}
