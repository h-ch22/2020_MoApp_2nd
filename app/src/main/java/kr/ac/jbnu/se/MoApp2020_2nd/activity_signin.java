package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class activity_signin extends BaseActivity {
    EditText email, password;
    Button register, forgot, signin;
    SignInButton Google_Signin;
    LoginButton Facebook_Signin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    CallbackManager mCallbackManager = CallbackManager.Factory.create();

    protected void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.layout_signin);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        forgot = findViewById(R.id.forgot);
        signin = findViewById(R.id.signin);
        Google_Signin = findViewById(R.id.sign_in_button);
        Facebook_Signin = (LoginButton) findViewById(R.id.login_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Google_Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        Facebook_Signin.setReadPermissions("email");

        Facebook_Signin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("Sign-in with Facebook", "Sign-in with Facebook was completed.");
                        toastMessage("Facebook 로그인 완료");
                    }

                    @Override
                    public void onCancel() {
                        Log.d("Sign-in with Facebook", "Sign-in with Facebook was canceled");
                        toastMessage("사용자의 요청에 의해 Facebook 로그인이 취소되었습니다.");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("Sign-in with Facebook", "Sign-in with Facebook was Failed : " + exception);
                        toastMessage("Facebook 로그인 실패 : Facebook에 등록된 E-mail과 동일한 E-mail로 회원가입을 진행한 적이 있는지 확인하세요.");
                    }
                });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = email.getText().toString();
                String pw = password.getText().toString();
                showProgressDialog();

                if(mail == null || pw == null){
                    hideProgressDialog();
                    Toast.makeText(activity_signin.this, "이메일과 비밀번호를 입력하십시오.", Toast.LENGTH_SHORT).show();
                }

                mAuth.signInWithEmailAndPassword(mail, pw)
                        .addOnCompleteListener(activity_signin.this, new OnCompleteListener<AuthResult>(){

                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    hideProgressDialog();
                                    Intent intent = new Intent(activity_signin.this, activity_main.class);
                                    Toast.makeText(activity_signin.this, "로그인을 완료하였습니다.", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressDialog();
                        Toast.makeText(activity_signin.this, "로그인을 완료하지 못했습니다.\n이메일과 비밀번호를 다시 확인하신 후 문제가 계속 발생할 경우 관리자에게 문의하세요.", Toast.LENGTH_LONG).show();
                        Log.d("Sign in", String.valueOf(e));
                    }
                });

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_signin.this, activity_tutorial.class);
                startActivity(intent);
                finish();
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_signin.this, activity_resetPassword.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Sign-in with Google", "Google sign in failed", e);
                toastMessage("Google 로그인이 완료되지 않았습니다.");
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Sign-in with Google", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign-in with Google", "signInWithCredential:success");
                            toastMessage("Google 로그인이 정상적으로 완료되었습니다.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        }

                        else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign-in with Google", "signInWithCredential:failure", task.getException());
                            toastMessage("Google 로그인이 완료되지 않았습니다.");
                            updateUI(null);
                        }

                    }
                });
    }

    private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
        if (user != null) {

        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
