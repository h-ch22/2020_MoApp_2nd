package kr.ac.jbnu.se.MoApp2020_2nd.ui.HabitTracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HabitTrackerViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public HabitTrackerViewModel(){

    }

    public LiveData<String> getText(){
        return null;
    }
}
