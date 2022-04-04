package com.tobeosft.plugin.biometricobject;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import static com.tobesoft.plugin.plugincommonlib.info.ActivityRequest.BIOMETRIC_ACTIVITY_REQUEST;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;


import com.nexacro.NexacroActivity;
import com.nexacro.plugin.NexacroPlugin;
import com.tobeosft.plugin.biometricobject.pluginnterface.BiometricInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class BiometricObject extends NexacroPlugin {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private final String LOG_TAG = getClass().getSimpleName();

    private static final String SVCID = "svcid";
    private static final String REASON = "reason";
    private static final String RETVAL = "returnvalue";

    private static final String CALL_BACK = "_oncallback";
    private static final String PAGE_LOAD = "_onpageload";

    private static final String METHOD_CALLMETHOD = "callMethod";

    public static final int CODE_SUCCES = 0;
    public static final int CODE_ERROR = -1;

    public String mServiceId = "";

    private String mKeyName = "";


    private NexacroActivity mActivity;
    private BiometricInterface mBiometricInterface = null;

    public BiometricObject(String objectId) {
        super(objectId);

        mBiometricInterface = (BiometricInterface) NexacroActivity.getInstance();
        mBiometricInterface.setBiometricObject(this);
        mActivity = NexacroActivity.getInstance();
    }

    @Override
    public void init(JSONObject paramObject) {
        if (checkBiometricEnable()) {
            initBiometric();
        }
    }

    @Override
    public void release(JSONObject paramObject) {
        biometricPrompt = null;
    }

    @Override
    public void execute(String method, JSONObject paramObject) {
        mServiceId = "";

        if (method.equals(METHOD_CALLMETHOD)) {
            try {
                JSONObject params = paramObject.getJSONObject("params");

                mServiceId = params.getString("serviceid");

                if (mServiceId.equals("biometricOpen")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                        JSONObject param = params.getJSONObject("param");

                        boolean biometricStrongOption = Boolean.parseBoolean(param.getString("biometricStrongOption"));
                        boolean biometricWeakOption = Boolean.parseBoolean(param.getString("biometricWeakOption"));
                        boolean biometricDeviceCredentialOption = Boolean.parseBoolean(param.getString("biometricDeviceCredentialOption"));
                        boolean biometricEncryptOption = Boolean.parseBoolean(param.getString("biometricEncryptOption"));


                        if (biometricEncryptOption) {
                            mKeyName = param.getString("biometricSecretKeyName");
                        }

                        mActivity.runOnUiThread(new Runnable() {

                            // 프롬프트는 UI Thread 에서만 실행가능

                            @Override
                            public void run() {
                                executeBiometricPrompt(biometricStrongOption, biometricWeakOption, biometricDeviceCredentialOption, biometricEncryptOption);
                            }
                        });

                    } else {
                        send(CODE_ERROR, METHOD_CALLMETHOD + ":" + "Android OS 28 이상이 필요합니다.");
                    }
                }
            } catch (Exception e) {
                send(CODE_ERROR, METHOD_CALLMETHOD + ":" + e.getMessage());
            }
        }
    }

    public boolean send(int reason, Object retval) {

        return send(mServiceId, CALL_BACK, reason, retval);
    }

    public boolean send(String svcid, String callMethod, int reason, Object retval) {

        JSONObject obj = new JSONObject();

        try {
            if (mServiceId != null) {
                obj.put(SVCID, svcid);
                obj.put(REASON, reason);
                obj.put(RETVAL, retval);

                callback(callMethod, obj);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    public void initBiometric() {
        executor = ContextCompat.getMainExecutor(mActivity);

        biometricPrompt = new BiometricPrompt(mActivity,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(mActivity,
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Log.e(LOG_TAG, " " + errorCode + errString);

                    send(CODE_ERROR, METHOD_CALLMETHOD + "errorCode : " + errorCode + "errString : " + errString);
                } else {
                    send(CODE_ERROR, METHOD_CALLMETHOD + "Android 10 이상이어야 합니다.");
                }
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {

                try {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(mActivity,
                            "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Log.e(LOG_TAG, String.valueOf(result.getAuthenticationType()));

                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put("authType", result.getAuthenticationType());
                        if(result.getCryptoObject() != null) {
                            Cipher returnCipher = result.getCryptoObject().getCipher();
                            jsonObject.put("cryptoObject", result.getCryptoObject().getCipher().doFinal());
                        }

                        send(mServiceId, CALL_BACK, CODE_SUCCES, jsonObject.toString());

                    } else {
                        send(CODE_ERROR, METHOD_CALLMETHOD + "Android 10 이상이어야 합니다.");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(mActivity, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();

                send(CODE_ERROR, METHOD_CALLMETHOD + "생체인증에 실패 하였습니다.");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }

    private SecretKey getSecretKey() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            // Before the keystore can be accessed, it must be loaded.
            keyStore.load(null);
            return ((SecretKey) keyStore.getKey(mKeyName, null));
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) {
        KeyGenerator keyGenerator = null;

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                keyGenerator.init(keyGenParameterSpec);
                if (keyGenerator != null) {
                    keyGenerator.generateKey();
                }
            } else{
                send(CODE_ERROR,"Android M 이상의 API가 필요합니다.");
            }
        } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }




    public void executeBiometricPrompt(boolean biometricStrong, boolean biometricWeak, boolean biometricDevice, boolean biometricEncryptOption){
        if (biometricStrong && biometricWeak && biometricDevice) {
            allAuthBioPromptCreate();
        } else if (biometricStrong && biometricDevice) {
            strongWithDeviceCredentialBioPromptCreate();
        } else if (biometricWeak && biometricDevice) {
            weakWithDeviceCredentialBioPromptCreate();
        } else if (biometricDevice) {
            deviceCredentialBioPromptCreate();
        } else if (biometricStrong) {
            strongBioPromptCreate();
        } else if (biometricWeak) {
            weakBioPromptCreate();
        } else {
            send(CODE_ERROR, METHOD_CALLMETHOD + "생체인증 옵션이 켜져있지 않습니다.");
        }

        if (biometricEncryptOption) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                try {
                    Cipher cipher = getCipher();
                    generateSecretKey(new KeyGenParameterSpec.Builder(
                            mKeyName,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .setUserAuthenticationRequired(true)
                            // Invalidate the keys if the user has registered a new biometric
                            // credential, such as a new fingerprint. Can call this method only
                            // on Android 7.0 (API level 24) or higher. The variable
                            // "invalidatedByBiometricEnrollment" is true by default.
                            .setInvalidatedByBiometricEnrollment(true)
                            .build());
                    SecretKey secretKey = getSecretKey();
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    biometricPrompt.authenticate(promptInfo,
                            new BiometricPrompt.CryptoObject(cipher));
                }catch (NoSuchAlgorithmException| NoSuchPaddingException | InvalidKeyException e){
                    send(CODE_ERROR,e);
                }
            } else {
                send(CODE_ERROR,METHOD_CALLMETHOD + "Android 10이상 부터 이용 가능 합니다.");
            }
        } else if (promptInfo == null ){
            send(CODE_ERROR, METHOD_CALLMETHOD + "생체인증 옵션이 켜져있지 않습니다.");
        } else {
            biometricPrompt.authenticate(promptInfo);
        }
        promptInfo = null;
    }

    private boolean checkBiometricEnable() {

        boolean enableBiometric = false;
        BiometricManager biometricManager = BiometricManager.from(mActivity);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(LOG_TAG, "App can authenticate using biometrics.");

                enableBiometric = true;
                send(CODE_SUCCES, "App can authenticate using biometrics.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(LOG_TAG, "No biometric features available on this device.");
                send(CODE_ERROR, "No biometric features available on this device.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(LOG_TAG, "Biometric features are currently unavailable.");
                send(CODE_ERROR, "Biometric features are currently unavailable.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.

                Log.e(LOG_TAG, "Prompts the user to create credentials that your app accepts.");
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL | BIOMETRIC_WEAK);

                mActivity.startActivityForResult(enrollIntent, BIOMETRIC_ACTIVITY_REQUEST);
                break;
        }
        return enableBiometric;
    }


    public void strongBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setNegativeButtonText("Use account password")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build();
    }

    public void weakBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setNegativeButtonText("Use account password")
                .setAllowedAuthenticators(BIOMETRIC_WEAK)
                .build();
    }

    public void deviceCredentialBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setAllowedAuthenticators(DEVICE_CREDENTIAL)
                .build();
    }

    public void strongWithDeviceCredentialBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();
    }

    public void weakWithDeviceCredentialBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }

    public void allAuthBioPromptCreate() {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Nexacro 바이오인증 모듈입니다.")
                .setSubtitle("생체 인증 정보를 사용합니다.")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .build();
    }


    public static boolean isActivityResult(int requestCode) {
        boolean result = false;
        switch (requestCode) {
            case BIOMETRIC_ACTIVITY_REQUEST:
                result = true;
                break;
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == BIOMETRIC_ACTIVITY_REQUEST) {
            if (intent != null) {
                if (checkBiometricEnable()) {
                    send(CODE_SUCCES, "새로운 Biometric 등록확인");
                } else {
                    send(CODE_ERROR, "새로운 Biometric 등록실패");
                }
            }
        }
    }
}
