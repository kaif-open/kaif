package io.kaif.model.account;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Authority {
  //DO NOT break order, or change name, the ordinal are used to calculate many other things
  /**
   * 這個值不會 assign 給用戶，只用來設定任何人都不能執行的權限上
   */
  FORBIDDEN,

  /**
   * (觀光客)
   * 可登入
   * 修改 password
   * 可以重發啟用信
   */
  TOURIST,

  /**
   * (公民)
   * 可以做文章 CREATE UPDATE, upvote, downvote
   * 修改 avatar
   */
  CITIZEN,

  /**
   * 投票權的人 (參政權)，
   * 他可以進行 iVoting 等進階的投票 zone
   * (也有一些 zone 不用 Suffrage 就能投)
   * <p>
   * 原則上應該是透過強力的認證手段確定是真人即可 (手機簡訊認證或是臉書帳號長達五年... etc)
   */
  SUFFRAGE,

  /**
   * sysop (站務)
   * 可以停權人 (未來可以獨立出去變成 `司法人員`)
   * 可以變更帳號的權限
   * 後台管理
   */
  SYSOP;

  public static Set<Authority> fromBits(long bits) {
    return Stream.of(values()).filter(auth -> bitsContains(bits, auth)).collect(Collectors.toSet());
  }

  public static boolean bitsContains(long bits, Authority authority) {
    return (authority.bit() & bits) == authority.bit();
  }

  public static long toBits(Collection<Authority> authorities) {
    return authorities.stream().mapToLong(Authority::bit).reduce(0L, (p, n) -> p | n);
  }

  private long bit() {
    return 1L << ordinal();
  }
}
