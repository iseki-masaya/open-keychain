package org.sufficientlysecure.keychain.service;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.keyimport.ParcelableKeyRing;
import org.sufficientlysecure.keychain.operations.ImportOperation;
import org.sufficientlysecure.keychain.operations.results.ImportKeyResult;
import org.sufficientlysecure.keychain.operations.results.OperationResult;
import org.sufficientlysecure.keychain.provider.KeychainContract;
import org.sufficientlysecure.keychain.provider.ProviderHelper;
import org.sufficientlysecure.keychain.service.input.CryptoInputParcel;
import org.sufficientlysecure.keychain.ui.OrbotRequiredDialogActivity;
import org.sufficientlysecure.keychain.ui.util.KeyFormattingUtils;
import org.sufficientlysecure.keychain.util.Log;
import org.sufficientlysecure.keychain.util.ParcelableProxy;
import org.sufficientlysecure.keychain.util.Preferences;
import org.sufficientlysecure.keychain.util.orbot.OrbotHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyserverSyncAdapterService extends Service {

    private static final String ACTION_IGNORE_TOR = "ignore_tor";
    private static final String ACTION_UPDATE_ALL = "update_all";
    private static final String ACTION_DELAYED_UPDATE = "delayed_update";
    private static final String ACTION_UPDATE_RESULT = "update_result";
    private static final String ACTION_DISMISS_NOTIFICATION = "cancel_sync";
    private static final String ACTION_START_ORBOT = "start_orbot";
    private static final String ACTION_CANCEL = "cancel";

    private static final String EXTRA_RESULT = "result";

    private static AtomicBoolean sCancelled = new AtomicBoolean(false);

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        Log.e("PHILIP", "Sync adapter service starting" + intent.getAction());

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(Constants.Notification.KEYSERVER_SYNC_FAIL_ORBOT);
        switch (intent.getAction()) {
            case ACTION_CANCEL: {
                sCancelled.set(true);
                break;
            }

            case ACTION_DELAYED_UPDATE: {
                // TODO: PHILIP make time 5 minutes after testing
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sCancelled.set(false);
                        asyncKeyUpdate(KeyserverSyncAdapterService.this, new CryptoInputParcel());
                    }
                }, 10 * 1000);
            }
            case ACTION_UPDATE_ALL: {
                sCancelled.set(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImportKeyResult result = updateKeysFromKeyserver(
                                KeyserverSyncAdapterService.this,
                                new CryptoInputParcel()
                        );
                        handleUpdateResult(result);
                    }
                }).start();
                break;
            }
            case ACTION_IGNORE_TOR: {
                asyncKeyUpdate(this, new CryptoInputParcel(ParcelableProxy.getForNoProxy()));
                break;
            }
            case ACTION_START_ORBOT: {
                Intent startOrbot = new Intent(this, OrbotRequiredDialogActivity.class);
                startOrbot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startOrbot.putExtra(OrbotRequiredDialogActivity.EXTRA_START_ORBOT, true);
                Messenger messenger = new Messenger(
                        new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                switch (msg.what) {
                                    case OrbotRequiredDialogActivity.MESSAGE_ORBOT_STARTED: {
                                        Log.e("PHILIP", "orbot activity returned");
                                        asyncKeyUpdate(KeyserverSyncAdapterService.this,
                                                new CryptoInputParcel());
                                        break;
                                    }
                                    case OrbotRequiredDialogActivity.MESSAGE_ORBOT_IGNORE: {
                                        asyncKeyUpdate(KeyserverSyncAdapterService.this,
                                                new CryptoInputParcel(
                                                        ParcelableProxy.getForNoProxy()));
                                        break;
                                    }
                                    case OrbotRequiredDialogActivity.MESSAGE_DIALOG_CANCEL: {
                                        // just stop service
                                        stopSelf();
                                        break;
                                    }
                                }
                            }
                        }
                );
                startOrbot.putExtra(OrbotRequiredDialogActivity.EXTRA_MESSENGER, messenger);
                startActivity(startOrbot);
                break;
            }
            case ACTION_DISMISS_NOTIFICATION: {
                // notification is dismissed at the beginning
                stopSelf(startId);
                break;
            }

            case ACTION_UPDATE_RESULT: {
                handleUpdateResult((ImportKeyResult) intent.getParcelableExtra(EXTRA_RESULT));
                break;
            }
        }
        return START_REDELIVER_INTENT;
    }

    private class KeyserverSyncAdapter extends AbstractThreadedSyncAdapter {

        public KeyserverSyncAdapter() {
            super(KeyserverSyncAdapterService.this, true);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                                  ContentProviderClient provider, SyncResult syncResult) {
            Log.d(Constants.TAG, "Performing a keyserver sync!");

            // updateKeysFromKeyserver(KeyserverSyncAdapterService.this, new CryptoInputParcel());
            Intent serviceIntent = new Intent(KeyserverSyncAdapterService.this,
                    KeyserverSyncAdapterService.class);
            serviceIntent.setAction(ACTION_UPDATE_ALL);
            startService(serviceIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new KeyserverSyncAdapter().getSyncAdapterBinder();
    }

    private void handleUpdateResult(ImportKeyResult result) {
        if (result.isPending()) {
            // result is pending due to Orbot not being started
            // try to start it silently, if disabled show notificationaa
            new OrbotHelper.SilentStartManager() {
                @Override
                protected void onOrbotStarted() {
                    // retry the update
                    updateKeysFromKeyserver(KeyserverSyncAdapterService.this,
                            new CryptoInputParcel());
                }

                @Override
                protected void onSilentStartDisabled() {
                    // show notification
                    NotificationManager manager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(Constants.Notification.KEYSERVER_SYNC_FAIL_ORBOT,
                            getOrbotNoification(KeyserverSyncAdapterService.this));
                }
            }.startOrbotAndListen(this, false);
        } else if (result.cancelled()) {
            Log.d(Constants.TAG, "Keyserver sync cancelled");
            Log.e("PHILIP", "Keyserver sync cancelled");
            // wait 5 minutes then try again
            Intent serviceIntent = new Intent(KeyserverSyncAdapterService.this,
                    KeyserverSyncAdapterService.class);
            serviceIntent.setAction(ACTION_DELAYED_UPDATE);
            startService(serviceIntent);
        } else {
            stopSelf();
        }
    }

    private static void asyncKeyUpdate(final Context context,
                                       final CryptoInputParcel cryptoInputParcel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImportKeyResult result = updateKeysFromKeyserver(context, cryptoInputParcel);
                Intent intent = new Intent(context, KeyserverSyncAdapterService.class);
                intent.setAction(ACTION_UPDATE_RESULT);
                intent.putExtra(EXTRA_RESULT, result);
                context.startService(intent);
            }
        }).start();
    }

    private static ImportKeyResult updateKeysFromKeyserver(final Context context,
                                                           final CryptoInputParcel cryptoInputParcel) {
        /**
         * 1. Get keys which have been updated recently and therefore do not need to
         * be updated now
         * 2. Get list of all keys and filter out ones that don't need to be updated
         * 3. Update the remaining keys.
         * At any time, if the operation is to be cancelled, the sCancelled AtomicBoolean may be set
         */

        // 1. Get keys which have been updated recently and don't need to updated now
        final int INDEX_UPDATED_KEYS_MASTER_KEY_ID = 0;
        final int INDEX_LAST_UPDATED = 1;

        // all time in seconds not milliseconds
        // TODO: correct TIME_MAX after testing
        // final long TIME_MAX = TimeUnit.DAYS.toSeconds(7);
        final long TIME_MAX = 1;
        final long CURRENT_TIME = GregorianCalendar.getInstance().getTimeInMillis() / 1000;
        Log.e("PHILIP", "week: " + TIME_MAX + " current: " + CURRENT_TIME);
        Cursor updatedKeysCursor = context.getContentResolver().query(
                KeychainContract.UpdatedKeys.CONTENT_URI,
                new String[]{
                        KeychainContract.UpdatedKeys.MASTER_KEY_ID,
                        KeychainContract.UpdatedKeys.LAST_UPDATED
                },
                "? - " + KeychainContract.UpdatedKeys.LAST_UPDATED + " < " + TIME_MAX,
                new String[]{"" + CURRENT_TIME},
                null
        );

        ArrayList<Long> ignoreMasterKeyIds = new ArrayList<>();
        while (updatedKeysCursor.moveToNext()) {
            long masterKeyId = updatedKeysCursor.getLong(INDEX_UPDATED_KEYS_MASTER_KEY_ID);
            Log.d(Constants.TAG, "Keyserver sync: {" + masterKeyId + "} last updated at {"
                    + updatedKeysCursor.getLong(INDEX_LAST_UPDATED) + "}s");
            ignoreMasterKeyIds.add(masterKeyId);
        }
        updatedKeysCursor.close();

        // 2. Make a list of public keys which should be updated
        final int INDEX_MASTER_KEY_ID = 0;
        final int INDEX_FINGERPRINT = 1;
        Cursor keyCursor = context.getContentResolver().query(
                KeychainContract.KeyRings.buildUnifiedKeyRingsUri(),
                new String[]{
                        KeychainContract.KeyRings.MASTER_KEY_ID,
                        KeychainContract.KeyRings.FINGERPRINT
                },
                null,
                null,
                null
        );

        if (keyCursor == null) {
            return new ImportKeyResult(ImportKeyResult.RESULT_FAIL_NOTHING,
                    new OperationResult.OperationLog());
        }

        ArrayList<ParcelableKeyRing> keyList = new ArrayList<>();
        while (keyCursor.moveToNext()) {
            long keyId = keyCursor.getLong(INDEX_MASTER_KEY_ID);
            if (ignoreMasterKeyIds.contains(keyId)) {
                continue;
            }
            String fingerprint = KeyFormattingUtils
                    .convertFingerprintToHex(keyCursor.getBlob(INDEX_FINGERPRINT));
            String hexKeyId = KeyFormattingUtils
                    .convertKeyIdToHex(keyId);
            // we aren't updating from keybase as of now
            keyList.add(new ParcelableKeyRing(fingerprint, hexKeyId, null));
        }
        keyCursor.close();

        if (isUpdateCancelled(context)) { // if we've already been cancelled
            return new ImportKeyResult(OperationResult.RESULT_CANCELLED,
                    new OperationResult.OperationLog());
        }

        // 3. Actually update the keys
        Log.e("PHILIP", keyList.toString());
        ImportOperation importOp = new ImportOperation(context, new ProviderHelper(context), null);
        ImportKeyResult result = importOp.execute(
                new ImportKeyringParcel(keyList,
                        Preferences.getPreferences(context).getPreferredKeyserver()),
                cryptoInputParcel
        );

        if (result.isPending()) {
            Log.d(Constants.TAG, "Keyserver sync failed due to pending result " +
                    result.getRequiredInputParcel().mType);
        } else {
            Log.d(Constants.TAG, "Background keyserver sync completed: new " + result.mNewKeys
                    + " updated " + result.mUpdatedKeys + " bad " + result.mBadKeys);
        }
        return result;
    }

    private static boolean isUpdateCancelled(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressWarnings("deprecation") // our min is API 15, deprecated only in 20
                boolean isScreenOn = pm.isScreenOn();
        return sCancelled.get() || isScreenOn;
    }

    /**
     * will cancel an update already in progress
     *
     * @param context
     */
    public static void cancelUpdates(Context context) {
        Intent intent = new Intent(context, KeyserverSyncAdapterService.class);
        intent.setAction(ACTION_CANCEL);
        context.startService(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("PHILIP", "onDestroy");
        super.onDestroy();
    }

    private static Notification getOrbotNoification(Context context) {
        // TODO: PHILIP work in progress
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_stat_notify_24dp)
                .setLargeIcon(getBitmap(R.drawable.ic_launcher, context))
                .setContentTitle(context.getString(R.string.keyserver_sync_orbot_notif_title))
                .setContentText(context.getString(R.string.keyserver_sync_orbot_notif_msg))
                .setAutoCancel(true);

        // In case the user decides to not use tor
        Intent ignoreTorIntent = new Intent(context, KeyserverSyncAdapterService.class);
        ignoreTorIntent.setAction(ACTION_IGNORE_TOR);
        PendingIntent ignoreTorPi = PendingIntent.getService(
                context,
                0, // security not issue since we're giving this pending intent to Notification Manager
                ignoreTorIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        builder.addAction(R.drawable.abc_ic_clear_mtrl_alpha,
                context.getString(R.string.keyserver_sync_orbot_notif_ignore),
                ignoreTorPi);

        // not enough space to show it
        Intent dismissIntent = new Intent(context, KeyserverSyncAdapterService.class);
        dismissIntent.setAction(ACTION_DISMISS_NOTIFICATION);
        PendingIntent dismissPi = PendingIntent.getService(
                context,
                0, // security not issue since we're giving this pending intent to Notification Manager
                dismissIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        /*builder.addAction(R.drawable.abc_ic_clear_mtrl_alpha,
                context.getString(android.R.string.cancel),
                dismissPi
        );*/

        Intent startOrbotIntent = new Intent(context, KeyserverSyncAdapterService.class);
        startOrbotIntent.setAction(ACTION_START_ORBOT);
        PendingIntent startOrbotPi = PendingIntent.getService(
                context,
                0, // security not issue since we're giving this pending intent to Notification Manager
                startOrbotIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        builder.addAction(R.drawable.abc_ic_commit_search_api_mtrl_alpha,
                context.getString(R.string.keyserver_sync_orbot_notif_start),
                startOrbotPi
        );
        builder.setContentIntent(startOrbotPi);

        return builder.build();
    }

    /**
     * will perform a staggered update of user's keys using delays to ensure new Tor circuits, as
     * performed by parcimonie
     *
     * @return result of the sync
     */
    private static ImportKeyResult staggeredSync() {
        // TODO: WIP
        return null;
    }

    // from de.azapps.mirakel.helper.Helpers from https://github.com/MirakelX/mirakel-android
    private static Bitmap getBitmap(int resId, Context context) {
        int mLargeIconWidth = (int) context.getResources().getDimension(
                android.R.dimen.notification_large_icon_width);
        int mLargeIconHeight = (int) context.getResources().getDimension(
                android.R.dimen.notification_large_icon_height);
        Drawable d;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // noinspection deprecation (can't help it at this api level)
            d = context.getResources().getDrawable(resId);
        } else {
            d = context.getDrawable(resId);
        }
        if (d == null) {
            return null;
        }
        Bitmap b = Bitmap.createBitmap(mLargeIconWidth, mLargeIconHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, mLargeIconWidth, mLargeIconHeight);
        d.draw(c);
        return b;
    }
}
