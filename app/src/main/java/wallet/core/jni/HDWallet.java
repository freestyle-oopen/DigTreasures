//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wallet.core.jni;

import java.security.InvalidParameterException;

public class HDWallet {
    public long nativeHandle;

    private HDWallet() {
        this.nativeHandle = 0L;
    }

    public static HDWallet createFromNative(long nativeHandle) {
        HDWallet instance = new HDWallet();
        instance.nativeHandle = nativeHandle;
        return instance;
    }

    static native long nativeCreate(int var0, String var1);

    static native long nativeCreateWithMnemonic(String var0, String var1);

    static native long nativeCreateWithData(byte[] var0, String var1);

    public static native void nativeDelete(long var0);

    public static native boolean isValid(String var0);

    public static native PublicKey getPublicKeyFromExtended(String var0, CoinType var1, String var2);

    public native byte[] seed();

    public native String mnemonic();

    public native PrivateKey getMasterKey(Curve var1);

    public native PrivateKey getKeyForCoin(CoinType var1);

    public native String getAddressForCoin(CoinType var1);

    public native PrivateKey getKey(CoinType var1, String var2);

    public native PrivateKey getKeyBIP44(CoinType var1, int var2, int var3, int var4);

    public native String getExtendedPrivateKey(Purpose var1, CoinType var2, HDVersion var3);

    public native String getExtendedPublicKey(Purpose var1, CoinType var2, HDVersion var3);

    public HDWallet(int strength, String passphrase) {
        this.nativeHandle = nativeCreate(strength, passphrase);
        if (this.nativeHandle == 0L) {
            throw new InvalidParameterException();
        } else {
        }
    }

    public HDWallet(String mnemonic, String passphrase) {
        this.nativeHandle = nativeCreateWithMnemonic(mnemonic, passphrase);
        if (this.nativeHandle == 0L) {
            throw new InvalidParameterException();
        }
    }

    public HDWallet(byte[] data, String passphrase) {
        this.nativeHandle = nativeCreateWithData(data, passphrase);
        if (this.nativeHandle == 0L) {
            throw new InvalidParameterException();
        } else {
        }
    }

    public String getAddressForCoins(CoinType var1){
        String addressForCoin = getAddressForCoin(var1);
        nativeDelete(nativeHandle);
        return addressForCoin;
    }
}
