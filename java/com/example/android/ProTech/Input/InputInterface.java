package com.example.android.ProTech.Input;

import android.graphics.drawable.Drawable;


public interface InputInterface {

    public void setInputMethodSettingsCategoryTitle(int resId);


    public void setInputMethodSettingsCategoryTitle(CharSequence title);


    public void setSubtypeEnablerTitle(int resId);


    public void setSubtypeEnablerTitle(CharSequence title);


    public void setSubtypeEnablerIcon(int resId);


    public void setSubtypeEnablerIcon(Drawable drawable);
}
