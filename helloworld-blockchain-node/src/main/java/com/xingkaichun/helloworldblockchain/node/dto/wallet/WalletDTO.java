package com.xingkaichun.helloworldblockchain.node.dto.wallet;

/**
 * 钱包
 *
 * @author 邢开春 xingkaichun@qq.com
 */
public class WalletDTO {

    private String privateKey;

    private String publicKey;

    private String address;

    public WalletDTO(String privateKey, String publicKey, String address) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.address = address;
    }




    //region get set

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    //endregion

}
