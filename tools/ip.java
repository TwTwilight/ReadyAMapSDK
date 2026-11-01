package com.amap.api.col.sl3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class ip {

    private static final String POOL_ASSET = "amap_keys.json";
    private static final String POOL_PREFS = "amapkit_keys";
    private static final String KEY_ACTIVE = "active";

    static String a;
    static boolean b;
    private static String c;
    private static String d;
    private static String e;
    private static String f;

    private static boolean poolLoaded;
    private static String[] poolKeys = new String[0];
    private static String[] poolPkgs = new String[0];
    private static String[] poolSha1s = new String[0];

    private ip() {
    }

    private static boolean hasEntry(int index) {
        return index >= 0 && index < poolKeys.length
                && poolKeys[index] != null && poolKeys[index].length() > 0;
    }

    private static synchronized void loadPool(Context context) {
        if (poolLoaded || context == null) {
            return;
        }
        poolLoaded = true;
        InputStream in = null;
        try {
            in = context.getAssets().open(POOL_ASSET);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            JSONObject root = new JSONObject(new String(bos.toByteArray(), "UTF-8"));
            JSONArray arr = root.optJSONArray("keys");
            if (arr == null || arr.length() == 0) {
                return;
            }
            String[] keys = new String[arr.length()];
            String[] pkgs = new String[arr.length()];
            String[] sha1s = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) {
                    continue;
                }
                keys[i] = o.optString("apiKey", "");
                pkgs[i] = o.optString("package", "");
                sha1s[i] = o.optString("sha1", "");
            }
            poolKeys = keys;
            poolPkgs = pkgs;
            poolSha1s = sha1s;
        } catch (Throwable ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static int activeIndex(Context context) {
        if (context == null) {
            return 0;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(POOL_PREFS, Context.MODE_PRIVATE);
            return sp.getInt(KEY_ACTIVE, 0);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static String currentKey(Context context) {
        loadPool(context);
        int idx = activeIndex(context);
        if (hasEntry(idx)) {
            return poolKeys[idx];
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), 128);
            if (ai != null && ai.metaData != null) {
                String k = ai.metaData.getString("com.amap.api.v2.apikey");
                if (k != null && k.length() > 0) {
                    return k;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static String a(Context context) {
        String k = currentKey(context);
        if (k != null) {
            return k;
        }
        return f != null && f.length() > 0 ? f : null;
    }

    public static void a(Context context, String str) {
        if (str != null && str.length() > 0) {
            f = str;
        }
    }

    public static void a(String str) {
        if (str != null && str.length() > 0) {
            d = str;
        }
    }

    public static boolean a() {
        b = true;
        return true;
    }

    public static String b(Context context) {
        try {
            return String.valueOf(context.getPackageManager().getApplicationLabel(
                    context.getPackageManager().getApplicationInfo(context.getPackageName(), 0)));
        } catch (Throwable t) {
            return "";
        }
    }

    public static void b(String str) {
        if (str != null && str.length() > 0) {
            f = str;
        }
    }

    public static String c(Context context) {
        if (d != null && d.length() > 0) {
            return d;
        }
        loadPool(context);
        int idx = activeIndex(context);
        if (hasEntry(idx) && poolPkgs[idx] != null && poolPkgs[idx].length() > 0) {
            return poolPkgs[idx];
        }
        try {
            return context.getPackageName();
        } catch (Throwable t) {
            return "";
        }
    }

    private static boolean c(String str) {
        return true;
    }

    public static String d(Context context) {
        try {
            String v = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            return v != null ? v : "";
        } catch (Throwable t) {
            return "";
        }
    }

    public static String e(Context context) {
        loadPool(context);
        int idx = activeIndex(context);
        if (hasEntry(idx)) {
            String sha1 = poolSha1s[idx] != null ? poolSha1s[idx] : "";
            String pkg = poolPkgs[idx] != null ? poolPkgs[idx] : "";
            String value = sha1 + ":" + pkg;
            a = value;
            return value;
        }
        try {
            android.content.pm.PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 64);
            byte[] digest = java.security.MessageDigest.getInstance("SHA1")
                    .digest(pi.signatures[0].toByteArray());
            StringBuilder sb = new StringBuilder();
            for (byte by : digest) {
                String hex = Integer.toHexString(by & 255).toUpperCase(java.util.Locale.US);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex).append(':');
            }
            sb.append(pi.packageName);
            String value = sb.toString();
            a = value;
            return value;
        } catch (Throwable t) {
            return a;
        }
    }

    public static String f(Context context) {
        return a(context);
    }

    private static String g(Context context) {
        return a(context);
    }

    private static String h(Context context) {
        return a(context);
    }
}
