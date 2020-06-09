package kr.ac.jbnu.se.MoApp2020_2nd;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.HabitTracker.HabitTrackerFragment;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.MyPage.MyPageFragment;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.Home.HomeFragment;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.TimeCapsule.TimeCapsuleFragment;

public class activity_main extends AppCompatActivity {
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HabitTrackerFragment fragmentDiary = new HabitTrackerFragment();
    private MyPageFragment fragmentFriends = new MyPageFragment();
    private HomeFragment fragmentHome = new HomeFragment();
    private TimeCapsuleFragment fragmentTimeCapsule = new TimeCapsuleFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        BottomNavigationView navView = findViewById(R.id.navigationView);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.menuHome, R.id.menuHabitTracker, R.id.menuMyPage, R.id.menuTimeCapsule)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
