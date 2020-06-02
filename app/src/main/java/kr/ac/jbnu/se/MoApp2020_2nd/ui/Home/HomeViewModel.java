package kr.ac.jbnu.se.MoApp2020_2nd.ui.Home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public HomeViewModel(){

    }

    public LiveData<String> getText(){ return null; }
}
