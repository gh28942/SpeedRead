package com.fitari.speed.read;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;

public class GoEditText extends androidx.appcompat.widget.AppCompatEditText
{
    ArrayList<GoEditTextListener> listeners;

    public GoEditText(Context context)
    {
        super(context);
        listeners = new ArrayList<>();
    }

    public GoEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        listeners = new ArrayList<>();
    }

    public GoEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        listeners = new ArrayList<>();
    }

    public void addListener(GoEditTextListener listener) {
        try {
            listeners.add(listener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Here you can catch paste, copy and cut events
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id){
            case android.R.id.cut:
                onTextCut();
                break;
            case android.R.id.paste:
                onTextPaste();
                break;
            case android.R.id.copy:
                onTextCopy();
        }
        return consumed;
    }

    public void onTextCut(){
    }

    public void onTextCopy(){
    }

    /**
     * adding listener for Paste for example
     */
    public void onTextPaste(){
        for (GoEditTextListener listener : listeners) {
            listener.onUpdate();
        }
    }
}