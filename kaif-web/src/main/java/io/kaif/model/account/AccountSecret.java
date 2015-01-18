package io.kaif.model.account;

import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.kaif.token.SecureTokenCodec;

@Component
@ConfigurationProperties(prefix = "account")
public class AccountSecret {

  private String key;

  private String mac;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getMac() {
    return mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public SecureTokenCodec getCodec() {
    return SecureTokenCodec.create(Base64.getUrlDecoder().decode(mac),
        Base64.getUrlDecoder().decode(key));
  }
}
